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
package org.pustefixframework.webservices.spring;

import org.osgi.framework.BundleContext;
import org.pustefixframework.config.Constants;
import org.pustefixframework.config.customization.CustomizationAwareParsingHandler;
import org.pustefixframework.config.generic.ParsingUtils;
import org.pustefixframework.webservices.ServiceRuntime;
import org.pustefixframework.webservices.config.WebserviceConfiguration;
import org.pustefixframework.webservices.osgi.ProtocolProviderServiceTracker;
import org.pustefixframework.webservices.osgi.WebserviceExtensionPointImpl;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.osgi.context.ConfigurableOsgiBundleApplicationContext;
import org.w3c.dom.Element;

import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.exception.ParserException;

import de.schlund.pfixcore.workflow.ContextImpl;
import de.schlund.pfixcore.workflow.context.ServerContextImpl;

/**
 * 
 * @author mleidig
 *
 */
public class WebServiceRequestHandlerParsingHandler extends CustomizationAwareParsingHandler {
    
    private WebserviceConfiguration configuration;
    
    @Override
    protected void handleNodeIfActive(HandlerContext context) throws ParserException {
        
        Element serviceElement = (Element)context.getNode();
        String elementName = serviceElement.getLocalName();
        if(elementName.equals("webservice-service")) {
        
            Element pathElement = (Element)serviceElement.getElementsByTagNameNS(Constants.NS_APPLICATION,"path").item(0);
            if (pathElement == null) throw new ParserException("Could not find expected <path> element");
            String path = pathElement.getTextContent().trim();
           
            configuration = new WebserviceConfiguration();
            
            BeanDefinitionBuilder beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(WebServiceHttpRequestHandler.class);
            beanBuilder.setScope("singleton");
            beanBuilder.addPropertyValue("configuration", configuration);
            beanBuilder.addPropertyValue("handlerURI", path + "/**");
            beanBuilder.addPropertyValue("serviceRuntime", new RuntimeBeanReference(ServiceRuntime.class.getName()));
            BeanDefinition beanDefinition = beanBuilder.getBeanDefinition();
            BeanDefinitionHolder beanHolder = new BeanDefinitionHolder(beanDefinition, WebServiceHttpRequestHandler.class.getName()+"#"+path);
            context.getObjectTreeElement().addObject(beanHolder);
            
            beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(ServiceRuntime.class);
            beanBuilder.setScope("singleton");
            beanBuilder.addPropertyValue("serverContext", new RuntimeBeanReference(ServerContextImpl.class.getName()));
            beanBuilder.addPropertyValue("context", new RuntimeBeanReference(ContextImpl.class.getName()));
            beanBuilder.addPropertyValue("protocolProviderRegistry", new RuntimeBeanReference(ProtocolProviderServiceTracker.class.getName()));
            beanDefinition = beanBuilder.getBeanDefinition();
            beanHolder = new BeanDefinitionHolder(beanDefinition, ServiceRuntime.class.getName());
            context.getObjectTreeElement().addObject(beanHolder);
            
            beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(ProtocolProviderServiceTracker.class);
            beanBuilder.setScope("singleton");
            ConfigurableOsgiBundleApplicationContext appContext = ParsingUtils.getSingleTopObject(ConfigurableOsgiBundleApplicationContext.class, context);
            BundleContext bundleContext = appContext.getBundleContext();
            beanBuilder.addConstructorArgValue(bundleContext);
            beanDefinition = beanBuilder.getBeanDefinition();
            beanHolder = new BeanDefinitionHolder(beanDefinition, ProtocolProviderServiceTracker.class.getName());
            context.getObjectTreeElement().addObject(beanHolder);
            
            beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(WebserviceExtensionPointImpl.class);
    		beanBuilder.addPropertyValue("id", "webservice.application");
        
        } else if(elementName.equals("admin")) {
            configuration.setAdminEnabled(true);
        } else if(elementName.equals("monitoring")) {
            configuration.setMonitoringEnabled(true);
        } else if(elementName.equals("logging")) {
            configuration.setLoggingEnabled(true);
        }
		
    }

}
