/*
 * de.schlund.pfixcore.webservice.config.ServiceGlobalConfig
 */
package de.schlund.pfixcore.webservice.config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import de.schlund.pfixcore.webservice.Constants;

/**
 * ServiceGlobalConfig.java 
 * 
 * Created: 27.07.2004
 * 
 * @author mleidig
 */
public class ServiceGlobalConfig {

    private final static String PROP_PREFIX="webservice-global.";
    private final static String PROP_REQUESTPATH=PROP_PREFIX+"requestpath";
    private final static String PROP_WSDLSUPPORT=PROP_PREFIX+"wsdlsupport.enabled";
    private final static String PROP_WSDLREPOSITORY=PROP_PREFIX+"wsdlsupport.repository";
    private final static String PROP_MONITORING=PROP_PREFIX+"monitoring.enabled";
    private final static String PROP_MONITORSCOPE=PROP_PREFIX+"monitoring.scope";

    ConfigProperties props;
    String reqPath;
    boolean wsdlSupport;
    String wsdlRepo;
    boolean monitoring;
    int monitorScope;
    
    public ServiceGlobalConfig(ConfigProperties props) throws ServiceConfigurationException {
        this.props=props;
        init();
    }
    
    private void init() throws ServiceConfigurationException {
        reqPath=props.getProperty(PROP_REQUESTPATH);
        if(reqPath==null) throw new ServiceConfigurationException(ServiceConfigurationException.MISSING_PROPERTY,PROP_REQUESTPATH);
        if(props.getProperty(PROP_WSDLSUPPORT)==null) throw new ServiceConfigurationException(ServiceConfigurationException.MISSING_PROPERTY,PROP_WSDLSUPPORT);
        wsdlSupport=new Boolean(props.getProperty(PROP_WSDLSUPPORT)).booleanValue();
        wsdlRepo=props.getProperty(PROP_WSDLREPOSITORY);
        if(wsdlRepo==null) throw new ServiceConfigurationException(ServiceConfigurationException.MISSING_PROPERTY,PROP_WSDLREPOSITORY);
        if(props.getProperty(PROP_MONITORING)==null) throw new ServiceConfigurationException(ServiceConfigurationException.MISSING_PROPERTY,PROP_MONITORING);
        monitoring=new Boolean(props.getProperty(PROP_MONITORING)).booleanValue();
        if(props.getProperty(PROP_MONITORSCOPE)==null) throw new ServiceConfigurationException(ServiceConfigurationException.MISSING_PROPERTY,PROP_MONITORSCOPE);
        if(props.getProperty(PROP_MONITORSCOPE).equalsIgnoreCase("session")) monitorScope=Constants.MONITOR_SCOPE_SESSION;
        else if(props.getProperty(PROP_MONITORSCOPE).equalsIgnoreCase("ip")) monitorScope=Constants.MONITOR_SCOPE_IP;
        else throw new ServiceConfigurationException(ServiceConfigurationException.ILLEGAL_PROPERTY_VALUE,PROP_MONITORSCOPE,props.getProperty(PROP_MONITORSCOPE));
    }
    
    public void reload() throws ServiceConfigurationException {
        init();
    }
    
    public String getRequestPath() {
        return reqPath;
    }
    
    public String getWSDLRepository() {
        return wsdlRepo;
    }
    
    public boolean isWSDLSupportEnabled() {
        return wsdlSupport;
    }
    
    public boolean monitoringEnabled() {
        return monitoring;
    }
    
    public int getMonitoringScope() {
        return monitorScope;
    }
    
    public String toString() {
        return "[webservice-global[reqpath="+reqPath+"][wsdlsupport="+wsdlSupport+"][wsdlrepository="+wsdlRepo+"][monitoring="+monitoring+
        "][monitorscope="+monitorScope+"]]";
    }
    
    public boolean changed(ServiceGlobalConfig sgc) {
        if(!equals(getRequestPath(),sgc.getRequestPath())) return true;
        if(isWSDLSupportEnabled()!=sgc.isWSDLSupportEnabled()) return true;
        if(!equals(getWSDLRepository(),sgc.getWSDLRepository())) return true;
        if(monitoringEnabled()!=sgc.monitoringEnabled()) return true;
        if(getMonitoringScope()!=sgc.getMonitoringScope()) return true;
        return false;
    }
    
    private boolean equals(String s1,String s2) {
        if(s1==null && s2==null) return true;
        if(s1==null || s2==null) return false;
        return s1.equals(s2);
    }
    
    public void saveProperties(File file) throws IOException {
        Properties p=props.getProperties("webservice-global\\..*");
        p.store(new FileOutputStream(file),"global properties");
    }
    
}
