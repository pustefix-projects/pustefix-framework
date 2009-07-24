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

package org.pustefixframework.container.spring.beans;

import java.io.IOException;

import org.pustefixframework.container.spring.beans.internal.WebApplicationContextStore;
import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.web.context.support.WebApplicationContextUtils;

import de.schlund.pfixcore.workflow.ContextImpl;


/**
 * <code>ApplicationContext</code> implementation that is used for Pustefix
 * modules. This implementation is OSGi-aware and thus can be used to 
 * import or export services from or to other bundles.
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class PustefixOsgiApplicationContext extends PustefixAbstractOsgiApplicationContext {
    
    protected void loadBeanDefinitions(DefaultListableBeanFactory beanFactory) throws IOException, BeansException {
        String configLocations[] = getConfigLocations();
        if (configLocations == null) {
            configLocations = getDefaultConfigLocations();
            if (configLocations == null) {
                return;
            }
        }
        
        try {
            PustefixModuleBeanDefinitionReader reader = new PustefixModuleBeanDefinitionReader(beanFactory, this);
            
            reader.setResourceLoader(this);
            reader.setEntityResolver(createEntityResolver(getBundleContext(), getClassLoader()));
            reader.setNamespaceHandlerResolver(createNamespaceHandlerResolver(getBundleContext(), getClassLoader()));
            reader.loadBeanDefinitions(configLocations);
            
            // Context proxy has to be created before processing
            // inject annotations. Otherwise, the context would
            // not be injected.
            createContextProxyBeanDefinition(beanFactory);
            
            afterLoadBeanDefinitions(beanFactory);
        } catch (RuntimeException e) {
            // TODO: This is just a workaround for a bug in Spring DM 1.2.0,
            // which causes exceptions thrown here to be lost
            logger.error(e);
            throw e;
        }
    }
    
    /**
     * Processes the bean definitions in the bean factory looking
     * for annotations that require wiring actions.
     * 
     * @param beanFactory bean factory that is being initialized
     * @throws IOException on error
     * @throws BeansException on error
     */
    protected void afterLoadBeanDefinitions(DefaultListableBeanFactory beanFactory) throws IOException, BeansException {
        AnnotationBeanDefinitionPostProcessor annotationPostProcessor = new AnnotationBeanDefinitionPostProcessor();
        annotationPostProcessor.postProcessBeanFactory(beanFactory);
    }

    /**
     * Creates and registers a bean definition for a factory bean proxying
     * {@link ContextImpl}. This bean is needed, as the context depends
     * on the application that is calling the module code and therefore
     * the context has to be stored in a thread local.
     * 
     * @param beanFactory registry the proxy is created within
     */
    protected void createContextProxyBeanDefinition(DefaultListableBeanFactory beanFactory) {
        TargetSource targetSource = new TargetSource() {

            public Object getTarget() throws Exception {
                PustefixOsgiWebApplicationContext applicationContext = WebApplicationContextStore.getApplicationContext();
                if (applicationContext == null) {
                    return null;
                }
                return applicationContext.getBean(ContextImpl.class.getName());
            }

            public Class<?> getTargetClass() {
                return ContextImpl.class;
            }

            public boolean isStatic() {
                return false;
            }

            public void releaseTarget(Object target) throws Exception {
                // Thread local is released by dispatcher servlet
            }
            
        };
        
        BeanDefinitionBuilder beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(ProxyFactoryBean.class);
        beanBuilder.setScope("singleton");
        beanBuilder.addPropertyValue("singleton", true);
        beanBuilder.addPropertyValue("targetSource", targetSource);
        BeanDefinition beanDefinition = beanBuilder.getBeanDefinition();
        beanFactory.registerBeanDefinition(ContextImpl.class.getName(), beanDefinition);
    }

    @Override
    protected void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        super.postProcessBeanFactory(beanFactory);
        
        WebApplicationContextUtils.registerWebApplicationScopes(beanFactory);
    }

}
