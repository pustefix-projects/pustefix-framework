/*
 * de.schlund.pfixcore.webservice.config.GlobalServiceConfig
 */
package de.schlund.pfixcore.webservice.config;

import java.util.Properties;

import de.schlund.pfixcore.webservice.Constants;

/**
 * GlobalServiceConfig.java 
 * 
 * Created: 27.07.2004
 * 
 * @author mleidig
 */
public class GlobalServiceConfig extends AbstractConfig {

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
    
    public GlobalServiceConfig(ConfigProperties props) throws ConfigException {
        this.props=props;
        init();
    }
    
    private void init() throws ConfigException {
        reqPath=props.getProperty(PROP_REQUESTPATH);
        if(reqPath==null) throw new ConfigException(ConfigException.MISSING_PROPERTY,PROP_REQUESTPATH);
        wsdlSupport=props.getBooleanProperty(PROP_WSDLSUPPORT,true);
        wsdlRepo=props.getProperty(PROP_WSDLREPOSITORY);
        if(wsdlRepo==null) throw new ConfigException(ConfigException.MISSING_PROPERTY,PROP_WSDLREPOSITORY);
        encStyle=props.getStringProperty(PROP_ENCODINGSTYLE,Constants.ENCODING_STYLES,true);
        encUse=props.getStringProperty(PROP_ENCODINGUSE,Constants.ENCODING_USES,true);
        monitoring=props.getBooleanProperty(PROP_MONITORING,true);
        monitorScope=props.getStringProperty(PROP_MONITORSCOPE,Constants.MONITOR_SCOPES,true);
        logging=props.getBooleanProperty(PROP_LOGGING,true);
    }
    
    public void reload() throws ConfigException {
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
    
    protected Properties getProperties() {
        Properties p=props.getProperties("webservice-global\\..*");
        return p;
    }
    
    protected String getPropertiesDescriptor() {
        return "global webservice properties";
    }
    
}
