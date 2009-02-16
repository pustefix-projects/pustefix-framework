package de.schlund.pfixxml.resources;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

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
            con.getJarEntry();
            return true;
        } catch(FileNotFoundException x) {
            return false;
        } catch(IOException x) {
            throw new PustefixRuntimeException("Failed existance check: " + uri, x);
        }
    }

    public InputStream getInputStream() throws IOException {
        JarURLConnection con = (JarURLConnection)url.openConnection();
        con.setUseCaches(false);
        return con.getInputStream();
    }

    public boolean isFile() {
        //TODO: support directory entries
        return true;
    }

    public long lastModified() {
        try {
            JarURLConnection con = (JarURLConnection)url.openConnection();
            return con.getLastModified();
        } catch(IOException x) {
            throw new RuntimeException("Error checking modification time", x);
        }
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
