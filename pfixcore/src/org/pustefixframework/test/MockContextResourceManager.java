package org.pustefixframework.test;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import de.schlund.pfixcore.workflow.ContextResourceManager;

/**
 * Mock the ContextResourceManager for unit tests.
 * Provides methods to programmatically add ContextResource objects.
 * 
 * @author mleidig@schlund.de
 *
 */
public class MockContextResourceManager implements ContextResourceManager {

    Map<String, Object> resourceMap = new HashMap<String, Object>();
    
    @SuppressWarnings("unchecked")
    public <T> T getResource(Class<T> clazz) {
        return (T)resourceMap.get(clazz.getName());
    }

    public Object getResource(String name) {
        return resourceMap.get(name);
    }

    public Iterator<Object> getResourceIterator() {
        return resourceMap.values().iterator();
    }
    
    public void addResource(Object resource) {
        resourceMap.put(resource.getClass().getName(), resource);
    }
    
    public void addResource(Class<?> itf, Object resource) {
        resourceMap.put(itf.getName(), resource);
        System.out.println("put "+itf.getName());
    }
    
    public void addResource(String name, Object resource) {
        resourceMap.put(name, resource);
    }

}
