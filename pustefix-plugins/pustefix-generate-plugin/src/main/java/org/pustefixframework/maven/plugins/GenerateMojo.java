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
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;


/**
 * Generate all XSL targets with TargetGenerator.
 *
 * @goal generate
 * @phase prepare-package
 *
 * @requiresDependencyResolution compile
 * @threadSafe
 */
public class GenerateMojo extends AbstractMojo {

    /**
     * The document root directory.
     * 
     * @parameter default-value="${basedir}/src/main/webapp"
     */
    private File docroot;

    /**
     * The webapp's Maven target directory.
     * 
     * @parameter default-value="${basedir}/target/${project.artifactId}-${project.version}/"
     */
    private File webappdir;
    
    /**
     * Generate page XSLs in parallel using one thread per CPU core.
     * 
     * @parameter default-value=false
     */
    private boolean parallel;
    
    /**
     * Remove intermediate output files after page XSLs are created.
     * 
     * @parameter default-value=true
     */
    private boolean cleanup;
    
    /**
     * The maximum size of a generated page XSL file (e.g. 500k, 0.5m)
     * 
     * @parameter
     */
    private String maxPageSize;
    
    /**
     * The maximum total size of all generated page XSL files (e.g. 500m, 0.5g)
     * 
     * @parameter
     */
    private String maxTotalPageSize;
    
    /**
     * List of page XSL files to ignore when checking the maximum size.
     * 
     * @parameter
     */
    private String maxPageSizeIgnore;
    
    /** 
     * @parameter default-value="${project}"
     */
    private MavenProject mavenProject;
    
    private long maxXslSize = Long.MAX_VALUE;
    private long maxTotalXslSize = Long.MAX_VALUE;
    private Set<String> maxXslSizeIgnore = new HashSet<>();
    

    public void execute() throws MojoExecutionException {
        
        if ("pom".equals(mavenProject.getPackaging())) {
            getLog().info("Generated Plugin invoked for packaging pom - ignored.");
            getLog().info("(This happens if you declare the plugin in your parent pom - which is fine)");
            return;
        }

        if(maxPageSize != null) {
            maxXslSize = getSizeInBytes(maxPageSize);
        }
        if(maxTotalPageSize != null) {
            maxTotalXslSize = getSizeInBytes(maxTotalPageSize);
        }
        if(maxPageSizeIgnore != null) {
            maxXslSizeIgnore = new HashSet<>();
            String[] ignores = maxPageSizeIgnore.split("(\\s+)|(\\s*,\\s*)");
            for(String ignore: ignores) {
                maxXslSizeIgnore.add(ignore);
            }
        }
        
        if(!webappdir.exists()) webappdir.mkdirs();
        
        File cache = new File(webappdir, ".cache");
        
        Logger reportLogger = new GenerateReportLogger(getLog());
        if(getLog().isDebugEnabled()) {
            reportLogger.setLevel(Level.FINE);
        } else if(getLog().isInfoEnabled()) {
            reportLogger.setLevel(Level.INFO);
        } else if(getLog().isWarnEnabled()) {
            reportLogger.setLevel(Level.WARNING);
        } else if(getLog().isErrorEnabled()) {
            reportLogger.setLevel(Level.SEVERE);
        }
        
        URLClassLoader loader = getProjectRuntimeClassLoader();
        ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
        try {
            Class<?> generator = Class.forName("de.schlund.pfixxml.targets.TargetGeneratorRunner", true, loader);
            Method meth = generator.getMethod("run", File.class, File.class, String.class, boolean.class, Logger.class);
            Object instance = generator.newInstance();
            Thread.currentThread().setContextClassLoader(loader);
            boolean ok = (Boolean) meth.invoke(instance, docroot, cache, "prod", parallel, reportLogger);
            if (!ok)
                throw new MojoExecutionException("Target generation errors occurred.");
        } catch (Exception x) {
            throw new MojoExecutionException("Can't create targets", x);
        } finally {
            Thread.currentThread().setContextClassLoader(contextLoader);
        }
        
        if(cleanup) {
            cleanupFiles(cache);
        }
        checkFileSize(cache);
    }

    /**
     * Remove non-top-level target and dependency information files
     * to reduce size of cache directory
     */
    private void cleanupFiles(File cacheDir) {
        File[] files = cacheDir.listFiles();
        for(File file: files) {
            String name = file.getName();
            if(name.endsWith(".aux")) {
                file.delete();
            } else if(name.endsWith(".xml")) {
                File xslFile = new File(file.getParentFile(), name.substring(0, name.length()-4) + ".xsl");
                if(xslFile.exists()) {
                    file.delete();
                }
            }
        }
    }
    
    private void checkFileSize(File cacheDir) throws MojoExecutionException {
       
        getLog().info("================================== XSL size analysis =======================================");
        File[] files = cacheDir.listFiles();
        long totalCount = 0;
        long totalSize = 0;
        for(File file: files) {
            String name = file.getName();
            if(name.endsWith(".xsl")) {
                totalCount++;
                totalSize += file.length();
            }
        }
        getLog().info("Generated " + totalCount + " XSL files with total size of " + readableFileSize(totalSize));
        
        long avgSize = totalSize / totalCount;
        long limit = avgSize * 4;
        List<File> largeFiles = new ArrayList<>();
        List<File> exceedingFiles = new ArrayList<>();
        for(File file: files) {
            String name = file.getName();
            if(name.endsWith(".xsl")) {
                if(file.length() > limit) {
                    largeFiles.add(file);
                }
                if(!maxXslSizeIgnore.contains(name) && file.length() > maxXslSize) {
                    exceedingFiles.add(file);
                }
            }
        }
        
        if(largeFiles.size() > 0) {
            getLog().info("--------------------------------------------------------------------------------------------");
            getLog().info("Remarkably large files (more than 4x average size of " + readableFileSize(avgSize) + "):");
            for(File file: largeFiles) {
                getLog().info(file.getName() + " => " + readableFileSize(file.length()));
            }
        }
        
        if(exceedingFiles.size() > 0) {
            getLog().info("--------------------------------------------------------------------------------------------");
            getLog().info("XSL files exceeding the maxPageSize limit '" + maxPageSize + "':");
            for(File file: exceedingFiles) {
                getLog().info(file.getName() + " => " + readableFileSize(file.length()));
            }
        }
        
        getLog().info("============================================================================================");
        
        if(exceedingFiles.size() > 0) {
            throw new MojoExecutionException(exceedingFiles.size() + " XSL file" + (exceedingFiles.size() > 1 ? "s" : "") + 
                    " exceeded the maxPageSize limit '" + maxPageSize + "'.");
        }
        if(totalSize > maxTotalXslSize) {
            throw new MojoExecutionException("The generated XSL files exceed the maxTotalPageSize limit '" + maxTotalPageSize + "'.");
        }
    }
    
    private String readableFileSize(long size) {
        
        String[] units = new String[] {"B", "KB", "MB", "GB", "TB"};
        String formatted;
        if(size > 0) {
            int ind = (int) (Math.log(size)/Math.log(1024));
            if(ind >= units.length) {
                ind = units.length - 1;
            }
            formatted = new DecimalFormat("###0.#").format(size/Math.pow(1024, ind)) + " " + units[ind];
        } else {
            formatted = "0 " + units[0];
        }
        return formatted;
    }
    
    private long getSizeInBytes(String size) {
        Pattern SIZE_PATTERN = Pattern.compile("([0-9]+([.][0-9]+)?)[ ]*(b|k|kb|m|mb|g|gb)?");
        Matcher m = SIZE_PATTERN.matcher(size.toLowerCase());
        if(m.matches()) {
            float f = Float.parseFloat(m.group(1));
            if(m.group(3) != null) {
                String unit = m.group(3);
                if(unit.equals("k") || unit.equals("kb")) {
                    f = f * 1024;
                } else if(unit.equals("m") || unit.equals("mb")) {
                    f = f * 1024 * 1024;
                } else if(unit.equals("g") || unit.equals("gb")) {
                    f = f * 1024 * 1024 * 1024;
                }
            }
            return (long)f;
        } else {
            throw new IllegalArgumentException("Illegal size value: " + size);
        }
    }
    
    private URLClassLoader getProjectRuntimeClassLoader() throws MojoExecutionException {
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

}
