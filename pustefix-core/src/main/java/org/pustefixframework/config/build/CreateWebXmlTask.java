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
import org.apache.tools.ant.Task;
import org.pustefixframework.config.customization.CustomizationInfo;
import org.pustefixframework.config.customization.PropertiesBasedCustomizationInfo;

import com.icl.saxon.TransformerFactoryImpl;

import de.schlund.pfixxml.config.BuildTimeProperties;
import de.schlund.pfixxml.config.GlobalConfig;

/**
 * @author mleidig
 */
public class CreateWebXmlTask extends Task {
    private File styleSheet;
    private File commonConfig;
    private File projectConfig;
    private File webappsDir;
    
    @Override
    public void execute() throws BuildException {
        Properties buildTimeProperties = BuildTimeProperties.getProperties();
        CustomizationInfo info = new PropertiesBasedCustomizationInfo(buildTimeProperties);

        File confDir = projectConfig.getParentFile();
        String projectName = confDir.getParentFile().getName();
                
        File destFile = new File(webappsDir, "web.xml");
            
        String configFiles = toPfixrootUri(projectConfig);
        File springConfigFile = new File(confDir, "spring.xml");
        if (springConfigFile.exists()) configFiles+=" "+toPfixrootUri(springConfigFile);
                
        TransformerFactory tf = new TransformerFactoryImpl();
        try {
            StreamSource xsl = new StreamSource(styleSheet);
            StreamSource xml = new StreamSource(projectConfig);
            StreamResult out = new StreamResult(destFile);
            Transformer t = tf.newTransformer(xsl);
            t.setParameter("projectname", projectName);
            t.setParameter("projectfile", projectConfig.getPath());
            t.setParameter("commonprojectsfile", commonConfig.getPath());
            t.setParameter("configfiles", configFiles);
            t.setParameter("customizationinfo", info);
            t.transform(xml, out);
        } catch(Exception x) {
            throw new BuildException(x);
        }
    }
    
    private String toPfixrootUri(File file) {
        return "WEB-INF/pfixroot/" + file.getParentFile().getName() + "/" + file.getName();  
    }
    
    public void setStyleSheet(File styleSheet) {
        this.styleSheet = styleSheet;
    }
    
    public void setCommonConfig(File commonConfig) {
        this.commonConfig = commonConfig;
    }
    
    public void setProjectConfig(File projectConfig) {
        this.projectConfig = projectConfig;
    }
        
    public void setWebappsDir(File webappsDir) {
        this.webappsDir = webappsDir;
    }
}
