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
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.jar.JarEntry;

import de.schlund.pfixcore.exception.PustefixRuntimeException;

public class ModuleResource implements Resource {

    private URI uri;
    private URI origUri;
    private URL url;
    
    public ModuleResource(URI uri, URL moduleUrl) {
        this.uri = uri;
        String path = "PUSTEFIX-INF"+uri.getPath();
        try {
            url = new URL(moduleUrl, path);
        } catch (MalformedURLException x) {
            //TODO: throw typed exception
            throw new PustefixRuntimeException(x);
        } 
    }
    
    public ModuleResource(URI uri) {
        this.uri = uri;
    }
    
    public boolean canRead() {
        //TODO: implement
        return true;
    }

    public boolean exists() {
        if(url == null) return false;
        try {
            JarURLConnection con = (JarURLConnection)url.openConnection();
            con.setUseCaches(false);
            con.getJarEntry();
            return true;
        } catch(FileNotFoundException x) {
            return false;
        } catch(IOException x) {
            throw new PustefixRuntimeException("Failed existance check: " + uri, x);
        }
    }

    public InputStream getInputStream() throws IOException {
        if(url == null) throw new IOException("Resource doesn't exist");
        JarURLConnection con = (JarURLConnection)url.openConnection();
        con.setUseCaches(false);
        return con.getInputStream();
    }

    public boolean isFile() {
        //TODO: support directory entries
        return true;
    }

    public long lastModified() {
        if(url == null) return 0;
        try {
            JarURLConnection con = (JarURLConnection)url.openConnection();
            con.setUseCaches(false);
            return con.getLastModified();
        } catch(IOException x) {
            throw new RuntimeException("Error checking modification time", x);
        }
    }

    public long length() {
        if(url == null) return 0;
        try {
            JarURLConnection con = (JarURLConnection)url.openConnection();
            con.setUseCaches(false);
            JarEntry entry = con.getJarEntry();
            if(entry != null) {
                return con.getJarEntry().getSize();
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
    
}
