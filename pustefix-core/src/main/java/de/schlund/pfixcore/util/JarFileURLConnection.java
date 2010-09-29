package de.schlund.pfixcore.util;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JarFileURLConnection extends JarURLConnection {
    
    private boolean doCache = true;
    private File cachedFile;
   
    public JarFileURLConnection(URL url) throws MalformedURLException {
        super(url);
    }

    @Override
    public synchronized void connect() throws IOException {
        if(!connected) {
            if(doCache) {
                cachedFile = JarFileCache.getInstance().getFile(getJarFileURL(), getEntryName());
            }
            connected = true;
        }
    }
    
    @Override
    public synchronized InputStream getInputStream() throws IOException {
        connect();
        if(doCache) {
            return new FileInputStream(cachedFile);
        } else {
            return getJarFile().getInputStream(getJarEntry());
        }
    }
    
    @Override
    public synchronized long getLastModified() {
        try {
            connect();
            if(doCache) {
                return cachedFile.lastModified();
            } else {
                long lastMod = getJarEntry().getTime();
                if(lastMod == -1) lastMod = JarFileCache.getInstance().getLastModified(getJarFileURL());
                return lastMod;
            }
        } catch(IOException x) {
            return 0;
        }
    }
    
    @Override
    public int getContentLength() {
        int len = -1;
        try {
            connect();
            if(doCache) {
                len = (int)cachedFile.length();
            } else {
                len = (int)getJarEntry().getSize();
            }
        } catch(IOException x) {
            //ignore
        }
        return len;
    }

    @Override
    public synchronized JarFile getJarFile() throws IOException {
        return JarFileCache.getInstance().getJarFile(getJarFileURL());
    }
    
    @Override
    public synchronized JarEntry getJarEntry() throws IOException {
        if(getEntryName() == null) return null;
        JarEntry entry = getJarFile().getJarEntry(getEntryName());
        if(entry == null) throw new FileNotFoundException("JAR entry "+ getEntryName() + "not found in " + getJarFileURL());
        return entry;
    }
       
}