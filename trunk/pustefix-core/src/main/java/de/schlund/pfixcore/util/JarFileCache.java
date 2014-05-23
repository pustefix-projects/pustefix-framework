package de.schlund.pfixcore.util;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import de.schlund.pfixxml.util.FileUtils;

/**
 * JarFile instance cache providing JarEntries backed by file system cache.
 * 
 * @author mleidig@schlund.de
 *
 */
public class JarFileCache {

    private static JarFileCache instance;

    private final File cacheDir;
    private final Map<URL,CacheEntry> jarFileCache = new HashMap<URL,CacheEntry>();

    public synchronized static JarFileCache getInstance() {
        if(instance == null) instance = new JarFileCache();
        return instance;
    }

    public synchronized static void setCacheDir(File cacheDir) {
        if(instance != null) {
            instance.dispose();
        }
        instance = new JarFileCache(cacheDir);
    }

    public JarFileCache() {
        Path tempDir = FileSystems.getDefault().getPath(System.getProperty("java.io.tmpdir"));
        try {
            this.cacheDir = Files.createTempDirectory(tempDir, "pustefix-jar-cache-").toFile();
        } catch(IOException x) {
            throw new RuntimeException("Error creating temporary directory for JAR caching", x);
        }
    }

    public JarFileCache(File cacheDir) {
        this.cacheDir = cacheDir;
        if(cacheDir.exists()) {
            FileUtils.delete(cacheDir);
        }
        cacheDir.mkdirs();
    }
    
    public JarFile getJarFile(URL url) throws IOException {
        return getCachedJarFile(url).jarFile;
    }

    public long getLastModified(URL url) throws IOException {
        return getCachedJarFile(url).lastMod;
    }

    private synchronized CacheEntry getCachedJarFile(URL url) throws IOException {
        //no "findbugs : DMI_COLLECTION_OF_URLS" here, because only file URLs in use
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
            //no "findbugs : DMI_COLLECTION_OF_URLS" here, because only file URLs in use
            jarFileCache.put(url, entry);
        }
        return entry;
    }

    public synchronized File getFile(URL jarURL, String path) throws IOException {
        CacheEntry cacheEntry = getCachedJarFile(jarURL);

        File file = new File(cacheDir, cacheEntry.id + "/" + path);

        if(!file.exists()) {
            JarEntry entry = cacheEntry.jarFile.getJarEntry(path);
            if(entry == null) throw new FileNotFoundException("Jar entry '" + path + 
                    "' not found in file '" + jarURL.toString() +"'");
            //Work around JDK bug: calling getJarEntry() for directory entry using name with 
            //no trailing slash returns JarEntry object keeping this name, and calling 
            //isDirectory() on this object checks for the slash and returns false.
            //Therefor we additionally check entries with with size 0 and try to get
            //an entry with a trailing slash
            if(!entry.isDirectory() && entry.getSize() == 0) {
                String dirPath = path + "/";
                JarEntry dirEntry = cacheEntry.jarFile.getJarEntry(dirPath);
                if(dirEntry != null) entry = dirEntry;
            }
            if(entry.isDirectory()) {
                file.mkdirs();
            } else {
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
        }
        return file;    
    }
    
    @Override
    protected void finalize() throws Throwable {
        if(cacheDir != null) dispose();
    }
    
    public void dispose() {
        FileUtils.delete(cacheDir);
    }

    class CacheEntry {
        File file;
        JarFile jarFile;
        long lastMod;
        int id;
    }

}
