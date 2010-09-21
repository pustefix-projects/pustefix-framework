package de.schlund.pfixcore.util;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.rmi.server.UID;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * JarFile instance cache providing JarEntries backed by file system cache.
 * 
 * @author mleidig@schlund.de
 *
 */
public class JarFileCache {
  
    private final static String PROP_JAVA_TMP = "java.io.tmpdir";
    //context attribute normally is only set as context attribute,
    //but can be used here as system property to override java.io.tmpdir
    private final static String PROP_SERVLET_TMP = "javax.servlet.context.tempdir";
    
    private File tempDir;
    private Map<URL,CacheEntry> jarFileCache = new HashMap<URL,CacheEntry>();
    
    public JarFileCache() {
        Runtime.getRuntime().addShutdownHook(new ShutdownHook());
        createTempDir();
    }
    
    private void createTempDir() {
        String tmp = System.getProperty(PROP_SERVLET_TMP);
        if(tmp == null || tmp.equals("")) {
            tmp = System.getProperty(PROP_JAVA_TMP);
            if(tmp == null || tmp.equals(""))  {
                throw new RuntimeException("Can't find temporary directory");
            }
        }
        UID uid = new UID();
        tempDir = new File(tmp, uid.toString());
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
        
        File file = new File(tempDir, cacheEntry.id + "/" + path);
       
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
        if(tempDir != null) delete(tempDir);
    }
    
    
    class ShutdownHook extends Thread {
        
        @Override
        public void run() {
            if(tempDir != null) delete(tempDir);
        }
        
    }
    
    
    class CacheEntry {
        File file;
        JarFile jarFile;
        long lastMod;
        int id;
    }
    
}
