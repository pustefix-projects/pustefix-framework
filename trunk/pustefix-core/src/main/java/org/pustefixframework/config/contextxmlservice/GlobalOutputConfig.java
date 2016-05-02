package org.pustefixframework.config.contextxmlservice;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class GlobalOutputConfig {

    private Map<String, Object> resources = new LinkedHashMap<String, Object>();
    private Set<String> lazyResources = new HashSet<String>();
    
    public void addContextResource(String node, Object resource, boolean lazy) {
        resources.put(node, resource);
        if(lazy) {
            lazyResources.add(node);
        }
    }
    
    public boolean containsNode(String node) {
        return resources.containsKey(node);
    }
    
    public Map<String, Object> getContextResources() {
        return resources;
    }
    
    public Set<String> getLazyContextResources() {
        return lazyResources;
    }
    
    public boolean isLazy(String node) {
        return lazyResources.contains(node);
    }
    
}
