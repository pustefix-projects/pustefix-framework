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

import java.util.Map;

import org.pustefixframework.config.contextxmlservice.PustefixContextXMLRequestHandlerConfig;
import org.pustefixframework.config.contextxmlservice.SSLOption;
import org.pustefixframework.config.contextxmlservice.ScriptedFlowProvider;

import de.schlund.pfixcore.workflow.ConfigurableState;

/**
 * Stores configuration for a Pustefix servlet
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class PustefixContextXMLRequestHandlerConfigImpl extends AbstractPustefixXMLRequestHandlerConfigImpl implements PustefixContextXMLRequestHandlerConfig, SSLOption {
    private final static Class<de.schlund.pfixcore.workflow.app.DefaultIWrapperState> DEFAULT_IHANDLER_STATE = de.schlund.pfixcore.workflow.app.DefaultIWrapperState.class;

    private final static Class<de.schlund.pfixcore.workflow.app.StaticState> DEFAULT_STATIC_STATE = de.schlund.pfixcore.workflow.app.StaticState.class;

    private Class<? extends ConfigurableState> defaultStateClass = DEFAULT_STATIC_STATE;
    private String defaultStaticStateParentBeanName;
    
    private Class<? extends ConfigurableState> defaultIHandlerStateClass = DEFAULT_IHANDLER_STATE;
    private String defaultIHandlerStateParentBeanName;

    private ContextConfigImpl contextConfig;

    private Map<String, ? extends ScriptedFlowProvider> scriptedFlows;

    private Map<String, ?> jsonOutputResources;

    public void setDefaultStaticState(Class<? extends ConfigurableState> clazz) {
        this.defaultStateClass = clazz;
    }

    public Class<? extends ConfigurableState> getDefaultStaticState() {
        return this.defaultStateClass;
    }

    public void setDefaultStaticStateParentBeanName(String parentBeanName) {
        defaultStaticStateParentBeanName = parentBeanName;
    }
    
    public String getDefaultStaticStateParentBeanName() {
        return defaultStaticStateParentBeanName;
    }
    
    public void setDefaultIHandlerState(Class<? extends ConfigurableState> clazz) {
        this.defaultIHandlerStateClass = clazz;
    }

    public Class<? extends ConfigurableState> getDefaultIHandlerState() {
        return this.defaultIHandlerStateClass;
    }
    
    public void setDefaultIHandlerStateParentBeanName(String parentBeanName) {
        defaultIHandlerStateParentBeanName = parentBeanName;
    }
    
    public String getDefaultIHandlerStateParentBeanName() {
        return defaultIHandlerStateParentBeanName;
    }

    public void setContextConfig(ContextConfigImpl config) {
        this.contextConfig = config;
    }

    /* (non-Javadoc)
     * @see de.schlund.pfixxml.config.ContextXMLServletConfig#getContextConfig()
     */
    public ContextConfigImpl getContextConfig() {
        return this.contextConfig;
    }

    public Map<String, ? extends ScriptedFlowProvider> getScriptedFlows() {
        return this.scriptedFlows;
    }

    public void setScriptedFlows(Map<String, ? extends ScriptedFlowProvider> scriptedFlows) {
        this.scriptedFlows = scriptedFlows;
    }

    public void setJSONOutputResources(Map<String, ?> jsonOutputResources) {
        this.jsonOutputResources = jsonOutputResources;
    }

    public Map<String, ?> getJSONOutputResources() {
        return jsonOutputResources;
    }

}