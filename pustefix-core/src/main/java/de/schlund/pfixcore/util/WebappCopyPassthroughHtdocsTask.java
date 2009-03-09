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
 */

package de.schlund.pfixcore.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.pustefixframework.config.Constants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import de.schlund.pfixxml.config.CustomizationHandler;
import de.schlund.pfixxml.config.GlobalConfig;
import de.schlund.pfixxml.util.TransformerHandlerAdapter;

/**
 * Ant task copying the paths specified using the passthrough option
 * in a projects.xml file from a source to a destination directory.  
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class WebappCopyPassthroughHtdocsTask extends Task {
    private File srcdir;

    private File destdir;

    private File projectsxml;
    
    private File projectxml;

    private String projectname;

    private Document projectsDoc;

    private Document projectDoc;

    public void setSrcdir(File srcdir) {
        this.srcdir = srcdir;
    }

    public void setDestdir(File destdir) {
        this.destdir = destdir;
    }

    public void setProjectsxml(File projectsxml) {
        this.projectsxml = projectsxml;
    }
    
    public void setProjectxml(File projectxml) {
        this.projectxml = projectxml;
    }

    public void setProjectname(String projectname) {
        this.projectname = projectname;
    }

    @Override
    public void execute() throws BuildException {
        checkParameters();
        readFiles();

        String htdocspath = getHtdocs();
        File htdocs = new File(htdocspath);
        if (htdocs.exists() && htdocs.isDirectory()) {
            // Change srcdir temporarily
            File temp = srcdir;
            srcdir = htdocs;
            copyDirectory("/");
            srcdir = temp;
        }

        Set<String> paths = getPassthroughPaths();

        for (String path : paths) {
            System.out.println("Processing passthrough " + path);
            File pathelement = new File(srcdir, path);
            if (pathelement.exists() && !pathelement.isHidden()) {
                if (pathelement.isFile()) {
                    copyFile(path);
                } else if (pathelement.isDirectory()) {
                    copyDirectory(path);
                } else {
                    System.out.println("Ignoring " + path);
                }
            }
        }
    }
    
    private void readFiles() throws BuildException {
        this.projectsDoc = readCustomizedDocument(projectsxml);
        this.projectDoc = readCustomizedDocument(projectxml);
    }
    
    private void checkParameters() throws BuildException {
        if (projectsxml == null) {
            throw new BuildException(
                    "Mandatory attribute \"projectsxml\" is not set!");
        }
        if (!projectsxml.exists() || !projectsxml.isFile()) {
            throw new BuildException("File " + projectsxml + " does not exist!");
        }
        if (projectxml == null) {
            throw new BuildException(
                    "Mandatory attribute \"projectxml\" is not set!");
        }
        if (!projectxml.exists() || !projectxml.isFile()) {
            throw new BuildException("File " + projectxml + " does not exist!");
        }
        if (srcdir == null) {
            throw new BuildException(
                    "Mandatory attribute \"srcdir\" is not set!");
        }
        if (!srcdir.exists() || !srcdir.isDirectory()) {
            throw new BuildException("Directory " + srcdir + " does not exist!");
        }
        if (destdir == null) {
            throw new BuildException(
                    "Mandatory attribute \"destdir\" is not set!");
        }
        if (!destdir.exists() || !destdir.isDirectory()) {
            throw new BuildException("Directory " + destdir
                    + " does not exist!");
        }
        if (projectname == null || projectname.equals("")) {
            throw new BuildException("Attribute \"projectname\" has to be set!");
        }
    }
    
    private Document readCustomizedDocument(File file) throws BuildException {
        XMLReader xreader;
        DOMResult result = new DOMResult();
        try {
            xreader = XMLReaderFactory.createXMLReader();
        } catch (SAXException e) {
            throw new RuntimeException("Could not create XMLReader", e);
        }
        TransformerFactory tf = TransformerFactory.newInstance();
        if (tf.getFeature(SAXTransformerFactory.FEATURE)) {
            SAXTransformerFactory stf = (SAXTransformerFactory) tf;
            TransformerHandler th;
            try {
                th = stf.newTransformerHandler();
            } catch (TransformerConfigurationException e) {
                throw new RuntimeException("Failed to configure TransformerFactory!", e);
            }

            th.setResult(result);
            DefaultHandler dh = new TransformerHandlerAdapter(th);
            DefaultHandler cushandler = new CustomizationHandler(dh, Constants.NS_PROJECT);
            xreader.setContentHandler(cushandler);
            xreader.setDTDHandler(cushandler);
            xreader.setErrorHandler(cushandler);
            xreader.setEntityResolver(cushandler);

            try {
                xreader.parse(new InputSource(new FileInputStream(file)));
            } catch (FileNotFoundException e) {
                throw new BuildException(e);
            } catch (IOException e) {
                throw new BuildException(e);
            } catch (SAXException e) {
                throw new BuildException(e);
            }
        } else {
            throw new BuildException("Could not get instance of SAXTransformerFactory!");
        }
        Node node = result.getNode();
        if (node.getNodeType() == Node.DOCUMENT_NODE) {
            return (Document) node;
        } else {
            return node.getOwnerDocument();
        }
    }
    
    private Set<String> getPassthroughPaths() throws BuildException {
        HashSet<String> paths = new HashSet<String>();
        paths.addAll(getPassthroughPathsFromDocument(projectsDoc));
        paths.addAll(getPassthroughPathsFromDocument(projectDoc));
        return paths;
    }
    
    private Set<String> getPassthroughPathsFromDocument(Document doc) throws BuildException {
        HashSet<String> paths = new HashSet<String>();
        
        NodeList nlist;
        Element root;
        Element application;
        Element eStatic;
        
        doc = projectsDoc;
        root = doc.getDocumentElement();
        nlist = root.getElementsByTagNameNS(Constants.NS_PROJECT, "application");
        if (nlist.getLength() != 1) {
            throw new BuildException("There is not exactly one <application> tag");
        }
        application = (Element) nlist.item(0);
        nlist = application.getElementsByTagNameNS(Constants.NS_PROJECT, "static");
        if (nlist.getLength() > 1) {
            throw new BuildException("There is not exactly one <application> tag");
        } else if (nlist.getLength() == 0) {
            return paths;
        }
        eStatic = (Element) nlist.item(0);
        nlist = eStatic.getElementsByTagNameNS(Constants.NS_PROJECT, "path");
        
        for (int i = 0; i < nlist.getLength(); i++) {
            Element path = (Element) nlist.item(i);
            String text = path.getTextContent().trim();
            if (text.length() > 0) {
                paths.add(text);
            }
        }

        return paths;
    }

    private String getHtdocs() throws BuildException {
        NodeList nlist;
        Element root;
        Element application;
        Element docrootPath;
        
        Document doc = projectDoc;
        root = doc.getDocumentElement();
        nlist = root.getElementsByTagNameNS(Constants.NS_PROJECT, "application");
        if (nlist.getLength() != 1) {
            throw new BuildException("There is not exactly one <application> tag");
        }
        application = (Element) nlist.item(0);
        nlist = application.getElementsByTagNameNS(Constants.NS_PROJECT, "docroot-path");
        if (nlist.getLength() > 1) {
            throw new BuildException("There is not exactly one <docroot-path> tag");
        } else if (nlist.getLength() == 0) {
            return null;
        }
        docrootPath = (Element) nlist.item(0);
        String text = docrootPath.getTextContent().trim();
        if (text.length() == 0) {
            return null;
        } else {
            if (text.startsWith("pfixroot:")) {
                text = text.substring("pfixroot:".length());
                return GlobalConfig.getDocroot() + text;
            } else if (text.startsWith("file:")) {
                return text.substring("file:".length());
            } else {
                return text;
            }
        }
        
    }

    private void copyFile(String path) {
        try {
            File srcfile = new File(srcdir, path);
            File destfile = new File(destdir, path);

            File parent = destfile.getParentFile();
            if (!parent.exists()) {
                parent.mkdirs();
            }

            FileInputStream fis = new FileInputStream(srcfile);
            FileOutputStream fos = new FileOutputStream(destfile);

            int bytes_read = 0;
            byte buffer[] = new byte[512];
            while ((bytes_read = fis.read(buffer)) != -1) {
                fos.write(buffer, 0, bytes_read);
            }

            fis.close();
            fos.close();

        } catch (IOException e) {
            throw new BuildException("Error while copying file " + path);
        }
    }

    private void copyDirectory(String path) {
        File dir = new File(srcdir, path);

        String subpaths[] = dir.list();
        for (String subpath : subpaths) {
            String completepath = path + File.separator + subpath;
            File subfile = new File(srcdir, completepath);

            if (subfile.exists() && !subfile.isHidden()) {
                if (subfile.isFile()) {
                    copyFile(completepath);
                } else if (subfile.isDirectory()) {
                    copyDirectory(completepath);
                } else {
                    System.out.println("Ignoring " + completepath);
                }
            }
        }
    }
}