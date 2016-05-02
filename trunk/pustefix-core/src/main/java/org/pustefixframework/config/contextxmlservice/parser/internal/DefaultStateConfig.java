package org.pustefixframework.config.contextxmlservice.parser.internal;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

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
    private Set<String> lazyResources = new HashSet<String>();
    
    public Map<String, ?> getContextResources() {
        return resources;
    }
    
    public boolean isLazyContextResource(String prefix) {
        return lazyResources.contains(prefix);
    }
    
    public void setContextResources(Map<String, ?> resources) {
        this.resources = resources;
    }
    
    public void setLazyContextResources(Set<String> lazyResources) {
        this.lazyResources = lazyResources;
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
