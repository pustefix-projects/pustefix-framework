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

import com.icl.saxon.expr.EmptyNodeSet;
import com.icl.saxon.expr.NodeSetValue;
import com.icl.saxon.expr.XPathException;

import de.schlund.pfixxml.targets.TraxXSLTProcessor;
import de.schlund.pfixxml.xpath.PFXPathEvaluator;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Category;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXParseException;


/**
 *  IncludeDocumentExtension.java
 * 
 * 
 *  @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 *  @author <a href="mailto:haecker@schlund.de">Joerg Haecker</a> 
 * 
 *  This class is responsible to return the requested parts of an {@link IncludeDocument}. 
 *  It provides a static method which is called from XSL via the extension 
 *  mechanism (and from nowhere else!). 
 */
public final class IncludeDocumentExtension {

    //~ Instance/static variables ..................................................................

    private static Category     CAT      = Category.getInstance(IncludeDocumentExtension.class.getName());
    private static final String DEFAULT  = "default";
    private static final String NOTARGET = "__NONE__";
    private static final String XPPARTNAME = "/include_parts/part[@name='";
    private static final String XPPRODNAME = "/product[@name = '";
    private static final String XPNAMEEND  = "']";
    
    //~ Methods ....................................................................................

    /**
     * Get the requested IncludeDocument from {@link IncludeDocumentFactory} and retrieve
     * desired information from it.</br>
     * Note: The nested document in the Includedocument is immutable, any attempts to modify it
     * will cause an exception.
     * @param path the path to the Includedocument in the file system relative to docroot.
     * @param part the part in the Includedocument.
     * @param docroot the document root in the file system
     * @param targetgen
     * @param targetkey
     * @param parent_path
     * @param parent_part
     * @param parent_product
     * @return a list of nodes understood by the current transformer(currently saxon)
     * @throws Exception on all errors
     */
    public static final NodeSetValue get(String path, String part, String product, String docroot, 
                                         String targetgen, String targetkey, String parent_path, 
                                         String parent_part, String parent_product) throws Exception {
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
            return new EmptyNodeSet();
        }
        // get the includedocument
        try {
            iDoc = IncludeDocumentFactory.getInstance().getIncludeDocument(path, false);
        } catch(Exception e) {
            if(dolog)
                DependencyTracker.log("text", path, part, product, parent_path, parent_part, parent_product, targetgen, targetkey);
            return handleError(part, e);
        }
        doc = iDoc.getDocument();
        
        // create a new buffer for xpath expressions
        StringBuffer sb = new StringBuffer(100);
        
        // Get the part
        sb.append(XPPARTNAME).append(part).append(XPNAMEEND);
        
        NodeSetValue ns;
        try {
            ns = PFXPathEvaluator.evaluateAsNodeSetValue(sb.toString(), doc);
        } catch (Exception e) {
            if(dolog)
                DependencyTracker.log("text", path, part, product, parent_path, parent_part, parent_product, targetgen, targetkey);
            throw e;
        }
        
        try {
            length = ns.getCount();
        } catch (XPathException e) {
            if(dolog)
                DependencyTracker.log("text", path, part, product, parent_path, parent_part, parent_product, targetgen, targetkey);
            throw e;
        }
        if (length == 0) {
        	// part not found 
            sb.delete(0, sb.length());
            sb.append("*** Part '").append(part).append("' is 0 times defined.");
            //CAT.debug("*** Part '" + part + "' is 0 times defined.");
            CAT.debug(sb.toString());
            if (dolog) {
                DependencyTracker.log("text", path, part, product, parent_path, parent_part, 
                                      parent_product, targetgen, targetkey);
            }
            return new EmptyNodeSet();
        } else if (length > 1) {
        	// too many parts. Error!
            if (dolog) {
                DependencyTracker.log("text", path, part, product, parent_path, parent_part, 
                                      parent_product, targetgen, targetkey);
            }
            sb.delete(0, sb.length());
            sb.append("*** Part '").append(part).append("' is multiple times defined! Must be exactly 1");
            throw new XMLException(sb.toString());
        }
        
        
        // OK, we have found the part. Find the specfic product.
        sb.delete(0, sb.length());
        sb.append(XPPARTNAME).append(part).append(XPNAMEEND).
        	append(XPPRODNAME).append(product).append(XPNAMEEND);
        try {
            ns     = PFXPathEvaluator.evaluateAsNodeSetValue(sb.toString(), doc);
        } catch (Exception e) {
            if(dolog)
                DependencyTracker.log("text", path, part, product, parent_path, parent_part, parent_product, targetgen, targetkey);
            throw e;
        }
        
        try {
            length = ns.getCount();
        } catch (XPathException e) {
            if(dolog)
                DependencyTracker.log("text", path, part, product, parent_path, parent_part, parent_product, targetgen, targetkey);
            throw e;
        }
        if (length == 0) {
            // Didn't find the specific product, trying default:
            sb.delete(0, sb.length());
            sb.append(XPPARTNAME).append(part).append(XPNAMEEND).
            	append(XPPRODNAME).append(DEFAULT).append(XPNAMEEND);
            try {
                ns = PFXPathEvaluator.evaluateAsNodeSetValue(sb.toString(), doc);
            } catch (Exception e) {
                if(dolog)
                    DependencyTracker.log("text", path, part, DEFAULT, parent_path, parent_part, parent_product, targetgen, targetkey);
                throw e;
            }
            
            int len;
            try {
                len = ns.getCount();
            } catch (XPathException e) {
                if(dolog)
                    DependencyTracker.log("text", path, part, product, parent_path, parent_part, parent_product, targetgen, targetkey);
                throw e;
            }
            if (len == 1 | len == 0) {
            	// Found one or none default products
                String retval = "0";
                if (dolog) {
                    retval = DependencyTracker.log("text", path, part, DEFAULT, parent_path, 
                                                   parent_part, parent_product, targetgen, 
                                                   targetkey);
                }
                if (len == 0) {
                	// Specific product and default product not found. Warning!
                    sb.delete(0, sb.length());
                    sb.append("*** Product '").append(product).
                    	append("' is not accessible under part '").append(part).append("@").
                    	append(path).append("', and a default product is not defined either.");
                    CAT.warn(sb.toString());
                    return new EmptyNodeSet();
                } else {
                    if (retval.equals("0")) {
                        return ns;
                    } else {
                        return new EmptyNodeSet();
                    }
                }
            } else {
            	// too many default products found. Error!
                sb.delete(0, sb.length());
                sb.append("*** Part '").append(part).
                	append("' has multiple default product branches! Must be 1.");
                throw new XMLException(sb.toString());
            }
        } else if (length == 1) {
        	// specific product found
            String retval = "0";
            if (dolog) {
                retval = DependencyTracker.log("text", path, part, product, parent_path, 
                                               parent_part, parent_product, targetgen, targetkey);
            }
            if (retval.equals("0")) {
                return ns;
            } else {
                return new EmptyNodeSet();
            }
        } else {
        	// too many specific products found. Error!
            if (dolog) {
                DependencyTracker.log("text", path, part, product, parent_path, parent_part, 
                                      parent_product, targetgen, targetkey);
            }
            sb.delete(0, sb.length());
            sb.append("*** Product '").append(product).
            	append("' is defined multiple times under part '").append(part).append("@").
            	append(path).append("'");
            throw new XMLException(sb.toString());
        }
    }

    private static NodeSetValue handleError(String part, Exception e)
        throws ParserConfigurationException, Exception, IOException {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document d = builder.newDocument();
        Element ele1 = d.createElement("include_error");
        ele1.setAttribute("part", part);
        ele1.setAttribute("type", e.getClass().getName());
        ele1.setAttribute("msg", e.getMessage());
        if(e instanceof SAXParseException) {
            SAXParseException saxex = (SAXParseException) e;
            ele1.setAttribute("id", saxex.getSystemId());
            ele1.setAttribute("line", ""+saxex.getLineNumber());
            ele1.setAttribute("column", ""+saxex.getColumnNumber());
        }
        d.importNode(ele1, true);
        d.appendChild(ele1);
        TraxXSLTProcessor trax = new TraxXSLTProcessor();
        d = trax.xmlObjectFromDocument(d);
        return PFXPathEvaluator.evaluateAsNodeSetValue("/*", d);
    }
}// end of class IncludeDocumentExtension
