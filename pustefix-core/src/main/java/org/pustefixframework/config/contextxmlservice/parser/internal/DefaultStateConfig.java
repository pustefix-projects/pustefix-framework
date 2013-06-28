package org.pustefixframework.config.contextxmlservice.parser.internal;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.pustefixframework.config.contextxmlservice.IWrapperConfig;
import org.pustefixframework.config.contextxmlservice.ProcessActionStateConfig;
import org.pustefixframework.config.contextxmlservice.StateConfig;

import de.schlund.pfixcore.workflow.ConfigurableState;
import de.schlund.pfixxml.Tenant;

/**
 * StateConfig implementation for default states.
 *
 */
public class DefaultStateConfig implements StateConfig {
	
	private Class<? extends ConfigurableState> stateType;
	private Map<String, ?> resources = new HashMap<String, Object>();
	
	public Map<String, ?> getContextResources() {
		return resources;
	}
	
	public void setContextResources(Map<String, ?> resources) {
		this.resources = resources;
	}

    public Policy getIWrapperPolicy() {
        return Policy.ANY;
    }

    public Map<String, ? extends IWrapperConfig> getIWrappers(Tenant tenant) {
        return Collections.emptyMap();
    }

    public Map<String, ? extends ProcessActionStateConfig> getProcessActions() {
        return Collections.emptyMap();
    }

    public Properties getProperties() {
        return new Properties();
    }

    public String getScope() {
        return "prototype";
    }

    public Class<? extends ConfigurableState> getState() {
        return stateType;
    }
     
    public void setState(Class<? extends ConfigurableState> stateType) {
    	this.stateType = stateType;
    }

    public boolean isExternalBean() {
        return false;
    }

    public boolean requiresToken() {
        return false;
    }

}
