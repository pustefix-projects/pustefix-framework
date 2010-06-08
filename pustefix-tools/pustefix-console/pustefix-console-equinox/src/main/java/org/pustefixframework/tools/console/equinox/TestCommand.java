package org.pustefixframework.tools.console.equinox;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

public class TestCommand implements CommandProvider {

	private final static String HELP = 
	    "---Pustefix commands---\n" +
	    "\text - Get extension points";
	
	private BundleContext bundleContext;
	
	public TestCommand(BundleContext bundleContext) {
		this.bundleContext = bundleContext;
	}
	
	public String getHelp() {
		return HELP;
	}
	
	public void _ext(CommandInterpreter interpreter) {
	    
	    Set<Long> bundleIds = new HashSet<Long>();
	    List<String> filters = new ArrayList<String>();

	    String arg = null;
	    while( (arg = interpreter.nextArgument()) != null) {
	        if(arg.startsWith("(") && arg.endsWith(")")) {
	            filters.add(arg);
	        } else if(arg.equals("application") || arg.equals("context") || arg.equals("direct") || arg.equals("xml")) {
	            filters.add("(type=" + arg + ".*)");
	        } else if(arg.startsWith("application.") || arg.startsWith("context.") || arg.startsWith("direct.") || arg.startsWith("xml.")) {
	            filters.add("(type=" + arg + ")");
	        } else {
	            try {
	                long bundleId = Long.parseLong(arg);
	                bundleIds.add(bundleId);
	            } catch(NumberFormatException x) {
	                interpreter.println("Warning: Argument '" + arg + "' is not supported. Ignoring it.");
	            }
	        }
	    }
	    
	    String filter = null;
	    if(filters.size() == 1) {
	        filter = filters.get(0);
	    } else if(filters.size() > 1) {
	        StringBuilder sb = new StringBuilder();
	        sb.append("(|");
	        for(String str: filters) sb.append(str);
	        sb.append(")");
	        filter = sb.toString();
	    }
	    
		try {
			ServiceReference[] refs = bundleContext.getServiceReferences("org.pustefixframework.extension.ExtensionPoint", filter);
			if(refs != null) {
			    for(ServiceReference ref: refs) {
			        Bundle bundle = ref.getBundle();
			        if(bundleIds.isEmpty() || bundleIds.contains(bundle.getBundleId())) {
			            interpreter.println("Extension-Point: " + ref.getProperty("extension-point"));
			            interpreter.println("Type: " + ref.getProperty("type"));
			            interpreter.println("Version: " + ref.getProperty("version"));
			            interpreter.println("Service-Id: " + ref.getProperty("service.id"));
			            interpreter.println("Bundle: " + getBundleInfo(bundle));
			            interpreter.println("Extended by:");
			            Bundle[] usingBundles = ref.getUsingBundles();
			            if(usingBundles != null) {
			                for(Bundle usingBundle: usingBundles) {
			                    interpreter.println("\t" + getBundleInfo(usingBundle));
			                }
			            }
			            interpreter.println();
			        }    
			    }
			}
		} catch (InvalidSyntaxException e) {
		    interpreter.printStackTrace(e);
		}
	}
	
	private String getBundleInfo(Bundle bundle) {
	    String name = bundle.getSymbolicName();
	    if(name == null) name = "unknown";
	    return (name + '_' + bundle.getVersion() + " [" + bundle.getBundleId() + "]");
    }
	
}
