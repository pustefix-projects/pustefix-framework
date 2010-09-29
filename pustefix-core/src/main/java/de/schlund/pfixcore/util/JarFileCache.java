package de.schlund.pfixcore.util;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.rmi.server.UID;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import de.schlund.pfixxml.util.MD5Utils;

/**
 * JarFile instance cache providing JarEntries backed by file system cache.
 * 
 * @author mleidig@schlund.de
 *
 */
public class JarFileCache {
    
    private static JarFileCache instance;
    
    private File cacheDir;
    private Map<URL,CacheEntry> jarFileCache = new HashMap<URL,CacheEntry>();
    
    public synchronized static JarFileCache getInstance() {
        if(instance == null) instance = new JarFileCache();
        return instance;
    }
    
    public synchronized static void setTempDir(File tempDir) {
        instance = new JarFileCache(tempDir);
    }
    
    public JarFileCache() {
        this(new File(System.getProperty("java.io.tmpdir")));
    }
    
    public JarFileCache(File tempDir) {
        if(!tempDir.exists()) throw new RuntimeException("Temporary directory doesn't exist: " + tempDir.getPath());
        UID uid = new UID();
        String md5 = MD5Utils.hex_md5(uid.toString());
        cacheDir = new File(tempDir, md5);
        Runtime.getRuntime().addShutdownHook(new ShutdownHook());
    }
    
    public JarFile getJarFile(URL url) throws IOException {
        return getCachedJarFile(url).jarFile;
    }
    
    public long getLastModified(URL url) throws IOException {
        return getCachedJarFile(url).lastMod;
    }
    
    private synchronized CacheEntry getCachedJarFile(URL url) throws IOException {
        CacheEntry entry = jarFileCache.get(url);
        if(entry == null || (entry.file.lastModified() > entry.lastMod)) {
            entry = new CacheEntry();
            try {
                entry.file = new File(url.toURI());
            } catch (URISyntaxException e) {
                throw new RuntimeException("Illegal jarfile URI", e);
            }
            entry.jarFile = new JarFile(entry.file);
            entry.lastMod = entry.file.lastModified();
            entry.id = jarFileCache.size();
            jarFileCache.put(url, entry);
        }
        return entry;
    }
    
    public synchronized File getFile(URL jarURL, String path) throws IOException {
        CacheEntry cacheEntry = getCachedJarFile(jarURL);
        
        File file = new File(cacheDir, cacheEntry.id + "/" + path);
       
        if(!file.exists()) {
            JarEntry entry = cacheEntry.jarFile.getJarEntry(path);
            long lastModified = entry.getTime();
            InputStream in = cacheEntry.jarFile.getInputStream(entry);
            if(!file.getParentFile().exists()) file.getParentFile().mkdirs();
            FileOutputStream out = new FileOutputStream(file);
            byte[] buffer = new byte[4096];
            int no = 0;
            try {
                while ((no = in.read(buffer)) != -1)
                    out.write(buffer, 0, no);
            } finally {
                in.close();
                out.close();
            }
            file.setLastModified(lastModified);
        }
        return file;    
    }
    
    public static boolean delete(File file) {
        if(file.isDirectory()) {
            File[] files=file.listFiles();
            for(int i=0;i<files.length;i++) {
                delete(files[i]);
            }
        }
        return file.delete();
    }
    
    @Override
    protected void finalize() throws Throwable {
        if(cacheDir != null) delete(cacheDir);
    }
    
    
    class ShutdownHook extends Thread {
        
        @Override
        public void run() {
            if(cacheDir != null) delete(cacheDir);
        }
        
    }
    
    
    class CacheEntry {
        File file;
        JarFile jarFile;
        long lastMod;
        int id;
    }
    
}
