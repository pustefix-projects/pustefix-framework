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
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.pustefixframework.config.customization.CustomizationInfo;
import org.pustefixframework.config.customization.PropertiesBasedCustomizationInfo;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.icl.saxon.TransformerFactoryImpl;

import de.schlund.pfixxml.config.BuildTimeProperties;

/**
 * @author Sebastian Marsching
 */
public class CreateServerConfigurationTask extends MatchingTask {

    public static class TransformationParam {
        private String name;
        private String expression;
        
        public void setName(String name) {
            this.name = name;
        }
        
        public void setExpression(String expression) {
            this.expression = expression;
        }
    }

    private File baseDir;
    private File outFile;
    private File styleSheet;
    private File commonConfig;
    private File docroot;
    private Map<String, String> transformationParams = new HashMap<String, String>();
    
    public void execute() throws BuildException {
        
        Properties buildTimeProperties = BuildTimeProperties.getProperties();
        CustomizationInfo info = new PropertiesBasedCustomizationInfo(buildTimeProperties);
        if(!baseDir.exists()) throw new BuildException("Base directory doesn't exist: "+baseDir.getPath());
      
        DirectoryScanner scanner = getDirectoryScanner(baseDir);
        String[] files = scanner.getIncludedFiles();
        
        DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
        dbfac.setNamespaceAware(true);
        dbfac.setXIncludeAware(true);
        
        DocumentBuilder db;
        try {
            db = dbfac.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new BuildException("Could not create DocumentBuilder!", e);
        }
        Document srcDoc = db.newDocument();
        srcDoc.appendChild(srcDoc.createElement("projects"));
        
        boolean modified = false;
        long refModTime = outFile.lastModified();
        if (commonConfig.lastModified() > refModTime || styleSheet.lastModified() > refModTime) {
            modified = true;
        }
        
        for (String file : files) {
            File srcFile = new File(baseDir, file);
                
            if(srcFile.lastModified()>refModTime) {
                modified = true;
            }
            Document projectConfig;
            try {
                projectConfig = db.parse(srcFile);
            } catch (SAXException e) {
                throw new BuildException("Error while parsing file " + srcFile, e);
            } catch (IOException e) {
                throw new BuildException("Error while reading file " + srcFile, e);
            }
            srcDoc.getDocumentElement().appendChild(srcDoc.importNode(projectConfig.getDocumentElement(), true));
        }
        if (modified) {
            TransformerFactory tf = new TransformerFactoryImpl();
            try {
                StreamSource xsl = new StreamSource(styleSheet);
                DOMSource xml = new DOMSource(srcDoc);
                StreamResult out = new StreamResult(outFile);
                Transformer t = tf.newTransformer(xsl);
                for (String key : transformationParams.keySet()) {
                    t.setParameter(key, transformationParams.get(key));
                }
                t.setParameter("commonprojectsfile", commonConfig.getPath());
                t.setParameter("customizationinfo", info);
                t.setParameter("docroot", docroot.getPath());
                t.transform(xml, out);
            } catch(Exception x) {
                throw new BuildException(x);
            }
        }
    }
    
    public void setBaseDir(File baseDir) {
        this.baseDir = baseDir;
    }
        
    public void setOutFile(File outFile) {
        this.outFile = outFile;
    }
    
    public void setStyleSheet(File styleSheet) {
        this.styleSheet = styleSheet;
    }
    
    public void setCommonConfig(File commonConfig) {
        this.commonConfig = commonConfig;
    }
    
    public void setDocroot(File docroot) {
        this.docroot = docroot;
    }
    
    public void addConfiguredParam(TransformationParam param) {
        this.transformationParams.put(param.name, param.expression);
    }
}
