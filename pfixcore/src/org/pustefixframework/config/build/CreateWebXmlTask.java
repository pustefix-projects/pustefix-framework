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
package org.pustefixframework.config.build;

import java.io.File;
import java.util.Properties;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.pustefixframework.config.customization.CustomizationInfo;
import org.pustefixframework.config.customization.PropertiesBasedCustomizationInfo;

import com.icl.saxon.TransformerFactoryImpl;

import de.schlund.pfixxml.config.BuildTimeProperties;

/**
 * @author mleidig
 */
public class CreateWebXmlTask extends MatchingTask {

    private File baseDir;
    private File webappsDir;
    private File webappTemplate;
    private File styleSheet;
    private File commonConfig;
    
    public void execute() throws BuildException {
        
        Properties buildTimeProperties = BuildTimeProperties.getProperties();
        CustomizationInfo info = new PropertiesBasedCustomizationInfo(buildTimeProperties);
        if(!baseDir.exists()) throw new BuildException("Base directory doesn't exist: "+baseDir.getPath());
      
        int totalCnt=0;
        int genCnt=0;
        
        DirectoryScanner scanner = getDirectoryScanner(baseDir);
        String[] files = scanner.getIncludedFiles();
        for (String file : files) {
            totalCnt++;
            File srcFile = new File(baseDir, file);
            File confDir = srcFile.getParentFile();
            String projectName = confDir.getParentFile().getName();
                
            File customTemplate = new File(confDir,"web.xml");
            if(customTemplate.exists()) webappTemplate = customTemplate;
                
            File destFile = new File(webappsDir, projectName+"/"+"WEB-INF/web.xml");
            
            long refModTime = destFile.lastModified();
                
            //TODO: Modification check doesn't honour creation/removal of
            //spring.xml or project specific web.xml template
            if(srcFile.lastModified()>refModTime || commonConfig.lastModified()>refModTime ||
                   webappTemplate.lastModified()>refModTime || styleSheet.lastModified()>refModTime) {
         
                String configFiles = srcFile.toURI().toString();
                File springConfigFile = new File(confDir,"spring.xml");
                if(springConfigFile.exists()) configFiles+=" "+springConfigFile.toURI().toString();
                
                TransformerFactory tf = new TransformerFactoryImpl();
                try {
                    StreamSource xsl = new StreamSource(styleSheet);
                    StreamSource xml = new StreamSource(webappTemplate);
                    StreamResult out = new StreamResult(destFile);
                    Transformer t = tf.newTransformer(xsl);
                    t.setParameter("projectname", projectName);
                    t.setParameter("projectfile", srcFile.getPath());
                    t.setParameter("commonprojectsfile", commonConfig.getPath());
                    t.setParameter("configfiles", configFiles);
                    t.setParameter("customizationinfo", info);
                    t.transform(xml, out);
                } catch(Exception x) {
                    throw new BuildException(x);
                }
                genCnt++;
            }
        }
        if(genCnt > 0) {
            log("Created "+genCnt+" (of "+totalCnt+") web.xml files.",Project.MSG_INFO);
        }
    }
    
    public void setBaseDir(File baseDir) {
        this.baseDir = baseDir;
    }
        
    public void setWebappsDir(File webappsDir) {
        this.webappsDir = webappsDir;
    }
    
    public void setWebappTemplate(File webappTemplate) {
        this.webappTemplate = webappTemplate;
    }
    
    public void setStyleSheet(File styleSheet) {
        this.styleSheet = styleSheet;
    }
    
    public void setCommonConfig(File commonConfig) {
        this.commonConfig = commonConfig;
    }
    
}
