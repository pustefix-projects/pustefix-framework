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

package org.pustefixframework.tools.console.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

/**
 * OSGi console command which displays Pustefix extension points.
 * Output can be filtered by bundle-id, filter expressions and
 * extension point types.
 * 
 * @author mleidig@schlund.de
 *
 */
public class ExtensionPointCommand implements ConsoleCommand {

	private BundleContext bundleContext;
	
	public ExtensionPointCommand(BundleContext bundleContext) {
		this.bundleContext = bundleContext;
	}
	
	public String getName() {
	    return "ext";
	}
	
	public String getUsage() {
	    return "ext [<bundle-id> ...] [<filter>] [xml|context|...] [<type>]";
	}
	
	public String getDescription() {
	    return "show Pustefix extension points";
	}
	
	public void execute(String[] args, ConsoleOutput out) {
	    
	    Set<Long> bundleIds = new HashSet<Long>();
	    List<String> filters = new ArrayList<String>();

	    for(String arg: args) {
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
	                out.println("Warning: Argument '" + arg + "' is not supported. Ignoring it.");
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
			            out.println("Extension-Point: " + ref.getProperty("extension-point"));
			            out.println("Type: " + ref.getProperty("type"));
			            out.println("Version: " + ref.getProperty("version"));
			            out.println("Service-Id: " + ref.getProperty("service.id"));
			            out.println("Bundle: " + bundle.toString());
			            out.print("Extended by: ");
			            Bundle[] usingBundles = ref.getUsingBundles();
			            if(usingBundles == null || usingBundles.length == 0) {
			                out.println("-");
			            } else {
			                out.println(usingBundles[0].toString());
			                if(usingBundles.length > 1) {
			                    for(int i=1; i<usingBundles.length; i++) {
			                        out.println("             " + usingBundles[i].toString());
			                    }
			                }
			            }
			            out.println("");
			        }    
			    }
			}
		} catch (InvalidSyntaxException e) {
		    out.printStackTrace(e);
		}
	}
	
}
