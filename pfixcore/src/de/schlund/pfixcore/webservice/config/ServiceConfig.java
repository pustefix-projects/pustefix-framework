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
public class ServiceConfig extends AbstractConfig {

    private final static String PROP_PREFIX="webservice.";
    private final static String PROP_ITFNAME=".interface.name";
    private final static String PROP_IMPLNAME=".implementation.name";
    private final static String PROP_CTXNAME=".context.name";
    private final static String PROP_SESSTYPE=".session.type";
    private final static String PROP_SCOPETYPE=".scope.type";
    private final static String PROP_SSLFORCE=".ssl.force";
    private final static String PROP_ENCODINGSTYLE=".encoding.style";
    private final static String PROP_ENCODINGUSE=".encoding.use";

    String  name;
    String  itfName;
    String  implName;
    String  ctxName;
    String  sessType=Constants.SESSION_TYPE_SERVLET;
    String scopeType;
    boolean sslForce;
    String  encStyle;
    String  encUse;
    HashMap params;
    
    public ServiceConfig() {
        
    }
    
    public ServiceConfig(ConfigProperties props,String name) throws ConfigException {
        this.props=props;
        this.name=name;
        init();
    }
    
    private void init() throws ConfigException {
        String prefix = PROP_PREFIX + name;
        itfName       = props.getStringProperty(prefix + PROP_ITFNAME,true);
        implName      = props.getStringProperty(prefix + PROP_IMPLNAME,true);
        ctxName       = props.getStringProperty(prefix + PROP_CTXNAME,false);
        sessType      = props.getStringProperty(prefix + PROP_SESSTYPE,Constants.SESSION_TYPES,true);
        scopeType=props.getStringProperty(prefix+PROP_SCOPETYPE,Constants.SERVICE_SCOPES,false);
        sslForce      = props.getBooleanProperty(prefix + PROP_SSLFORCE,false,false);
        encStyle      = props.getStringProperty(prefix + PROP_ENCODINGSTYLE,Constants.ENCODING_STYLES,false);
        encUse        = props.getStringProperty(prefix + PROP_ENCODINGUSE,Constants.ENCODING_USES,false);
        //TODO: get params
        params        = new HashMap();
    }

    public void reload() throws ConfigException {
        init();
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name=name;
    }
    
    public String getInterfaceName() {
        return itfName;
    }
    
    public void setInterfaceName(String itfName) {
        this.itfName=itfName;
    }
    
    public String getImplementationName() {
        return implName;
    }
    
    public void setImplementationName(String implName) {
        this.implName=implName;
    }
    
    public void setContextName(String ctxName) {
        this.ctxName=ctxName;
    }
    
    public String getContextName() {
        return ctxName;
    }
    
    public void setSessionType(String sessType) {
        this.sessType=sessType;
    }
    
    public String getSessionType() {
        return sessType;
    }
    
    public void setScopeType(String scopeType) {
    	this.scopeType=scopeType;
    }
    
    public String getScopeType() {
    	return scopeType;
    }
    
    public boolean getSSLForce() {
        return sslForce;
    }
    
    public void setSSLForce(boolean sslForce) {
        this.sslForce=sslForce;
    }
    
    public String getEncodingStyle() {
        return encStyle;
    }
    
    public String getEncodingUse() {
        return encUse;
    }
    
    public Iterator getParameterNames() {
        return params.keySet().iterator();
    }
    
    public String getParameter(String name) {
        return (String)params.get(name);
    }

    protected Properties getProperties() {
        return props.getProperties("webservice\\."+getName()+"\\..*");
    }
    
    protected String getPropertiesDescriptor() {
        return "webservice properties";
    }
    
}
