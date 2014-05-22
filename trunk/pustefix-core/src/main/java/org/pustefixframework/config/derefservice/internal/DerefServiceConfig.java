package org.pustefixframework.config.derefservice.internal;

import java.util.Enumeration;
import java.util.Properties;

import org.pustefixframework.config.contextxmlservice.ServletManagerConfig;

public class DerefServiceConfig implements ServletManagerConfig {

	private Properties properties = new Properties();
	
	@Override
	public Properties getProperties() {
		return properties;
	}

    public void setProperties(Properties props) {
        Enumeration<?> e = props.propertyNames();
        while (e.hasMoreElements()) {
            String propname = (String) e.nextElement();
            properties.setProperty(propname, props.getProperty(propname));
        }
    }
	
	@Override
	public boolean isSSL() {
		return false;
	}
	
	public boolean needsReload() {
		return false;
	}

}
