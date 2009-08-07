/*
 * This file is part of Pustefix.
 *
 * Pustefix is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Pustefix is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Pustefix; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.pustefixframework.config.contextxmlservice.parser.internal;

import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import org.pustefixframework.config.contextxmlservice.IWrapperConfig;
import org.pustefixframework.config.contextxmlservice.ProcessActionStateConfig;
import org.pustefixframework.config.contextxmlservice.StateConfig;

import de.schlund.pfixcore.workflow.ConfigurableState;

/**
 * Stores configuration for a PageRequest
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class StateConfigImpl implements Cloneable, StateConfig {
    
    private Class<? extends ConfigurableState> stateClass = null;
    private Class<? extends ConfigurableState> defaultStaticStateClass = null;
    private String defaultStaticStateParentBeanName = null;
    private Class<? extends ConfigurableState> defaultIWrapperStateClass = null;
    private String defaultIWrapperStateParentBeanName = null;
    private Map<String, IWrapperConfig> iwrappers = new LinkedHashMap<String, IWrapperConfig>();
    private Map<String, Object> resources = new LinkedHashMap<String, Object>();
    private Properties props = new Properties();
    private StateConfig.Policy policy = StateConfig.Policy.ANY;
    private boolean requiresToken = false;
    private boolean externalBean = false;
    private String beanName;
    private String parentBeanName = null;
    private String scope = "singleton";
    private Map<String, ProcessActionStateConfig> actions = new LinkedHashMap<String, ProcessActionStateConfig>();
    
    public void setState(Class<? extends ConfigurableState> clazz) {
        this.stateClass = clazz;
    }
    
    public Class<? extends ConfigurableState> getState() {
        return this.stateClass;
    }
    
    public String getParentBeanName() {
        return parentBeanName;
    }
    
    public void setParentBeanName(String parentBeanName) {
        this.parentBeanName = parentBeanName;
    }
    
    public void setDefaultStaticState(Class<? extends ConfigurableState> clazz) {
        this.defaultStaticStateClass = clazz;
    }

    public Class<? extends ConfigurableState> getDefaultStaticState() {
        return this.defaultStaticStateClass;
    }
    
    public void setDefaultStaticStateParentBeanName(String parentBeanName) {
        defaultStaticStateParentBeanName = parentBeanName;
    }
    
    public String getDefaultStaticStateParentBeanName() {
        return defaultStaticStateParentBeanName;
    }
    
    public void setDefaultIHandlerState(Class<? extends ConfigurableState> clazz) {
        this.defaultIWrapperStateClass = clazz;
    }
    
    public Class<? extends ConfigurableState> getDefaultIHandlerState() {
        return this.defaultIWrapperStateClass;
    }
    
    public void setDefaultIHandlerStateParentBeanName(String parentBeanName) {
        defaultIWrapperStateParentBeanName = parentBeanName;
    }
    
    public String getDefaultIHandlerStateParentBeanName() {
        return defaultIWrapperStateParentBeanName;
    }
    
    public void setIWrapperPolicy(StateConfig.Policy policy) {
        this.policy = policy;
    }
    
    public StateConfig.Policy getIWrapperPolicy() {
        return this.policy;
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

    @Override
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
        //When Spring's PropertyPlaceholderConfigurer is used it will clear and
        //refill property value maps, even if there's nothing to replace, take a
        //look at http://jira.springframework.org/browse/SPR-5318 
        //To be able to work with Spring 2.5.6 we'll return a modifiable map here
        //TODO: return unmodifiable map when issue is fixed
        return this.actions; 
        //return Collections.unmodifiableMap(this.actions);
    }
    
    public void addProcessAction(String name, ProcessActionStateConfig action) {
        actions.put(name, action);
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public String getBeanName() {
        return beanName;
    }
 }
