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

package org.pustefixframework.webservices.config;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.HashMap;

import org.pustefixframework.webservices.Constants;
import org.pustefixframework.webservices.fault.FaultHandler;


/**
 * GlobalServiceConfig.java
 * 
 * Created: 27.07.2004
 * 
 * @author mleidig@schlund.de
 */
public class GlobalServiceConfig implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -8134435783633908273L;
    private String                 server;
    private String                 reqPath          = "/webservice";
    private Boolean                wsdlSupport      = Boolean.TRUE;
    private String                 wsdlRepo         = "/wsdl";
    private Boolean                stubGeneration   = Boolean.TRUE;
    private String                 stubRepo         = "/wsscript";
    private String                 jsNamespace      = Constants.STUBGEN_JSNAMESPACE_COMPAT;
    private String                 protocolType     = Constants.PROTOCOL_TYPE_JSONWS;
    private String                 encStyle         = Constants.ENCODING_STYLE_RPC;
    private String                 encUse           = Constants.ENCODING_USE_ENCODED;
    private Boolean                jsonClassHinting = Boolean.FALSE;
    private String                 sessType         = Constants.SESSION_TYPE_SERVLET;
    private String                 scopeType        = Constants.SERVICE_SCOPE_APPLICATION;
    private Boolean                sslForce         = Boolean.FALSE;
    private String                 ctxName;
    private String                 authConstraintRef;
    private Boolean                ctxSync          = Boolean.TRUE;
    private Boolean                admin            = Boolean.FALSE;
    private Boolean                monitoring       = Boolean.FALSE;
    private String                 monitorScope     = Constants.MONITOR_SCOPE_SESSION;
    private Integer                monitorSize      = 10;
    private Boolean                logging          = Boolean.FALSE;
    private transient FaultHandler faultHandler;
    private URL                    defaultBeanMetaDataUrl;

    public GlobalServiceConfig() {}

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getRequestPath() {
        return reqPath;
    }

    public void setRequestPath(String reqPath) {
        this.reqPath = reqPath;
    }

    public String getWSDLRepository() {
        return wsdlRepo;
    }

    public void setWSDLRepository(String wsdlRepo) {
        this.wsdlRepo = wsdlRepo;
    }

    public Boolean getWSDLSupportEnabled() {
        return wsdlSupport;
    }

    public void setWSDLSupportEnabled(Boolean wsdlSupport) {
        this.wsdlSupport = wsdlSupport;
    }

    public String getStubRepository() {
        return stubRepo;
    }

    public void setStubRepository(String stubRepo) {
        this.stubRepo = stubRepo;
    }

    public Boolean getStubGenerationEnabled() {
        return stubGeneration;
    }

    public void setStubGenerationEnabled(Boolean stubGeneration) {
        this.stubGeneration = stubGeneration;
    }

    public String getStubJSNamespace() {
        return jsNamespace;
    }

    public void setStubJSNamespace(String jsNamespace) {
        this.jsNamespace = jsNamespace;
    }

    public String getProtocolType() {
        return protocolType;
    }

    public void setProtocolType(String protocolType) {
        this.protocolType = protocolType;
    }

    public String getEncodingStyle() {
        return encStyle;
    }

    public void setEncodingStyle(String encStyle) {
        this.encStyle = encStyle;
    }

    public String getEncodingUse() {
        return encUse;
    }

    public void setEncodingUse(String encUse) {
        this.encUse = encUse;
    }

    public Boolean getJSONClassHinting() {
        return jsonClassHinting;
    }

    public void setJSONClassHinting(Boolean jsonClassHinting) {
        this.jsonClassHinting = jsonClassHinting;
    }

    public String getSessionType() {
        return sessType;
    }

    public void setSessionType(String sessType) {
        this.sessType = sessType;
    }

    public void setScopeType(String scopeType) {
        this.scopeType = scopeType;
    }

    public String getScopeType() {
        return scopeType;
    }

    public Boolean getSSLForce() {
        return sslForce;
    }

    public void setSSLForce(Boolean sslForce) {
        this.sslForce = sslForce;
    }

    public String getContextName() {
        return ctxName;
    }

    public void setContextName(String ctxName) {
        this.ctxName = ctxName;
    }
    
    public String getAuthConstraintRef() {
        return authConstraintRef;
    }

    public void setAuthConstraintRef(String authConstraintRef) {
        this.authConstraintRef = authConstraintRef;
    }

    public boolean getSynchronizeOnContext() {
        return ctxSync;
    }

    public void setSynchronizeOnContext(Boolean ctxSync) {
        this.ctxSync = ctxSync;
    }

    public Boolean getAdminEnabled() {
        return admin;
    }

    public void setAdminEnabled(Boolean admin) {
        this.admin = admin;
    }

    public Boolean getMonitoringEnabled() {
        return monitoring;
    }

    public void setMonitoringEnabled(Boolean monitoring) {
        this.monitoring = monitoring;
    }

    public String getMonitoringScope() {
        return monitorScope;
    }

    public void setMonitoringScope(String monitorScope) {
        this.monitorScope = monitorScope;
    }

    public Integer getMonitoringHistorySize() {
        return monitorSize;
    }

    public void setMonitoringHistorySize(Integer monitorSize) {
        this.monitorSize = monitorSize;
    }

    public Boolean getLoggingEnabled() {
        return logging;
    }

    public void setLoggingEnabled(Boolean logging) {
        this.logging = logging;
    }

    public FaultHandler getFaultHandler() {
        return faultHandler;
    }

    public void setFaultHandler(FaultHandler faultHandler) {
        this.faultHandler = faultHandler;
    }

    public URL getDefaultBeanMetaDataURL() {
        return defaultBeanMetaDataUrl;
    }

    public void setDefaultBeanMetaDataURL(URL defaultBeanMetaDataUrl) {
        this.defaultBeanMetaDataUrl = defaultBeanMetaDataUrl;
    }

    @Override
    public boolean equals(Object obj) {

        if (obj instanceof GlobalServiceConfig) {
            GlobalServiceConfig ref = (GlobalServiceConfig) obj;
            Method[] meths = getClass().getDeclaredMethods();
            for (int i = 0; i < meths.length; i++) {
                Method meth = meths[i];
                if (meth.getName().startsWith("get") && Modifier.isPublic(meth.getModifiers()) && !meth.getName().equals("getDefaultBeanMetaDataURL")) {
                    try {
                        Object res = meth.invoke(this, new Object[0]);
                        Object refRes = meth.invoke(ref, new Object[0]);
                        if (res == null ^ refRes == null) {
                            System.out.println("Difference found: " + meth.getName() + " " + res + " " + refRes);
                            return false;
                        }
                        if (res != null && !res.equals(refRes)) {
                            System.out.println("Difference found: " + meth.getName() + " " + res + " " + refRes);
                            return false;
                        }
                    } catch (Exception x) {
                        x.printStackTrace();
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        assert false : "hashCode not supported";
        return 0;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        if (faultHandler != null)
            out.writeObject(faultHandler.getClass().getName());
        else
            out.writeObject(null);
        if (faultHandler != null && faultHandler.getParams() != null)
            out.writeObject(faultHandler.getParams());
        else
            out.writeObject(null);
    }

    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        String str = (String) in.readObject();
        if (str != null) {
            Class<?> clazz = Class.forName(str);
            try {
                faultHandler = (FaultHandler) clazz.newInstance();
                HashMap<String, String> params = (HashMap<String, String>) in.readObject();
                if (params != null) faultHandler.setParams(params);
            } catch (IllegalAccessException x) {

            } catch (InstantiationException x) {

            }
        }
    }

}
