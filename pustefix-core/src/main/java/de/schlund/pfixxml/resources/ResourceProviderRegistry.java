package de.schlund.pfixxml.resources;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author mleidig@schlund.de
 *  
 */
public class ResourceProviderRegistry {
    
    private static Map<String, ResourceProvider> resourceProviders = new HashMap<String, ResourceProvider>();
    
    static {
        registerDefaultResourceProviders();
    }
    
    private static void registerDefaultResourceProviders() {
        //DocrootResourceProvider gets dynamically registered by GlobalConfig
        register(new ModuleResourceProvider());
        register(new DynamicResourceProvider());
    }
    
    public static void reset() {
        resourceProviders.clear();
        registerDefaultResourceProviders();
    }
    
    public static void register(ResourceProvider resourceProvider) {
        String[] schemes = resourceProvider.getSupportedSchemes();
        for(String scheme : schemes) {
            if(resourceProviders.containsKey(scheme)) 
                throw new RuntimeException("ResourceProvider for scheme '" + scheme + "' is already registered.");
            resourceProviders.put(scheme, resourceProvider);
        }
    }
    
    public static ResourceProvider getResourceProvider(String scheme) {
        return resourceProviders.get(scheme);
    }

}
