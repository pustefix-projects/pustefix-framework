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
package de.schlund.pfixcore.util;

import java.io.File;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import de.schlund.pfixxml.util.Xml;

/**
 * @author adam
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class XsltWebXmlTask extends XsltGenericTask {
    
    public void init() throws BuildException {
        super.init();
    }

    protected void doTransformations() {
        StringBuffer tmp = new StringBuffer(100);
        tmp.append("Created web.xml for ");
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
                Document        doc;
                try {
                    doc = Xml.parseMutable(in);
                } catch (Exception e) {
                    throw new BuildException("Could not parse file "+in, e);
                }
                NodeList        nl        = doc.getElementsByTagName("project");
                for (int j = 0; j < nl.getLength(); j++) {
                    Element currproject  = (Element) nl.item(j);
                    String  name = currproject.getAttribute("name");
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
                            tmp.append(", ");
                        }
                        tmp.append(name);
                        created = created + count;
                    }
                }
            }
        } finally {
            if ( created > 0 ) {
                log(tmp.toString(), Project.MSG_INFO);
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
