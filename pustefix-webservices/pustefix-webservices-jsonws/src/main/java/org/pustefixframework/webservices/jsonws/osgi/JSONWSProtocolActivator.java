package org.pustefixframework.webservices.jsonws.osgi;

import java.util.Dictionary;
import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.pustefixframework.webservices.ProtocolProvider;



public class JSONWSProtocolActivator implements BundleActivator{

	private ServiceRegistration registration;
	
	public void start(BundleContext context) throws Exception {
		ProtocolProvider provider = new JSONWSProtocolProvider();
		Dictionary<String,String> properties = new Hashtable<String,String>();
		properties.put("protocolName", provider.getProtocolName());
		properties.put("protocolVersion", provider.getProtocolVersion());
		registration = context.registerService(ProtocolProvider.class.getName(), provider, properties);
	}
	
	public void stop(BundleContext context) throws Exception {
		registration.unregister();
	}
	
}
