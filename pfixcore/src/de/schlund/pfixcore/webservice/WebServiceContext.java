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

    private Configuration config;
    private HashMap attributes;
    
    public WebServiceContext(Configuration config) {
        this.config=config;
        attributes=new HashMap();
    }
    
    public Configuration getConfiguration() {
        return config;
    }
    
    public synchronized void setAttribute(String name,Object obj) {
        attributes.put(name,obj);
    }
    
    public synchronized Object getAttribute(String name) {
        return attributes.get(name);
    }
    
}
