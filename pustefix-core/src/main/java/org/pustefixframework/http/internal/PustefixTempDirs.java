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
        for(File tempDir: tempDirs) {
            FileUtils.delete(tempDir);
        }
    }
    
    public static PustefixTempDirs getInstance(ServletContext servletContext) {
        PustefixTempDirs creator = (PustefixTempDirs)servletContext.getAttribute(PustefixTempDirs.class.getName());
        if(creator == null) {
            creator = new PustefixTempDirs(servletContext);
            servletContext.setAttribute(PustefixTempDirs.class.getName(), creator);
        }
        return creator;
    }

}
