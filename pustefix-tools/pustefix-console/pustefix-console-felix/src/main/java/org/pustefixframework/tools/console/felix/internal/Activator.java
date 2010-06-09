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

package org.pustefixframework.tools.console.felix.internal;

import org.apache.felix.shell.Command;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.pustefixframework.tools.console.core.ConsoleCommand;
import org.pustefixframework.tools.console.core.ExtensionPointCommand;

/**
 * Registers additional commands for the Felix shell.
 * 
 * @author mleidig@schlund.de
 *
 */
public class Activator implements BundleActivator {

	public void start(BundleContext context) throws Exception {
	    ConsoleCommand command = new ExtensionPointCommand(context);
		context.registerService(Command.class.getName(), new CommandImpl(command), null);
	}

	public void stop(BundleContext arg0) throws Exception {
	}
	
}
