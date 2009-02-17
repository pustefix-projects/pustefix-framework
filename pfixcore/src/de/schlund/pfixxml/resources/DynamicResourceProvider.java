package de.schlund.pfixxml.resources;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import de.schlund.pfixcore.util.ModuleInfo;
import de.schlund.pfixxml.IncludeDocument;
import de.schlund.pfixxml.IncludeDocumentFactory;
import de.schlund.pfixxml.util.URIParameters;
import de.schlund.pfixxml.util.XPath;
import de.schlund.pfixxml.util.XsltProvider;

/**
 * 
 * @author mleidig@schlund.de
 *
 */
public class DynamicResourceProvider implements ResourceProvider {
    
    private Logger LOG = Logger.getLogger(DynamicResourceProvider.class);
    
    private static String DYNAMIC_SCHEME = "dynamic";
    private static String[] supportedSchemes = {DYNAMIC_SCHEME};
    
    public String[] getSupportedSchemes() {
        return supportedSchemes;
    }
    
    public Resource getResource(URI uri) throws ResourceProviderException {
        if(uri.getScheme()==null) 
            throw new ResourceProviderException("Missing URI scheme: "+uri);
        if(!uri.getScheme().equals(DYNAMIC_SCHEME)) 
            throw new ResourceProviderException("URI scheme not supported: "+uri);
        
        URIParameters params;
        try {
            params = new URIParameters(uri.getQuery(), "utf-8");
        } catch(Exception x) {
            throw new ResourceProviderException("Error reading URI parameters: "+uri.toString(), x);
        }
        String project = params.getParameter("project");
        if(project == null) throw new ResourceProviderException("Missing project URI parameter: "+uri.toString());
        String module = params.getParameter("module");
        String part = params.getParameter("part");
        String themes[] = null;
        if(params.getParameter("themes")!=null) {
            themes = params.getParameter("themes").split(",");
        }
        if(themes==null) themes = new String[] {""};
        
        //search in local project
        for(String theme:themes) {
            try {
                String uriPath = uri.getPath();
                uriPath = uriPath.replace("THEME", theme);
                URI  prjUri = new URI("pfixroot:/"+project+uriPath);
                if(LOG.isDebugEnabled()) LOG.debug("trying "+prjUri.toString());
                Resource resource = ResourceUtil.getResource(prjUri);
                if(resource.exists()) {
                    resource.setOriginatingURI(uri);
                    if(part == null) return resource;
                    if(containsPart(resource, part)) return resource;
                }
            } catch(URISyntaxException x) {
                throw new ResourceProviderException("Error while searching project resource: " + uri, x);
            }
        }
            
        //search in common
        for(String theme:themes) {
            try {
                String uriPath = uri.getPath();
                uriPath = uriPath.replace("THEME", theme);
                URI  prjUri = new URI("pfixroot:/"+"common"+uriPath);
                if(LOG.isDebugEnabled()) LOG.debug("trying "+prjUri.toString());
                Resource resource = ResourceUtil.getFileResource(prjUri);
                resource.setOriginatingURI(uri);
                if(resource.exists()) {
                    if(part == null) return resource;
                    if(containsPart(resource, part)) return resource;
                } else if(module == null) {
                    return resource;
                }
            } catch(URISyntaxException x) {
                throw new ResourceProviderException("Error while searching common resource: " + uri, x);
            }
        }
        
        ModuleInfo moduleInfo = ModuleInfo.getInstance();
        String path = uri.getPath();
        if(path.startsWith("/")) path = path.substring(1);
        List<String> overMods = moduleInfo.getOverridingModules(module, path);
        if(overMods.size()>1) {
            LOG.warn("Multiple modules found which override resource '"+path+"' from module '"+module+"'.");
        }
        
        //search in overriding modules
        for(String theme:themes) {
            for(String overMod:overMods) {
                try {
                    String uriPath = uri.getPath();
                    uriPath = uriPath.replace("THEME", theme);
                    URI modUri = new URI("module://" + overMod + uriPath);
                    if(LOG.isDebugEnabled()) LOG.debug("trying "+modUri.toString());
                    Resource resource = ResourceUtil.getResource(modUri);
                    if(resource.exists()) {
                        resource.setOriginatingURI(uri);
                        if(part==null) return resource;
                        if(containsPart(resource, part)) return resource;
                    }
                } catch(URISyntaxException x) {
                    throw new ResourceProviderException("Error while searching overrided module resource: " + uri, x);
                }
            }
        }
        
        //use resource from specified module
        for(String theme:themes) {
            try {
                String uriPath = uri.getPath();
                uriPath = uriPath.replace("THEME", theme);
                URI modUri = new URI("module://" + module + uriPath);
                if(LOG.isDebugEnabled()) LOG.debug("trying "+modUri.toString());
                Resource resource = ResourceUtil.getResource(modUri);
                if(resource.exists()) {
                    resource.setOriginatingURI(uri);
                    return resource;
                }
            } catch(URISyntaxException x) {
                throw new ResourceProviderException("Error while getting module resource: " + uri, x);
            }
        }
        
        //Return non-existing project resource if search failed
        try {
            URI  prjUri = new URI("pfixroot:/"+project+uri.getPath());
            Resource resource = ResourceUtil.getResource(prjUri);
            return resource;
        } catch(URISyntaxException x) {
            throw new ResourceProviderException("Error while getting project resource: " + uri, x);
        }
        
    }
 
    private boolean containsPart(Resource res, String part) throws ResourceProviderException {
        try {
            IncludeDocument incDoc = IncludeDocumentFactory.getInstance().getIncludeDocument(XsltProvider.getPreferredXsltVersion(), res, false);
            Document doc = incDoc.getDocument();
            List<Node> ns = XPath.select(doc, "/include_parts/part[@name='" + part + "']");
            if(ns.size()>0) return true;
            return false;
        } catch (Exception x) {
            throw new ResourceProviderException("Error while searching part in document: " + res.toURI(), x);
        }
    }
    
}
