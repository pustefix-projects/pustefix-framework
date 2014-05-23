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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.jar.JarEntry;

import org.pustefixframework.util.URLUtils;

import de.schlund.pfixcore.exception.PustefixRuntimeException;
import de.schlund.pfixcore.util.JarFileURLConnection;

public class ModuleResource implements Resource {

    private URI uri;
    private URI origUri;
    private URL url;
    
    public ModuleResource(URI uri, URL moduleUrl, String moduleResourcePath) {
        this.uri = uri;
        String path = moduleResourcePath + uri.getPath();
        try {
            url = new URL(moduleUrl, path);
        } catch (MalformedURLException x) {
            throw new PustefixRuntimeException(x);
        } 
    }
    
    public ModuleResource(URI uri) {
        this.uri = uri;
    }
    
    protected JarURLConnection getConnection() throws IOException {
        JarURLConnection con = new JarFileURLConnection(url);
        //Use own JarURLConnection implementation to workaround problem
        //with standard implementation, which causes too many open file
        //handles because they are not destroyed until garbage collection
        //JarURLConnection con = (JarURLConnection)url.openConnection();
        //con.setUseCaches(false);
        return con;
    }
    
    public boolean canRead() {
        return exists();
    }

    public boolean exists() {
        if(url == null) return false;
        try {
            JarURLConnection con = getConnection();
            con.getJarEntry();
            return true;
        } catch(FileNotFoundException x) {
        	try {	
        		return ResourceIndex.getInstance().exists(url);
        	} catch(IOException ex) {
        		//ignore 
        	}
            return false;
        } catch(IOException x) {
            throw new PustefixRuntimeException("Failed existance check: " + uri, x);
        }
    }

    public InputStream getInputStream() throws IOException {	
        if(url == null) throw new IOException("Resource doesn't exist: " + uri);
        JarURLConnection con = getConnection();
        return con.getInputStream();
    }

    public boolean isFile() {
        if(url == null) return false;
        try {
            JarURLConnection con = getConnection();
            JarEntry entry = con.getJarEntry();
            if(entry == null) return true; //it's a jar file
            return !entry.isDirectory();
        } catch(IOException x) {
            return false;
        }
    }

    public long lastModified() {
        if(url == null) return 0;
        try {
            JarURLConnection con = getConnection();
            return con.getLastModified();
        } catch(IOException x) {
            throw new RuntimeException("Error checking modification time", x);
        }
    }

    public long length() {
        if(url == null) return 0;
        try {
            JarURLConnection con = getConnection();
            JarEntry entry = con.getJarEntry();
            if(entry != null) {
                return entry.getSize();
            }
        } catch(FileNotFoundException x) {
        } catch(IOException x) {
            throw new RuntimeException("Error checking length", x);
        }
        return 0;
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
    
    public int compareTo(Resource res) {
        return uri.compareTo(res.toURI());
    }
    
    @Override
    public String toString() {
    	return uri.toString();
    }
    
    //Spring Resource compatibility methods

    public boolean isReadable() {
        return true;
    }

    public boolean isOpen() {
        return false;
    }

    public URL getURL() throws IOException {
        return toURI().toURL();
    }

    public URI getURI() throws IOException {
        return toURI();
    }

    public String getFilename() {
        String path = uri.getPath();
        int ind = path.lastIndexOf('/');
        return ind > -1 ? path.substring(ind + 1) : path;
    }
    
    public long contentLength() throws IOException {
        return length();
    }
    
    public File getFile() throws IOException {
        throw new IOException("Resource isn't available on the file system: " + toURI());
    }

    public String getDescription() {
        return toURI().toASCIIString();
    }
    
    public org.springframework.core.io.Resource createRelative(String relativePath) throws IOException {
        String parentPath = null;
        if(!isFile()) {
            parentPath = uri.getPath();
        } else {
            parentPath = URLUtils.getParentPath(uri.getPath());
        }
        if(parentPath.endsWith("/")) parentPath = parentPath.substring(0, parentPath.length()-1);
        try {
            URI relUri = new URI(uri.getScheme(), uri.getAuthority(), parentPath + "/" + relativePath , null);
            return ResourceUtil.getResource(relUri);
        } catch (URISyntaxException e) {
            throw new IOException("Error creating relative URI: " + uri.toASCIIString() + " " + relativePath, e);
        }
    }

}
