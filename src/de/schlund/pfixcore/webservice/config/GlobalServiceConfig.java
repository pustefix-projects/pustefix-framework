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
import java.net.URL;

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
    
    String server;
    String reqPath="/xml/webservice";
    Boolean wsdlSupport=Boolean.TRUE;
    String wsdlRepo="/wsdl";
    Boolean stubGeneration=Boolean.TRUE;
    String stubRepo="/wsscript";
    String jsNamespace=Constants.STUBGEN_JSNAMESPACE_COMPAT;
    String protocolType=Constants.PROTOCOL_TYPE_ANY;
    String encStyle=Constants.ENCODING_STYLE_RPC;
    String encUse=Constants.ENCODING_USE_ENCODED;
    Boolean jsonClassHinting=Boolean.FALSE;
    String sessType=Constants.SESSION_TYPE_SERVLET;
    String scopeType=Constants.SERVICE_SCOPE_APPLICATION;
    Boolean sslForce=Boolean.FALSE;
    String ctxName;
    Boolean ctxSync=Boolean.TRUE;
    Boolean admin=Boolean.FALSE;
    Boolean monitoring=Boolean.FALSE;
    String monitorScope=Constants.MONITOR_SCOPE_SESSION;
    Integer monitorSize=20;
    Boolean logging=Boolean.FALSE;
    FaultHandler faultHandler;
    URL defaultBeanMetaDataUrl;
    
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
    
    public Boolean getWSDLSupportEnabled() {
        return wsdlSupport;
    }
    
    public void setWSDLSupportEnabled(Boolean wsdlSupport) {
        this.wsdlSupport=wsdlSupport;
    }
    
    public String getStubRepository() {
        return stubRepo;
    }
    
    public void setStubRepository(String stubRepo) {
        this.stubRepo=stubRepo;
    }
    
    public Boolean getStubGenerationEnabled() {
        return stubGeneration;
    }
    
    public void setStubGenerationEnabled(Boolean stubGeneration) {
        this.stubGeneration=stubGeneration;
    }
    
    public String getStubJSNamespace() {
        return jsNamespace;
    }
    
    public void setStubJSNamespace(String jsNamespace) {
        this.jsNamespace=jsNamespace;
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
    
    public Boolean getJSONClassHinting() {
        return jsonClassHinting;
    }
    
    public void setJSONClassHinting(Boolean jsonClassHinting) {
        this.jsonClassHinting=jsonClassHinting;
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
    
    public Boolean getSSLForce() {
        return sslForce;
    }
    
    public void setSSLForce(Boolean sslForce) {
        this.sslForce=sslForce;
    }
    
    public String getContextName() {
        return ctxName;
    }
    
    public void setContextName(String ctxName) {
        this.ctxName=ctxName;
    }
    
    public boolean getSynchronizeOnContext() {
        return ctxSync;
    }
    
    public void setSynchronizeOnContext(Boolean ctxSync) {
        this.ctxSync=ctxSync;
    }
    
    public Boolean getAdminEnabled() {
        return admin;
    }
    
    public void setAdminEnabled(Boolean admin) {
        this.admin=admin;
    }
    
    public Boolean getMonitoringEnabled() {
        return monitoring;
    }
    
    public void setMonitoringEnabled(Boolean monitoring) {
        this.monitoring=monitoring;
    }
    
    public String getMonitoringScope() {
        return monitorScope;
    }
    
    public void setMonitoringScope(String monitorScope) {
        this.monitorScope=monitorScope;
    }
    
    public Integer getMonitoringHistorySize() {
    	return monitorSize;
    }
    
    public void setMonitoringHistorySize(Integer monitorSize) {
        this.monitorSize=monitorSize;
    }
    
    public Boolean getLoggingEnabled() {
        return logging;
    }
    
    public void setLoggingEnabled(Boolean logging) {
        this.logging=logging;
    }
    
    public FaultHandler getFaultHandler() {
    	return faultHandler;
    }
    
    public void setFaultHandler(FaultHandler faultHandler) {
    	this.faultHandler=faultHandler;
    }
    
    public URL getDefaultBeanMetaDataURL() {
        return defaultBeanMetaDataUrl;
    }
    
    public void setDefaultBeanMetaDataURL(URL defaultBeanMetaDataUrl) {
        this.defaultBeanMetaDataUrl=defaultBeanMetaDataUrl;
    }
    
    @Override
    public boolean equals(Object obj) {
    	if(obj instanceof GlobalServiceConfig) {
    		GlobalServiceConfig ref=(GlobalServiceConfig)obj;
    		Method[] meths=getClass().getDeclaredMethods();
    		for(int i=0;i<meths.length;i++) {
    			Method meth=meths[i];		
    			if(meth.getName().startsWith("get")&&Modifier.isPublic(meth.getModifiers())&&!meth.getName().equals("getDefaultBeanMetaDataURL")) { 
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
