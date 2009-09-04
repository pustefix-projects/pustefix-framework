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

package org.pustefixframework.maven.plugins.iwrapperbean;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

/**
 * Creates IWrappers from annotated beans.
 *
 * @goal generate
 * @phase generate-sources
 * @requiresDependencyResolution compile
 */
public class IWrapperBeanMojo extends AbstractMojo {
  
    /**
     * Where to place apt-generated classes.
     * 
     * @parameter default-value="${project.build.directory}/generated-sources/apt"
     */
    private File aptdir;

    /** @parameter expression="${project}" */
    private MavenProject mavenProject;
    
    /**
     * @parameter expression="${plugin.artifacts}"
     */
    private List<Artifact> pluginClasspath;
    
    /**
     * @see org.apache.maven.plugin.Mojo#execute()
     */
    public void execute() throws MojoExecutionException {
    	
        File basedir = mavenProject.getBasedir();
        if (new Apt(basedir, aptdir, getLog()).execute(getPluginClasspath()) > 0) {
            mavenProject.addCompileSourceRoot(aptdir.getAbsolutePath());
        }
        
    }
    
    private String getPluginClasspath() {
        StringBuilder result;
        
        result = new StringBuilder();
        for (String path : pathStrings(pluginClasspath)) {
            if (result.length() > 0) {
                result.append(':');
            } 
            result.append(path);
        }
        return result.toString();
    }
    
    private static List<String> pathStrings(Collection<Artifact> artifacts) {
        List<String> lst;
        
        lst = new ArrayList<String>();
        if (artifacts != null) {
            for (Artifact a : artifacts) {
                lst.add(a.getFile().getPath());
            }
        }

        return lst;
    }

}
