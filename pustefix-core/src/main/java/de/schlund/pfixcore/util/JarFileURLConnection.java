package de.schlund.pfixcore.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JarFileURLConnection extends JarURLConnection {

    static Map<URI,CacheEntry> jarFileCache = new HashMap<URI,CacheEntry>();

    static class CacheEntry {
        File file;
        JarFile jarFile;
        long lastMod;
    }
    
    private static CacheEntry getCacheEntry(URI uri) throws IOException {
        CacheEntry entry = jarFileCache.get(uri);
        if(entry == null || (entry.file.lastModified() > entry.lastMod)) {
            entry = new CacheEntry();
            entry.file = new File(uri);
            entry.jarFile = new JarFile(entry.file);
            entry.lastMod = entry.file.lastModified();
            jarFileCache.put(uri, entry);
        }
        return entry;
    }
    
    
    private URI jarFileURI;
    private String entryPath;
    
    
    public JarFileURLConnection(URL url) throws MalformedURLException {
        super(url);
        if(!url.getProtocol().equals("jar")) 
            throw new IllegalArgumentException("URL protocol not supported: " + url.toString()); 
        String path = getURL().getPath();
        int ind = path.indexOf("!");
        String jarPath = path.substring(0,ind);
        entryPath = path.substring(ind+1);
        if(entryPath.startsWith("/")) entryPath = entryPath.substring(1);
        try {
            jarFileURI = new URI(jarPath);
        } catch (URISyntaxException e) {
            throw new RuntimeException("Can't create URI to JAR file: " + jarPath, e);
        }
    }

    @Override
    public void connect() {
        
    }
    
    @Override
    public InputStream getInputStream() throws IOException {
        JarEntry entry = getJarEntry();
        return getCacheEntry(jarFileURI).jarFile.getInputStream(entry);
    }
    
    @Override
    public long getLastModified() {
        try {
            return getCacheEntry(jarFileURI).file.lastModified();
        } catch(IOException x) {
            return 0;
        }
    }

    @Override
    public JarFile getJarFile() throws IOException {
        return getCacheEntry(jarFileURI).jarFile;
    }
    
    @Override
    public JarEntry getJarEntry() throws IOException {
        JarEntry entry = getCacheEntry(jarFileURI).jarFile.getJarEntry(entryPath);
        if(entry == null) throw new FileNotFoundException("JAR entry "+ entryPath + "not found in " + jarFileURI.getPath());
        return entry;
    }
    
}
