/*
 * de.schlund.pfixcore.webservice.config.ServiceConfig
 */
package de.schlund.pfixcore.webservice.config;

import java.util.*;

import de.schlund.pfixcore.webservice.*;

/**
 * ServiceConfig.java 
 * 
 * Created: 27.07.2004
 * 
 * @author mleidig
 */
public class ServiceConfig {

    private final static String PROP_PREFIX="webservice.";
    private final static String PROP_ITFNAME=".interface.name";
    private final static String PROP_CTXNAME=".context.name";
    private final static String PROP_SESSTYPE=".session.type";

    ConfigProperties props;
    String name;
    String itfName;
    String ctxName;
    int sessType=Constants.SESSION_TYPE_SERVLET;
    HashMap params;
    
    public ServiceConfig(ConfigProperties props,String name) throws ServiceConfigurationException {
        this.props=props;
        this.name=name;
        init();
    }
    
    private void init() throws ServiceConfigurationException {
        String propKey=PROP_PREFIX+name+PROP_ITFNAME;
        itfName=props.getProperty(propKey);
        if(itfName==null) throw new ServiceConfigurationException(ServiceConfigurationException.MISSING_PROPERTY,propKey);
        propKey=PROP_PREFIX+name+PROP_CTXNAME;
        ctxName=props.getProperty(propKey);
        propKey=PROP_PREFIX+name+PROP_SESSTYPE;
        String str=props.getProperty(propKey);
        if(str!=null) {
            if(str.equalsIgnoreCase("none")) sessType=Constants.SESSION_TYPE_NONE;
            else if(str.equalsIgnoreCase("servlet")) sessType=Constants.SESSION_TYPE_SERVLET;
            else if(str.equalsIgnoreCase("soapheader")) sessType=Constants.SESSION_TYPE_SOAPHEADER;
            else throw new ServiceConfigurationException(ServiceConfigurationException.ILLEGAL_PROPERTY_VALUE,propKey,str);
        } else sessType=Constants.SESSION_TYPE_SERVLET;
        //TODO: get params
    }

    public void reload() throws ServiceConfigurationException {
        init();
    }
    
    public String getName() {
        return name;
    }
    
    public String getInterfaceName() {
        return itfName;
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
        return "[webservice[name="+name+"][interfacename="+itfName+"][contextname="+ctxName+"][sessiontype="+sessType+"]]";
    }
    
}
