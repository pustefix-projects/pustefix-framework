/*
* This file is part of PFIXCORE.
*
* PFIXCORE is free software; you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation; either version 2 of the License, or
* (at your option) any later version.
*
* PFIXCORE is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with PFIXCORE; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*
*/

package de.schlund.pfixxml.loader;

/**
 * IllegalCommandException.java 
 * 
 * Created: 29.04.2003
 * 
 * @author mleidig
 */
public class IllegalCommandException extends Exception {

	final static String NO_CMD="No command specified";
	final static String NO_ARG="Command doesn't accept arguments";
	final static String ARG_NO="Wrong number of arguments";
	final static String ILL_ARG="Illegal argument";
	final static String INV_CMD="Invalid command";

	String type;
	String cmd;

	public IllegalCommandException(String type) {
		this.type=type;
	}

	public IllegalCommandException(String type,String cmd) {
		this.type=type;
		this.cmd=cmd;
	}

	public String toString() {
		return "IllegalCommandException for command '"+cmd+"': "+type+".";
	}
	
	public String getMessage() {
		return type;
	}

	public String getCommand() {
		return cmd;
	}
}
