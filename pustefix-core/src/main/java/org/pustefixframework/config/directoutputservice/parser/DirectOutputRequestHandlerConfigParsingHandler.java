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

package org.pustefixframework.config.directoutputservice.parser;

import java.util.List;
import java.util.Properties;

import org.pustefixframework.config.customization.CustomizationAwareParsingHandler;
import org.pustefixframework.config.directoutputservice.DirectOutputPageRequestConfigHolder;
import org.pustefixframework.config.directoutputservice.DirectOutputRequestHandlerConfigHolder;
import org.pustefixframework.config.directoutputservice.parser.internal.DirectOutputPageRequestConfigExtensionPointImpl;
import org.pustefixframework.config.directoutputservice.parser.internal.DirectOutputPageRequestConfigMap;
import org.pustefixframework.config.directoutputservice.parser.internal.DirectOutputRequestHandlerConfigImpl;
import org.pustefixframework.config.generic.ParsingUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.beans.factory.support.DefaultBeanNameGenerator;
import org.springframework.beans.factory.support.ManagedList;

import com.marsching.flexiparse.configuration.RunOrder;
import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.exception.ParserException;

public class DirectOutputRequestHandlerConfigParsingHandler extends CustomizationAwareParsingHandler {

    @Override
    protected void handleNodeIfActive(HandlerContext context) throws ParserException {
        if (context.getRunOrder() == RunOrder.START) {
            DirectOutputRequestHandlerConfigImpl serviceConfig = new DirectOutputRequestHandlerConfigImpl();
            Properties properties = new Properties(System.getProperties());
            serviceConfig.setProperties(properties);
            context.getObjectTreeElement().addObject(serviceConfig);
        } else if (context.getRunOrder() == RunOrder.END) {
            DirectOutputRequestHandlerConfigImpl serviceConfig = context.getObjectTreeElement().getObjectsOfType(DirectOutputRequestHandlerConfigImpl.class).iterator().next();
            BeanDefinitionRegistry beanRegistry = ParsingUtils.getSingleTopObject(BeanDefinitionRegistry.class, context);
            
            BeanDefinitionBuilder beanBuilder;
            BeanDefinition beanDefinition;
            BeanNameGenerator beanNameGenerator = new DefaultBeanNameGenerator();
            
            @SuppressWarnings("unchecked")
            List<Object> pageObjects = new ManagedList();
            for (Object o : context.getObjectTreeElement().getObjectsOfTypeFromSubTree(Object.class)) {
                if (o instanceof DirectOutputPageRequestConfigHolder) {
                    DirectOutputPageRequestConfigHolder holder = (DirectOutputPageRequestConfigHolder) o;
                    pageObjects.add(holder.getDirectOutputPageRequestConfigObject());
                } else if (o instanceof DirectOutputPageRequestConfigExtensionPointImpl) {
                    pageObjects.add(o);
                }
            }
            
            beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(DirectOutputPageRequestConfigMap.class);
            beanBuilder.setScope("singleton");
            beanBuilder.addPropertyValue("directOutputPageRequestConfigObjects", pageObjects);
            beanDefinition = beanBuilder.getBeanDefinition();
            String pageMapBeanName = beanNameGenerator.generateBeanName(beanDefinition, beanRegistry);
            beanRegistry.registerBeanDefinition(pageMapBeanName, beanDefinition);
            
            beanDefinition = serviceConfig.createBeanDefinition(new RuntimeBeanReference(pageMapBeanName));
            String configBeanName = beanNameGenerator.generateBeanName(beanDefinition, beanRegistry);
            beanRegistry.registerBeanDefinition(configBeanName, beanDefinition);
            final RuntimeBeanReference beanReference = new RuntimeBeanReference(configBeanName);
            
            context.getObjectTreeElement().addObject(new DirectOutputRequestHandlerConfigHolder() {

                public Object getDirectOutputServiceConfigObject() {
                    return beanReference;
                }
                
            });
        }
    }

}
