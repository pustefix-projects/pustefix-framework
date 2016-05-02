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
    }
    
    public void addResource(String name, Object resource) {
        resourceMap.put(name, resource);
    }

}
