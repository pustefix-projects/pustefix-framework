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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.tools.ant.BuildException;

import de.schlund.pfixxml.config.GlobalConfigurator;
import de.schlund.pfixxml.resources.FileResource;
import de.schlund.pfixxml.resources.ResourceUtil;
import de.schlund.pfixxml.targets.TargetGenerator;

/**
 * Generate all XSL targets with TargetGenerator
 *
 * @author mleidig@schlund.de
 *
 * @goal generate
 * @phase prepare-package
 */
public class GenerateMojo extends AbstractMojo {
    
    /**
     * @parameter default-value="${basedir}/src/main/webapp"
     * @required
     */
    private File docroot;
    
    /**
     * @parameter default-value="${basedir}/src/main/webapp/WEB-INF/depend.xml"
     * @required
     */
    private File config;
    
    
    public void execute() throws MojoExecutionException {

        try {
            GlobalConfigurator.setDocroot(docroot.getPath());
        } catch (IllegalStateException e) {
            // Ignore exception as there is no problem
            // if the docroot has already been configured
        }
        
        FileResource confile = ResourceUtil.getFileResource(config.toURI()); 
        if(confile.exists() && confile.canRead() && confile.isFile()) {
            try {
                TargetGenerator gen = new TargetGenerator(confile);
                gen.setIsGetModTimeMaybeUpdateSkipped(false);
                gen.generateAll();
                TargetGenerator.resetFactories();
            } catch (Exception e) {
                throw new BuildException(confile + ": " + e.getMessage(), e);
            } finally {
                getLog().info(TargetGenerator.getReportAsString());
                if(TargetGenerator.errorsReported()) throw new MojoExecutionException("TargetGenerator reported errors.");
            }
        } else {
            throw new BuildException("Can't read TargetGenerator configuration '" +  confile + "'");
        }    
                
    }
        
  
}
