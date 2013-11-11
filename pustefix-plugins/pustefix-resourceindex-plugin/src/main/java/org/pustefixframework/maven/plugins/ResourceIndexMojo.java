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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

/**
 * Generate Pustefix resource index.
 *
 * @goal generate
 * @phase prepare-package
 */
public class ResourceIndexMojo extends AbstractMojo {

    /**
     * @parameter default-value="${basedir}"
     * @required
     */
    private File baseDir;

    /**
     * @parameter default-value="${project.build.outputDirectory}"
     * @required
     */
    private File buildOutputDir;

    /**
     * @parameter expression="${project}"
     * @required
     */
    private MavenProject project;

    public void execute() throws MojoExecutionException {

        File resourceDir = null;
        if("jar".equalsIgnoreCase(project.getPackaging())) {
            resourceDir = new File(baseDir, "src/main/resources");
        } else if("war".equalsIgnoreCase(project.getPackaging())) {
            resourceDir = new File(baseDir, "src/main/webapp");
        }

        if(resourceDir != null && resourceDir.exists()) {
            File indexDir = new File(buildOutputDir, "META-INF");
            if(!indexDir.exists()) {
                indexDir.mkdir();
            }
            File indexFile = new File(indexDir, "pustefix-resource.index");
            try {
                Writer writer = new OutputStreamWriter(new FileOutputStream(indexFile), "UTF-8");
                createIndex(resourceDir, resourceDir.getCanonicalPath(), writer);
                writer.close();
            } catch(IOException x) {
                throw new MojoExecutionException("Error writing resource index", x);
            }
        }
    }

    private static void createIndex(File file, String rootPath, Writer writer) throws IOException {
        if(!file.isHidden()) {
        	String path = file.getCanonicalPath();
        	if(path.length() > rootPath.length()) {
	        	String relPath = path.substring(rootPath.length() + 1);
	        	if(file.isDirectory()) {
	        		relPath += "/";
	        	}
	        	writer.write(relPath);
	        	writer.write('\n');
        	}
        	if(file.isDirectory()) {
                File[] subFiles = file.listFiles();
                for(File subFile: subFiles) {
                    createIndex(subFile, rootPath, writer);
                }
            }
        }
    }

}
