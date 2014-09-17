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

import org.pustefixframework.config.contextxmlservice.parser.internal.IWrapperConfigImpl;
import org.pustefixframework.web.mvc.InputHandler;
import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultBeanNameGenerator;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

/**
 * Implements {@link BeanFactoryPostProcessor} for post-processing {@link IWrapperConfigImpl} bean
 * definitions to replace {@link InputHandler} property references by {@link InputHandlerAdapter}
 * references and adding according bean definitions.
 * 
 */
public class InputHandlerProcessor implements BeanFactoryPostProcessor {

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        
        DefaultBeanNameGenerator nameGenerator = new DefaultBeanNameGenerator();
        String[] beanNames = beanFactory.getBeanNamesForType(IWrapperConfigImpl.class);
        for(String beanName: beanNames) {
            BeanDefinition beanDef = beanFactory.getBeanDefinition(beanName);
            PropertyValue propVal = beanDef.getPropertyValues().getPropertyValue("handler");
            if(propVal != null) {
                Object val = propVal.getValue();
                if(val != null && val instanceof RuntimeBeanReference) {
                    RuntimeBeanReference ref = (RuntimeBeanReference)val;
                    BeanDefinition refDef = beanFactory.getBeanDefinition(ref.getBeanName());
                    refDef = getOriginatingBeanDefinition(refDef);
                    try {
                        Class<?> refClass = Class.forName(refDef.getBeanClassName());
                        if(InputHandler.class.isAssignableFrom(refClass)) {
                            BeanDefinitionBuilder beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(InputHandlerAdapter.class);
                            beanBuilder.addPropertyReference("delegate", ref.getBeanName());
                            BeanDefinition beanDefinition = beanBuilder.getBeanDefinition();
                            String handlerBeanName = nameGenerator.generateBeanName(beanDefinition, (DefaultListableBeanFactory)beanFactory);
                            ((DefaultListableBeanFactory)beanFactory).registerBeanDefinition(handlerBeanName, beanDefinition);
                            beanDef.getPropertyValues().addPropertyValue("handler", new RuntimeBeanReference(handlerBeanName));
                        }
                    } catch (ClassNotFoundException e) {
                        throw new FatalBeanException("Error while post processing bean defintions for InputHandler setup", e);
                    }
                }
            }
        }
    }

    private BeanDefinition getOriginatingBeanDefinition(BeanDefinition beanDef) {
        
        BeanDefinition originBeanDef = beanDef.getOriginatingBeanDefinition();
        if(originBeanDef != null) {
            return getOriginatingBeanDefinition(originBeanDef);
        } else {
            return beanDef;
        }
    }
    
}
