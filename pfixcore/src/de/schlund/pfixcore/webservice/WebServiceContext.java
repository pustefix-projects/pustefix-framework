/*
 * de.schlund.pfixcore.webservice.WebServiceContext
 */
package de.schlund.pfixcore.webservice;

import java.util.*;

/**
 * WebServiceContext.java 
 * 
 * Created: 22.07.2004
 * 
 * @author mleidig
 */
public class WebServiceContext {

    private ConfigProperties props;
    private HashMap services;
    
    public WebServiceContext(ConfigProperties props) {
        this.props=props;
        services=new HashMap();
        initServices(props);
    }
    
    private void initServices(ConfigProperties props) {
        Iterator it=props.getPropertyKeys("webservice\\.[^\\.]*\\.name");
        while(it.hasNext()) {
            String key=(String)it.next();
            String name=props.getProperty(key);
            WebServiceConfig wsc=new WebServiceConfig(name);
            String ctxName=props.getProperty("webservice."+name+".context.name");
            if(ctxName!=null) wsc.setContextName(ctxName);
            String sessType=props.getProperty("webservice."+name+".session.type");
            if(sessType!=null) {
                int type=-1;
                if(sessType.equalsIgnoreCase("none")) type=Constants.SESSION_TYPE_NONE;
                else if(sessType.equalsIgnoreCase("servlet")) type=Constants.SESSION_TYPE_SERVLET;
                else if(sessType.equalsIgnoreCase("soapheader")) type=Constants.SESSION_TYPE_SOAPHEADER;
                if(type>-1) wsc.setSessionType(type);
            }
            //TODO: get params
            
            services.put(name,wsc);
        }
    }
    
    public WebServiceConfig getWebServiceConfig(String name) {
        return (WebServiceConfig)services.get(name);
    }
    
}
