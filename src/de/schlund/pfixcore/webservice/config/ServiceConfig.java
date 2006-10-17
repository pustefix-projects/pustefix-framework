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

package de.schlund.pfixcore.webservice.config;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import de.schlund.pfixcore.webservice.Constants;
import de.schlund.pfixcore.webservice.fault.FaultHandler;

/**
 * ServiceConfig.java 
 * 
 * Created: 27.07.2004
 * 
 * @author mleidig@schlund.de
 */
public class ServiceConfig {

    String  name;
    String  itfName;
    String  implName;
    String  ctxName;
    boolean ctxSync;
    String  sessType=Constants.SESSION_TYPE_SERVLET;
    String scopeType=Constants.SERVICE_SCOPE_APPLICATION;
    boolean sslForce;
    String protocolType=Constants.PROTOCOL_TYPE_ANY;
    String  encStyle;
    String  encUse;
    FaultHandler faultHandler;
    
    public ServiceConfig() {}
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name=name;
    }
    
    public String getInterfaceName() {
        return itfName;
    }
    
    public void setInterfaceName(String itfName) {
        this.itfName=itfName;
    }
    
    public String getImplementationName() {
        return implName;
    }
    
    public void setImplementationName(String implName) {
        this.implName=implName;
    }
    
    public void setContextName(String ctxName) {
        this.ctxName=ctxName;
    }
    
    public String getContextName() {
        return ctxName;
    }
    
    public boolean doSynchronizeOnContext() {
        return ctxSync;
    }
    
    public void setSessionType(String sessType) {
        this.sessType=sessType;
    }
    
    public String getSessionType() {
        return sessType;
    }
    
    public void setScopeType(String scopeType) {
    	this.scopeType=scopeType;
    }
    
    public String getScopeType() {
    	return scopeType;
    }
    
    public boolean getSSLForce() {
        return sslForce;
    }
    
    public void setSSLForce(boolean sslForce) {
        this.sslForce=sslForce;
    }
    
    public String getProtocolType() {
    	return protocolType;
    }
    
    public void setProtocolType(String protocolType) {
    	this.protocolType=protocolType;
    }
    
    public String getEncodingStyle() {
        return encStyle;
    }
    
    public void setEncodingStyle(String encStyle) {
    	this.encStyle=encStyle;
    }
    
    public String getEncodingUse() {
        return encUse;
    }
    
    public void setEncodingUse(String encUse) {
    	this.encUse=encUse;
    }
    
    public FaultHandler getFaultHandler() {
    	return faultHandler;
    }
    
    public void setFaultHandler(FaultHandler faultHandler) {
    	this.faultHandler=faultHandler;
    }
    
    @Override
    public boolean equals(Object obj) {
    	if(obj instanceof ServiceConfig) {
    		ServiceConfig ref=(ServiceConfig)obj;
    		Method[] meths=getClass().getDeclaredMethods();
    		for(int i=0;i<meths.length;i++) {
    			Method meth=meths[i];
    			if(meth.getName().startsWith("get")&&Modifier.isPublic(meth.getModifiers())) { 
    				try {
    					Object res=meth.invoke(this,new Object[0]);
    					Object refRes=meth.invoke(ref,new Object[0]);
    					if(res==null ^ refRes==null) {
    						System.out.println("Difference found: "+meth.getName()+" "+res+" "+refRes);
    						return false;
    					}
    					if(res!=null && !res.equals(refRes)) {
    						System.out.println("Difference found: "+meth.getName()+" "+res+" "+refRes);
    						return false;
    					}
    				} catch(Exception x) {
    					x.printStackTrace();
    					return false;
    				}
    			}
    		}
    		return true;
    	}
    	return false;
    }
	
}
