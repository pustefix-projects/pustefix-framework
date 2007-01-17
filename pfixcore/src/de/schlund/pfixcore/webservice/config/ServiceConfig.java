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
    
    GlobalServiceConfig globConf;
    
    String  name;
    String  itfName;
    String  implName;
    
    String  ctxName;
    Boolean ctxSync;
    String  sessType;
    String scopeType;
    Boolean sslForce;
    String protocolType;
    String  encStyle;
    String  encUse;
    FaultHandler faultHandler;
    String jsNamespace;
    
    public ServiceConfig(GlobalServiceConfig globConf) {
        this.globConf=globConf;
    }
    
    public GlobalServiceConfig getGlobalServiceConfig() {
        return globConf;
    }
    
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
    
    public String getContextName() {
        if(ctxName==null&&globConf!=null) return globConf.getContextName();
        return ctxName;
    }
    
    public void setContextName(String ctxName) {
        this.ctxName=ctxName;
    }
    
    public boolean getSynchronizeOnContext() {
        if(ctxSync==null&&globConf!=null) return globConf.getSynchronizeOnContext();
        return ctxSync;
    }
    
    public void setSynchronizeOnContext(Boolean ctxSync) {
        this.ctxSync=ctxSync;
    }
    
    public String getSessionType() {
        if(sessType==null&&globConf!=null) return globConf.getSessionType();
        return sessType;
    }
    
    public void setSessionType(String sessType) {
        this.sessType=sessType;
    }
    
    public String getScopeType() {
        if(scopeType==null&&globConf!=null) return globConf.getScopeType();
        return scopeType;
    }
    
    public void setScopeType(String scopeType) {
    	this.scopeType=scopeType;
    }
    
    public Boolean getSSLForce() {
        if(sslForce==null&&globConf!=null) return globConf.getSSLForce();
        return sslForce;
    }
    
    public void setSSLForce(Boolean sslForce) {
        this.sslForce=sslForce;
    }
    
    public String getProtocolType() {
        if(protocolType==null&&globConf!=null) return globConf.getProtocolType();
    	return protocolType;
    }
    
    public void setProtocolType(String protocolType) {
    	this.protocolType=protocolType;
    }
    
    public String getEncodingStyle() {
        if(encStyle==null&&globConf!=null) return globConf.getEncodingStyle();
        return encStyle;
    }
    
    public void setEncodingStyle(String encStyle) {
    	this.encStyle=encStyle;
    }
    
    public String getEncodingUse() {
        if(encUse==null&&globConf!=null) return globConf.getEncodingUse();
        return encUse;
    }
    
    public void setEncodingUse(String encUse) {
    	this.encUse=encUse;
    }
    
    public String getStubJSNamespace() {
        if(jsNamespace==null&&globConf!=null) return globConf.getStubJSNamespace();
        return jsNamespace;
    }
    
    public void setStubJSNamespace(String jsNamespace) {
        this.jsNamespace=jsNamespace;
    }
    
    public FaultHandler getFaultHandler() {
    	if(faultHandler==null&&globConf!=null) return globConf.getFaultHandler();
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
