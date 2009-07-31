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

import org.pustefixframework.config.contextxmlservice.ContextConfig;
import org.pustefixframework.config.contextxmlservice.ContextConfigHolder;
import org.pustefixframework.config.contextxmlservice.PustefixContextXMLRequestHandlerConfig;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;

import de.schlund.pfixcore.scriptedflow.ScriptedFlowConfig;

/**
 * Very simple implementation of {@link PustefixContextXMLRequestHandlerConfig}, that
 * is used to provide the final configuration object, injected into the
 * request handler.  
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class SimplePustefixContextXMLRequestHandlerConfig extends AbstractPustefixXMLRequestHandlerConfigImpl implements PustefixContextXMLRequestHandlerConfig {

    private ContextConfig contextConfig;

    private ScriptedFlowConfig scriptedFlowConfig;

    public void setContextConfig(ContextConfig contextConfig) {
        this.contextConfig = contextConfig;
    }

    public ContextConfig getContextConfig() {
        return contextConfig;
    }

    public void setScriptedFlowConfig(ScriptedFlowConfig scriptedFlowConfig) {
        this.scriptedFlowConfig = scriptedFlowConfig;
    }

    public ScriptedFlowConfig getScriptedFlowConfig() {
        return scriptedFlowConfig;
    }
    
    public static BeanDefinition generateBeanDefinition(PustefixContextXMLRequestHandlerConfig config, ContextConfigHolder contextConfigHolder) {
        BeanDefinitionBuilder beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(SimplePustefixContextXMLRequestHandlerConfig.class);
        beanBuilder.setScope("singleton");
        beanBuilder.addPropertyValue("properties", config.getProperties());
        beanBuilder.addPropertyValue("scriptedFlowConfig", config.getScriptedFlowConfig());
        beanBuilder.addPropertyValue("servletName", config.getServletName());
        beanBuilder.addPropertyValue("SSL", config.isSSL());
        beanBuilder.addPropertyValue("contextConfig", contextConfigHolder.getContextConfigObject());
        return beanBuilder.getBeanDefinition();
    }
}
