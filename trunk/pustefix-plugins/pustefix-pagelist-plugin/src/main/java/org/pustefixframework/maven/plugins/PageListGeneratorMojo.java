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
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;


/**
 * Generates list with display names of all pages.
 *
 * @goal generate
 * @phase generate-test-resources
 *
 * @requiresDependencyResolution compile
 */
public class PageListGeneratorMojo extends AbstractMojo {

    /**
     * @parameter default-value="${basedir}/src/main/webapp"
     * @required
     */
    private File docroot;
    
    /**
     * @parameter default-value="prod"
     */
    private String mode;

    /**
     * @parameter default-value="${project.build.directory}/generated-test-resources"
     */
    private File outputDirectory;

    /** @parameter default-value="${project}" */
    private MavenProject mavenProject;

    public void execute() throws MojoExecutionException {
        
        if ("pom".equals(mavenProject.getPackaging())) {
            getLog().info("Generated Plugin invoked for packaging pom - ignored.");
            getLog().info("(This happens if you declare the plugin in your parent pom - which is fine)");
            return;
        }
        if(!outputDirectory.exists()) outputDirectory.mkdirs();
        
        URLClassLoader loader = getProjectRuntimeClassLoader();
        ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
        try {
            Class<?> generator = Class.forName("org.pustefixframework.test.PageListGenerator", true, loader);
            Method meth = generator.getMethod("generate", File.class, File.class, String.class);
            Object instance = generator.newInstance();
            Thread.currentThread().setContextClassLoader(loader);
            List<File> files = (List<File>)meth.invoke(instance, docroot, outputDirectory, mode);
            if(getLog().isDebugEnabled()) {
                for(File file : files) {
                    getLog().debug("Generated pagelist file " + file.getAbsolutePath());
                }
            } else if(getLog().isInfoEnabled()) {
                getLog().info("Generated " + files.size() + " pagelist" + ( files.size() > 1 ? "s" : "") + ".");
            }
        } catch (Exception x) {
            throw new MojoExecutionException("Can't create pagelist", x);
        } finally {
            Thread.currentThread().setContextClassLoader(contextLoader);
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
