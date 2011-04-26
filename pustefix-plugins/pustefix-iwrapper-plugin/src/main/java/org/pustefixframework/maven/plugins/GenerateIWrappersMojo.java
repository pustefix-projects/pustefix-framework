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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.DirectoryScanner;

/**
 * Generate IWrapper classes from .iwrp files.
 *
 * @author mleidig@schlund.de
 *
 * @goal generate
 * @phase generate-sources
 */
public class GenerateIWrappersMojo extends AbstractMojo {
    
    /**
     * @parameter default-value="target/generated-sources/iwrappers"
     * @required
     */
    private File genDir;
    
    /**
     * @parameter default-value="src/main/java"
     * @required
     */
    private File srcDir;
    
    /**
     * @parameter expression="${project}"
     * @required
     */
    private MavenProject project;
    
    /** @parameter expression="${plugin.artifacts}" */
    private java.util.List<Artifact> pluginArtifacts;

    
    public void execute() throws MojoExecutionException {
        
        if(!srcDir.exists()) return;
        
        String version = getPustefixVersion();
        if(version == null) throw new MojoExecutionException("Can't get Pustefix version from dependencies!");
        
        String lastBuildVersion = null;
        File versionFile = new File(genDir,".pustefix_version");
        if(versionFile.exists()) {
            try {
                lastBuildVersion = readVersionString(versionFile);
            } catch(IOException x) {
                throw new MojoExecutionException("Can't read Pustefix version of last build.",x);
            }
        }
        
        boolean pustefixVersionChanged = !version.equals(lastBuildVersion);
        
        StreamSource xsl = new StreamSource(getClass().getResourceAsStream("/pustefix/xsl/iwrapper.xsl"));
        
        DirectoryScanner ds = new DirectoryScanner();
        ds.setIncludes(new String[] {"**/*.iwrp"});
        ds.setBasedir(srcDir);
        ds.setCaseSensitive(true);
        ds.scan();
        String[] files = ds.getIncludedFiles();
   
        TransformerFactory factory = TransformerFactory.newInstance();
        Templates templates = null;
        try {
            templates = factory.newTemplates(xsl);
        } catch(TransformerConfigurationException x) {
            throw new MojoExecutionException("Can't instantiate IWrapper stylesheet.",x);
        }
        
        int iwrpFileCount=0;
        int iwrpGenCount=0;
        
        for (String file:files) {
            
            iwrpFileCount++;
            File iwrpFile = new File(srcDir, file);
            String targetPath = file.substring(0,file.lastIndexOf('.'))+".java";
            File targetFile = new File(genDir, targetPath);
            if(pustefixVersionChanged || (iwrpFile.lastModified() > targetFile.lastModified())) {
                int ind = file.lastIndexOf(File.separator);               
                if(ind == -1) throw new MojoExecutionException("IWrapper definition file must be inside package: "+file);
                String packageName = file.substring(0,ind);
                packageName = packageName.replace(File.separator,".");
                String className = file.substring(ind+1,file.lastIndexOf('.'));
                
                if(!targetFile.getParentFile().exists()) targetFile.getParentFile().mkdirs();
                
                try {
                    Transformer trf = templates.newTransformer();
                    StreamSource src = new StreamSource(new FileInputStream(iwrpFile));
                    StreamResult res = new StreamResult(new FileOutputStream(targetFile));
                    trf.setParameter("package", packageName);
                    trf.setParameter("classname", className);
                    trf.transform(src, res);
                } catch(TransformerException x) {
                    throw new MojoExecutionException("Error while transforming IWrapper file: "+iwrpFile.getAbsolutePath(),x);
                } catch(IOException x) {
                    throw new MojoExecutionException("IO error while transforming IWrapper file: "+iwrpFile.getAbsolutePath(),x);
                }
                iwrpGenCount++;
            }
        }
        
        if(iwrpGenCount>0) {
            getLog().info("Generated "+iwrpGenCount+" IWrapper class"+(iwrpGenCount>1?"es":""));
        }
        
        if(iwrpFileCount>0) {
            project.addCompileSourceRoot(genDir.getAbsolutePath());
            try {
                saveVersionString(versionFile, version);
            } catch(IOException x) {
                throw new MojoExecutionException("Can't save Pustefix version for this build.",x);
            }
        }
    }
    
    private String getPustefixVersion() {
        for(Artifact artifact:pluginArtifacts) {
            if(artifact.getGroupId().equals("org.pustefixframework") &&
                    artifact.getArtifactId().equals("pustefix-core") &&
                    artifact.getType().equals("jar")) {
                return artifact.getVersion();
            }
        }
        return null;
    }

    private String readVersionString(File file) throws IOException {
        FileInputStream in = new FileInputStream(file);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in,"UTF-8"));
        String line = null;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if(!line.equals("")) break;
        }
        reader.close();
        in.close();
        return line;
    }
    
    private void saveVersionString(File file, String version) throws IOException {
        FileOutputStream out = new FileOutputStream(file);
        OutputStreamWriter writer = new OutputStreamWriter(out, "UTF-8");
        writer.write(version);
        writer.close();
        out.close();
    }
  
}
