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
package org.pustefixframework.webservices.spring;

/**
 * Instances of this class are holding the configuration of a webservice
 * and are referencing the backing service object.
 * 
 * @author mleidig@schlund.de
 *
 */
public class WebserviceRegistration {

    private String serviceName;
    private Object target;
    private String interfaceName;
    private String protocol;
    private String sessionType;
    private String authConstraint;
    private Boolean ctxSync;
    private Boolean sslForce;
    private Boolean classHinting;
    private String jsNamespace;
    
    
    public String getServiceName() {
        return serviceName;
    }
    
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
    
    public void setInterface(String interfaceName) {
        this.interfaceName = interfaceName;
    }
    
    public String getInterface() {
        return interfaceName;
    }
    
    public Object getTarget() {
        return target;
    }
    
    public void setTarget(Object target) {
        this.target = target;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }
    
    public String getSessionType() {
        return sessionType;
    }
    
    public void setSessionType(String sessionType) {
        this.sessionType = sessionType;
    }

    public String getAuthConstraint() {
        return authConstraint;
    }

    public void setAuthConstraint(String authConstraint) {
        this.authConstraint = authConstraint;
    }
    
    public Boolean getSynchronizeOnContext() {
    	return ctxSync;
    }

    public void setSynchronizeOnContext(Boolean ctxSync) {
        this.ctxSync = ctxSync;
    }
    
    public Boolean getSSLForce() {
        return sslForce;
    }

    public void setSSLForce(Boolean sslForce) {
        this.sslForce = sslForce;
    }
    
    public Boolean getClassHinting() {
        return classHinting;
    }

    public void setJSONClassHinting(Boolean classHinting) {
        this.classHinting = classHinting;
    }

    public String getStubJSNamespace() {
        return jsNamespace;
    }

    public void setStubJSNamespace(String jsNamespace) {
        this.jsNamespace = jsNamespace;
    }
    
}
