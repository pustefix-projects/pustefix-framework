/*
 * de.schlund.pfixcore.webservice.config.ServiceGlobalConfig
 */
package de.schlund.pfixcore.webservice.config;

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

    ConfigProperties props;
    String reqPath;
    
    public ServiceGlobalConfig(ConfigProperties props) throws ServiceConfigurationException {
        this.props=props;
        init();
    }
    
    private void init() throws ServiceConfigurationException {
        reqPath=props.getProperty(PROP_REQUESTPATH);
        if(reqPath==null) throw new ServiceConfigurationException(ServiceConfigurationException.MISSING_PROPERTY,PROP_REQUESTPATH);
    }
    
    public void reload() throws ServiceConfigurationException {
        init();
    }
    
    public String getRequestPath() {
        return reqPath;
    }
    
    public String toString() {
        return "[webservice-global[reqpath="+reqPath+"]]";
    }
    
}
