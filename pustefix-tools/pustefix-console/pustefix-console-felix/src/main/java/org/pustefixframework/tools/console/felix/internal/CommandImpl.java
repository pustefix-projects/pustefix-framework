package org.pustefixframework.tools.console.felix.internal;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.felix.shell.Command;
import org.pustefixframework.tools.console.core.ConsoleCommand;
import org.pustefixframework.tools.console.core.ConsoleOutput;

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


/**
 * Generic wrapper for Felix shell commands.
 * 
 * @author mleidig@schlund.de
 *
 */
public class CommandImpl implements Command {

    private ConsoleCommand command;
    
    public CommandImpl(ConsoleCommand command) {
        this.command = command;
    }
    
    public String getName() {
        return command.getName();
    }
    
    public String getShortDescription() {
        return command.getDescription();
    }
    
    public String getUsage() {
        return command.getUsage();
    }
    
    public void execute(String line, PrintStream out, PrintStream err) {
        String[] args = parseArguments(line); 
        ConsoleOutput consoleOut = new CommandOutput(out);
        command.execute(args, consoleOut);   
    }
    
    
    protected static String[] parseArguments(String line) {
        List<String> argList = new ArrayList<String>();
        StringTokenizer st = new StringTokenizer(line, " ");
        st.nextToken();
        while(st.hasMoreTokens()) {
            argList.add(st.nextToken());
        }
        String[] args = new String[argList.size()];
        return argList.toArray(args);
    }
    
    
    protected class CommandOutput implements ConsoleOutput {

        private PrintStream out;
        
        public CommandOutput(PrintStream out) {
            this.out = out;
        }
            
        public void print(String str) {
            out.print(str);
        }
            
        public void println(String str) {
            out.println(str);
        }
            
        public void printStackTrace(Throwable t) {
            t.printStackTrace(out);
        }
            
    }
       
}
