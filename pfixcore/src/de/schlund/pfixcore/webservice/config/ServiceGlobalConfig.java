/*
 * de.schlund.pfixcore.webservice.config.ServiceGlobalConfig
 */
package de.schlund.pfixcore.webservice.config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

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
    
    public boolean changed(ServiceGlobalConfig sgc) {
        if(!equals(getRequestPath(),sgc.getRequestPath())) return true;
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
