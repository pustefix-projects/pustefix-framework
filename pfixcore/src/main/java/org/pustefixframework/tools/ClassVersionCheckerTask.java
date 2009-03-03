package org.pustefixframework.tools;

import java.io.File;
import java.io.IOException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.taskdefs.MatchingTask;

/**
 * 
 * Ant task which checks the class file version compatibility of
 * class files (directly on the file system or within jar files).
 * 
 * @author mleidig@schlund.de
 *
 */
public class ClassVersionCheckerTask extends MatchingTask {

    private File baseDir;
    private String targetVersion;
    
    @Override
    public void execute() throws BuildException {
        if(baseDir == null) baseDir = new File(".");
        if(!ClassVersionChecker.supportsJavaVersion(targetVersion)) 
            throw new BuildException("Checker doesn't support this Java version: " + targetVersion);
        DirectoryScanner scanner = getDirectoryScanner(baseDir); 
        String[] incFiles = scanner.getIncludedFiles();
        int total = 0;
        for(String incFile: incFiles) {
            File file = getProject().resolveFile(incFile);
            try {
                int result = ClassVersionChecker.checkCompatibility(file, targetVersion);
                total += result;
            } catch(IOException x) {
                throw new BuildException("Error checking class version of file: " + file.getAbsolutePath(), x);
            }
        }
        if(total > 0) 
            throw new BuildException("Found " + total + " class" + (total>1?"es":"") + 
                    " with incompatible class file version.");
    }
    
    public void setBaseDir(File baseDir) {
        this.baseDir = baseDir;
    }
    
    public void setTargetVersion(String targetVersion) {
        this.targetVersion = targetVersion;        
    }
    
}
