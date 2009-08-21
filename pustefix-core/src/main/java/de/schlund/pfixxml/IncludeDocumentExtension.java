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
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.pustefixframework.resource.IncludePartResource;
import org.pustefixframework.resource.NullResource;
import org.pustefixframework.resource.Resource;
import org.pustefixframework.xmlgenerator.targets.TargetGenerator;
import org.pustefixframework.xmlgenerator.targets.VirtualTarget;
import org.pustefixframework.xmlgenerator.view.ViewExtensionResolver;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import de.schlund.pfixxml.util.ExtensionFunctionUtils;
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
        
        if(dynamic) {
        	//TODO: dynamic scheme support
            uriStr = "dynamic:/" + path_str + "?part=" + part + "&parent=" + parent_uri_str;
            if(module != null) uriStr += "&module="+module;
            else if("bundle".equals(parentURI.getScheme())) {
                uriStr += "&module="+parentURI.getAuthority();
            }
            uriStr += "&project=" + targetGen.getName();
        } else {
        	if(!uriStr.matches("^\\w+:.*")) {
        		if(module != null) {
        			uriStr = "bundle://" + module + "/PUSTEFIX-INF/" + path_str;
        		} else if("bundle".equals(parentURI.getScheme())) {
        			uriStr = "bundle://" + parentURI.getAuthority() + "/PUSTEFIX-INF/" + path_str;
        		} else {
        			throw new IllegalArgumentException("Don't know which bundle should be referenced: " + uriStr);
        		}
            }
        }
        
        try {
                  		
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

        	Map<String,Object> paramMap = new HashMap<String,Object>();
        	paramMap.put("preferredThemes", themes);
        	URI uri = new URI("includepart:"+uriStr+":"+part);
        	
        	//TODO: cache include parts
        	IncludePartResource resource = targetGen.getResourceLoader().getResource(uri, paramMap, IncludePartResource.class);
        	resolvedUri.set(resource == null? uri.toString():resource.getURI().toString());
            
        	Resource parentResource = null;
        	if(!parent_uri_str.equals("")) {
        		URI parentUri = new URI(parent_uri_str);
        		parentResource = targetGen.getResourceLoader().getResource(parentUri);
        	}
        	
        	boolean dolog = !targetkey.equals(NOTARGET);
            if (resource == null) {
                if (dolog) {
                    DependencyTracker.logTyped("text", new NullResource(uri), part, DEF_THEME,
                                               parentResource, parent_part, parent_theme, target);
                }
                return errorNode(context, DEF_THEME);
            } else {
            	if (dolog) {
            		DependencyTracker.logTyped("text", resource, part, resource.getTheme(),
                                                       parentResource, parent_part, parent_theme, target);
            	}
            	return resource.getElement();
            }
            
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
            return uri.getPath();
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
