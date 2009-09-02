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
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.osgi.DefaultMaven2OsgiConverter;
import org.apache.maven.shared.osgi.Maven2OsgiConverter;
import org.codehaus.plexus.util.DirectoryScanner;
import org.pustefixframework.maven.plugins.GenerateSCodes.Result;

/**
 * Generate StatusCode constant classes from statusmessage files.
 *
 * @author mleidig@schlund.de
 *
 * @goal generate
 * @phase generate-sources
 */
public class PustefixStatuscodeMojo extends AbstractMojo {
    /**
     * @parameter default-value="${project.build.directory}/generated-sources/statuscodes"
     * @required
     */
    private File genDir;
    
    /**
     * @parameter default-value="${basedir}/src/main/resources/PUSTEFIX-INF"
     * @required
     */
    private File resDir;
    
    /**
     * @parameter
     */
    private String module;
    
    /**
     * @parameter
     */
    private String targetPath;
    
    /**
     * If not specified, dyntxt/statuscodeinfo.xml is used.
     * 
     * @parameter
     */
    private String[] includes;
    
    /**
     * @parameter
     */
    private String[] excludes;
    
    /**
     * @parameter expression="${project}"
     * @required
     */
    private MavenProject project;
    
    public void execute() throws MojoExecutionException {
        
	if(!resDir.exists()) return;
  
        DirectoryScanner ds = new DirectoryScanner();
        if(includes!=null) {
            ds.setIncludes(includes);
        } else {
            ds.setIncludes(new String[] { "dyntxt/statuscodeinfo.xml" });
        }
        if(excludes!=null) ds.setExcludes(excludes);
        ds.setBasedir(resDir);
        ds.setCaseSensitive(true);
        ds.scan();
        String[] files = ds.getIncludedFiles();

        List<String> resList = new ArrayList<String>();        
        for (int i = 0; i < files.length; i++) {
            String file = files[i];
            resList.add(file);
        }
        
        if(module == null) {
        	Maven2OsgiConverter converter = new DefaultMaven2OsgiConverter();
        	module = converter.getBundleSymbolicName(project.getArtifact());
        }
        
        try {
            Result result = GenerateSCodes.generateFromInfo(resList, resDir.getAbsolutePath(), genDir, module, targetPath);
            if(result.generatedClasses.size()>0) {
                getLog().info("Generated "+result.generatedClasses.size()+" statuscode class"+
                        (result.generatedClasses.size()>1?"es":""));
            }
            if(result.allClasses.size()>0) project.addCompileSourceRoot(genDir.getAbsolutePath());
            
        } catch(Exception x) {
            throw new MojoExecutionException("Can't generate StatusCode constant classes",x);
        }

    }
}
