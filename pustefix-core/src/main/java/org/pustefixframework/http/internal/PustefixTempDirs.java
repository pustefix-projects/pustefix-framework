package org.pustefixframework.http.internal;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;

import de.schlund.pfixxml.util.FileUtils;

public class PustefixTempDirs {
    
    private File baseTempDir;
    private List<File> tempDirs = new ArrayList<File>();
    private Thread shutdownHook;
    
    public PustefixTempDirs(ServletContext servletContext) {
        baseTempDir = (File)servletContext.getAttribute("javax.servlet.context.tempdir");
        if(baseTempDir == null) {
            String tmp = System.getProperty("java.io.tmpdir");
            if(tmp != null) {
                baseTempDir = new File(tmp);
            } else {
                baseTempDir = new File(".");
            }
        }
    }
    
    public File createTempDir(String prefix) throws IOException {
        File tempDir = Files.createTempDirectory(baseTempDir.toPath(), prefix).toFile();
        tempDirs.add(tempDir);
        return tempDir;
    }
    
    public void dispose() {
        cleanup();
        unregisterShutdownHook();
    }
    
    private void cleanup() {
        for(File tempDir: tempDirs) {
            FileUtils.delete(tempDir);
        }
    }
    
    private void registerShutdownHook() {
        shutdownHook = new Thread() {
            public void run() {
                cleanup();
            };
        };
        Runtime.getRuntime().addShutdownHook(shutdownHook);
    }
    
    private void unregisterShutdownHook() {
        if(shutdownHook != null) {
            try {
                Runtime.getRuntime().removeShutdownHook(shutdownHook);
            } catch(IllegalStateException x) {
                //ignore if JVM is already in shutdown process
            }
        }
    }
    
    public static PustefixTempDirs getInstance(ServletContext servletContext) {
        PustefixTempDirs creator = (PustefixTempDirs)servletContext.getAttribute(PustefixTempDirs.class.getName());
        if(creator == null) {
            creator = new PustefixTempDirs(servletContext);
            creator.registerShutdownHook();
            servletContext.setAttribute(PustefixTempDirs.class.getName(), creator);
        }
        return creator;
    }

}
