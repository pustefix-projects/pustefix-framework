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
 * AppLoaderConfigException.java 
 * 
 * Created: 29.04.2003
 * 
 * @author mleidig
 */
public class StateTransferException extends Exception {
    
    public static final int MEMBER_TYPE_CHANGED=0;
    public static final int MEMBER_TYPE_CONVERSION=1;
    public static final int MEMBER_REMOVED=2;
    public static final int MEMBER_ADDED=3;
    public static final int MEMBER_FINAL=4;
    public static final int CLASS_REMOVED=5;
    public static final int UNHANDLED_EXCEPTION=6;
    public static final int NULLHASH_EXCEPTION=7;
    
    int type;
    String msg;
    String className;
    Exception x;
    
	public StateTransferException(int type,String className,String msg) {
        this.type=type;
        this.className=className;
        this.msg=msg;
    }
    
    public StateTransferException(int type,String className,Exception x) {
        this.type=type;
        this.className=className;
        this.x=x;
    }

	public String toString() {
        if(x==null) return "StateTransferException: Class '"+className+"': "+msg;
        return "StateTransferException: Class '"+className+": "+x;
	}

	public String getMessage() {
        return msg;
    }
    
    public Exception getException() {
        return x;
    }
    
    public int getType() {
        return type;
    }

    public String getClassName() {
        return className;
    }
    
}
