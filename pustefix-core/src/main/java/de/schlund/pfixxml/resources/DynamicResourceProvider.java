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
package de.schlund.pfixxml.resources;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.log4j.Logger;

import de.schlund.pfixcore.util.ModuleInfo;
import de.schlund.pfixxml.IncludePartsInfoFactory;
import de.schlund.pfixxml.IncludePartsInfoParsingException;
import de.schlund.pfixxml.util.URIParameters;

/**
 * 
 * @author mleidig@schlund.de
 *
 */
public class DynamicResourceProvider implements ResourceProvider {
    
    private Logger LOG = Logger.getLogger(DynamicResourceProvider.class);
    
    private final static String DYNAMIC_SCHEME = "dynamic";
    private final static String[] supportedSchemes = {DYNAMIC_SCHEME};
    
    private IncludePartsInfoFactory incInfo = new IncludePartsInfoFactory();
    
    public String[] getSupportedSchemes() {
        return supportedSchemes;
    }
    
    public Resource getResource(URI uri) throws ResourceProviderException {
       return getResource(uri, null);
    }
        
    public Resource getResource(URI uri, DynamicResourceInfo info) throws ResourceProviderException {
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
        String module = params.getParameter("module");
        if(module == null) module = uri.getAuthority();
        String part = params.getParameter("part");
        String themes[] = null;
        if(params.getParameter("themes")!=null) {
            themes = params.getParameter("themes").split(",");
        }
        if(themes==null) themes = new String[] {""};
        
        Resource infoRes = null;
        
        //search in local project
        for(String theme:themes) {
            try {
                String uriPath = uri.getPath();
                uriPath = uriPath.replace("THEME", theme);
                URI  prjUri = new URI("docroot:"+uriPath);
                if(LOG.isDebugEnabled()) LOG.debug("trying "+prjUri.toString());
                Resource resource = ResourceUtil.getResource(prjUri);
                if(resource.exists()) {
                    resource.setOriginatingURI(uri);
                    if(part == null) {
                        if(info == null) return resource;
                        else {
                            if(infoRes == null) infoRes = resource;
                            info.addEntry("webapp", true, false);
                        }
                    } else if(containsPart(resource, part)) {
                        if(info == null) return resource;
                        else {
                            if(infoRes == null) infoRes = resource;
                            info.addEntry("webapp", true, true);
                        }
                    } else if(info != null) info.addEntry("webapp", true, false);
                } else if(info != null) info.addEntry("webapp", false, false);
            } catch(URISyntaxException x) {
                throw new ResourceProviderException("Error while searching project resource: " + uri, x);
            }
        }
        
        ModuleInfo moduleInfo = ModuleInfo.getInstance();
        String path = uri.getPath();
        if(path.startsWith("/")) path = path.substring(1);
        
        //search in defaultSearchModules
        List<String> defaultSearchModules = moduleInfo.getDefaultSearchModules();
        for(String theme:themes) {
            for(String defaultSearchModule: defaultSearchModules) {
                try {
                    String uriPath = uri.getPath();
                    uriPath = uriPath.replace("THEME", theme);
                    URI modUri = new URI("module://" + defaultSearchModule + uriPath);
                    if(LOG.isDebugEnabled()) LOG.debug("trying "+modUri.toString());
                    Resource resource = ResourceUtil.getResource(modUri);
                    if(resource.exists()) {
                        resource.setOriginatingURI(uri);
                        if(part==null) {
                            if(info == null) return resource;
                            else {
                                if(infoRes == null) infoRes = resource;
                                info.addEntry(defaultSearchModule, true, false);
                            }
                        } else if(containsPart(resource, part)) {
                            if(info == null) return resource;
                            else {
                                if(infoRes == null) infoRes = resource;
                                info.addEntry(defaultSearchModule, true,true);
                            }
                        } else if(info != null) info.addEntry(defaultSearchModule, true, false);
                    } else if(info != null) info.addEntry(defaultSearchModule, false, false);
                } catch(URISyntaxException x) {
                    throw new ResourceProviderException("Error while searching defaultsearch module resource: " + uri, x);
                }
            }
        }
        
        if(module != null) {

            //search in overriding modules
            List<String> overMods = moduleInfo.getOverridingModules(module, path);
            if(overMods.size()>1) {
                LOG.warn("Multiple modules found which override resource '"+path+"' from module '"+module+"'.");
            }
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
                            if(part==null) {
                                if(info == null) return resource;
                                else {
                                    if(infoRes == null) infoRes = resource;
                                    info.addEntry(overMod, true, false);
                                }
                            } else if(containsPart(resource, part)) {
                                if(info == null) return resource;
                                else {
                                    if(infoRes == null) infoRes = resource;
                                    info.addEntry(overMod, true, true);
                                }
                            } else if(info != null) info.addEntry(overMod, true, false);
                        } else if(info != null) info.addEntry(overMod, false, false);
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
                        if(info == null) return resource;
                        else {
                            if(infoRes == null) infoRes = resource;
                            if(part != null && containsPart(resource, part)) info.addEntry(module, true, true);
                            else info.addEntry(module, true, false);
                        }
                    } else if(info != null) info.addEntry(module, false, false);
                } catch(URISyntaxException x) {
                    throw new ResourceProviderException("Error while getting module resource: " + uri, x);
                }
            }
        
        }
        
        if(infoRes != null) return infoRes;
        
        //Return non-existing project resource if search failed
        try {
            URI  prjUri = new URI("docroot:"+uri.getPath());
            Resource resource = ResourceUtil.getResource(prjUri);
            return resource;
        } catch(URISyntaxException x) {
            throw new ResourceProviderException("Error while getting project resource: " + uri, x);
        }
        
    }
 
    private boolean containsPart(Resource res, String part) throws ResourceProviderException {
        try {
            return incInfo.containsPart(res, part);
        } catch(IncludePartsInfoParsingException x) {
            LOG.warn("Checking existence of part '" + part + "' in resource '" + res.toURI().toString() + "' failed.", x);
            return false;
        }
    }
    
}
