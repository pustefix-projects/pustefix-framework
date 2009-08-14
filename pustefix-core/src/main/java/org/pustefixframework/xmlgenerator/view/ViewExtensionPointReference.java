package org.pustefixframework.xmlgenerator.view;

import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

/**
 * This class manages retrieving and updating ViewExtensionPoint services.
 * 
 * @author mleidig@schlund.de
 *
 */
public class ViewExtensionPointReference extends ServiceTracker {

	private Log logger = LogFactory.getLog(ViewExtensionPointReference.class);
	
	private ViewExtensionResolver resolver;
	
	private ViewExtensionPointImpl extensionPoint;
	
	private String extensionPointId;
	private String extensionPointVersion;
	
	public ViewExtensionPointReference(BundleContext bundleContext, ViewExtensionResolver resolver,
			String extensionPointId, String extensionPointVersion) {
		super(bundleContext, createFilter(bundleContext, extensionPointId, extensionPointVersion), null);
		this.resolver = resolver;
		this.extensionPointId = extensionPointId;
		this.extensionPointVersion = extensionPointVersion;
	}
	
	public String getExtensionPointId() {
		return extensionPointId;
	}
	
	public String getExtensionPointVersion() {
		return extensionPointVersion;
	}
	
	private static Filter createFilter(BundleContext bundleContext, String extensionPointId, String extensionPointVersion) {
		String filter = 
			"(&(objectclass=" + ViewExtensionPoint.class.getName() + ")" +
			"(extension-point=" + extensionPointId + ")" +
			"(type=" + ViewExtensionPoint.TYPE + ")" +
			"(version=" + extensionPointVersion + "))";
		try {
			return bundleContext.createFilter(filter);
		} catch (InvalidSyntaxException x) {
			throw new RuntimeException("Error in service filter expression: " + filter, x);
		} 
	}
	
	@Override
	public Object addingService(ServiceReference reference) {
		if(extensionPoint == null) {
			extensionPoint = (ViewExtensionPointImpl)super.addingService(reference);
		} else {
			logger.warn("Matching extension point already registered. Ignoring newly added instance.");
		}
		return extensionPoint;
	}
	
	@Override
	public void removedService(ServiceReference reference, Object service) {
		if(extensionPoint == service) {
			extensionPoint = null;
			resolver.invalidate(this);
		}
		super.removedService(reference, service);
	}
	
	public Collection<ViewExtension> getExtensions() {
		if(extensionPoint != null) {
			return extensionPoint.getExtensions();
		}
		return null;
	}
	
}
