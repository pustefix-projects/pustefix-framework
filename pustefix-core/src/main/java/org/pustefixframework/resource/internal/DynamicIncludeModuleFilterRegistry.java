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

import java.util.HashMap;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.osgi.context.BundleContextAware;


public class DynamicIncludeModuleFilterRegistry implements BundleContextAware, InitializingBean, DisposableBean {
    
    private BundleContext bundleContext;
    private ServiceTracker serviceTracker;
	private Map<String, DynamicIncludeModuleFilter> nameToFilter = new HashMap<String, DynamicIncludeModuleFilter>();
	
    public void setBundleContext(BundleContext bundleContext) {
    	this.bundleContext = bundleContext;
    }
    
    public void afterPropertiesSet() throws Exception {
    	serviceTracker = new DynamicIncludeModuleFilterServiceTracker(bundleContext);
    	serviceTracker.open();
    }
    
    public void destroy() throws Exception {
    	serviceTracker.close();
    }
    
    public DynamicIncludeModuleFilter getDynamicIncludeModuleFilter(String application) {
    	synchronized(nameToFilter) {
    		return nameToFilter.get(application);
    	}
    }
    
   
    private class DynamicIncludeModuleFilterServiceTracker extends ServiceTracker {
    
    	public DynamicIncludeModuleFilterServiceTracker(BundleContext bundleContext) {
    		super(bundleContext, DynamicIncludeModuleFilter.class.getName(), null);
    	}
    	
        @Override
        public Object addingService(ServiceReference reference) {
        	DynamicIncludeModuleFilter filter = (DynamicIncludeModuleFilter)super.addingService(reference);
        	synchronized(nameToFilter) {
        		nameToFilter.put(filter.getApplication(), filter);
        	}
        	return filter;
        }
        
        @Override
        public void removedService(ServiceReference reference, Object service) {
        	DynamicIncludeModuleFilter filter = (DynamicIncludeModuleFilter)service;
        	synchronized(nameToFilter) {
        		nameToFilter.remove(filter.getApplication());
        	}
        	super.removedService(reference, service);
        }
    	
    }
    
}
