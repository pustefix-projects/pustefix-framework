/*
 * This file is part of PFIXCORE.
 *
 * PFIXCORE is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * PFIXCORE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with PFIXCORE; if not, write to the Free Software
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
import java.util.Set;

import javax.servlet.ServletContext;

import de.schlund.pfixxml.config.GlobalConfig;

/**
 * Actual implementation of the {@link DocrootResource}. This class should never
 * be exposed to any other packages as only the interface should be used to access
 * the class.  
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
class DocrootResourceImpl implements DocrootResource {
    private final static String ROOT_PREFIX = "/WEB-INF/pfixroot";
    
    private File file;
    private String path;
    private URI uri;
    private ServletContext servletContext;
    
    DocrootResourceImpl(URI uri) {
        // Sanity checks
        if (uri.getScheme() == null || !uri.getScheme().equals("pfixroot")) {
            throw new IllegalArgumentException("Cannot handle scheme " + uri.getScheme());
        }
        if (uri.getAuthority() != null && uri.getAuthority().length() != 0) {
            throw new IllegalArgumentException("pfixroot:// URI may not specify authority");
        }
        if (uri.getHost() != null && uri.getHost().length() != 0) {
            throw new IllegalArgumentException("pfixroot:// URI may not specify host");
        }
        if (uri.getPort() != -1) {
            throw new IllegalArgumentException("pfixroot:// URI may not specify port");
        }
        if (uri.getQuery() != null && uri.getQuery().length() != 0) {
            throw new IllegalArgumentException("pfixroot:// URI may not specify query");
        }
        if (uri.getFragment() != null && uri.getFragment().length() != 0) {
            throw new IllegalArgumentException("pfixroot:// URI may not specify fragment");
        }
        
        String path = uri.getPath();
        if (!path.startsWith("/")) {
            throw new IllegalArgumentException("Path \"" + path + "\" does not start with a /");
        }
        if (path.contains("//")) {
            throw new IllegalArgumentException("Path \"" + path + "\" is not well-formed");
        }
        boolean dirPath = false;
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
            dirPath = true;
        }
        
        try {
            this.uri = new URI("pfixroot", null, path, null, null).normalize();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        this.path = this.uri.getPath();
        String docroot = GlobalConfig.getDocroot();
        if (docroot != null) {
            this.file = new File(docroot, path.substring(1));
            this.servletContext = null;
        } else {
            this.file = null;
            this.servletContext = GlobalConfig.getServletContext();
            if (this.servletContext == null) {
                throw new IllegalStateException("ServletContext or document root path has to be set before using this class");
            }
        }
        
        if (dirPath && this.exists() && !this.isDirectory()) {
            throw new IllegalArgumentException("URI \"" + uri.toString() + "\" points to a non-existent directory");
        }
    }
    
    public boolean canRead() {
        if (file != null) {
            return file.canRead();
        } else {
            return exists();
        }
    }

    public boolean canWrite() {
        if (file != null) {
            return file.canWrite();
        } else {
            return false;
        }
    }

    public boolean createNewFile() throws IOException {
        if (file != null) {
            return file.createNewFile();
        } else {
            throw new IOException("Cannot create file in WAR-archive");
        }
    }

    public boolean delete() {
        if (file != null) {
            return file.delete();
        } else {
            return false;
        }
    }

    public boolean exists() {
        if (file != null) {
            return file.exists();
        } else {
            return isFile() || isDirectory();
        }
    }

    public String getName() {
        if (path == "/") {
            return "/";
        } else {
            return path.substring(path.lastIndexOf('/') + 1);
        }
    }

    public FileResource getParentAsFileResource() {
        if (path.equals("/")) {
            return null;
        } else {
            String parentPath = path.substring(0, path.lastIndexOf('/'));
            try {
                return ResourceUtil.getFileResource(new URI("pfixroot", null, parentPath, null, null));
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public boolean isDirectory() {
        if (file != null) {
            return file.isDirectory();
        } else {
            Set temp = this.servletContext.getResourcePaths(ROOT_PREFIX + path + "/");
            return (temp != null && temp.size() > 0);
        }
    }

    public boolean isFile() {
        if (file != null) {
            return file.isFile();
        } else {
            try {
                return (servletContext.getResource(ROOT_PREFIX + path) != null);
            } catch (MalformedURLException e) {
                return false;
            }
        }
    }

    public boolean isHidden() {
        if (file != null) {
            return file.isHidden();
        } else {
            return getName().startsWith(".");
        }
    }

    public long lastModified() {
        if (file != null) {
            return file.lastModified();
        } else {
            return -1;
        }
    }

    public String[] list() {
        if (file != null) {
            return file.list();
        } else {
            if (!isDirectory()) {
                return null;
            }
            Set paths = this.servletContext.getResourcePaths(ROOT_PREFIX + path + "/");
            ArrayList<String> rpaths = new ArrayList<String>();
            for (Object item : paths) {
                String sitem = (String) item;
                if (sitem.endsWith("/")) {
                    sitem = sitem.substring(0, sitem.length() - 1);
                    rpaths.add(sitem);
                }
            }
            return rpaths.toArray(new String[rpaths.size()]);
        }
    }

    public FileResource[] listFileResources() {
        String[] names = list();
        ArrayList<FileResource> list = new ArrayList<FileResource>();
        for (String name : names) {
            list.add(ResourceUtil.getFileResource(this, name));
        }
        return list.toArray(new FileResource[list.size()]);
    }

    public boolean mkdir() {
        if (file != null) {
            return file.mkdir();
        } else {
            return false;
        }
    }

    public boolean mkdirs() {
        if (file != null) {
            return file.mkdirs();
        } else {
            return false;
        }
    }

    public URI toURI() {
        return uri;
    }
    
    public URL toURL() throws MalformedURLException {
        if (file != null) {
            return file.toURL();
        } else {
            return servletContext.getResource(ROOT_PREFIX + path);
        }
    }

    public InputStream getInputStream() throws FileNotFoundException {
        if (file != null) {
            return new FileInputStream(file);
        } else {
            if (isFile()) {
                return servletContext.getResourceAsStream(ROOT_PREFIX + path);
            } else {
                throw new FileNotFoundException("Cannot find file \"" + path + "\" in Pustefix docroot");
            }
        }
    }

    public OutputStream getOutputStream() throws FileNotFoundException {
        if (file != null) {
            return new FileOutputStream(file);
        } else {
            throw new FileNotFoundException("Cannot write to file in WAR archive");
        }
    }
    
    public OutputStream getOutputStream(boolean append) throws FileNotFoundException {
        if (file != null) {
            return new FileOutputStream(file,append);
        } else {
            throw new FileNotFoundException("Cannot write to file in WAR archive");
        }
    }

    public int compareTo(FileResource o) {
        return uri.compareTo(o.toURI());
    }

    public String toString() {
        return uri.toString();
    }
    
    public boolean equals(Object o) {
        DocrootResourceImpl res;
        try {
            res = (DocrootResourceImpl) o;
        } catch (ClassCastException e) {
            return false;
        }
        return uri.equals(res.uri);
    }
    
    public int hashCode() {
        return uri.hashCode();
    }

    public String getRelativePath() {
        String path = toURI().getPath();
        if (path.length() > 0) {
            path = path.substring(1);
        }
        return path;
    }
}
