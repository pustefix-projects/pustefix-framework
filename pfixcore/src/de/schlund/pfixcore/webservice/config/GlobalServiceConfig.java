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
 * GlobalServiceConfig.java 
 * 
 * Created: 27.07.2004
 * 
 * @author mleidig@schlund.de
 */
public class GlobalServiceConfig {
    
    final static String DEFAULT_SESSTYPE=Constants.SESSION_TYPE_SERVLET;
    final static String DEFAULT_SCOPETYPE=Constants.SERVICE_SCOPE_APPLICATION;
    final static String DEFAULT_PROTOCOLTYPE=Constants.PROTOCOL_TYPE_SOAP;
    
    String server;
    String reqPath;
    boolean wsdlSupport;
    String wsdlRepo;
    boolean stubGeneration;
    String stubRepo;
    String protocolType;
    String encStyle;
    String encUse;
    String sessType;
    String scopeType;
    boolean admin;
    boolean monitoring;
    String monitorScope;
    int monitorSize;
    boolean logging;
    FaultHandler faultHandler;
    
    public GlobalServiceConfig() {}
    
    public String getServer() {
        return server;
    }
    
    public void setServer(String server) {
        this.server=server;
    }
    
    public String getRequestPath() {
        return reqPath;
    }
    
    public void setRequestPath(String reqPath) {
        this.reqPath=reqPath;
    }
    
    public String getWSDLRepository() {
        return wsdlRepo;
    }
    
    public void setWSDLRepository(String wsdlRepo) {
        this.wsdlRepo=wsdlRepo;
    }
    
    public boolean getWSDLSupportEnabled() {
        return wsdlSupport;
    }
    
    public void setWSDLSupportEnabled(boolean wsdlSupport) {
        this.wsdlSupport=wsdlSupport;
    }
    
    public String getStubRepository() {
        return stubRepo;
    }
    
    public void setStubRepository(String stubRepo) {
        this.stubRepo=stubRepo;
    }
    
    public boolean getStubGenerationEnabled() {
        return stubGeneration;
    }
    
    public void setStubGenerationEnabled(boolean stubGeneration) {
        this.stubGeneration=stubGeneration;
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
    
    public String getSessionType() {
        return sessType;
    }
    
    public void setSessionType(String sessType) {
        this.sessType=sessType;
    }
    
    public void setScopeType(String scopeType) {
        this.scopeType=scopeType;
    }
    
    public String getScopeType() {
        return scopeType;
    }
    
    public boolean getAdminEnabled() {
        return admin;
    }
    
    public void setAdminEnabled(boolean admin) {
        this.admin=admin;
    }
    
    public boolean getMonitoringEnabled() {
        return monitoring;
    }
    
    public void setMonitoringEnabled(boolean monitoring) {
        this.monitoring=monitoring;
    }
    
    public String getMonitoringScope() {
        return monitorScope;
    }
    
    public void setMonitoringScope(String monitorScope) {
        this.monitorScope=monitorScope;
    }
    
    public int getMonitoringHistorySize() {
    	return monitorSize;
    }
    
    public void setMonitoringHistorySize(int monitorSize) {
        this.monitorSize=monitorSize;
    }
    
    public boolean getLoggingEnabled() {
        return logging;
    }
    
    public void setLoggingEnabled(boolean logging) {
        this.logging=logging;
    }
    
    public FaultHandler getFaultHandler() {
    	return faultHandler;
    }
    
    public void setFaultHandler(FaultHandler faultHandler) {
    	this.faultHandler=faultHandler;
    }
    
    @Override
    public boolean equals(Object obj) {
    	if(obj instanceof GlobalServiceConfig) {
    		GlobalServiceConfig ref=(GlobalServiceConfig)obj;
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
