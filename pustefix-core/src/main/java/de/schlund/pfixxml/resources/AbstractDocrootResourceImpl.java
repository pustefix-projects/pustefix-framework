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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Base class for classes implementing the {@link DocrootResource} interface.  
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public abstract class AbstractDocrootResourceImpl implements DocrootResource {
    /**
     * Stores the path relative to the docroot
     */
    protected String path;
    
    /**
     * Stores the complete URI of the resource
     */
    protected URI uri;
    
    protected URI origUri;
    
    /**
     * Is set to true, if the URI used to create the resource had a trailing
     * slash ("/").
     */
    protected boolean trailingSlash;
    
    protected AbstractDocrootResourceImpl(URI uri) {
        // Sanity checks
        if (uri.getScheme() == null || !uri.getScheme().equals("docroot")) {
            throw new IllegalArgumentException("Cannot handle scheme " + uri.getScheme());
        }
        if (uri.getAuthority() != null && uri.getAuthority().length() != 0) {
            throw new IllegalArgumentException("docroot:// URI may not specify authority");
        }
        if (uri.getHost() != null && uri.getHost().length() != 0) {
            throw new IllegalArgumentException("docroot:// URI may not specify host");
        }
        if (uri.getPort() != -1) {
            throw new IllegalArgumentException("docroot:// URI may not specify port");
        }
        if (uri.getQuery() != null && uri.getQuery().length() != 0) {
            throw new IllegalArgumentException("docroot:// URI may not specify query");
        }
        if (uri.getFragment() != null && uri.getFragment().length() != 0) {
            throw new IllegalArgumentException("docroot:// URI may not specify fragment");
        }
        
        String path = uri.getPath();
        if (!path.startsWith("/")) {
            throw new IllegalArgumentException("Path \"" + path + "\" does not start with a /");
        }
        if (path.contains("//")) {
            throw new IllegalArgumentException("Path \"" + path + "\" is not well-formed");
        }

        if (path.endsWith("/")) {
            this.trailingSlash = true;
            path = path.substring(0, path.length() - 1);
        }
        
        try {
            this.uri = new URI("docroot", null, path, null, null).normalize();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        this.path = this.uri.getPath();
    }
    
    protected AbstractDocrootResourceImpl(URI uri, URI origUri) {
        this(uri);
        this.origUri = origUri;
    }
    
    public abstract boolean canRead();
    public abstract boolean canWrite();
    public abstract boolean createNewFile() throws IOException;
    public abstract boolean delete();
    public abstract boolean exists();

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
                return ResourceUtil.getFileResource(new URI("docroot", null, parentPath, null, null));
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public abstract boolean isDirectory();
    public abstract boolean isFile();
    public abstract boolean isHidden();
    public abstract long lastModified();
    public abstract long length();
    public abstract String[] list();
    
    public FileResource[] listFileResources() {
        String[] names = list();
        ArrayList<FileResource> list = new ArrayList<FileResource>();
        for (String name : names) {
            list.add(ResourceUtil.getFileResource(this, name));
        }
        return list.toArray(new FileResource[list.size()]);
    }

    public abstract boolean mkdir();
    public abstract boolean mkdirs();

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
    
    public abstract URL toURL() throws MalformedURLException;
    public abstract InputStream getInputStream() throws FileNotFoundException;
    public abstract OutputStream getOutputStream() throws FileNotFoundException;
    public abstract OutputStream getOutputStream(boolean append) throws FileNotFoundException;

    public int compareTo(Resource o) {
        return uri.compareTo(o.toURI());
    }

    @Override
    public String toString() {
        return uri.toString();
    }
    
    @Override
    public boolean equals(Object o) {
        AbstractDocrootResourceImpl res;
        try {
            res = (AbstractDocrootResourceImpl) o;
            if (res == null) {
            	return false;
            }
        } catch (ClassCastException e) {
            return false;
        }
        return uri.equals(res.uri);
    }
    
    @Override
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
