/*
 * de.schlund.pfixcore.webservice.config.ServiceConfig
 */
package de.schlund.pfixcore.webservice.config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
    private final static String PROP_IMPLNAME=".implementation.name";
    private final static String PROP_CTXNAME=".context.name";
    private final static String PROP_SESSTYPE=".session.type";

    ConfigProperties props;
    String name;
    String itfName;
    String implName;
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
        propKey=PROP_PREFIX+name+PROP_IMPLNAME;
        implName=props.getProperty(propKey);
        if(implName==null) throw new ServiceConfigurationException(ServiceConfigurationException.MISSING_PROPERTY,propKey);
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
        params=new HashMap();
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
    
    public String getImplementationName() {
        return implName;
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
        return "[webservice[name="+name+"][interfacename="+itfName+"][implementationname="+implName+"][contextname="+
                    ctxName+"][sessiontype="+sessType+"]]";
    }
    
    public boolean changed(ServiceConfig sc) {
        if(!equals(getName(),sc.getName())) return true;
        if(!equals(getInterfaceName(),sc.getInterfaceName())) return true;
        if(!equals(getImplementationName(),sc.getImplementationName())) return true;
        if(!equals(getContextName(),sc.getContextName())) return true;
        if(getSessionType()!=sc.getSessionType()) return true;
        Iterator it=getParameterNames();
        int cnt=0;
        while(it.hasNext()) {
            cnt++;
            String param=(String)it.next();
            String val=getParameter(param);
            String scVal=sc.getParameter(param);
            if(scVal==null || !scVal.equals(val)) return true;
        }
        it=sc.getParameterNames();
        int scCnt=0;
        while(it.hasNext()) {
            scCnt++;
            it.next();
        }
        if(cnt!=scCnt) return true;
        return false;
    }
    
    private boolean equals(String s1,String s2) {
        if(s1==null && s2==null) return true;
        if(s1==null || s2==null) return false;
        return s1.equals(s2);
    }
    
    public void saveProperties(File file) throws IOException {
        Properties p=props.getProperties("webservice\\."+getName()+"\\..*");
        p.store(new FileOutputStream(file),"service properties");
    }
    
}
