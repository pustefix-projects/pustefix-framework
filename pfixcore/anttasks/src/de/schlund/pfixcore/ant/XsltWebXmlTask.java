/*
 * Created on Oct 1, 2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.schlund.pfixcore.ant;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * @author adam
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class XsltWebXmlTask extends XsltGenericTask {
    
    protected DocumentBuilderFactory dbFactory;
    
    public void init() throws BuildException {
        super.init();
//        System.getProperties().put("javax.xml.parsers.DocumentBuilderFactory", "org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");
//        System.getProperties().put("javax.xml.parsers.SAXParserFactory", "org.apache.xerces.jaxp.SAXParserFactoryImpl");
//        System.getProperties().put("javax.xml.transform.TransformerFactory", "com.icl.saxon.TransformerFactoryImpl");

        this.dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setNamespaceAware(true);
        dbFactory.setValidating(false);
    }

    protected void doTransformations() {
        StringBuffer sb = new StringBuffer(100);
        sb.append("Created web.xml for ");
        int created = 0;
        try {
            if ( infile != null || outfile != null ) {
                throw new BuildException("don't use "+ATTR_INFILE+"/"+ATTR_OUTFILE+", use "+ATTR_SRCDIR+"/"+ATTR_DESTDIR+" etc. instead");
            }
            
            
            scanner = getDirectoryScanner(srcdirResolved);
            infilenames = scanner.getIncludedFiles();
            int count;

            // There should be only one in: projects.xml
            for (int i = 0; i < infilenames.length; i++) {

                inname = infilenames[i];
                in = new File(srcdirResolved, inname);

                DocumentBuilder domp;
                Document        doc;
                try {
                    domp = dbFactory.newDocumentBuilder();
                    doc = domp.parse(in);
                } catch (Exception e) {
                    throw new BuildException("Could not parse file "+in, e);
                }
                NodeList        nl        = doc.getElementsByTagName("project");
                for (int j = 0; j < nl.getLength(); j++) {
                    Element project  = (Element) nl.item(j);
                    String  name = project.getAttribute("name");
                    log("found project "+name+" in "+in, Project.MSG_DEBUG);
                    getTransformer().setParameter(new XsltParam("prjname", name));
                    String outdir = "webapps/" + name + "/WEB-INF";
                    File webinfdir = new File(getDestdir(), outdir);
                    webinfdir.mkdirs();
                    outname = "web.xml";
                    out = new File(webinfdir,outname);
                    count = doTransformationMaybe();
                    if ( count > 0 ) {
                        if ( created > 0 ) {
                            sb.append(", ");
                        }
                        sb.append(name);
                        created = created + count;
                    }
                }
            }
        } finally {
            if ( created > 0 ) {
                log(sb.toString(), Project.MSG_INFO);
            }
            scanner = null;
            infilenames = null; /* input filenames relative to srcdirResolved */
            inname = null; /* filename relative to srcdirResolved */
            infilenameNoExt = null; /* filename relative to srcdirResolved without extension */
            outname = null; /* filename relative to srcdirResolved */
            in = null;
            out = null;
            inLastModified = 0;
            outLastModified = 0;
            styleLastModified = 0;
        }
    }

}
