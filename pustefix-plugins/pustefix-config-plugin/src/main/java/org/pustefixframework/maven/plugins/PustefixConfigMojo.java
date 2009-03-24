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
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.apache.tools.ant.types.Path;
import org.codehaus.plexus.util.StringUtils;

/**
 * Generates everything still on ant.
 *
 * @goal generate
 * @phase generate-sources
 * @requiresDependencyResolution compile
 */
public class PustefixConfigMojo extends AbstractMojo {
    /**
     * Docroot of the application
     * 
     * @parameter default-value="${project.build.directory}/${project.artifactId}-${project.version}/WEB-INF/pfixroot"
     */
    private String pfixroot;
    
    /**
     * Where to place apt-generated classes.
     * 
     * @parameter default-value="${project.build.directory}/generated-sources/apt"
     */
    private String aptdir;

    /**
     * @parameter default-value="test"
     */
    private String makemode;

    /**
     * @parameter default-value="true"
     */
    private boolean standaloneTomcat;
    
    
    /**
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * @parameter expression="${plugin.artifacts}"
     * @required
     * @readonly
     */
    private List<Artifact> pluginClasspath;

    
    
    /**
     * @see org.apache.maven.plugin.Mojo#execute()
     */
    public void execute() throws MojoExecutionException {
        try {
            Project ant;
            DefaultLogger logger;
            
            getLog().info("init");
            ant = new Project();
            ant.init();
            ProjectHelper.configureProject(ant, new File("/home/mhm/Projects/pustefixframework/pustefix-plugins/pustefix-config-plugin/src/main/resources/build.xml"));
            logger = new DefaultLogger();
            logger.setOutputPrintStream(System.out);
            logger.setErrorPrintStream(System.err);
            logger.setMessageOutputLevel(getLog().isDebugEnabled() ? Project.MSG_DEBUG : Project.MSG_INFO);
            ant.addBuildListener(logger);
            ant.setBaseDir(project.getBasedir());
            ant.setProperty("pfixroot", pfixroot);
            ant.setProperty("aptdir", aptdir);
            ant.setProperty("standalone.tomcat", Boolean.toString(standaloneTomcat));
            ant.setProperty("makemode", makemode);
            ant.setProperty("data.tar.gz", getDataTarGz());
            try {
                ant.addReference("maven.compile.classpath", path(ant, project.getCompileClasspathElements()));
            } catch (DependencyResolutionRequiredException e) {
                throw new IllegalStateException(e);
            }
            ant.addReference("maven.plugin.classpath", path(ant, pathStrings(pluginClasspath)));
            ant.executeTarget("generate");
        } catch (BuildException e) {
            throw new MojoExecutionException("Ant failure: " + e.getMessage(), e);
        }
        project.addCompileSourceRoot(aptdir);
    }

    private String getDataTarGz() {
        for (Artifact artifact : pluginClasspath) {
            if ("data".equals(artifact.getClassifier()) && "pustefix-core".equals(artifact.getArtifactId()) 
                    && "org.pustefixframework".equals(artifact.getGroupId())) {
                return artifact.getFile().getAbsolutePath();
            }
        }
        throw new IllegalStateException(pluginClasspath.toString());
    }
    
    private Path path(Project antProject, List<String> items) {
        Path result;
        
        result = new Path(antProject);
        result.setPath(StringUtils.join(items.iterator(), File.pathSeparator));
        return result;
        
    }
    
    private static List<String> pathStrings(Collection<Artifact> artifacts) {
        List<String> list;
        
        list = new ArrayList<String>();
        if (artifacts != null) {
            for (Artifact a : artifacts) {
                list.add(a.getFile().getPath());
            }
        }

        return list;
    }
}
