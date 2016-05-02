package org.pustefixframework.http;

import java.util.Properties;

import org.pustefixframework.config.contextxmlservice.ServletManagerConfig;

public class SessionHandlingTestHandlerConfig implements ServletManagerConfig {

    private Properties properties;
    
    public SessionHandlingTestHandlerConfig(Properties properties) {
        this.properties = properties;
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
