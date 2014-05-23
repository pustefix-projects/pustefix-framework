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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

/**
 * Make XSL transformation (supports referencing stylesheet 
 * from the project classpath using the 'classpath:' URL scheme)
 *
 * @author mleidig@schlund.de
 *
 * @goal transform
 * @phase generate-resources
 * @requiresDependencyResolution compile
 */
public class XSLTMojo extends AbstractMojo {
    
    /**
     * @parameter
     * @required
     */
    private File in;
    
    /**
     * @parameter
     * @required
     */
    private File out;
    
    /**
     * @parameter
     * @required
     */
    private String style;
    
    /**
     * @parameter
     */
    private Map<String,String> params;
    
    /**
     * @parameter expression="${project}"
     * @required
     */
    private MavenProject mavenProject;
    
 
    public void execute() throws MojoExecutionException {
        
        if(in.exists()) {
        
            if(getLog().isDebugEnabled()) {
                getLog().debug("Transforming file: " + in.getPath());
            }
            
            InputStream styleIn;
            try {
                if(style.startsWith("classpath:")) {
                    style = style.substring(10);
                    URLClassLoader loader = getProjectClassLoader();
                    URL url = loader.getResource(style);
                    styleIn = url.openStream();
                } else {
                    File file = new File(style);
                    styleIn = new FileInputStream(file);
                }
            } catch(IOException x) {
                throw new MojoExecutionException("Error reading stylesheet '" + style + "'.", x);
            }
            
            try {
                TransformerFactory tf = TransformerFactory.newInstance();
                StreamSource styleSrc = new StreamSource(styleIn);
                Transformer t = tf.newTransformer(styleSrc);
                Iterator<String> names = params.keySet().iterator();
                while(names.hasNext()) {
                    String name = names.next();
                    String value = params.get(name);
                    t.setParameter(name, value);
                }
                StreamSource inSrc = new StreamSource(in);
                if(!out.getParentFile().exists()) out.getParentFile().mkdirs();
                FileOutputStream outStream = new FileOutputStream(out);
                StreamResult outRes = new StreamResult(outStream);
                t.transform(inSrc, outRes);
                outStream.close();
            } catch(Exception x) {
                throw new MojoExecutionException("Error transforming '" + in.getPath() + "'.", x);
            }
            
        } else {
            if(getLog().isDebugEnabled()) {
                getLog().debug("Do nothing because input file '" + in.getPath() + "' doesn't exist.");
            }
        }
   
    }
    
    private URLClassLoader getProjectClassLoader() throws MojoExecutionException {
        try {
            List<?> elements = mavenProject.getCompileClasspathElements();
            URL[] urls = new URL[elements.size()];
            for (int i = 0; i < elements.size(); i++) {
                String element = (String) elements.get(i);
                urls[i] = new File(element).toURI().toURL();
            }
            return new URLClassLoader(urls);
        } catch (Exception x) {
            throw new MojoExecutionException("Can't create project classloader", x);
        }
    }
 
}
