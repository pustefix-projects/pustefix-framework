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

package org.pustefixframework.webservices.config;

import org.xml.sax.SAXException;

/**
 * ConfigException.java 
 * 
 * Created: 03.08.2004
 * 
 * @author mleidig@schlund.de
 */
public class ConfigException extends SAXException {
    
    /**
     * 
     */
    private static final long serialVersionUID = -4775287108925223591L;
    public final static int MISSING_ATTRIBUTE=0;
    public final static int ILLEGAL_ATTRIBUTE_VALUE=1;
    
    private int type;
    private String attrName;
    private String attrVal;
    private Throwable cause;
    
    public ConfigException(int type,String attrName) {
        super();
        this.type=type;
        this.attrName=attrName;
    }
    
    public ConfigException(int type,String attrName,String attrVal) {
        super();
        this.type=type;
        this.attrName=attrName;
        this.attrVal=attrVal;
    }
    
    public ConfigException(int type,String attrName,String attrVal,Throwable cause) {
    	super();
    	this.type=type;
    	this.attrName=attrName;
    	this.attrVal=attrVal;
    	this.cause=cause;
    }
    
    @Override
    public String getMessage() {
    	String msg="";
        if(type==MISSING_ATTRIBUTE) {
            msg="Mandatory attribute '"+attrName+"' is not set.";
        } else if(type==ILLEGAL_ATTRIBUTE_VALUE) {
            msg="Attribute '"+attrName+"' has illegal value: '"+attrVal+"'.";
        } else msg="Unknown error";
        if(cause!=null) msg+="[Cause: "+cause.toString()+"]";
        return msg;
    }

}
