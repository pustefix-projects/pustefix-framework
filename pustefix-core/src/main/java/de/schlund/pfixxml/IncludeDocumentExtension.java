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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

import org.apache.log4j.Logger;
import org.pustefixframework.util.LocaleUtils;
import org.springframework.cache.interceptor.SimpleKeyGenerator;
import org.springframework.context.NoSuchMessageException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import de.schlund.pfixxml.config.EnvironmentProperties;
import de.schlund.pfixxml.resources.DynamicResourceInfo;
import de.schlund.pfixxml.resources.DynamicResourceProvider;
import de.schlund.pfixxml.resources.Resource;
import de.schlund.pfixxml.resources.ResourceProviderRegistry;
import de.schlund.pfixxml.resources.ResourceUtil;
import de.schlund.pfixxml.targets.SPCache;
import de.schlund.pfixxml.targets.Target;
import de.schlund.pfixxml.targets.TargetGenerator;
import de.schlund.pfixxml.targets.VirtualTarget;
import de.schlund.pfixxml.util.ExtensionFunctionUtils;
import de.schlund.pfixxml.util.URIParameters;
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

    private static final Logger       LOG        = Logger.getLogger(IncludeDocumentExtension.class);
    private static final String NOTARGET   = "__NONE__";
    
    private static ThreadLocal<String> resolvedUri = new ThreadLocal<String>();
    
    private static Pattern dynamicUriPattern = Pattern.compile("dynamic://[^?#]*(\\?([^#]*))?(#.*)?");
    
    private static DynamicResourceProvider dynamicResourceProvider = (DynamicResourceProvider)ResourceProviderRegistry.getResourceProvider("dynamic");
    
    private static DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
    
    public static final Object get(XsltContext context, String path_str, String part,
            TargetGenerator targetgen, String targetkey,
            String parent_part_in, String parent_theme_in, String computed_inc,
            String module, String search, String tenant, String language) throws Exception {

        if(NOTARGET.equals(targetkey) && targetgen.getCacheFactory().getExtensionCache() != null) {
            ConcurrentMap<Object, Object> cache = getExtensionCacheMap(context, targetgen);
            Object cacheKey = SimpleKeyGenerator.generateKey(IncludeDocumentExtension.class.getName(), 
                    "get", path_str, part, parent_part_in, parent_theme_in, computed_inc, module, search, tenant, language);
            Object cachedResult = cache.get(cacheKey);
            if(cachedResult != null) {
                return cachedResult;
            } else {
                Object result = getNoCache(context, path_str, part, targetgen, targetkey, parent_part_in, parent_theme_in, 
                        computed_inc, module, search, tenant, language);
                cache.putIfAbsent(cacheKey, result);
                return result;
            }
        } else {
            return getNoCache(context, path_str, part, targetgen, targetkey, parent_part_in, parent_theme_in, 
                    computed_inc, module, search, tenant, language);
        }
    }

    /**
     * Get the requested IncludeDocument from {@link IncludeDocumentFactory}
     * and retrieve desired information from it.</br> Note: The nested
     * document in the included document is immutable, any attempts to modify it
     * will cause an exception.
     * 
     * @param context - the XSLT context
     * @param pathStr - the path to the include document in the file system relative to docroot
     * @param part - the part name within the include document
     * @param targetgen - the target generator instance
     * @param targetkey - the target key
     * @param parentPartIn - the part from where the include is done
     * @param parentThemeIn - the theme from where the include is done
     * @param computedInc - computed include
     * @param module - the source module
     * @param search - the search strategy (dynamic or not)
     * @param tenant - the selected tenant
     * @param language - the selected language
     * @return a list of nodes understood by the current transformer (currently saxon)
     * @throws Exception on all errors
     */
    public static final Object getNoCache(XsltContext context, String pathStr, String part, TargetGenerator targetgen, 
            String targetkey, String parentPartIn, String parentThemeIn, String computedInc, String module, 
            String search, String tenant, String language) throws Exception {

        if (pathStr.startsWith("docroot:")) {
            pathStr = pathStr.substring(9);
        }
        if (module != null) {
            module = module.trim();
            if (module.equals("")) {
                module = null;
            }
        }
        if (pathStr == null || pathStr.equals("")) {
            throw new XMLException("Need href attribute for pfx:include or path of parent part must be deducible");
        }
        if (pathStr.startsWith("/")) {
            pathStr = pathStr.substring(1);
        }
        String parentUriStr = "";
        String parentPart = "";
        String parentTheme = "";
        String parentSystemId = getSystemId(context);
        URI parentURI = new URI(parentSystemId);
        if (computedInc.equals("false") && isIncludeDocument(context)) {
            parentUriStr = parentSystemId;
            parentPart = parentPartIn;
            parentTheme = parentThemeIn;
        }
        String uriStr = makeURI(pathStr, part, module, search, tenant, language, parentURI);

        try {
            Resource path = ResourceUtil.getResource(uriStr);
            resolvedUri.set(path.toURI().toString());
            Resource parent_path = "".equals(parentUriStr) ? null : ResourceUtil.getResource(parentUriStr);
            VirtualTarget target = (VirtualTarget) targetgen.getTarget(targetkey);
            boolean dolog = !targetkey.equals(NOTARGET);
            String DEF_THEME = targetgen.getDefaultTheme();

            // resource path not found
            if (path == null || !path.exists()) {
                if (dolog) {
                    DependencyTracker.logTyped("text", path, part, DEF_THEME,
                            parent_path, parentPart, parentTheme, target);
                }
                return errorNode(context, DEF_THEME);
            }

            // get the include document
            IncludeDocument iDoc;
            try {
                iDoc = targetgen.getIncludeDocumentFactory().getIncludeDocument(context.getXsltVersion(), path, false);
            } catch (SAXException saxex) {
                if (dolog)
                    DependencyTracker.logTyped("text", path, part, DEF_THEME,
                            parent_path, parentPart, parentTheme, target);
                target.setStoredException(saxex);
                throw saxex;
            }

            // get the part
            List<Node> partNodes;
            try {
                partNodes = iDoc.getNodes(part);
            } catch (TransformerException e) {
                if (dolog)
                    DependencyTracker.logTyped("text", path, part, DEF_THEME,
                            parent_path, parentPart, parentTheme, target);
                throw e;
            }
            if (partNodes.size() == 0) {
                // part not found
                if (dolog) {
                    DependencyTracker.logTyped("text", path, part, DEF_THEME,
                            parent_path, parentPart, parentTheme, target);
                }
                return errorNode(context,DEF_THEME);
            } else if (partNodes.size() > 1) {
                // multiple parts found
                if (dolog) {
                    DependencyTracker.logTyped("text", path, part, DEF_THEME,
                            parent_path, parentPart, parentTheme, target);
                }
                XMLException ex = new XMLException("*** Part '" + part + "' is multiple times defined! Must be exactly 1");
                if(target!=null) target.setStoredException(ex);
                throw ex;
            }

            // get theme lookup array
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
            
            // We have found the part. Find the specific theme branch matching the theme fallback list.
            for (int i = 0; i < themes.length; i++) {
                String theme = themes[i]; 
                List<Node> themeNodes;
                try {
                    themeNodes = iDoc.getNodes(part, theme);
                } catch (TransformerException e) {
                    if (dolog)
                        DependencyTracker.logTyped("text", path, part, DEF_THEME,
                                parent_path, parentPart, parentTheme, target);
                    throw e;
                }
                if (themeNodes.size() == 1) {
                    Element elem = (Element)themeNodes.get(0);
                    if(elem.getAttribute("tenant").isEmpty() && elem.getAttribute("lang").isEmpty()) {
                        // found one specific theme without language or tenant attribute
                        if (dolog) {
                            DependencyTracker.logTyped("text", path, part, theme,
                                    parent_path, parentPart, parentTheme, target);
                        } else {
                            DependencyTracker.logInclude(true, path, part, targetgen);
                        }
                        return elem;
                    }
                }
                if (themeNodes.size() > 0) {
                    // found multiple themes, select best matching by tenant and language
                    String languagePart = LocaleUtils.getLanguagePart(language);
                    int lastMatchFactor = 0;
                    Element lastMatch = null;
                    for(Node themeNode: themeNodes) {
                        Element elem = (Element)themeNode;
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
                    if (dolog) {
                        DependencyTracker.logTyped("text", path, part, theme,
                                parent_path, parentPart, parentTheme, target);
                    } else {
                        DependencyTracker.logInclude(true, path, part, targetgen);
                    }
                    return lastMatch != null ? (Object) lastMatch : errorNode(context, theme);
                }
            }

            if (dolog) {
                DependencyTracker.logTyped("text", path, part, DEF_THEME,
                        parent_path, parentPart, parentTheme, target);
            }
            return errorNode(context,DEF_THEME);

        } catch (Exception e) {
            Object[] args = {uriStr, part, targetgen, targetkey, 
                             parentUriStr, parentPart, parentTheme};
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
        
        if(NOTARGET.equals(targetkey) && targetgen.getCacheFactory().getExtensionCache() != null) {
            ConcurrentMap<Object, Object> cache = getExtensionCacheMap(context, targetgen);
            Object cacheKey = SimpleKeyGenerator.generateKey(IncludeDocumentExtension.class.getName(), 
                    "exists", path_str, part, module, search, tenant, language);
            Boolean cachedResult = (Boolean)cache.get(cacheKey);
            if(cachedResult != null) {
                return cachedResult;
            } else {
                boolean result = existsNoCache(context, path_str, part, targetgen, targetkey, module, search, tenant, language);
                cache.putIfAbsent(cacheKey, result);
                return result;
            }
        } else {
            return existsNoCache(context, path_str, part, targetgen, targetkey, module, search, tenant, language);
        }
    }
    
    public static final boolean existsNoCache(XsltContext context, String path_str, String part, TargetGenerator targetgen, 
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
    
    public static Node getIncludeInfo(XsltContext context, String path, String module, String search, 
            String tenant, String language, String targetkey, TargetGenerator targetgen) throws Exception {
        
        if(NOTARGET.equals(targetkey) && targetgen.getCacheFactory().getExtensionCache() != null) {
            ConcurrentMap<Object, Object> cache = getExtensionCacheMap(context, targetgen);
            Object cacheKey = SimpleKeyGenerator.generateKey(IncludeDocumentExtension.class.getName(), 
                    "getIncludeInfo", path, module, search, tenant, language);
            Node cachedResult = (Node)cache.get(cacheKey);
            if(cachedResult != null) {
                return cachedResult;
            } else {
                Node result = getIncludeInfoNoCache(context, path, module, search, tenant, language);
                cache.put(cacheKey, result);
                return result;
            }
        } else {
            return getIncludeInfoNoCache(context, path, module, search, tenant, language);
        }
    }
    
    private static Node getIncludeInfoNoCache(XsltContext context, String path, String module, String search, 
            String tenant, String language) throws Exception {
        
        try {
            if (path.startsWith("/")) path = path.substring(1);
            if(module != null) {
                module = module.trim();
                if(module.equals("")) module = null;
            }
            URI parentURI = new URI(getSystemId(context));
            String uriStr = makeURI(path, "test", module, search, tenant, language, parentURI);
            
            IncludePartsInfoFactory infoFactory = dynamicResourceProvider.getIncludePartsInfoFactory();
            Map<String, IncludePartInfo> infoMap = new LinkedHashMap<>();
            
            if(search.equals("dynamic")) {
                DynamicResourceInfo info = new DynamicResourceInfo();
                dynamicResourceProvider.getResource(new URI(uriStr), info);
                for(DynamicResourceInfo.Entry entry: info.getEntries()) {
                    Resource res;
                    if(entry.getModule().equals("webapp")) {
                        res = ResourceUtil.getResource(path);
                    } else {
                        res = ResourceUtil.getResource("module://" + entry.getModule() + "/" + path);
                    }    
                    IncludePartsInfo partsInfo = infoFactory.getIncludePartsInfo(res);
                    if(partsInfo != null) {
                        for(IncludePartInfo partInfo: partsInfo.getParts().values()) {
                            if(!infoMap.containsKey(partInfo.getName())) {
                                infoMap.put(partInfo.getName(), partInfo);
                            }
                        }
                    }
                }
            } else {
                Resource res = ResourceUtil.getResource(uriStr);
                IncludePartsInfo partsInfo = infoFactory.getIncludePartsInfo(res);
                for(IncludePartInfo partInfo: partsInfo.getParts().values()) {
                    infoMap.put(partInfo.getName(), partInfo);
                }
            }
            
            DocumentBuilder db = docBuilderFactory.newDocumentBuilder();
            Document doc = db.newDocument();
            Element root = doc.createElement("parts");
            doc.appendChild(root);
            for(IncludePartInfo partInfo: infoMap.values()) {
                Element elem = doc.createElement("part");
                root.appendChild(elem);
                elem.setAttribute("name", partInfo.getName());
                if(partInfo.isRender()) {
                    elem.setAttribute("render", "true");
                }
                if(partInfo.isContextual()) {
                    elem.setAttribute("contextual", "true");
                }
                if(partInfo.getContentType() != null) {
                    elem.setAttribute("content-type", partInfo.getContentType());
                }
            }
            return Xml.parse(context.getXsltVersion(), doc);
            
        } catch (Exception x) {
            ExtensionFunctionUtils.setExtensionFunctionError(x);
            throw x;
        }
    }
    
    private static ConcurrentMap<Object, Object> getExtensionCacheMap(XsltContext context, TargetGenerator targetgen) {
        String mapKey = context.getStylesheetSystemId();
        SPCache<String, ConcurrentMap<Object, Object>> extCache = targetgen.getCacheFactory().getExtensionCache();
        ConcurrentMap<Object, Object> cache = extCache.getValue(mapKey);
        if(cache == null) {
            cache = new ConcurrentHashMap<Object, Object>();
            extCache.setValue(mapKey, cache);
        }
        return cache;
    }

    public static String getMessage(TargetGenerator targetGen, String key, String lang, 
            Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) throws Exception {
        try {
            Locale locale = LocaleUtils.getLocale(lang);
            Object[] args = new Object[] {arg1, arg2, arg3, arg4, arg5};
            try {
                return targetGen.getMessageSource().getMessage(key, args, locale);
            } catch(NoSuchMessageException x) {
                LOG.warn("Can't resolve message key '" + key + "'.");
                if("prod".equals(EnvironmentProperties.getProperties().getProperty("mode"))) {
                    return "";
                } else {
                    return "[MESSAGE NOT FOUND: " + key + "]";
                }
            }
        } catch (Exception x) {
            ExtensionFunctionUtils.setExtensionFunctionError(x);
            throw x;
        }
    }

    public static boolean messageExists(TargetGenerator targetGen, String key, String lang) {
        try {
            Locale locale = LocaleUtils.getLocale(lang);
            try {
                targetGen.getMessageSource().getMessage(key, null, locale);
                return true;
            } catch(NoSuchMessageException x) {
                return false;
            }
        } catch (Exception x) {
            ExtensionFunctionUtils.setExtensionFunctionError(x);
            throw x;
        }
    }

}
