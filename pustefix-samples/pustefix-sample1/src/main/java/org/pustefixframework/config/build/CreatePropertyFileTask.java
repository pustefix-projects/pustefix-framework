/*
 * This file is part of PFIXCORE.
 *
 * PFIXCORE is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * PFIXCORE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with PFIXCORE; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.pustefixframework.config.build;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.pustefixframework.config.generic.PropertyFileReader;

/**
 * Ant task which creates standard Java property files from customizable
 * Pustefix XML property files.
 * 
 * TODO: support replacement of docroot in created property files (for property
 * files consumed by non-Pustefix objects, which can't handle special URLs)
 * 
 * @author mleidig
 *
 */
public class CreatePropertyFileTask extends MatchingTask {

    private final static String DEFAULT_EXTENSION = ".prop";
    
    private File baseDir;
    private String extension;
    
    @Override
    public void execute() throws BuildException {
        
        if(extension==null) extension = DEFAULT_EXTENSION;
        if(!extension.startsWith(".")) extension = "."+extension;
        if(baseDir == null) throw new BuildException("Missing 'basedir' attribute!");
        
        DirectoryScanner scanner = getDirectoryScanner(baseDir);
        String[] files = scanner.getIncludedFiles();
        int createCnt = 0;
        int totalCnt = 0;
        for (String file : files) {
            totalCnt++;
            File srcFile = new File(baseDir, file);
            String destFileName = srcFile.getName();
            destFileName = destFileName.substring(0,destFileName.indexOf('.'))+extension;
            File destFile = new File(srcFile.getParentFile(), destFileName);
            if(!destFile.exists() || srcFile.lastModified()>destFile.lastModified()) {
                Properties props = new Properties();
                try {
                    //without setting the context classloader the config parser
                    //doesn't find the parser configuration file in META-INF
                    ClassLoader oldCL = Thread.currentThread().getContextClassLoader();
                    Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
                    PropertyFileReader.read(srcFile, props);
                    Thread.currentThread().setContextClassLoader(oldCL);
                } catch(Exception x) {
                    throw new BuildException("Reading property file '" + srcFile.getAbsolutePath() +
                            "' failed: " + x.getMessage(), x);
                }
                try {
                    FileOutputStream out = new FileOutputStream(destFile);
                    props.store(out, "Auto-generated from "+srcFile.getName());
                } catch(IOException x) {
                    throw new BuildException("Writing property file '" + destFile.getAbsolutePath() +
                            "' failed: " + x.getMessage(), x);
                }
                log("Created property file: " + destFile.getAbsolutePath(), Project.MSG_VERBOSE);
                createCnt++;
            }
        }
        if(createCnt>0) {
            log("Created " + createCnt + " from " + totalCnt + " property files.", Project.MSG_INFO);
        }
    }
    
    public void setBaseDir(File baseDir) {
        this.baseDir = baseDir;
    }
    
    public void setExtension(String extension) {
        this.extension = extension;
    }
    
}
