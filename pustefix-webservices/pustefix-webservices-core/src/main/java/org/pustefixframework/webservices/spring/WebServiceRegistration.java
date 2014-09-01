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
 * 
 * @author mleidig
 *
 */
public class WebServiceRegistration {

    private String serviceName;
    private String targetBeanName;
    private Object target;
    private String interfaceName;
    private String protocol;
    private String sessionType;
    private String authConstraint;
    private Boolean synchronize;
    
    public String getTargetBeanName() {
        return targetBeanName;
    }

    public void setTargetBeanName(String targetBeanName) {
        this.targetBeanName = targetBeanName;
    }

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
 
    public Boolean getSynchronize() {
        return synchronize;
    }
    
    public void setSynchronize(Boolean synchronize) {
        this.synchronize = synchronize;
    }

}
