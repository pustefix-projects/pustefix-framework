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
package de.schlund.pfixxml;

import java.io.File;

import org.apache.log4j.Category;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;


/**
 *  IncludeDocumentExtension.java
 * 
 * 
 *  Created: ?
 * 
 *  @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 *
 *  This class is responsible to return the requested parts of an {@link IncludeDocument}. 
 *  It provides a static method which is called from XSL via the extension 
 *  mechanism. It gets the requested IncludeDocument from {@link IncludeDocumentFactory}
 *  and processes it via the XPathAPI.  
 */
public class IncludeDocumentExtension {
    private static Category     CAT      = Category.getInstance(IncludeDocumentExtension.class.getName());
    private static final String DEFAULT  = "default";
    private static final String NOTARGET = "__NONE__";

    public static NodeList get(String path, String part, String product, String docroot, 
                               String targetgen, String targetkey, String parent_path, 
                               String parent_part, String parent_product) throws Exception {
        NodeList        nl      = null;
        boolean         dolog   = ! targetkey.equals(NOTARGET);
        int             length  = 0;
        File            incfile = new File(path);
        IncludeDocument iDoc    = null;
        Document        doc;
        if (! incfile.exists()) {
            if (dolog) {
                DependencyTracker.log("text", path, part, DEFAULT, parent_path, parent_part, 
                                      parent_product, targetgen, targetkey);
            }
            return null;
        }
        // here we want an immutable DOM, currently saxons tinytree
        iDoc   = IncludeDocumentFactory.getInstance().getIncludeDocument(path, false);
        doc    = iDoc.getDocument();
        nl     = XPathAPI.selectNodeList(doc, "/include_parts/part[@name='" + part + "']");
        length = nl.getLength();
        if (length == 0) {
            CAT.debug("*** Part '" + part + "' is 0 times defined.");
            if (dolog) {
                DependencyTracker.log("text", path, part, DEFAULT, parent_path, parent_part, 
                                      parent_product, targetgen, targetkey);
            }
            return null;
        } else if (nl.getLength() > 1) {
            if (dolog) {
                DependencyTracker.log("text", path, part, DEFAULT, parent_path, parent_part, 
                                      parent_product, targetgen, targetkey);
            }
            throw new XMLException("*** Part '" + part
                                   + "' is multiple times defined! Must be exactly 1");
        }
        // OK, we have found the part.
        nl     = XPathAPI.selectNodeList(doc, 
                                         "/include_parts/part[@name = '" + part + "']"
                                         + "/product[@name = '" + product + "']");
        length = nl.getLength();
        if (length == 0) {
            // Didn't find the specific product, trying default:
            nl = XPathAPI.selectNodeList(doc, 
                                         "/include_parts/part[@name = '" + part + "']"
                                         + "/product[@name = '" + DEFAULT + "']");
            int len = nl.getLength();
            if (len == 1 | len == 0) {
                String retval = "0";
                if (dolog) {
                    retval = DependencyTracker.log("text", path, part, DEFAULT, parent_path, 
                                                   parent_part, parent_product, targetgen, 
                                                   targetkey);
                }
                if (len == 0) {
                    CAT.warn("*** Product '" + product + "' is not accessible under part '" + part
                             + "@" + path + "', and a default product is not defined either.");
                    return null;
                } else {
                    if (retval.equals("0")) {
                        return nl;
                    } else {
                        return null;
                    }
                }
            } else {
                throw new XMLException("*** Part '" + part
                                       + "' has multiple default product branches! Must be 1.");
            }
        } else if (length == 1) {
            String retval = "0";
            if (dolog) {
                retval = DependencyTracker.log("text", path, part, product, parent_path, 
                                               parent_part, parent_product, targetgen, targetkey);
            }
            if (retval.equals("0")) {
                return nl;
            } else {
                return null;
            }
        } else {
            if (dolog) {
                DependencyTracker.log("text", path, part, product, parent_path, parent_part, 
                                      parent_product, targetgen, targetkey);
            }
            throw new XMLException("*** Product '" + product
                                   + "' is defined multiple times under part '" + part + "@" + path
                                   + "'");
        }
    }
}