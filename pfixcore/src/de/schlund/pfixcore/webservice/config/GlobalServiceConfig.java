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
    private final static String PROP_STUBGENERATION=PROP_PREFIX+"stubgeneration.enabled";
    private final static String PROP_STUBREPOSITORY=PROP_PREFIX+"stubgeneration.repository";
    private final static String PROP_ENCODINGSTYLE=PROP_PREFIX+"encoding.style";
    private final static String PROP_ENCODINGUSE=PROP_PREFIX+"encoding.use";
    private final static String PROP_ADMIN=PROP_PREFIX+"admin.enabled";
    private final static String PROP_MONITORING=PROP_PREFIX+"monitoring.enabled";
    private final static String PROP_MONITORSCOPE=PROP_PREFIX+"monitoring.scope";
    private final static String PROP_MONITORSIZE=PROP_PREFIX+"monitoring.historysize";
    private final static String PROP_LOGGING=PROP_PREFIX+"logging.enabled";
    
    String reqPath;
    boolean wsdlSupport;
    String wsdlRepo;
    boolean stubGeneration;
    String stubRepo;
    String encStyle;
    String encUse;
    boolean admin;
    boolean monitoring;
    String monitorScope;
    int monitorSize;
    boolean logging;
    
    public GlobalServiceConfig(ConfigProperties props) throws ConfigException {
        this.props=props;
        init();
    }
    
    private void init() throws ConfigException {
        reqPath=props.getProperty(PROP_REQUESTPATH);
        if(reqPath==null) throw new ConfigException(ConfigException.MISSING_PROPERTY,PROP_REQUESTPATH);
        wsdlSupport=props.getBooleanProperty(PROP_WSDLSUPPORT,true,false);
        if(wsdlSupport) wsdlRepo=props.getStringProperty(PROP_WSDLREPOSITORY,true);
        stubGeneration=props.getBooleanProperty(PROP_STUBGENERATION,true,false);
        if(stubGeneration) stubRepo=props.getStringProperty(PROP_STUBREPOSITORY,true);
        encStyle=props.getStringProperty(PROP_ENCODINGSTYLE,Constants.ENCODING_STYLES,true);
        encUse=props.getStringProperty(PROP_ENCODINGUSE,Constants.ENCODING_USES,true);
        admin=props.getBooleanProperty(PROP_ADMIN,true,false);
        monitoring=props.getBooleanProperty(PROP_MONITORING,true,false);
        if(monitoring) {
            monitorScope=props.getStringProperty(PROP_MONITORSCOPE,Constants.MONITOR_SCOPES,true);
            monitorSize=props.getIntegerProperty(PROP_MONITORSIZE,true,0);
        }
        logging=props.getBooleanProperty(PROP_LOGGING,true,false);
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
    
    public String getStubRepository() {
        return stubRepo;
    }
    
    public boolean getStubGenerationEnabled() {
        return stubGeneration;
    }
    
    public String getEncodingStyle() {
        return encStyle;
    }
    
    public String getEncodingUse() {
        return encUse;
    }
    
    public boolean getAdminEnabled() {
        return admin;
    }
    
    public boolean getMonitoringEnabled() {
        return monitoring;
    }
    
    public String getMonitoringScope() {
        return monitorScope;
    }
    
    public int getMonitoringHistorySize() {
    	return monitorSize;
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
