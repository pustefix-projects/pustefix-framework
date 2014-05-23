package org.pustefixframework.config.contextxmlservice;

import java.util.LinkedHashMap;
import java.util.Map;

public class GlobalOutputConfig {

	private Map<String, Object> resources = new LinkedHashMap<String, Object>();
	
	public void addContextResource(String node, Object resource) {
        this.resources.put(node, resource);
    }
	
	public boolean containsNode(String node) {
		return resources.containsKey(node);
	}
	
	public Map<String, Object> getContextResources() {
		return resources;
	}
	
}
