package org.pustefixframework.tools.console.equinox;

import org.eclipse.osgi.framework.console.CommandProvider;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class Activator implements BundleActivator {

	private ServiceRegistration reg;
	
	public void start(BundleContext context) throws Exception {
		CommandProvider testCommand = new TestCommand(context);
		reg = context.registerService(CommandProvider.class.getName(), testCommand, null);
	}

	public void stop(BundleContext arg0) throws Exception {
		reg.unregister();
	}
	
}
