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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.tools.ant.BuildException;
import org.codehaus.plexus.util.DirectoryScanner;

import de.schlund.pfixxml.config.GlobalConfigurator;
import de.schlund.pfixxml.resources.FileResource;
import de.schlund.pfixxml.resources.ResourceUtil;
import de.schlund.pfixxml.targets.TargetGenerator;

/**
 * Generate IWrapper classes from .iwrp files.
 *
 * @author mleidig@schlund.de
 *
 * @goal generate
 * @phase prepare-package
 */
public class GenerateMojo extends AbstractMojo {
    
    /**
     * @parameter
     * @required
     */
    private File docroot;
    
    /**
     * @parameter
     * @required
     */
    private String[] includes;
    
    
    public void execute() throws MojoExecutionException {

        try {
            GlobalConfigurator.setDocroot(docroot.getPath());
        } catch (IllegalStateException e) {
            // Ignore exception as there is no problem
            // if the docroot has already been configured
        }
            
        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setIncludes(includes);
        scanner.setBasedir(docroot);
        scanner.setCaseSensitive(true);
        scanner.scan();
        String[] files = scanner.getIncludedFiles();
     
        if (files.length > 0) {
            try {
                for (int i = 0; i < files.length; i++) {
                    FileResource confile = ResourceUtil.getFileResourceFromDocroot(files[i]);
                    if (confile.exists() && confile.canRead() && confile.isFile()) {
                        try {
                            TargetGenerator gen = new TargetGenerator(confile);
                            gen.setIsGetModTimeMaybeUpdateSkipped(false);
                            getLog().info("---------- Doing " + files[i] + "...");
                            gen.generateAll();
                            getLog().info("---------- ...done [" + files[i] + "]");

                            TargetGenerator.resetFactories();
                        } catch (Exception e) {
                            throw new BuildException(confile + ": " + e.getMessage(), e);
                        }
                    } else {
                        throw new BuildException("Couldn't read configfile '" + files[i] + "'");
                    }
                }
            } finally {
                getLog().info(TargetGenerator.getReportAsString());
                if(TargetGenerator.errorsReported()) throw new BuildException("TargetGenerator reported errors.");
            }
        } else {
            getLog().warn("Need configfile to work on");
        }
            
    }
        
  
}
