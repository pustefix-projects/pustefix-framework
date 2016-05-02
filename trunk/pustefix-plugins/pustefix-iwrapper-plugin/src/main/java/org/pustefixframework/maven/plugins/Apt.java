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
 */

package org.pustefixframework.maven.plugins;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.processing.AbstractProcessor;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import de.schlund.pfixcore.generator.iwrpgen.IWrapperAnnotationProcessor;

/**
 * 
 * @author mleidig@schlund.de
 * 
 */
public class Apt implements DiagnosticListener<JavaFileObject> {
    
    private final File srcDir;
    private final File destDir;
    private final File aptDir;
    private final File basedir;
    private final Log log;
    private MavenProject mavenProject;
    
    public Apt(File basedir, File aptDir, Log log, MavenProject mavenProject) {
        this.basedir = basedir;
        //TODO: support src/test/java too
        this.srcDir = new File(basedir, "src/main/java");
        this.destDir = new File(basedir, "target/classes");
        this.aptDir = aptDir;
        this.log = log;
        this.mavenProject = mavenProject;
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
            if(!lastRunFile.getParentFile().exists()) lastRunFile.getParentFile().mkdirs();
            lastRunFile.createNewFile();
        } catch (IOException x) {
            throw new MojoExecutionException("cannot create " + lastRun, x);
        }
        return modified.size();
    }

    private List<File> getModifiedFiles(long lastAptRun) throws MojoExecutionException {
        IWrapperFileScanner fileScanner = new IWrapperFileScanner();
        List<File> modList = new ArrayList<File>();
        if (!srcDir.exists()) throw new MojoExecutionException("Source directory doesn't exist: " + srcDir.getAbsolutePath());
        List<File> newFiles = fileScanner.getChangedFiles(srcDir, destDir, lastAptRun);
        modList.addAll(newFiles);
        if(log.isDebugEnabled()) {
            if (fileScanner.getScanCount() > 0) log.debug(fileScanner.printStatistics());
        }
        return modList;
    }

    private void callApt(List<File> files, String classpath) throws MojoExecutionException {
        File filelist;
        PrintWriter out;
        if(log.isDebugEnabled()) {
            log.debug("Processing " + files.size() + " source file" + (files.size() > 1 ? "s" : ""));
        }
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
        
        if(!aptDir.exists()) {
            aptDir.mkdirs();
        }

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        
        List<String> options = new ArrayList<String>();
        options.add("-classpath");
        StringBuilder sb = new StringBuilder();
        URLClassLoader urlClassLoader = getCompileClassLoader();
        for(URL url: urlClassLoader.getURLs()) {
            sb.append(url.getFile()).append(File.pathSeparator);
        }
        sb.append(srcDir.getAbsolutePath());
        List<?> elements = mavenProject.getCompileSourceRoots();
        for (int i = 0; i < elements.size(); i++) {
            String srcPath = (String)elements.get(i);
            if(!srcPath.equals(srcDir.getAbsolutePath())) {
                sb.append(File.pathSeparator).append(srcPath);
            }
        }
        options.add(sb.toString());
        options.add("-proc:only");
        options.add("-s");
        options.add(aptDir.getAbsolutePath());
        
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(files);
        CompilationTask task = compiler.getTask(null, fileManager, this, options, null, compilationUnits);
        LinkedList<AbstractProcessor> processors = new LinkedList<AbstractProcessor>();
        processors.add(new IWrapperAnnotationProcessor());
        task.setProcessors(processors);
        task.call();
        
        File[] aptFiles = aptDir.listFiles();
        if(aptFiles == null) {
            aptDir.delete();
        }
        
        filelist.delete();
    }
    
    private URLClassLoader getCompileClassLoader() throws MojoExecutionException {
        try {
            List<?> elements = mavenProject.getCompileClasspathElements();
            URL[] urls = new URL[elements.size()];
            for (int i = 0; i < elements.size(); i++) {
                String element = (String) elements.get(i);
                urls[i] = new File(element).toURI().toURL();
            }
            return new URLClassLoader(urls);
        } catch (Exception x) {
            throw new MojoExecutionException("Can't create project runtime classloader", x);
        }
    }
    
    public void report(Diagnostic<? extends JavaFileObject> diagnostic) {
        Diagnostic.Kind kind = diagnostic.getKind();
        if(kind == Diagnostic.Kind.NOTE || kind == Diagnostic.Kind.OTHER) {
            log.debug(diagnostic.toString());
        } else if(kind == Diagnostic.Kind.ERROR) {
            log.error(diagnostic.toString());
        } else if(kind == Diagnostic.Kind.WARNING) {
            log.debug(diagnostic.toString());
        } else if(kind == Diagnostic.Kind.MANDATORY_WARNING) {
            log.warn(diagnostic.toString());
        }
    }

}
