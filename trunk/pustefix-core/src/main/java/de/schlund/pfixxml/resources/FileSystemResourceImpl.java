/*
 * This file is part of Pustefix.
 *
 * Pustefix is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Pustefix is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Pustefix; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package de.schlund.pfixxml.resources;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Implementation of FileResource used to handle <code>file://</code> URIs. This 
 * class should never be exposed to another package, instead the interface should
 * be used. 
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
class FileSystemResourceImpl implements FileSystemResource {
    
    URI uri;
    URI origUri;
    File file;
    
    FileSystemResourceImpl(URI uri) {
        // Sanity checks
        if (uri.getScheme() == null || !uri.getScheme().equals("file")) {
            throw new IllegalArgumentException("Cannot handle scheme " + uri.getScheme());
        }
        if (uri.getAuthority() != null && uri.getAuthority().length() != 0) {
            throw new IllegalArgumentException("docroot:// URI may not specify authority");
        }
        if (uri.getHost() != null && uri.getHost().length() != 0) {
            throw new IllegalArgumentException("file:// URI may not specify host");
        }
        if (uri.getPort() != -1) {
            throw new IllegalArgumentException("file:// URI may not specify port");
        }
        if (uri.getQuery() != null && uri.getQuery().length() != 0) {
            throw new IllegalArgumentException("file:// URI may not specify query");
        }
        if (uri.getFragment() != null && uri.getFragment().length() != 0) {
            throw new IllegalArgumentException("file:// URI may not specify fragment");
        }
        
        String path = uri.getPath();
        if (path.contains("//")) {
            throw new IllegalArgumentException("\"" + uri.toString() + "\" is not a valid URI");
        }
        if (!path.startsWith("/")) {
            throw new IllegalArgumentException("Path in URI + \"" + uri.toString() + "\" is not absolute");
        }
        boolean dirPath = false;
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
            dirPath = true;
        }
        file = new File(path);
        if (dirPath && file.exists() && !file.isDirectory()) {
            throw new IllegalArgumentException("URI \"" + uri.toString() + "\" does not point to a directory");
        }
        try {
            this.uri = new URI("file", null, path, null, null).normalize();
        } catch (URISyntaxException e) {
            // Should never happen
            throw new RuntimeException(e);
        }
    }

    public boolean canRead() {
        return file.canRead();
    }

    public boolean canWrite() {
        return file.canWrite();
    }

    public boolean createNewFile() throws IOException {
        return file.createNewFile();
    }

    public boolean delete() {
        return file.delete();
    }

    public boolean exists() {
        return file.exists();
    }

    public String getName() {
        return file.getName();
    }

    public FileResource getParentAsFileResource() {
        File parentFile = file.getParentFile();
        if (parentFile != null) {
            return ResourceUtil.getFileResource(file.getParentFile().toURI());
        } else {
            return null;
        }
    }

    public boolean isDirectory() {
        return file.isDirectory();
    }

    public boolean isFile() {
        return file.isFile();
    }

    public boolean isHidden() {
        return file.isHidden();
    }

    public long lastModified() {
        return file.lastModified();
    }
    
    public long length() {
        return file.length();
    }

    public String[] list() {
        return file.list();
    }

    public FileResource[] listFileResources() {
        File[] files = file.listFiles();
        ArrayList<FileResource> list = new ArrayList<FileResource>();
        for (File item : files) {
            list.add(ResourceUtil.getFileResource(item.toURI()));
        }
        return list.toArray(new FileResource[list.size()]);
    }

    public boolean mkdir() {
        return file.mkdir();
    }

    public boolean mkdirs() {
        return file.mkdirs();
    }

    public URI toURI() {
        return uri;
    }
    
    public URI getOriginatingURI() {
        if(origUri == null) return toURI();
        return origUri;
    }
    
    public void setOriginatingURI(URI origUri) {
        this.origUri = origUri;
    }
    
    public URL toURL() throws MalformedURLException {
        return file.toURI().toURL();
    }

    public InputStream getInputStream() throws FileNotFoundException {
        return new FileInputStream(file);
    }

    public OutputStream getOutputStream() throws FileNotFoundException {
        return new FileOutputStream(file);
    }
    
    public OutputStream getOutputStream(boolean append) throws FileNotFoundException {
        return new FileOutputStream(file,append);
    }

    public int compareTo(Resource o) {
        return uri.compareTo(o.toURI());
    }
    
    @Override
    public String toString() {
        return uri.toString();
    }

    @Override
    public boolean equals(Object o) {
        FileSystemResourceImpl res;
        try {
            res = (FileSystemResourceImpl) o;
        } catch (ClassCastException e) {
            return false;
        }
        return uri.equals(res.uri);
    }

    @Override
    public int hashCode() {
        return uri.hashCode();
    }

    public String getPathOnFileSystem() {
        return toURI().getPath();
    }

    //Spring Resource compatibility methods
    
    public boolean isReadable() {
        return true;
    }

    public boolean isOpen() {
        return false;
    }

    public URL getURL() throws IOException {
        return toURL();
    }

    public URI getURI() throws IOException {
        return toURI();
    }

    public File getFile() throws IOException {
        return file;
    }
    
    public String getFilename() {
        return getName();
    }

    public long contentLength() throws IOException {
        return length();
    }

    public String getDescription() {
        return toURI().toASCIIString();
    } 
    
    public org.springframework.core.io.Resource createRelative(
            String relativePath) throws IOException {
        // TODO implement
        throw new RuntimeException("Method not yet implemented");
    }
    
}
