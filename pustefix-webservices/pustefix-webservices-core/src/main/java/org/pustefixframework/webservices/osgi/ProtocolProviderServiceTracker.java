package org.pustefixframework.webservices.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.pustefixframework.webservices.ProtocolProvider;
import org.pustefixframework.webservices.ProtocolProviderRegistry;
import org.pustefixframework.webservices.ProtocolProviderRegistryImpl;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * 
 * @author mleidig@schlund.de
 *
 */
public class ProtocolProviderServiceTracker extends ServiceTracker implements ProtocolProviderRegistry, InitializingBean, DisposableBean {

	private ProtocolProviderRegistryImpl registry;
	
	public ProtocolProviderServiceTracker(BundleContext bundleContext) {
		super(bundleContext, ProtocolProvider.class.getName(), null);
		registry = new ProtocolProviderRegistryImpl();
	}

	public void afterPropertiesSet() throws Exception {
		open();	
	}
	
	@Override
	public Object addingService(ServiceReference reference) {
		ProtocolProvider service = (ProtocolProvider)super.addingService(reference);
		registry.addProtocolProvider(service);
		return service;
	}
	
	@Override
	public void removedService(ServiceReference reference, Object service) {
		registry.removeProtocolProvider((ProtocolProvider)service);
		super.removedService(reference, service);
	}
	
	public ProtocolProvider getProtocolProvider(String protocolName, String protocolVersion) {
		return registry.getProtocolProvider(protocolName, protocolVersion);
	}
	
	public void destroy() throws Exception {
		close();
	}
	
}
