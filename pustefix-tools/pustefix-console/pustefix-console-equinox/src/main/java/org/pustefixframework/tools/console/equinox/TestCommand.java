package org.pustefixframework.tools.console.equinox;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

public class TestCommand implements CommandProvider {

	private final static String HELP = "---Pustefix commands---\n\text - Get extension points";
	
	private BundleContext bundleContext;
	
	public TestCommand(BundleContext bundleContext) {
		this.bundleContext = bundleContext;
	}
	
	public String getHelp() {
		return HELP;
	}
	
	public void _ext(CommandInterpreter interpreter) {
		try {
			ServiceReference[] refs = bundleContext.getServiceReferences("org.pustefixframework.extension.ExtensionPoint", null);
			for(ServiceReference ref: refs) {
				String[] propKeys = ref.getPropertyKeys();
				for(String propKey: propKeys) {
					System.out.println(propKey + ": " + ref.getProperty(propKey));
				}
				String[] objectClasses = (String[])ref.getProperty("objectClass");
				for(String objectClass: objectClasses) {
					System.out.println(objectClass);
				}
			}
		} catch (InvalidSyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
