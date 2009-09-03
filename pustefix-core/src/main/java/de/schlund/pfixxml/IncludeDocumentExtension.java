/*
 * This file is part of Pustefix.
 *
 * Pustefix is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Pustefix is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Pustefix; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package de.schlund.pfixxml;


import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.pustefixframework.resource.Resource;
import org.pustefixframework.resource.support.NullResource;
import org.pustefixframework.xmlgenerator.targets.TargetGenerator;
import org.pustefixframework.xmlgenerator.targets.VirtualTarget;
import org.pustefixframework.xmlgenerator.view.ViewExtensionResolver;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import de.schlund.pfixxml.util.ExtensionFunctionUtils;
import de.schlund.pfixxml.util.XPath;
import de.schlund.pfixxml.util.Xml;
import de.schlund.pfixxml.util.XsltContext;

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
    private static final Logger       LOG        = Logger.getLogger(IncludeDocumentExtension.class);
    private static final String NOTARGET   = "__NONE__";
    private static final String XPPARTNAME = "/include_parts/part[@name='";
    private static final String XTHEMENAME = "/theme[@name = '";
    private static final String XPNAMEEND  = "']";
    
    private static ThreadLocal<String> resolvedUri = new ThreadLocal<String>();
    
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
     * @param parent_theme
     * @return a list of nodes understood by the current transformer(currently saxon)
     * @throws Exception on all errors
     */
    public static final Object get(XsltContext context, String path_str, String part,
                                   TargetGenerator targetGen, String targetkey,
                                   String parent_part_in, String parent_theme_in, String computed_inc,
                                   String module, String search) throws Exception {
        
        boolean dynamic = false;
        if(search!=null && !search.trim().equals("")) {
            if(search.equals("dynamic")) dynamic = true;
            else throw new XMLException("Unsupported include search argument: " + search);
        }
        
        if(module!=null) {
            module = module.trim();
            if(module.equals("")) module = null;
        }
        
        if (path_str == null || path_str.equals("")) {
            throw new XMLException("Need href attribute for pfx:include or path of parent part must be deducible");
        }
        if (path_str.startsWith("/")) path_str = path_str.substring(1);
        
        String       parent_uri_str  = "";
        String       parent_part     = "";
        String       parent_theme    = "";
        
        String parentSystemId = getSystemId(context);
        URI parentURI = new URI(parentSystemId);
        
        if (computed_inc.equals("false") && isIncludeDocument(context)) {
            parent_uri_str = parentSystemId;
            parent_part     = parent_part_in;
            parent_theme  = parent_theme_in;
        }
        
        String uriStr = path_str;
        
        if(!uriStr.matches("^\\w+:.*")) {

            if(module == null && "bundle".equals(parentURI.getScheme())) module = parentURI.getAuthority();
            if(module == null) throw new IllegalArgumentException("Can't detect source bundle of: " + uriStr + " parent: " + parentURI.toASCIIString());

            if(dynamic) {
                    uriStr = "dynamic://" + module + "/PUSTEFIX-INF/" + path_str + "?part=" + part +
                                            "&parent=" + parent_uri_str + "&application=" + targetGen.getApplicationBundle();
            } else {
                    uriStr = "bundle://" + module + "/PUSTEFIX-INF/" + path_str;
            }
        }
        
        try {
          
            URI uri = new URI(uriStr);
            Resource path = targetGen.getResourceLoader().getResource(uri);
            
            resolvedUri.set(path == null? uri.toString():path.getURI().toString());
            
            Resource    parent_path = null;
            if(!parent_uri_str.equals("")) {
            	uri = new URI(parent_uri_str);
            	parent_path = targetGen.getResourceLoader().getResource(uri);
            }
            boolean            dolog       = !targetkey.equals(NOTARGET);
            int                length      = 0;
            IncludeDocument    iDoc        = null;
            Document           doc;

            VirtualTarget target = (VirtualTarget) targetGen.getTarget(targetkey);

            String[] themes = targetGen.getGlobalThemes().getThemesArr();
            if (!targetkey.equals(NOTARGET)) {
                themes = target.getThemes().getThemesArr();
            }
            if (themes == null) {
                XMLException ex = new XMLException("Target has a 'null' themes array!");
                target.setStoredException(ex);
                throw ex;
            }
            if (themes.length < 1) {
                XMLException ex = new XMLException("Target has an empty themes array!");
                target.setStoredException(ex);
                throw ex;
            }
            
            String DEF_THEME = targetGen.getDefaultTheme();

            if (path == null) {
                if (dolog) {
                    DependencyTracker.logTyped("text", new NullResource(uri), part, DEF_THEME,
                                               parent_path, parent_part, parent_theme, target);
                }
                return errorNode(context,DEF_THEME);
                //return new EmptyNodeSet();
            }
            
            // get the includedocument
            try {
                iDoc = targetGen.getIncludeDocument(context.getXsltVersion(), path, false);
            } catch (SAXException saxex) {
                if (dolog)
                    DependencyTracker.logTyped("text", path, part, DEF_THEME,
                                               parent_path, parent_part, parent_theme, target);
                target.setStoredException(saxex);
                throw saxex;
            }
            doc = iDoc.getDocument();
            // Get the part
            List<Node> ns;
            try {
                ns = XPath.select(doc, XPPARTNAME + part + XPNAMEEND);
            } catch (TransformerException e) {
                if (dolog)
                    DependencyTracker.logTyped("text", path, part, DEF_THEME,
                                               parent_path, parent_part, parent_theme, target);
                throw e;
            }
            length = ns.size();
            if (length == 0) {
                // part not found
                LOG.debug("*** Part '" + part + "' is 0 times defined.");
                if (dolog) {
                    DependencyTracker.logTyped("text", path, part, DEF_THEME,
                                               parent_path, parent_part, parent_theme, target);
                }
                return errorNode(context,DEF_THEME);
                //return new EmptyNodeSet();
            } else if (length > 1) {
                // too many parts. Error!
                if (dolog) {
                    DependencyTracker.logTyped("text", path, part, DEF_THEME,
                                               parent_path, parent_part, parent_theme, target);
                }
                XMLException ex = new XMLException("*** Part '" + part + "' is multiple times defined! Must be exactly 1");
                if(target!=null) target.setStoredException(ex);
                throw ex;
            }

            // OK, we have found the part. Find the specfic theme branch matching the theme fallback list.
            LOG.debug("   => Found part '" +  part + "'");
            
            for (int i = 0; i < themes.length; i++) {

                String curr_theme = themes[i]; 
                LOG.debug("     => Trying to find theme branch for theme '" + curr_theme + "'");
                
                try {
                    ns = XPath.select(doc, XPPARTNAME + part + XPNAMEEND + XTHEMENAME + curr_theme + XPNAMEEND);
                } catch (TransformerException e) {
                    if (dolog)
                        DependencyTracker.logTyped("text", path, part, DEF_THEME,
                                                   parent_path, parent_part, parent_theme, target);
                    throw e;
                }
                length = ns.size();
                if (length == 0) {
                    // Didn't find a theme part matching curr_theme, trying next in fallback line
                    if (i < (themes.length - 1)) {
                        LOG.debug("        Part '" + part + "' has no theme branch matching '" + curr_theme + "', trying next theme");
                    } else {
                        LOG.warn("        Part '" + part + "' has no theme branch matching '" + curr_theme + "', no more theme to try!");
                    }
                    continue;
                } else if (length == 1) {
                    LOG.debug("        Found theme branch '" + curr_theme + "' => STOP");
                    // specific theme found
                    boolean ok = true;
                    if (dolog) {
                        try {
                            DependencyTracker.logTyped("text", path, part, curr_theme,
                                                       parent_path, parent_part, parent_theme, target);
                        } catch (Exception e) {
                            // TODO
                            ok = false;
                        }
                    }
                    return ok? (Object) ns.get(0) : errorNode(context,curr_theme);
                    //return ok? (Object) ns.get(0) : new EmptyNodeSet();
                } else {
                    // too many specific themes found. Error!
                    if (dolog) {
                        DependencyTracker.logTyped("text", path, part, DEF_THEME,
                                                   parent_path, parent_part, parent_theme, target);
                    }
                    XMLException ex = new XMLException("*** Theme branch '" + curr_theme +
                                                       "' is defined multiple times under part '" + part + "@" + path + "'");
                    target.setStoredException(ex);
                    throw ex;
                }
            }
            
            // We are only here if none of the themes produced a match:
            @SuppressWarnings("unused")
            boolean ok = true;
            if (dolog) {
                try {
                    DependencyTracker.logTyped("text", path, part, DEF_THEME,
                                               parent_path, parent_part, parent_theme, target);
                } catch (Exception e) { // TODO
                    ok = false;
                }
            }
            return errorNode(context,DEF_THEME);
            //return new EmptyNodeSet();
            
        } catch (Exception e) {
            Object[] args = {uriStr, part, targetGen, targetkey, 
                             parent_uri_str, parent_part, parent_theme};
            String sb = MessageFormat.format("path={0}|part={1}|targetgen={2}|targetkey={3}|"+
                                             "parent_path={4}|parent_part={5}|parent_theme={6}", args);
            LOG.error("Caught exception in extension function! Params:\n"+ sb+"\n Stacktrace follows.");
            ExtensionFunctionUtils.setExtensionFunctionError(e);
            throw e;
        }
        
    }
    
    public static String getResolvedURI() {
        return resolvedUri.get();
    }

    private static final Node errorNode(XsltContext context,String prodname) {
        Document retdoc  = Xml.createDocumentBuilder().newDocument();
        Element  retelem = retdoc.createElement("missing");
        retelem.setAttribute("name", prodname);
        retdoc.appendChild(retelem);
        retdoc = Xml.parse(context.getXsltVersion(),retdoc);
        return retdoc.getDocumentElement();  
    }

    public static final String getSystemId(XsltContext context) {
        return context.getSystemId();
    }
    
    public static final String getRelativePathFromSystemId(XsltContext context) {
        String sysid = context.getSystemId();
        try {
            URI uri = new URI(sysid);
            String path = uri.getPath();
            if(path.startsWith("/PUSTEFIX-INF")) path = path.substring(13);
            return path;
        } catch(URISyntaxException x) {
            throw new IllegalArgumentException("Illegal system id: " + sysid, x);
        }
    }

    public static boolean isIncludeDocument(XsltContext context) {
        return context.getDocumentElementName().equals("include_parts");
    }
    
    
    public static final Node getExtensions(XsltContext context, TargetGenerator targetGen,
    		String targetKey, String extensionPointId, String extensionPointVersion) throws Exception {
    	try {
    		ViewExtensionResolver resolver = targetGen.getViewExtensionResolver();
    		Node node = resolver.getExtensionNodes(context, targetKey, extensionPointId, extensionPointVersion);
    		return node;
    	} catch (Exception x) {
    		ExtensionFunctionUtils.setExtensionFunctionError(x);
    		throw x;
    	}
    }
    
    
}// end of class IncludeDocumentExtension
