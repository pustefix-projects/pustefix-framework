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

package org.pustefixframework.resource.internal;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pustefixframework.resource.Resource;
import org.pustefixframework.resource.ResourceLoader;
import org.pustefixframework.resource.ResourceProvider;
import org.pustefixframework.resource.URLResource;
import org.pustefixframework.resource.support.DynamicResourceImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import de.schlund.pfixxml.util.URIParameters;
import de.schlund.pfixxml.util.XPath;
import de.schlund.pfixxml.util.Xml;
import de.schlund.pfixxml.util.XsltVersion;



public class DynamicResourceProvider implements ResourceProvider {

    private final static String[] SUPPORTED_SCHEMES = new String[] {"dynamic"};

    private Log logger = LogFactory.getLog(DynamicResourceProvider.class);
    
    private DynamicIncludeModuleFilterRegistry dynamicIncludeModuleFilterRegistry;
    private DynamicIncludeInfoRegistry dynamicIncludeInfoRegistry;
    
    public DynamicResourceProvider() {
    }
    
    public String[] getSchemes() {
        return SUPPORTED_SCHEMES;
    }
    
    public void setDynamicIncludeModuleFilterRegistry(DynamicIncludeModuleFilterRegistry dynamicIncludeModuleFilterRegistry) {
        this.dynamicIncludeModuleFilterRegistry = dynamicIncludeModuleFilterRegistry;
    }
    
    public void setDynamicIncludeInfoRegistry(DynamicIncludeInfoRegistry dynamicIncludeInfoRegistry) {
    	this.dynamicIncludeInfoRegistry = dynamicIncludeInfoRegistry;
    }

    public Resource[] getResources(URI uri, URI originallyRequestedURI, ResourceLoader resourceLoader) {
        if (uri.getScheme() == null || !uri.getScheme().equals("dynamic")) {
            throw new IllegalArgumentException("Cannot handle URI \"" + uri.toASCIIString() + "\": Scheme is not supported");
        }
        if (uri.getPath() == null || !uri.getPath().startsWith("/")) {
            throw new IllegalArgumentException("Error: URI \"" + uri.toASCIIString() + "\" does not specify an absolute path");
        }
        String module = uri.getAuthority();
        if(module == null) throw new IllegalArgumentException("Missing module name: " + uri.toASCIIString());
        
        Resource resource = getResource(uri, module, resourceLoader);
        Resource[] resources = new Resource[] {resource};
        return resources;
    }

 
    
    public Resource getResource(URI uri, String module, ResourceLoader resourceLoader) {
      
        URIParameters params;
        try {
            params = new URIParameters(uri.getQuery(), "utf-8");
        } catch(Exception x) {
            throw new RuntimeException("Error reading URI parameters: "+uri.toString(), x);
        }
        
        String application = params.getParameter("application");
        if(application == null) throw new IllegalArgumentException("Missing application URI parameter: "+uri.toString());
        
        String part = params.getParameter("part");
        
        String themes[] = null;
        if(params.getParameter("themes")!=null) {
            themes = params.getParameter("themes").split(",");
        }
        if(themes==null) themes = new String[] {""};

        //try find resource in application bundle
        for(String theme:themes) {
            try {
                String uriPath = uri.getPath();
                uriPath = uriPath.replace("THEME", theme);
                URI modUri = new URI("bundle://" + application + uriPath);
                if(logger.isDebugEnabled()) logger.debug("trying application bundle "+modUri.toString());
                URLResource resource = resourceLoader.getResource(modUri, URLResource.class);
                if(resource != null) {
                	DynamicResourceImpl dynRes = new DynamicResourceImpl(resource.getURI(), resource.getOriginalURI(), resource.getURL(), uri);
                	if(part == null || containsPart(resource, part)) return dynRes;
                }
            } catch(URISyntaxException x) {
                throw new IllegalArgumentException("Error while getting module resource: " + uri, x);
            }
        }
        
        //search in dynamic fallback chain
        List<String> chainMods = dynamicIncludeInfoRegistry.getDynamicSearchChain();
        for(String theme:themes) {
        	for(String chainMod: chainMods) {
        		try {
        			String uriPath = uri.getPath();
        			uriPath = uriPath.replace("THEME", theme);
        			URI modUri = new URI("bundle://" + chainMod + uriPath);
        			if(logger.isDebugEnabled()) logger.debug("trying fallback bundle "+modUri.toString());
        			URLResource resource = resourceLoader.getResource(modUri, URLResource.class);
        			if(resource != null) {
        				DynamicResourceImpl dynRes = new DynamicResourceImpl(resource.getURI(), resource.getOriginalURI(), resource.getURL(), uri);
        				if(part == null || containsPart(dynRes, part)) return dynRes;
        			}
        		} catch(URISyntaxException x) {
        			throw new IllegalArgumentException("Error while searching resource fallback chain: " + uri, x);
        		}
        	}
        }

        DynamicIncludeModuleFilter filter = dynamicIncludeModuleFilterRegistry.getDynamicIncludeModuleFilter(application);
        List<String> overMods = dynamicIncludeInfoRegistry.getOverridingModules(module, filter, uri.getPath());
        if(overMods.size()>1) {
            logger.warn("Multiple modules found which override resource '" + uri.getPath() + "' from module '" + module + "'.");
        }

        //search in overriding modules
        for(String theme:themes) {
            for(String overMod:overMods) {
                try {
                    String uriPath = uri.getPath();
                    uriPath = uriPath.replace("THEME", theme);
                    URI modUri = new URI("bundle://" + overMod + uriPath);
                    if(logger.isDebugEnabled()) logger.debug("trying overriding bundle "+modUri.toString());
                    URLResource resource = resourceLoader.getResource(modUri, URLResource.class);
                    if(resource != null) {
                    	DynamicResourceImpl dynRes = new DynamicResourceImpl(resource.getURI(), resource.getOriginalURI(), resource.getURL(), uri);
                    	if(part == null || containsPart(resource, part)) return dynRes;
                    }
                } catch(URISyntaxException x) {
                    throw new IllegalArgumentException("Error while searching resource from overriding modules: " + uri, x);
                }
            }
        }

        //use resource from specified module
        for(String theme:themes) {
            try {
                String uriPath = uri.getPath();
                uriPath = uriPath.replace("THEME", theme);
                URI modUri = new URI("bundle://" + module + uriPath);
                if(logger.isDebugEnabled()) logger.debug("trying module bundle "+modUri.toString());
                URLResource resource = resourceLoader.getResource(modUri, URLResource.class);
                if(resource != null) {
                	DynamicResourceImpl dynRes = new DynamicResourceImpl(resource.getURI(), resource.getOriginalURI(), resource.getURL(), uri);
                	if(part == null || containsPart(resource, part)) return dynRes;
                }
            } catch(URISyntaxException x) {
                throw new IllegalArgumentException("Error while getting module resource: " + uri, x);
            }
        }

        return null;

    }
       
    private boolean containsPart(URLResource resource, String part) {
    	//TODO: cache include documents
    	Document document;
    	try {
    		document = Xml.parse(XsltVersion.XSLT1, resource);
    	} catch (TransformerException x) {
          	throw new RuntimeException("Error reading include document: " + resource.getURI().toString(), x);
    	}
        try {
        	List<Node> ns = XPath.select(document, "/include_parts/part[@name='" + part + "']");
            if(ns.size()>0) return true;
            return false;
        } catch (Exception x) {
            throw new RuntimeException("Error while searching part in document: " + resource.getURI().toString(), x);
        }
    }


}
