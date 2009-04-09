/*
 * This file is part of Pustefix.
 *
 * Pustefix is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Pustefix is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Pustefix; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */

package org.pustefixframework.maven.plugins;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

/**
 * 
 * @author mleidig@schlund.de
 * 
 */
public class Apt {
    private final File srcDir;
    private final File destDir;
    private final File preprocessDir;

    private final File basedir;
    private final Log log;
    
    public Apt(File basedir, File preprocessDir, Log log) {
        this.basedir = basedir;
        this.srcDir = new File(basedir, "src/main/java");
        this.destDir = new File(basedir, "target/classes");
        this.preprocessDir = preprocessDir;
        this.log = log;
    }

    public int execute(String classpath) throws MojoExecutionException {
        File lastRunFile = new File(basedir, "target/.lastaptrun");
        long lastRun = lastRunFile.lastModified();
        List<File> modified = getModifiedFiles(lastRun);
        if (modified.size() > 0) {
            callApt(modified, classpath);
        }
        lastRunFile.delete();
        try {
            lastRunFile.createNewFile();
        } catch (IOException x) {
            throw new MojoExecutionException("cannot create " + lastRun);
        }
        return modified.size();
    }

    private List<File> getModifiedFiles(long lastAptRun) throws MojoExecutionException {
        IWrapperFileScanner fileScanner = new IWrapperFileScanner();
        List<File> modList = new ArrayList<File>();
        if (!srcDir.exists()) throw new MojoExecutionException("Source directory doesn't exist: " + srcDir.getAbsolutePath());
        List<File> newFiles = fileScanner.getChangedFiles(srcDir, destDir, lastAptRun);
        modList.addAll(newFiles);
        if (fileScanner.getScanCount() > 0) System.out.println(fileScanner.printStatistics());
        return modList;
    }

    private void callApt(List<File> files, String classpath) throws MojoExecutionException {
        File filelist;
        PrintWriter out;

        log.info("Processing " + files.size() + " source file" + (files.size() > 1 ? "s" : ""));
        try {
            filelist = File.createTempFile("pfx-aptfiles-", ".tmp", null);
            filelist.deleteOnExit();
            out = new PrintWriter(new FileWriter(filelist));
            for (File file : files) {
                String path = file.getAbsolutePath();
                if (path.indexOf(" ") > -1) {
                    path = path.replace(File.separatorChar, '/');
                    path = "\"" + path + "\"";
                } 
                out.println(path);
            }
            out.close();
        } catch (IOException x) {
            throw new MojoExecutionException("Error creating file list", x);
        }

        List<String> cmd = new ArrayList<String>();
        cmd.add("apt");
        cmd.add("-J-Xmx512m");
        cmd.add("-classpath");
        cmd.add(classpath);
        cmd.add("-sourcepath");
        cmd.add(srcDir.toString());
        cmd.add("-nocompile");
        cmd.add("-encoding");
        cmd.add("UTF-8");
        cmd.add("-factory");
        cmd.add("de.schlund.pfixcore.util.CommonAnnotationProcessorFactory");
        cmd.add("-s");
        cmd.add(preprocessDir.toString());
        cmd.add("@" + filelist);
        log.debug(cmd.toString());
            
        try {
            ProcessBuilder builder = new ProcessBuilder(cmd);
            builder.directory(basedir);
            builder.redirectErrorStream(true);
            int ret = builder.start().waitFor();
            if (ret != 0) throw new MojoExecutionException("Error while executing apt (exit value: " + ret + ").");
        } catch (IOException e) {
            throw new MojoExecutionException("Error invoking apt", e);
        } catch (InterruptedException e) {
            throw new MojoExecutionException("Apt interrupted", e);
        }
        filelist.delete();
    }
}
