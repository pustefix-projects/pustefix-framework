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
import java.util.HashMap;

import org.pustefixframework.webservices.fault.FaultHandler;


/**
 * ServiceConfig.java
 * 
 * Created: 27.07.2004
 * 
 * @author mleidig@schlund.de
 */
public class ServiceConfig implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -7058920718918701708L;

    private GlobalServiceConfig    globConf;

    private String                 name;
    private String                 itfName;
    private String                 implName;

    private String                 ctxName;
    private Boolean                ctxSync;
    private String                 authConstraintRef;
    private String                 sessType;
    private String                 scopeType;
    private Boolean                sslForce;
    private String                 protocolType;
    private String                 encStyle;
    private String                 encUse;
    private Boolean                jsonClassHinting;
    private transient FaultHandler faultHandler;
    private String                 jsNamespace;

    public ServiceConfig(GlobalServiceConfig globConf) {
        this.globConf = globConf;
    }

    public GlobalServiceConfig getGlobalServiceConfig() {
        return globConf;
    }

    public void setGlobalServiceConfig(GlobalServiceConfig globConf) {
        this.globConf = globConf;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInterfaceName() {
        return itfName;
    }

    public void setInterfaceName(String itfName) {
        this.itfName = itfName;
    }

    public String getImplementationName() {
        return implName;
    }

    public void setImplementationName(String implName) {
        this.implName = implName;
    }

    public String getContextName() {
        if (ctxName == null && globConf != null) return globConf.getContextName();
        return ctxName;
    }

    public void setContextName(String ctxName) {
        this.ctxName = ctxName;
    }
    
    public String getAuthConstraintRef() {
        if (authConstraintRef == null && globConf != null) return globConf.getAuthConstraintRef();
        return authConstraintRef;
    }

    public void setAuthConstraintRef(String authConstraintRef) {
        this.authConstraintRef = authConstraintRef;
    }

    
    public boolean getSynchronizeOnContext() {
        if (ctxSync == null && globConf != null) return globConf.getSynchronizeOnContext();
        return ctxSync;
    }

    public void setSynchronizeOnContext(Boolean ctxSync) {
        this.ctxSync = ctxSync;
    }

    public String getSessionType() {
        if (sessType == null && globConf != null) return globConf.getSessionType();
        return sessType;
    }

    public void setSessionType(String sessType) {
        this.sessType = sessType;
    }

    public String getScopeType() {
        if (scopeType == null && globConf != null) return globConf.getScopeType();
        return scopeType;
    }

    public void setScopeType(String scopeType) {
        this.scopeType = scopeType;
    }

    public Boolean getSSLForce() {
        if (sslForce == null && globConf != null) return globConf.getSSLForce();
        return sslForce;
    }

    public void setSSLForce(Boolean sslForce) {
        this.sslForce = sslForce;
    }

    public String getProtocolType() {
        if (protocolType == null && globConf != null) return globConf.getProtocolType();
        return protocolType;
    }

    public void setProtocolType(String protocolType) {
        this.protocolType = protocolType;
    }

    public String getEncodingStyle() {
        if (encStyle == null && globConf != null) return globConf.getEncodingStyle();
        return encStyle;
    }

    public void setEncodingStyle(String encStyle) {
        this.encStyle = encStyle;
    }

    public String getEncodingUse() {
        if (encUse == null && globConf != null) return globConf.getEncodingUse();
        return encUse;
    }

    public void setEncodingUse(String encUse) {
        this.encUse = encUse;
    }

    public Boolean getJSONClassHinting() {
        if (jsonClassHinting == null && globConf != null) return globConf.getJSONClassHinting();
        return jsonClassHinting;
    }

    public void setJSONClassHinting(Boolean jsonClassHinting) {
        this.jsonClassHinting = jsonClassHinting;
    }

    public String getStubJSNamespace() {
        if (jsNamespace == null && globConf != null) return globConf.getStubJSNamespace();
        return jsNamespace;
    }

    public void setStubJSNamespace(String jsNamespace) {
        this.jsNamespace = jsNamespace;
    }

    public FaultHandler getFaultHandler() {
        if (faultHandler == null && globConf != null) return globConf.getFaultHandler();
        return faultHandler;
    }

    public void setFaultHandler(FaultHandler faultHandler) {
        this.faultHandler = faultHandler;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ServiceConfig) {
            ServiceConfig ref = (ServiceConfig) obj;
            Method[] meths = getClass().getDeclaredMethods();
            for (int i = 0; i < meths.length; i++) {
                Method meth = meths[i];
                if (meth.getName().startsWith("get") && Modifier.isPublic(meth.getModifiers())) {
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
