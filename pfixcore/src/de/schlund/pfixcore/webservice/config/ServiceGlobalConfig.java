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
public class ServiceGlobalConfig extends AbstractConfiguration {

    private final static String PROP_PREFIX="webservice-global.";
    private final static String PROP_REQUESTPATH=PROP_PREFIX+"requestpath";
    private final static String PROP_WSDLSUPPORT=PROP_PREFIX+"wsdlsupport.enabled";
    private final static String PROP_WSDLREPOSITORY=PROP_PREFIX+"wsdlsupport.repository";
    private final static String PROP_ENCODINGSTYLE=PROP_PREFIX+"encoding.style";
    private final static String PROP_ENCODINGUSE=PROP_PREFIX+"encoding.use";
    private final static String PROP_MONITORING=PROP_PREFIX+"monitoring.enabled";
    private final static String PROP_MONITORSCOPE=PROP_PREFIX+"monitoring.scope";
    private final static String PROP_LOGGING=PROP_PREFIX+"logging.enabled";
    
    String reqPath;
    boolean wsdlSupport;
    String wsdlRepo;
    String encStyle;
    String encUse;
    boolean monitoring;
    String monitorScope;
    boolean logging;
    
    public ServiceGlobalConfig(ConfigProperties props) throws ServiceConfigurationException {
        this.props=props;
        init();
    }
    
    private void init() throws ServiceConfigurationException {
        reqPath=props.getProperty(PROP_REQUESTPATH);
        if(reqPath==null) throw new ServiceConfigurationException(ServiceConfigurationException.MISSING_PROPERTY,PROP_REQUESTPATH);
        wsdlSupport=getBooleanProperty(PROP_WSDLSUPPORT,true);
        wsdlRepo=props.getProperty(PROP_WSDLREPOSITORY);
        if(wsdlRepo==null) throw new ServiceConfigurationException(ServiceConfigurationException.MISSING_PROPERTY,PROP_WSDLREPOSITORY);
        encStyle=getStringProperty(PROP_ENCODINGSTYLE,Constants.ENCODING_STYLES,true);
        encUse=getStringProperty(PROP_ENCODINGUSE,Constants.ENCODING_USES,true);
        monitoring=getBooleanProperty(PROP_MONITORING,true);
        monitorScope=getStringProperty(PROP_MONITORSCOPE,Constants.MONITOR_SCOPES,true);
        logging=getBooleanProperty(PROP_LOGGING,true);
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
    
    public boolean getWSDLSupportEnabled() {
        return wsdlSupport;
    }
    
    public String getEncodingStyle() {
        return encStyle;
    }
    
    public String getEncodingUse() {
        return encUse;
    }
    
    public boolean getMonitoringEnabled() {
        return monitoring;
    }
    
    public String getMonitoringScope() {
        return monitorScope;
    }
    
    public boolean getLoggingEnabled() {
        return logging;
    }
    
    public String toString() {
        return "[webservice-global[reqpath="+reqPath+"][wsdlsupport="+wsdlSupport+"][wsdlrepository="+wsdlRepo+"][monitoring="+monitoring+
        "][monitorscope="+monitorScope+"][logging="+logging+"]]";
    }
    
    public boolean changed(ServiceGlobalConfig sgc) {
        System.out.println("check");
        super.changed(sgc);
        if(!equals(getRequestPath(),sgc.getRequestPath())) return true;
        if(getWSDLSupportEnabled()!=sgc.getWSDLSupportEnabled()) return true;
        if(!equals(getWSDLRepository(),sgc.getWSDLRepository())) return true;
        if(getMonitoringEnabled()!=sgc.getMonitoringEnabled()) return true;
        if(getMonitoringScope()!=sgc.getMonitoringScope()) return true;
        if(getLoggingEnabled()!=sgc.getLoggingEnabled()) return true;
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
