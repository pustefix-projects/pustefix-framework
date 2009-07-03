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

package org.pustefixframework.resource;

import java.lang.reflect.Array;
import java.net.URI;
import java.util.ArrayList;
import java.util.Map;


/**
 * Abstract implementation of {@link ResourceLoader}, implementing most methods.  
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public abstract class AbstractResourceLoader implements ResourceLoader {

    public Resource getResource(URI uri) {
        return getResource(uri, (Map<String, ?>) null);
    }

    public Resource getResource(URI uri, Map<String, ?> parameters) {
        Resource[] resources = getResources(uri, parameters);
        if (resources != null) {
            return resources[0];
        } else {
            return null;
        }
    }

    public Resource[] getResources(URI uri) {
        return getResources(uri, (Map<String, ?>) null);
    }

    public <T extends Resource> T getResource(URI uri, Class<? extends T> resourceClass) {
        Resource resource = getResource(uri);
        if (resource == null) {
            return null;
        }
        if (resourceClass.isAssignableFrom(resource.getClass())) {
            return resourceClass.cast(resource);
        }
        return null;
    }

    public <T extends Resource> T getResource(URI uri, Map<String, ?> parameters, Class<? extends T> resourceClass) {
        Resource resource = getResource(uri, parameters);
        if (resource == null) {
            return null;
        }
        if (resourceClass.isAssignableFrom(resource.getClass())) {
            return resourceClass.cast(resource);
        }
        return null;
    }

    public <T extends Resource> T[] getResources(URI uri, Class<? extends T> resourceClass) {
        return filterResourcesForClass(getResources(uri), resourceClass);
    }

    public <T extends Resource> T[] getResources(URI uri, Map<String, ?> parameters, Class<? extends T> resourceClass) {
        return filterResourcesForClass(getResources(uri, parameters), resourceClass);
    }
    
    protected <T extends Resource> T[] filterResourcesForClass(Resource[] resources, Class<? extends T> resourceClass) {
        if (resources == null) {
            return null;
        }
        ArrayList<T> filteredResources = new ArrayList<T>();
        for (int i = 0; i < resources.length; i++) {
            Resource resource = resources[i];
            if (resourceClass.isAssignableFrom(resource.getClass())) {
                filteredResources.add(resourceClass.cast(resource));
            }
        }
        if (filteredResources.size() == 0) {
            return null;
        }
        @SuppressWarnings("unchecked")
        T[] result = (T[]) Array.newInstance(resourceClass, filteredResources.size());
        filteredResources.toArray(result);
        return result;
    }
}
