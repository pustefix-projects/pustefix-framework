/*
 * de.schlund.pfixcore.webservice.WebServiceContext
 */
package de.schlund.pfixcore.webservice;

import de.schlund.pfixcore.webservice.config.*;

import java.util.HashMap;

/**
 * WebServiceContext.java 
 * 
 * Created: 22.07.2004
 * 
 * @author mleidig
 */
public class WebServiceContext {

    private ServiceConfiguration config;
    private HashMap attributes;
    
    public WebServiceContext(ServiceConfiguration config) {
        this.config=config;
        attributes=new HashMap();
    }
    
    public ServiceConfiguration getServiceConfiguration() {
        return config;
    }
    
    public synchronized void setAttribute(String name,Object obj) {
        attributes.put(name,obj);
    }
    
    public synchronized Object getAttribute(String name) {
        return attributes.get(name);
    }
    
}
