/*
 * de.schlund.pfixcore.webservice.WebServiceContext
 */
package de.schlund.pfixcore.webservice;

import de.schlund.pfixcore.webservice.config.*;

/**
 * WebServiceContext.java 
 * 
 * Created: 22.07.2004
 * 
 * @author mleidig
 */
public class WebServiceContext {

    private ServiceConfiguration config;
    
    public WebServiceContext(ServiceConfiguration config) {
        this.config=config;
    }
    
    public ServiceConfiguration getServiceConfiguration() {
        return config;
    }
    
}
