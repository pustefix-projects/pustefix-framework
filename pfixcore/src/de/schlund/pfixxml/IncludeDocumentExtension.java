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
import java.text.MessageFormat;
import java.util.List;
import javax.xml.transform.TransformerException;

import org.apache.log4j.Category;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import net.sf.saxon.value.EmptySequence;

import de.schlund.pfixxml.util.Path;
import de.schlund.pfixxml.targets.TargetGeneratorFactory;
import de.schlund.pfixxml.targets.VirtualTarget;
import de.schlund.pfixxml.util.XPath;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.om.NodeInfo;

/**
 * IncludeDocumentExtension.java
 * 
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher </a>
 * @author <a href="mailto:haecker@schlund.de">Joerg Haecker </a>
 * 
 * This class is responsible to return the requested parts of an
 * {@link IncludeDocument}. It provides a static method which is called from
 * XSL via the extension mechanism (and from nowhere else!).
 */
public final class IncludeDocumentExtension {
    //~ Instance/static variables
    // ..................................................................
    private static Category     CAT        = Category.getInstance(IncludeDocumentExtension.class.getName());
    private static final String DEFAULT    = "default";
    private static final String NOTARGET   = "__NONE__";
    private static final String XPPARTNAME = "/include_parts/part[@name='";
    private static final String XPPRODNAME = "/product[@name = '";
    private static final String XPNAMEEND  = "']";
    
    //~ Methods
    // ....................................................................................
    /**
     * Get the requested IncludeDocument from {@link IncludeDocumentFactory}
     * and retrieve desired information from it.</br> Note: The nested
     * document in the Includedocument is immutable, any attempts to modify it
     * will cause an exception.
     * 
     * @param path    the path to the Includedocument in the file system relative
     *                to docroot.
     * @param part    the part in the Includedocument.
     * @param docroot the document root in the file system
     * @param targetgen
     * @param targetkey
     * @param parent_path
     * @param parent_part
     * @param parent_product
     * @return a list of nodes understood by the current transformer(currently saxon)
     * @throws Exception on all errors
     */
    public static final Object get(XPathContext context, String path_str, String part, String product,
                                   String targetgen, String targetkey,
                                   String parent_part_in, String parent_product_in, String computed_inc) throws Exception {

        PathFactory pf              = PathFactory.getInstance();
        String      parent_path_str = "";
        String      parent_part     = "";
        String      parent_product  = "";
        Path        tgen_path       = pf.createPath(targetgen);
        
        if (computed_inc.equals("false") && isIncludeDocument(context)) {
            parent_path_str = makeSystemIdRelative(context);
            parent_part     = parent_part_in;
            parent_product  = parent_product_in;
        }

        if (path_str == null || path_str.equals("")) {
            if (!parent_path_str.equals("")) {
                path_str = parent_path_str;
            } else {
                throw new XMLException("Need href attribute for pfx:include or path of parent part");
            }
        }
        
        try {
            Path            path        = pf.createPath(path_str);
            Path            parent_path = "".equals(parent_path_str) ? null : pf.createPath(parent_path_str);
            boolean         dolog       = !targetkey.equals(NOTARGET);
            File            incfile     = path.resolve();
            int             length      = 0;
            IncludeDocument iDoc        = null;
            Document        doc;
            
            VirtualTarget target = (VirtualTarget) TargetGeneratorFactory.getInstance().createGenerator(tgen_path).getTarget(targetkey);
            if (!incfile.exists()) {
                if (dolog) {
                    DependencyTracker.logTyped("text", path, part, DEFAULT,
                                               parent_path, parent_part, parent_product, target);
                }
                return EmptySequence.getInstance();
            }
            // get the includedocument
            try {
                iDoc = IncludeDocumentFactory.getInstance().getIncludeDocument(path, false);
            } catch (SAXException saxex) {
                if (dolog)
                    DependencyTracker.logTyped("text", path, part, DEFAULT,
                                               parent_path, parent_part, parent_product, target);
                target.setStoredException(saxex);
                throw saxex;
            }
            doc = iDoc.getDocument();
            // create a new buffer for xpath expressions
            StringBuffer sb = new StringBuffer(100);
            // Get the part
            sb.append(XPPARTNAME).append(part).append(XPNAMEEND);
            List ns;
            try {
                ns = XPath.select(doc, sb.toString());
            } catch (TransformerException e) {
                if (dolog)
                    DependencyTracker.logTyped("text", path, part, DEFAULT,
                                               parent_path, parent_part, parent_product, target);
                throw e;
            }
            length = ns.size();
            if (length == 0) {
                // part not found
                sb.delete(0, sb.length());
                sb.append("*** Part '").append(part).append("' is 0 times defined.");
                //CAT.debug("*** Part '" + part + "' is 0 times defined.");
                CAT.debug(sb.toString());
                if (dolog) {
                    DependencyTracker.logTyped("text", path, part, DEFAULT,
                                               parent_path, parent_part, parent_product, target);
                }
                return EmptySequence.getInstance();
            } else if (length > 1) {
                // too many parts. Error!
                if (dolog) {
                    DependencyTracker.logTyped("text", path, part, DEFAULT,
                                               parent_path, parent_part, parent_product, target);
                }
                sb.delete(0, sb.length());
                sb.append("*** Part '").append(part).append("' is multiple times defined! Must be exactly 1");
                XMLException ex = new XMLException(sb.toString());
                target.setStoredException(ex);
                throw ex;
            }
            // OK, we have found the part. Find the specfic product.
            sb.delete(0, sb.length());
            sb.append(XPPARTNAME).append(part).append(XPNAMEEND).append(XPPRODNAME).append(product).append(XPNAMEEND);
            try {
                ns = XPath.select(doc, sb.toString());
            } catch (TransformerException e) {
                if (dolog)
                    DependencyTracker.logTyped("text", path, part, DEFAULT,
                                               parent_path, parent_part, parent_product, target);
                throw e;
            }
            length = ns.size();
            if (length == 0) {
                // Didn't find the specific product, trying default:
                sb.delete(0, sb.length());
                sb.append(XPPARTNAME).append(part).append(XPNAMEEND).append(XPPRODNAME).
                    append(DEFAULT).append(XPNAMEEND);
                try {
                    ns = XPath.select(doc, sb.toString());
                } catch (TransformerException e) {
                    if (dolog)
                        DependencyTracker.logTyped("text", path, part, DEFAULT,
                                                   parent_path, parent_part, parent_product, target);
                    throw e;
                }
                int len = ns.size();
                if (len == 1 | len == 0) {
                    // Found one or none default products
                    boolean ok = true;
                    if (dolog) {
                        try {
                            DependencyTracker.logTyped("text", path, part, DEFAULT,
                                                       parent_path, parent_part, parent_product, target);
                        } catch (Exception e) { // TODO
                            ok = false;
                        }
                    }
                    if (len == 0) {
                        // Specific product and default product not found.
                        // Warning!
                        sb.delete(0, sb.length());
                        sb.append("*** Product '").append(product).
                            append("' is not accessible under part '").append(part).append("@").append(path).
                            append("', and a default product is not defined either.");
                        CAT.warn(sb.toString());
                        return EmptySequence.getInstance();
                    } else {
                        return ok? ns.get(0) : EmptySequence.getInstance();
                    }
                } else {
                    // too many default products found. Error!
                    if (dolog) {
                        DependencyTracker.logTyped("text", path, part, DEFAULT,
                                                   parent_path, parent_part, parent_product, target);
                    }
                    sb.delete(0, sb.length());
                    sb.append("*** Part '").append(part).append("' has multiple default product branches! Must be 1.");
                    XMLException ex = new XMLException(sb.toString());
                    target.setStoredException(ex);
                    throw ex;
                }
            } else if (length == 1) {
                // specific product found
                boolean ok = true;
                if (dolog) {
                    try {
                        DependencyTracker.logTyped("text", path, part, product,
                                                   parent_path, parent_part, parent_product, target);
                    } catch (Exception e) { // TODO
                        ok = false;
                    }
                }
                return ok? (Object) ns.get(0) : EmptySequence.getInstance();
            } else {
                // too many specific products found. Error!
                if (dolog) {
                    DependencyTracker.logTyped("text", path, part, DEFAULT,
                                               parent_path, parent_part, parent_product, target);
                }
                sb.delete(0, sb.length());
                sb.append("*** Product '").append(product).append("' is defined multiple times under part '").
                    append(part).append("@").append(path).append("'");
                XMLException ex = new XMLException(sb.toString());
                target.setStoredException(ex);
                throw ex;
            }
        } catch (Exception e) {
            Object[] args = { path_str, part, product,
                              targetgen, targetkey, 
                              parent_path_str, parent_part, parent_product};
            String sb = MessageFormat.format("path={0}|part={1}|product={2}|"+
                                             "targetgen={3}|targetkey={4}|"+
                                             "parent_path={5}|parent_part={6}|parent_product={7}", args);
            CAT.error("Caught exception in extension function! Params:\n"+ sb+"\n Stacktrace follows.");
            CAT.error(e);
            throw e;
        }
    }

    public static final String makeSystemIdRelative(XPathContext context) {
        return makeSystemIdRelative(context, "dummy");
    }

    public static final String makeSystemIdRelative(XPathContext context, String dummy) {
        NodeInfo citem   = (NodeInfo) context.getContextItem();
        String   sysid   = citem.getSystemId();
        String   docroot = PathFactory.getInstance().createPath("dummy").getBase().getPath();

        if (sysid.startsWith("file://" + docroot)) {
            sysid = sysid.substring(("file://" + docroot).length());
        }
        if (sysid.startsWith("/")) {
            sysid = sysid.substring(1);
        }
        return sysid;
    }

    public static boolean isIncludeDocument(XPathContext context) {
        NodeInfo citem   = (NodeInfo) context.getContextItem();
        return ((Document) citem.getDocumentRoot()).getDocumentElement().getNodeName().equals("include_parts");
    }
}// end of class IncludeDocumentExtension
