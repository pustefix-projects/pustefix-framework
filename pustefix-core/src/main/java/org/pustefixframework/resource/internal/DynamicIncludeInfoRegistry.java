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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.osgi.context.BundleContextAware;


public class DynamicIncludeInfoRegistry implements BundleContextAware, InitializingBean, DisposableBean {
    
    private BundleContext bundleContext;
    private ServiceTracker serviceTracker;
	private SortedMap<String, DynamicIncludeInfo> nameToInfo = new TreeMap<String, DynamicIncludeInfo>();
    private SortedSet<DynamicIncludeInfo> dynamicSearchChain = new TreeSet<DynamicIncludeInfo>(new DynamicSearchComparator());
	
    public void setBundleContext(BundleContext bundleContext) {
    	this.bundleContext = bundleContext;
    }
    
    public void afterPropertiesSet() throws Exception {
    	serviceTracker = new DynamicIncludeInfoServiceTracker(bundleContext);
    	serviceTracker.open();
    }
    
    public void destroy() throws Exception {
    	serviceTracker.close();
    }
    
    public List<String> getDynamicSearchChain() {
    	List<String> modules = new ArrayList<String>();
    	synchronized(dynamicSearchChain) {
    		for(DynamicIncludeInfo dynInfo: dynamicSearchChain) modules.add(dynInfo.getModuleName());
    	}
    	return modules;
    }
    
    public DynamicIncludeInfo getDynamicIncludeInfo(String moduleName) {
    	synchronized(nameToInfo) {
    		return nameToInfo.get(moduleName);
    	}
    }
    
    public Set<String> getModules() {
    	synchronized(nameToInfo) {
    		return nameToInfo.keySet();
    	}
    }
    
    public List<String> getOverridingModules(String moduleName, String resourcePath) {
        List<String> modules = new ArrayList<String>();
        getOverridingModules(moduleName, resourcePath, modules);
        return modules;
    }
    
    private void getOverridingModules(String moduleName, String resourcePath, List<String> modules) {
    	synchronized(nameToInfo) {
	        for(DynamicIncludeInfo moduleDesc: nameToInfo.values()) {
	            if(moduleDesc.overridesResource(moduleName, resourcePath)) {
	                if(!modules.contains(moduleDesc.getModuleName())) {
	                    modules.add(0, moduleDesc.getModuleName());
	                    getOverridingModules(moduleDesc.getModuleName(), resourcePath, modules);
	                }
	            }
	        }
    	}
    }
    
    @Override
    public String toString() {
    	synchronized(nameToInfo) {
	    	StringBuilder sb = new StringBuilder();
	    	sb.append("Module information - ");
	    	synchronized(nameToInfo) {
	    		int no = nameToInfo.values().size();
	    		sb.append("Detected " + no + " module" + (no==1?"":"s") + " [");
	    		for(DynamicIncludeInfo moduleDesc: nameToInfo.values()) {
	    			sb.append(moduleDesc.getModuleName() + " ");
	    		}
	    		if(no>0) sb.deleteCharAt(sb.length()-1);
	    		sb.append("]");
	    	}
	    	synchronized(dynamicSearchChain) {
	    		sb.append(" Dynamic search chain:");
	    		for(DynamicIncludeInfo module: dynamicSearchChain) {
	    			sb.append(" " + module.getModuleName());
	    		}
	    	}
	    	return sb.toString();
    	}
    }
    
   
    private class DynamicIncludeInfoServiceTracker extends ServiceTracker {
    
    	public DynamicIncludeInfoServiceTracker(BundleContext bundleContext) {
    		super(bundleContext, DynamicIncludeInfo.class.getName(), null);
    	}
    	
        @Override
        public Object addingService(ServiceReference reference) {
        	DynamicIncludeInfo dynInfo = (DynamicIncludeInfo)super.addingService(reference);
        	synchronized(nameToInfo) {
        		nameToInfo.put(dynInfo.getModuleName(), dynInfo);
        		if(dynInfo.getDynamicSearchLevel()>-1) dynamicSearchChain.add(dynInfo);
        	}
        	return dynInfo;
        }
        
        @Override
        public void removedService(ServiceReference reference, Object service) {
        	DynamicIncludeInfo dynInfo = (DynamicIncludeInfo)service;
        	synchronized(nameToInfo) {
        		nameToInfo.remove(dynInfo.getModuleName());
        		dynamicSearchChain.remove(dynInfo);
        	}
        	super.removedService(reference, service);
        }
    	
    }

    
    private class DynamicSearchComparator implements Comparator<DynamicIncludeInfo> {
    	
    	public int compare(DynamicIncludeInfo m1, DynamicIncludeInfo m2) {
    		if(m1.getDynamicSearchLevel() == m2.getDynamicSearchLevel()) {
    			return m1.getModuleName().compareTo(m2.getModuleName());
    		} else {
    			return m1.getDynamicSearchLevel() - m2.getDynamicSearchLevel();
    		}
    	}
    	
    }
    
}
