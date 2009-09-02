package org.pustefixframework.container.spring.beans.internal;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.osgi.context.BundleContextAware;

/**
 * BundleSourceLocatorRegistry implementation collecting all BundleSourceLocators
 * by implementing a ServiceTracker getting them from the service registry.
 * 
 * @author mleidig@schlund.de
 *
 */
public class BundleSourceLocatorRegistryImpl implements BundleSourceLocatorRegistry, BundleContextAware, InitializingBean, DisposableBean {

	private BundleContext bundleContext;
	private List<BundleSourceLocator> bundleSourceLocators = new ArrayList<BundleSourceLocator>();
	private BundleSourceLocatorTracker locatorTracker;
	
	public void setBundleContext(BundleContext bundleContext) {
		this.bundleContext = bundleContext;
	}
	
	public void afterPropertiesSet() throws Exception {
		locatorTracker = new BundleSourceLocatorTracker(bundleContext);
		locatorTracker.open();
	}
	
	public void destroy() throws Exception {
		locatorTracker.close();	
	}
	
	public File getSourceLocation(String bundleSymbolicName, String bundleVersion) {
		BundleSourceLocator[] locators;
		synchronized(bundleSourceLocators) {
			locators = new BundleSourceLocator[bundleSourceLocators.size()];
			bundleSourceLocators.toArray(locators);
		}
		for(BundleSourceLocator locator: locators) {
			File file = locator.getSourceLocation(bundleSymbolicName, bundleVersion);
			if(file != null) return file;
		}
		return null;
	}
	
    private class BundleSourceLocatorTracker extends ServiceTracker {
    	
    	BundleSourceLocatorTracker(BundleContext bundleContext) {
    		super(bundleContext, BundleSourceLocator.class.getName(), null);
    	}
  
    	@Override
    	public Object addingService(ServiceReference reference) {
    		BundleSourceLocator locator = (BundleSourceLocator)super.addingService(reference);
    		synchronized(bundleSourceLocators) {
    			bundleSourceLocators.add(locator);
    		}
    		return locator;
    	}
    	
    	@Override
    	public void removedService(ServiceReference reference, Object service) {
    		BundleSourceLocator locator = (BundleSourceLocator)service;
    		synchronized(bundleSourceLocators) {
    			bundleSourceLocators.remove(locator);
    		}
    		super.removedService(reference, service);
    	}
    	
    }

}
