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
package org.pustefixframework.web.mvc.internal;

import org.pustefixframework.web.mvc.RequestMappingHandlerAdapterConfig;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

public class ControllerStatePostProcessor implements BeanFactoryPostProcessor {

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

        String configBeanName = null;
        String[] beanNames = beanFactory.getBeanDefinitionNames();
        for(String beanName: beanNames) {
            BeanDefinition beanDef = beanFactory.getBeanDefinition(beanName);
            if(RequestMappingHandlerAdapterConfig.class.getName().equals(beanDef.getBeanClassName())) {
                configBeanName = beanName;
            }
        }
        if(configBeanName == null) {
            BeanDefinitionBuilder beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(RequestMappingHandlerAdapterConfig.class);
            beanBuilder.setFactoryMethod("createDefaultConfig");
            BeanDefinition beanDefinition = beanBuilder.getBeanDefinition();
            configBeanName = RequestMappingHandlerAdapterConfig.class.getName();
            ((DefaultListableBeanFactory)beanFactory).registerBeanDefinition(configBeanName, beanDefinition);
        }
        BeanDefinitionBuilder beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(ControllerStateAdapterImpl.class);
        beanBuilder.addPropertyReference("adapterConfig", configBeanName);
        BeanDefinition definition = beanBuilder.getBeanDefinition();
        ((DefaultListableBeanFactory)beanFactory).registerBeanDefinition(ControllerStateAdapter.class.getName(), definition);

    }

}
