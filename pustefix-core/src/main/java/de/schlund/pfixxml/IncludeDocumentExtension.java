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
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

import org.apache.log4j.Logger;
import org.pustefixframework.util.LocaleUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import de.schlund.pfixxml.resources.DynamicResourceInfo;
import de.schlund.pfixxml.resources.DynamicResourceProvider;
import de.schlund.pfixxml.resources.Resource;
import de.schlund.pfixxml.resources.ResourceProviderRegistry;
import de.schlund.pfixxml.resources.ResourceUtil;
import de.schlund.pfixxml.targets.Target;
import de.schlund.pfixxml.targets.TargetGenerator;
import de.schlund.pfixxml.targets.VirtualTarget;
import de.schlund.pfixxml.util.ExtensionFunctionUtils;
import de.schlund.pfixxml.util.URIParameters;
import de.schlund.pfixxml.util.XPath;
import de.schlund.pfixxml.util.Xml;
import de.schlund.pfixxml.util.Xslt.ResourceResolver;
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
    
    private static Pattern dynamicUriPattern = Pattern.compile("dynamic://[^?#]*(\\?([^#]*))?(#.*)?");
    
    private static DynamicResourceProvider dynamicResourceProvider = (DynamicResourceProvider)ResourceProviderRegistry.getResourceProvider("dynamic");
    
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
                                   TargetGenerator targetgen, String targetkey,
                                   String parent_part_in, String parent_theme_in, String computed_inc,
                                   String module, String search, String tenant, String language) throws Exception {

        if(path_str.startsWith("docroot:")) path_str = path_str.substring(9);
        if(module != null) {
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
        
        String uriStr = makeURI(path_str, part, module, search, tenant, language, parentURI);
        
        // EEEEK! this code is in need of some serious beautifying....
        
        try {
            
            Resource    path        = ResourceUtil.getResource(uriStr);
            resolvedUri.set(path.toURI().toString());
            Resource    parent_path = "".equals(parent_uri_str) ? null : ResourceUtil.getResource(parent_uri_str);
            boolean            dolog       = !targetkey.equals(NOTARGET);
            int                length      = 0;
            IncludeDocument    iDoc        = null;
            Document           doc;

            VirtualTarget target = (VirtualTarget) targetgen.getTarget(targetkey);

            String[] themes = targetgen.getGlobalThemes().getThemesArr();
            if(targetkey.equals(NOTARGET)) {
                Target parentTarget = getParentTarget(context);
                if(parentTarget != null && parentTarget.getThemes() != null && !parentTarget.getThemes().isEmpty()) {
                    themes = parentTarget.getThemes().getThemesArr();
                }
            } else {
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
            
            String DEF_THEME = targetgen.getDefaultTheme();

            if (path == null || !path.exists()) {
                if (dolog) {
                    DependencyTracker.logTyped("text", path, part, DEF_THEME,
                                               parent_path, parent_part, parent_theme, target);
                }
                return errorNode(context,DEF_THEME);
                //return new EmptyNodeSet();
            }
            // get the includedocument
            try {
                iDoc = targetgen.getIncludeDocumentFactory().getIncludeDocument(context.getXsltVersion(), path, false);
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
                    } else {
                    	DependencyTracker.logInclude(true, path, part, targetgen);
                    }
                    return ok? (Object) ns.get(0) : errorNode(context,curr_theme);
                    //return ok? (Object) ns.get(0) : new EmptyNodeSet();
                } else {
                    
                    String languagePart = LocaleUtils.getLanguagePart(language);
                    int lastMatchFactor = 0;
                    Element lastMatch = null;
                    for(Node node: ns) {
                        Element elem = (Element)node;
                        String tenantAttr = elem.getAttribute("tenant");
                        String langAttr = elem.getAttribute("lang");
                        int matchFactor = 0;
                        if(tenantAttr.equals(tenant)) {
                            matchFactor += 8;
                        } else if(tenantAttr.isEmpty()) {
                            matchFactor += 4;
                        }
                        if(matchFactor > 0) {
                            if(langAttr.equals(language)) {
                                matchFactor += 3;
                            } else if(langAttr.isEmpty()) {
                                matchFactor += 1;
                            } else if(langAttr.equals(languagePart)) {
                                matchFactor += 2;
                            } else {
                                matchFactor = 0;
                            }
                        }
                        if(matchFactor > lastMatchFactor) {
                            lastMatchFactor = matchFactor;
                            lastMatch = elem;
                        }
                    }
                    
                    boolean ok = true;
                    if (dolog) {
                        try {
                            DependencyTracker.logTyped("text", path, part, curr_theme,
                                                       parent_path, parent_part, parent_theme, target);
                        } catch (Exception e) {
                            // TODO
                            ok = false;
                        }
                    } else {
                    	DependencyTracker.logInclude(true, path, part, targetgen);
                    }
                    return ok? (Object) lastMatch : errorNode(context,curr_theme);
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
            Object[] args = {uriStr, part, targetgen, targetkey, 
                             parent_uri_str, parent_part, parent_theme};
            String sb = MessageFormat.format("path={0}|part={1}|targetgen={2}|targetkey={3}|"+
                                             "parent_path={4}|parent_part={5}|parent_theme={6}", args);
            LOG.error("Caught exception in extension function! Params:\n"+ sb+"\n Stacktrace follows.");
            ExtensionFunctionUtils.setExtensionFunctionError(e);
            throw e;
        }
    }
    
    private static String makeURI(String path, String part, String module, String search, String tenant, String language, URI parentURI) throws Exception {
        String uriStr = path;
        if(!uriStr.matches("^\\w+:.*")) {
            boolean dynamic = false;
            if(search!=null && !search.trim().equals("")) {
                if(search.equals("dynamic")) dynamic = true;
                else throw new XMLException("Unsupported include search argument: " + search);
            }
            if(dynamic) {
                String filter = FilterHelper.getFilter(tenant, language);
                uriStr = "dynamic:/" + path + "?part=" + part + "&parent=" + parentURI.toASCIIString();
                if(!"WEBAPP".equalsIgnoreCase(module)) {
                    if(module != null) {
                        uriStr += "&module="+module;
                    } else if("module".equals(parentURI.getScheme())) {
                        uriStr += "&module="+parentURI.getAuthority();
                    }
                    if(filter != null)  uriStr += "&filter=" + URLEncoder.encode(filter, "UTF-8");
                }
            } else {
                if(!"WEBAPP".equalsIgnoreCase(module)) {
                    if(module != null) {
                        uriStr = "module://" + module + "/" + path;
                    } else if("module".equals(parentURI.getScheme())) {
                        uriStr = "module://" + parentURI.getAuthority() + "/" + path;
                    }
                }
            }
        } else if(uriStr.matches("^dynamic://.*")) {
            //add missing dynamic URI parameters
            Matcher matcher = dynamicUriPattern.matcher(uriStr);
            if(matcher.matches()) {
                URIParameters params;
                if(matcher.group(2) != null) params = new URIParameters(matcher.group(2), "UTF-8");
                else params = new URIParameters();
                if(params.getParameter("part") == null) params.addParameter("part", part);
                if(params.getParameter("parent") == null) params.addParameter("parent", parentURI.toASCIIString());
                if(matcher.group(2) == null) {
                    if(matcher.group(3) == null) uriStr += "?" + params.toString();
                    else uriStr = uriStr.substring(0, matcher.start(3)) + "?" +  params.toString() + uriStr.substring(matcher.start(3));
                } else {
                    uriStr = uriStr.substring(0, matcher.start(2)) + params.toString() + uriStr.substring(matcher.end(2));
                }
            }
        }
        return uriStr;
    }
    
    public static final boolean exists(XsltContext context, String path_str, String part, TargetGenerator targetgen, 
                                       String targetkey, String module, String search, String tenant, String language) throws Exception {

        if(path_str.startsWith("docroot:")) path_str = path_str.substring(9);
        if(module != null) {
            module = module.trim();
            if(module.equals("")) module = null;
        }
        if (path_str == null || path_str.equals("")) {
            throw new XMLException("Need href for pfx:include existence check");
        }
        if (path_str.startsWith("/")) path_str = path_str.substring(1);
        
        String parentSystemId = getSystemId(context);
        URI parentURI = new URI(parentSystemId);
        String uriStr = makeURI(path_str, part, module, search, tenant, language, parentURI);
        
        try {
            
            Resource    path        = ResourceUtil.getResource(uriStr);
            resolvedUri.set(path.toURI().toString());
          
            VirtualTarget target = (VirtualTarget) targetgen.getTarget(targetkey);

            String[] themes = targetgen.getGlobalThemes().getThemesArr();
            if(targetkey.equals(NOTARGET)) {
                Target parentTarget = getParentTarget(context);
                if(parentTarget != null && parentTarget.getThemes() != null && !parentTarget.getThemes().isEmpty()) {
                    themes = parentTarget.getThemes().getThemesArr();
                }
            } else {
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
            
            if (path != null && path.exists()) {
                IncludePartsInfo partsInfo = dynamicResourceProvider.getIncludePartsInfoFactory().getIncludePartsInfo(path);
                if(partsInfo != null) {
                    IncludePartInfo partInfo = partsInfo.getPart(part);
                    if(partInfo != null) {
                        String matchingTheme = partInfo.getMatchingTheme(themes);
                        if(matchingTheme != null) {
                            return true;
                        }
                    }
                }
            }
            return false;
            
        } catch (Exception e) {
            ExtensionFunctionUtils.setExtensionFunctionError(e);
            throw e;
        }
    } 
    
    
    public static String getResolvedURI() {
        return resolvedUri.get();
    }
    
    public static String getDynIncInfo(String part, String theme, String path, String resolvedModule, String requestedModule, String tenant, String language) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(part).append("|").append(theme).append("|").append(path).append("|").append(resolvedModule);
            sb.append("|").append(requestedModule).append("|");
            if(!path.startsWith("/")) path = "/" + path;
            String filter = FilterHelper.getFilter(tenant, language);
            if(filter != null) {
                filter = "&filter=" + URLEncoder.encode(filter, "UTF-8");
            } else {
                filter = "";
            }
            URI uri = new URI("dynamic://" + requestedModule + path + "?part=" + part + filter);
            DynamicResourceInfo info = new DynamicResourceInfo();
            dynamicResourceProvider.getResource(uri, info);
            sb.append(info.toString());
            return sb.toString();
        } catch (Exception x) {
            LOG.error("Error getting dynamic include information", x);
            return "n/a";
        }
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
    
    public static final String getModuleFromSystemId(XsltContext context) {
        String sysid = context.getSystemId();
        if(sysid.startsWith("module://")) {
            int ind = sysid.indexOf('/', 9);
            String module = sysid.substring(9, ind);
            return module;
        }
        return "";
    }
    
    private static Target getParentTarget(XsltContext context) {
        URIResolver resolver = context.getURIResolver();
        if(resolver != null && resolver instanceof ResourceResolver) {
            ResourceResolver resResolver = (ResourceResolver)resolver;
            return resResolver.getParentTarget();
        }
        return null;
    }

    public static boolean isIncludeDocument(XsltContext context) {
        return context.getDocumentElementName().equals("include_parts");
    }

}// end of class IncludeDocumentExtension
