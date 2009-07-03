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

package org.pustefixframework.container.spring.beans.internal;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.pustefixframework.resource.AbstractResourceLoader;
import org.pustefixframework.resource.Resource;
import org.pustefixframework.resource.ResourceProvider;
import org.pustefixframework.resource.ResourceSelector;


/**
 * OSGi-aware resource loader implementation. Uses the bundle context
 * to get resources from this bundle and other bundles using the "bundle:"
 * scheme. Resource providers and resource selectors registered as OSGi
 * services are automatically detected and used by this implementation.  
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class BundleResourceLoader extends AbstractResourceLoader {
    
    private ResourceProvider bundleResourceProvider;
    private LinkedHashSet<ResourceSelector> resourceSelectors = new LinkedHashSet<ResourceSelector>();
    private LinkedHashSet<ResourceProvider> resourceProviders = new LinkedHashSet<ResourceProvider>();
    private LinkedHashMap<String, ResourceProvider> resourceProviderMap = new LinkedHashMap<String, ResourceProvider>();
    private ResourceProviderServiceTracker resourceProviderServiceTracker;
    private ResourceSelectorServiceTracker resourceSelectorServiceTracker;
    
    public BundleResourceLoader(BundleContext bundleContext) {
        this.bundleResourceProvider = new BundleResourceProvider(bundleContext);
        this.resourceProviderServiceTracker = new ResourceProviderServiceTracker(bundleContext);
        resourceProviderServiceTracker.open(true);
        this.resourceSelectorServiceTracker = new ResourceSelectorServiceTracker(bundleContext);
        resourceSelectorServiceTracker.open();
    }
    
    public Resource[] getResources(URI uri, Map<String, ?> parameters) {

        Resource[] foundResources = findResources(uri);
        if (foundResources == null) {
            return null;
        }
        Resource[] filteredResources = filterResources(foundResources, parameters);
        return filteredResources;
    }

    private Resource[] filterResources(Resource[] resources, Map<String, ?> parameters) {
        LinkedList<ResourceSelector> resourceSelectors = new LinkedList<ResourceSelector>();
        synchronized (this.resourceSelectors) {
            resourceSelectors.addAll(this.resourceSelectors);
        }
        for (ResourceSelector resourceSelector : resourceSelectors) {
            resources = resourceSelector.selectResources(resources, parameters);
        }
        return resources;
    }

    private Resource[] findResources(URI uri) {
        String scheme = uri.getScheme();
        if (scheme == null) {
            return null;
        }
        if (scheme.equals("bundle")) {
            return bundleResourceProvider.getResources(uri, null);
        }
        ResourceProvider resourceProvider;
        synchronized (resourceProviderMap) {
            resourceProvider = resourceProviderMap.get(scheme);
        }
        if (resourceProvider == null) {
            return null;
        }
        return resourceProvider.getResources(uri, null);
    }
    
    private void updateResourceProviderMap() {
        synchronized (resourceProviders) {
            synchronized (resourceProviderMap) {
                resourceProviderMap.clear();
                for (ResourceProvider resourceProvider : resourceProviders) {
                    String[] supportedSchemes = resourceProvider.getSchemes();
                    for (int i = 0; i < supportedSchemes.length; i++) {
                        String scheme = supportedSchemes[i];
                        resourceProviderMap.put(scheme, resourceProvider);
                    }
                }
            }
        }
    }
    
    @Override
    protected void finalize() throws Throwable {
        resourceProviderServiceTracker.close();
        resourceSelectorServiceTracker.close();
        super.finalize();
    }

    private class ResourceProviderServiceTracker extends ServiceTracker {
        ResourceProviderServiceTracker(BundleContext bundleContext) {
            super(bundleContext, ResourceProvider.class.getName(), null);
        }

        @Override
        public Object addingService(ServiceReference reference) {
            ResourceProvider resourceProvider = (ResourceProvider) context.getService(reference);
            synchronized (resourceProviders) {
                resourceProviders.add(resourceProvider);
                updateResourceProviderMap();
            }
            return resourceProvider;
        }

        @Override
        public void removedService(ServiceReference reference, Object service) {
            ResourceProvider resourceProvider = (ResourceProvider) service;
            synchronized (resourceProviders) {
                resourceProviders.remove(resourceProvider);
                updateResourceProviderMap();
            }
            super.removedService(reference, service);
        }
    }
    
    private class ResourceSelectorServiceTracker extends ServiceTracker {
        ResourceSelectorServiceTracker(BundleContext bundleContext) {
            super(bundleContext, ResourceSelector.class.getName(), null);
        }

        @Override
        public Object addingService(ServiceReference reference) {
            ResourceSelector resourceSelector = (ResourceSelector) context.getService(reference);
            synchronized (resourceSelector) {
                resourceSelectors.add(resourceSelector);
            }
            return resourceSelector;
        }

        @Override
        public void removedService(ServiceReference reference, Object service) {
            ResourceSelector resourceSelector = (ResourceSelector) service;
            synchronized (resourceSelector) {
                resourceSelectors.remove(resourceSelector);
            }
            super.removedService(reference, service);
        }
    }
}
