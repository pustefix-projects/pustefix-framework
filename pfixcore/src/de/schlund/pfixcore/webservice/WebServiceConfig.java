/*
 * de.schlund.pfixcore.webservice.WebServiceConfig
 */
package de.schlund.pfixcore.webservice;

import java.util.*;

/**
 * WebServiceConfig.java 
 * 
 * Created: 27.07.2004
 * 
 * @author mleidig
 */
public class WebServiceConfig {

    String name;
    String ctxName;
    int sessType=Constants.SESSION_TYPE_SERVLET;
    HashMap params;
    
    public WebServiceConfig(String name) {
        this.name=name;
        params=new HashMap();
    }
    
    public String getName() {
        return name;
    }
    
    public void setContextName(String ctxName) {
        this.ctxName=ctxName;
    }
    
    public String getContextName() {
        return ctxName;
    }
    
    public void setSessionType(int sessType) {
        this.sessType=sessType;
    }
    
    public int getSessionType() {
        return sessType;
    }
    
    public Iterator getParameterNames() {
        return params.keySet().iterator();
    }
    
    public String getParameter(String name) {
        return (String)params.get(name);
    }

    public String toString() {
        return "[webservice[name="+name+"][contextname="+ctxName+"][sessiontype="+sessType+"]]";
    }
    
}
