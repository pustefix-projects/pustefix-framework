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

package de.schlund.pfixxml.resources.internal;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Set;

import javax.servlet.ServletContext;

import de.schlund.pfixxml.resources.AbstractDocrootResourceImpl;
import de.schlund.pfixxml.resources.DocrootResource;

/**
 * Implementation of the {@link DocrootResource} using a servlet context to fetch resources. 
 * This class should never be exposed to any other packages as only the interface should be 
 * used to access the class.  
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
class DocrootResourceByServletContextImpl extends AbstractDocrootResourceImpl {
    private final static String ROOT_PREFIX = "/WEB-INF/pfixroot";
    
    private ServletContext servletContext;
    
    DocrootResourceByServletContextImpl(URI uri, URI originatingUri, ServletContext servletContext) {
        super(uri, originatingUri);
        
        this.servletContext = servletContext;

        if (this.trailingSlash && this.exists() && !this.isDirectory()) {
            throw new IllegalArgumentException("URI \"" + uri.toString() + "\" points to a non-existent directory");
        }
    }
    
    DocrootResourceByServletContextImpl(URI uri, ServletContext servletContext) {
        this(uri, uri, servletContext);
    }
    
    @Override
    public boolean canRead() {
        return exists();
    }

    @Override
    public boolean canWrite() {
        return false;
    }

    @Override
    public boolean createNewFile() throws IOException {
        throw new IOException("Cannot create file in WAR-archive");
    }

    @Override
    public boolean delete() {
        return false;
    }

    @Override
    public boolean exists() {
        return isFile() || isDirectory();
    }

    @Override
    public boolean isDirectory() {
        Set<?> temp = this.servletContext.getResourcePaths(ROOT_PREFIX + path + "/");
        return (temp != null && temp.size() > 0);
    }

    @Override
    public boolean isFile() {
        try {
            return (servletContext.getResource(ROOT_PREFIX + path) != null);
        } catch (MalformedURLException e) {
            return false;
        }
    }

    @Override
    public boolean isHidden() {
        return getName().startsWith(".");
    }

    @Override
    public long lastModified() {
        return -1;
    }

    @Override
    public String[] list() {
        if (!isDirectory()) {
            return null;
        }
        Set<?> paths = this.servletContext.getResourcePaths(ROOT_PREFIX + path + "/");
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

    @Override
    public boolean mkdir() {
        return false;
    }

    @Override
    public boolean mkdirs() {
        return false;
    }

    @Override
    public URI toURI() {
        return uri;
    }
    
    @Override
    public URL toURL() throws MalformedURLException {
        return servletContext.getResource(ROOT_PREFIX + path);
    }

    @Override
    public InputStream getInputStream() throws FileNotFoundException {
        if (isFile()) {
            return servletContext.getResourceAsStream(ROOT_PREFIX + path);
        } else {
            throw new FileNotFoundException("Cannot find file \"" + path + "\" in Pustefix docroot");
        }
    }

    @Override
    public OutputStream getOutputStream() throws FileNotFoundException {
        throw new FileNotFoundException("Cannot write to file in WAR archive");
    }
    
    @Override
    public OutputStream getOutputStream(boolean append) throws FileNotFoundException {
        throw new FileNotFoundException("Cannot write to file in WAR archive");
    }

}
