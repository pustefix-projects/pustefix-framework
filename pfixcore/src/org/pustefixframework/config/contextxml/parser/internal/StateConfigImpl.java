/*
 * This file is part of PFIXCORE.
 *
 * PFIXCORE is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * PFIXCORE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with PFIXCORE; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.pustefixframework.config.contextxml.parser.internal;

import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import org.pustefixframework.config.contextxml.IWrapperConfig;
import org.pustefixframework.config.contextxml.ProcessActionStateConfig;
import org.pustefixframework.config.contextxml.StateConfig;

import de.schlund.pfixcore.workflow.ConfigurableState;
import de.schlund.pfixcore.workflow.app.ResdocFinalizer;

/**
 * Stores configuration for a PageRequest
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class StateConfigImpl implements Cloneable, StateConfig {
    
    private Class<? extends ConfigurableState> stateClass = null;
    private Class<? extends ConfigurableState> defaultStaticStateClass = null;
    private Class<? extends ConfigurableState> defaultIWrapperStateClass = null;
    private Class<? extends ResdocFinalizer> finalizer = null;
    private Map<String, IWrapperConfig> iwrappers = new LinkedHashMap<String, IWrapperConfig>();
    private Map<String, Object> resources = new LinkedHashMap<String, Object>();
    private Properties props = new Properties();
    private StateConfig.Policy policy = StateConfig.Policy.ANY;
    private boolean requiresToken = false;
    private boolean externalBean = false;
    private String scope = "singleton";
    private Map<String, ProcessActionStateConfig> actions = new LinkedHashMap<String, ProcessActionStateConfig>();
    
    public void setState(Class<? extends ConfigurableState> clazz) {
        this.stateClass = clazz;
    }
    
    public Class<? extends ConfigurableState> getState() {
        if (this.stateClass != null) {
            return this.stateClass;
        } else {
            if (this.iwrappers.size() > 0) {
                return this.defaultIWrapperStateClass;
            } else {
                return this.defaultStaticStateClass;
            }
        }
    }
    
    public void setDefaultStaticState(Class<? extends ConfigurableState> clazz) {
        this.defaultStaticStateClass = clazz;
    }
    
    public void setDefaultIHandlerState(Class<? extends ConfigurableState> clazz) {
        this.defaultIWrapperStateClass = clazz;
    }
    
    public void setIWrapperPolicy(StateConfig.Policy policy) {
        this.policy = policy;
    }
    
    public StateConfig.Policy getIWrapperPolicy() {
        return this.policy;
    }
    
    public void setFinalizer(Class<? extends ResdocFinalizer> clazz) {
        this.finalizer = clazz;
    }
    
    public Class<? extends ResdocFinalizer> getFinalizer() {
        return this.finalizer;
    }
    
    public void addIWrapper(IWrapperConfigImpl config) {
        this.iwrappers.put(config.getPrefix(), config);
    }
    
    public void setIWrappers(Map<String, IWrapperConfig> iwrappers) {
        this.iwrappers = iwrappers;
    }
    
    public Map<String, IWrapperConfig> getIWrappers() {
        return Collections.unmodifiableMap(this.iwrappers);
    }
    
    public void addContextResource(String prefix, Object resource) {
        this.resources.put(prefix, resource);
    }
    
    public void setContextResources(Map<String, Object> resources) {
        this.resources = resources;
    }
    
    public Map<String, ?> getContextResources() {
        return this.resources;
    }
    
    public void setProperties(Properties props) {
        this.props = new Properties();
        Enumeration<?> e = props.propertyNames();
        while (e.hasMoreElements()) {
            String propname = (String) e.nextElement();
            this.props.setProperty(propname, props.getProperty(propname));
        }
    }
    
    public Properties getProperties() {
        return this.props;
    }
 
    public boolean requiresToken() {
        return requiresToken;
    }
    
    public void setRequiresToken(boolean requiresToken) {
        this.requiresToken = requiresToken;
    }

    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
    
    public boolean isExternalBean() {
        return externalBean;
    }
    
    public void setExternalBean(boolean useBeanReference) {
        this.externalBean = useBeanReference;
    }
    
    public String getScope() {
        return scope;
    }
    
    public void setScope(String scope) {
        this.scope = scope;
    }
    
    public void setProcessActions(Map<String, ProcessActionStateConfig> actions) {
        this.actions = actions;
    }
    
    public Map<String, ? extends ProcessActionStateConfig> getProcessActions() {
        return Collections.unmodifiableMap(this.actions);
    }
    
    public void addProcessAction(String name, ProcessActionStateConfig action) {
        actions.put(name, action);
    }
 }
