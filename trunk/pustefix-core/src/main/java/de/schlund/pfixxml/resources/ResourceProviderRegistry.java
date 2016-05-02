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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;

import org.apache.log4j.Logger;

/**
 * 
 * @author mleidig@schlund.de
 *  
 */
public class ResourceProviderRegistry {
    
    private static Logger LOG = Logger.getLogger(ResourceProviderRegistry.class);
    
    private static Map<String, ResourceProvider> resourceProviders = new HashMap<String, ResourceProvider>();
    
    static {
        registerDefaultResourceProviders();
    }
    
    private static void registerDefaultResourceProviders() {
        //DocrootResourceProvider gets dynamically registered by GlobalConfig
        ServiceLoader<ResourceProvider> loader = ServiceLoader.load(ResourceProvider.class);
        Iterator<ResourceProvider> resourceProviders = loader.iterator();
        while(resourceProviders.hasNext()) {
            ResourceProvider resourceProvider = resourceProviders.next();
            register(resourceProvider);
        }
    }
    
    public static void reset() {
        resourceProviders.clear();
        registerDefaultResourceProviders();
    }
    
    public static void register(ResourceProvider resourceProvider) {
        String[] schemes = resourceProvider.getSupportedSchemes();
        for(String scheme : schemes) {
            if(resourceProviders.containsKey(scheme)) 
                LOG.warn("ResourceProvider for scheme '" + scheme + "' is already registered.");
            resourceProviders.put(scheme, resourceProvider);
        }
    }
    
    public static ResourceProvider getResourceProvider(String scheme) {
        return resourceProviders.get(scheme);
    }

}
