/*
 * Created on Wed Jun 23 13:13:48 CEST 2004 @509 /Internet Time/
 *
 */
package de.schlund.pfixcore.util;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author <a href="mailto:benjamin@schlund.de">Benjamin Reitzammer</a>
 * @version $Id$
 */
public class XsltCustomWebXmlTask extends XsltGenericTask {

    protected DocumentBuilderFactory dbFactory;

    /** contains the location of projects.xml file, relative to srcdirResolved
     */
    protected String projects;

    /** contains the location of core web.xml template, which is used, when a
     * project has no custom web.xml specified, relative to srcdirResolved
     */
    protected String corewebxml;

    public void init() throws BuildException {
        super.init();

        this.dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setNamespaceAware(true);
        dbFactory.setValidating(false);
    }

    protected void doTransformations() {
        StringBuffer tmp = new StringBuffer(100);
        tmp.append("Created web.xml for ");
        int created = 0;
        try {
            if ( infile != null || outfile != null ) {
                throw new BuildException("don't use "+ATTR_INFILE+"/"+ATTR_OUTFILE+", use "+ATTR_SRCDIR+"/"+ATTR_DESTDIR+" etc. instead");
            }
            if ( projects == null || "".equals(projects) || corewebxml == null || "".equals(corewebxml) ) {
                throw new BuildException("Attributes 'projects' and 'corewebxml' are not allowed to be null or empty");
            }

            File coreWebXml = new File(srcdirResolved, corewebxml);
            if ( !coreWebXml.exists() || !coreWebXml.canRead() ) {
                throw new BuildException("File '"+corewebxml+"' can't be found or can't be read");
            }

            DocumentBuilder domp;
            Document        doc;
            File projectsFile = new File(srcdirResolved, projects);
            try {
                log("Parsing "+projectsFile, Project.MSG_DEBUG);
                domp = dbFactory.newDocumentBuilder();
                doc = domp.parse(projectsFile);
            } catch (Exception e) {
                throw new BuildException("Could not parse file "+projectsFile, e);
            }

            int count;
            NodeList nl = doc.getElementsByTagName("project");
            for (int i = 0; i < nl.getLength(); i++) {
                Element currproject  = (Element) nl.item(i);
                String  name = currproject.getAttribute("name");
                log("found project "+name+" in "+projectsFile, Project.MSG_DEBUG);

                File cusWebXml = new File(srcdirResolved+"/"+name+"/conf", "web.xml");
                if ( cusWebXml.exists() && cusWebXml.canRead() ) {
                    in = cusWebXml;
                } else {
                    in = coreWebXml;
                }

                log("Using "+in+" as sourcefile for web.xml transformation", Project.MSG_DEBUG);
                getTransformer().setParameter(new XsltParam("prjname", name));
                getTransformer().setParameter(new XsltParam("projectsxmlfile", projectsFile.getPath()));
                String outdir = "webapps/" + name + "/WEB-INF";
                File webinfdir = new File(getDestdir(), outdir);
                webinfdir.mkdirs();
                outname = "web.xml";
                out = new File(webinfdir,outname);
                count = doTransformationMaybe();
                if ( count > 0 ) {
                    if ( created > 0 ) {
                        tmp.append(", ");
                    }
                    tmp.append(name);
                    created = created + count;
                }
            }
        } finally {
            if ( created > 0 ) {
                log(tmp.toString(), Project.MSG_INFO);
            }
            scanner = null;
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

    public String getProjects() {
      return projects;
    }

    public void setProjects(String newProjects) {
      this.projects = newProjects;
    }

    public String getCorewebxml() {
      return corewebxml;
    }

    public void setCorewebxml(String newCorewebxml) {
      this.corewebxml = newCorewebxml;
    }


}
