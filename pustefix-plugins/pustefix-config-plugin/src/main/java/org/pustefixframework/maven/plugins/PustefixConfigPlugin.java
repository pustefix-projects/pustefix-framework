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
 *
 */

package org.pustefixframework.maven.plugins;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.types.Path;
import org.codehaus.plexus.util.StringUtils;

/**
 * Generates everything still on ant.
 *
 * @goal generate
 * @requiresDependencyResolution compile
 */
public class PustefixConfigPlugin extends AbstractMojo {
    protected void executeTasks() throws MojoExecutionException {
        try {
            Project antProject;
            DefaultLogger antLogger;
            
            antProject = new Project();
            ProjectHelper.configureProject(antProject, new File("/home/mhm/Projects/pustefixframework/pustefix-plugins/pustefix-config-plugin/src/main/resources/build.xml"));
            antLogger = new DefaultLogger();
            antLogger.setOutputPrintStream(System.out);
            antLogger.setErrorPrintStream(System.err);
            antLogger.setMessageOutputLevel(getLog().isDebugEnabled() ? Project.MSG_DEBUG : Project.MSG_INFO );
            antProject.addBuildListener(antLogger);
            antProject.setBaseDir(project.getBasedir());
            antProject.addReference("maven.compile.classpath", path(antProject, project.getCompileClasspathElements()));
            antProject.addReference("maven.plugin.classpath", path(antProject, getPath(pluginClasspath)));
            ((Target) antProject.getTargets().get("all")).execute();
        } catch (BuildException e) {
            throw new MojoExecutionException("An Ant BuildException has occured: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new MojoExecutionException("Error executing ant tasks: " + e.getMessage(), e);
        }
    }

    public Path path(Project antProject, List<String> items) {
        Path result;
        
        result = new Path(antProject);
        result.setPath(StringUtils.join(items.iterator(), File.pathSeparator));
        return result;
    	
    }
    public static List<String> getPath(Collection<Artifact> artifacts) {
        List<String> list;
        
        list = new ArrayList<String>();
        if (artifacts != null) {
        	for (Artifact a : artifacts) {
        		list.add(a.getFile().getPath());
        	}
        }

        return list;
    }

    /**
     * The Maven project object
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * The plugin dependencies.
     *
     * @parameter expression="${plugin.artifacts}"
     * @required
     * @readonly
     */
    private List pluginClasspath;

    /**
     * This folder is added to the list of those folders
     * containing source to be compiled. Use this if your
     * ant script generates source code.
     *
     * @parameter expression="${sourceRoot}"
     */
    private File sourceRoot;

    /**
     * @see org.apache.maven.plugin.Mojo#execute()
     */
    public void execute() throws MojoExecutionException {
    	executeTasks();

        if (sourceRoot != null) {
            getLog().info( "Registering compile source root " + sourceRoot );
            project.addCompileSourceRoot( sourceRoot.toString() );
        }

    }
}
