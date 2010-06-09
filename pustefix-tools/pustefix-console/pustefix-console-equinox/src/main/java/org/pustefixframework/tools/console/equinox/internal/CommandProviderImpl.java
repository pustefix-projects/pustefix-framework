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

package org.pustefixframework.tools.console.equinox.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.osgi.framework.BundleContext;
import org.pustefixframework.tools.console.core.ConsoleCommand;
import org.pustefixframework.tools.console.core.ConsoleOutput;
import org.pustefixframework.tools.console.core.ExtensionPointCommand;


/**
 * Provides additional Equinox console commands.
 * 
 * @author mleidig@schlund.de
 *
 */
public class CommandProviderImpl implements CommandProvider {

	private ExtensionPointCommand extensionPointCommand;
	
	public CommandProviderImpl(BundleContext bundleContext) {
		extensionPointCommand = new ExtensionPointCommand(bundleContext);
	}
	
	public String getHelp() {
	    StringBuilder sb = new StringBuilder();
	    sb.append("---Pustefix commands---\n");
	    getHelp(sb, extensionPointCommand);
		return sb.toString();
	}
	
	private void getHelp(StringBuilder sb, ConsoleCommand command) {
	    sb.append("\t");
        sb.append(command.getUsage());
        sb.append(" - ");
        sb.append(command.getDescription());
        sb.append("\n");
	}
	
	public void _ext(CommandInterpreter interpreter) {
	   String[] args = parseArguments(interpreter); 
	   ConsoleOutput out = new CommandInterpreterOutput(interpreter);
	   extensionPointCommand.execute(args, out);   
	}
	
	
	private static String[] parseArguments(CommandInterpreter interpreter) {
        List<String> argList = new ArrayList<String>();
        String arg = null;
        while( (arg = interpreter.nextArgument()) != null) {
            argList.add(arg);
        }
        String[] args = new String[argList.size()];
        return argList.toArray(args);
    }
	
	
	private class CommandInterpreterOutput implements ConsoleOutput {

	    private CommandInterpreter interpreter;
	    
	    public CommandInterpreterOutput(CommandInterpreter interpreter) {
	        this.interpreter = interpreter;
	    }
	    
	    public void print(String str) {
	        interpreter.print(str);
	    }
	    
	    public void println(String str) {
	        interpreter.println(str);
	    }
	    
	    public void printStackTrace(Throwable t) {
	        interpreter.printStackTrace(t);
	    }
	    
	}

	
}
