package org.pustefixframework.http;

import java.util.Properties;

import org.pustefixframework.config.contextxmlservice.ServletManagerConfig;

public class SessionHandlingTestHandlerConfig implements ServletManagerConfig {

    private Properties properties = new Properties();
    
    public SessionHandlingTestHandlerConfig() {
        properties.setProperty("pfixcore.ssl_redirect_port.for.8080", "8443");
        properties.setProperty("servlet.encoding", "utf-8");
    }
    
    public boolean isSSL() {
        return false;
    }

    public Properties getProperties() {
        return properties;
    }

    public boolean needsReload() {
        return false;
    }
    
}
