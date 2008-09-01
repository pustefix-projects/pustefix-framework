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
package de.schlund.pfixcore.util.basicapp.basics;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import de.schlund.pfixcore.util.basicapp.helper.AppValues;
import de.schlund.pfixcore.util.basicapp.objects.Project;
import de.schlund.pfixcore.util.basicapp.projectdom.ConfigXmlDom;
import de.schlund.pfixcore.util.basicapp.projectdom.DependXmlDom;
import de.schlund.pfixcore.util.basicapp.projectdom.FrameXmlDom;
import de.schlund.pfixcore.util.basicapp.projectdom.PageXmlDom;
import de.schlund.pfixcore.util.basicapp.projectdom.ProjectXmlDom;


/**
 * A class responsible for handling all xml files.
 * This means that editing and also to copy a template
 * will start here.
 * 
 * @author <a href="mailto:rapude@schlund.de">Ralf Rapude</a>
 * @version $Id$
 */
public class HandleXMLFiles {
    private static final Logger LOG = Logger.getLogger(HandleXMLFiles.class);
    /** The project settings */
    private Project project    = null;
    /** Path to the template folder */
    private String pathToTmpl  = null;
    /** A path to the conf folder of the new project */
    private String pathToConf  = null;
    /** The project name */
    private String projectName = null;
    /** The path to the pages folder */
    private String pathToPages = null;
    
    /** 
     * Constructor just initializes an Project Object
     * in order to get the project settings given by 
     * the user in the steps before
     */
    public HandleXMLFiles(Project project) {
        this.project = project;
    }
        
    /** 
     * The "starter method for this class".
     * @param project An object containing all necessary settings
     * for a new project 
     */
    public void runHandleXMLFiles() {
        System.out.println("\nXml files editing starts now...\n");
        projectName        = project.getProjectName();
        // setting basicpaths
        pathToTmpl         = AppValues.BASICPATH + AppValues.TEMPLFOLDERPATH;
        pathToConf         = AppValues.BASICPATH + projectName + AppValues.CONFFOLDER;
        pathToPages        = AppValues.BASICPATH + projectName + AppValues.PATHTO_PAGES;
        Document domDoc    = null; 
        
        // loop through all necessary files needed to create a project
        for (int i = 0; i < AppValues.TEMPLATEARR.length; i++) {
            String tmpDoc  = AppValues.TEMPLATEARR[i];
            String wrtDoc  = null;
            domDoc         = readDom(pathToTmpl + tmpDoc);
            
            // desciding what to do with the current document
            // the config,prop.in
            if (tmpDoc.equals(AppValues.CONFIG_TMPL)) {
            
                String defServletName = "config";
                wrtDoc                   = defServletName + AppValues.CFGFILESUFF;
                ConfigXmlDom configDom   = new ConfigXmlDom(domDoc);
                domDoc                   = configDom.getDom();
                writeXmlFile(domDoc, wrtDoc);
                
            // the depend.xml    
            } else if (tmpDoc.equals(AppValues.DEPEND_TMPL)) {
                wrtDoc                   = AppValues.DEPENDXML;
                DependXmlDom dependDom   = new DependXmlDom(project, domDoc);
                domDoc                   = dependDom.getDom();
                
            // changing the project.xml
            } else if (tmpDoc.equals(AppValues.PROJECT_TMPL)) {
                wrtDoc                   = AppValues.PROJECTXML;
                ProjectXmlDom projectDom = new ProjectXmlDom(project, domDoc);
                domDoc                   = projectDom.getDom();
                
            // changing the frame.xml
            } else if (tmpDoc.equals(AppValues.FRAME_TMPL)) {
                wrtDoc                   = AppValues.FRAMEXML;
                FrameXmlDom frameDom     = new FrameXmlDom(project, domDoc);
                domDoc                   = frameDom.getDom();
                
            // the skin.xml also
            } else if (tmpDoc.equals(AppValues.SKIN_TMPL)) {
                wrtDoc                   = AppValues.SKINXSL;

            // and the metatags.xsl will also appear
            } else if (tmpDoc.equals(AppValues.METATAGS_TMPL)) {
                wrtDoc                   =AppValues.METATAGSXSL;
            }
            
            // the page will appear in myproject/txt/pages
            else if (tmpDoc.equals(AppValues.PAGE_TMPL)) {
                    wrtDoc              = AppValues.PAGEDEFPREFIX + 
                                          AppValues.PAGEDEFAULT + 
                                          AppValues.PAGEDEFSUFFIX;
                    PageXmlDom pageDom = new PageXmlDom(project, domDoc);
                    domDoc             = pageDom.getDom();
                    writeXmlFile(domDoc, wrtDoc);
            }
            
            // writing the dom to a file
            writeXmlFile(domDoc, wrtDoc);
        }
    }  
  

    /**
     * Creating a domtree for the current file in order
     * to edit the content
     * @return A dom
     */
    private Document readDom(String currentDoc) {
        System.out.println("Try to read the dom of the given Document: " + currentDoc);
        
        try {
            DocumentBuilder myBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc              = myBuilder.parse(currentDoc);
            System.out.println("Transforming into dom has been successfull");
            return doc;
        } catch (ParserConfigurationException e) {
            LOG.debug(e.getMessage(), e);
        } catch (IOException e) {
            LOG.debug(e.getMessage(), e);
        } catch (SAXException e) {
            LOG.debug(e.getMessage(), e);
        }
        return null;
    }
    
    
    /**
     * Method for writing the dom into an xml file
     * @param domDoc The current dom to write
     * @param fileName The fileName to write
     */
    private void writeXmlFile(Document domDoc, String fileName) {
        System.out.println("Writing the file: " + fileName);
        
        StringBuffer buffy = new StringBuffer();
        File file          = null;
        
        try {
            // Prepare the DOM document for writing
            Source source = new DOMSource(domDoc);
                
            // preparing the output file for frame.xml
            if (fileName.equals(AppValues.FRAMEXML)) {
                buffy.append(AppValues.BASICPATH);
                buffy.append(projectName);
                buffy.append(AppValues.XMLCONSTANT);
                file = new File(buffy.toString() + fileName);
                buffy.setLength(0);
                
            // preparing the output for skin.xsl
            } else if (fileName.equals(AppValues.SKINXSL)) {
                buffy.append(AppValues.BASICPATH);
                buffy.append(projectName);
                buffy.append(AppValues.XSLFOLDER);
                file = new File(buffy.toString() + fileName);
                buffy.setLength(0);
                
            // and in the case that is a normal config file
              // e.g. The config.prop.in
            } else if (fileName.endsWith(AppValues.CFGFILESUFF)) {
                file = new File(pathToConf + fileName);
                
              // or the depend.xml.in 
            } else if (fileName.equals(AppValues.DEPENDXML)) {
                file = new File(pathToConf + fileName);
                
            } else if (fileName.startsWith(AppValues.PAGEDEFPREFIX)) {
                file = new File(pathToPages + fileName);
              // or maybe also the project.xml.in
            } else if (fileName.equals(AppValues.PROJECTXML)) {
                file = new File(pathToConf + fileName);
            
            // and last but not least the metatags.xsl
            } else if (fileName.equals(AppValues.METATAGSXSL)) {
                buffy.append(AppValues.BASICPATH);
                buffy.append(projectName);
                buffy.append(AppValues.XSLFOLDER);
                file = new File(buffy.toString() + fileName);
                buffy.setLength(0);
            }
            
            
            
            // the output will happen anyway ;o)
            Result result = new StreamResult(file);
    
            // Write the DOM document to the file
            Transformer xformer = TransformerFactory.newInstance().newTransformer();
            xformer.transform(source, result);
            System.out.println("Writing file has been successfull\n");
        } catch (TransformerConfigurationException e) {
            LOG.debug(e.getMessage(), e);
        } catch (TransformerException e) {
            LOG.debug(e.getMessage(), e);
        }
    }
}
