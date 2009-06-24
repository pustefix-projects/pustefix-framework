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

package org.pustefixframework.container.spring.beans;

import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.pustefixframework.container.spring.util.PustefixPropertiesPersister;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.PropertyOverrideConfigurer;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.beans.factory.config.PropertyResourceConfigurer;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultBeanNameGenerator;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.ResourceEntityResolver;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContextException;
import org.springframework.core.io.Resource;
import org.springframework.web.context.support.AbstractRefreshableWebApplicationContext;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class PustefixWebApplicationContext extends AbstractRefreshableWebApplicationContext {
    
    @Override
    protected void loadBeanDefinitions(DefaultListableBeanFactory beanFactory) throws IOException, BeansException {
        String configLocations[] = getConfigLocations();
        if (configLocations == null) {
            configLocations = getDefaultConfigLocations();
            if (configLocations == null) {
                return;
            }
        }

        XmlBeanDefinitionReader springReader = new XmlBeanDefinitionReader(beanFactory);
        springReader.setResourceLoader(this);
        springReader.setEntityResolver(new ResourceEntityResolver(this));

        PustefixProjectBeanDefinitionReader pustefixReader = new PustefixProjectBeanDefinitionReader(beanFactory);
        pustefixReader.setResourceLoader(this);

        for (int i = 0; i < configLocations.length; i++) {
            String configLocation = configLocations[i];
            Resource configResource = this.getResource(configLocation);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            dbf.setXIncludeAware(true);
            DocumentBuilder db;
            Document doc;
            try {
                db = dbf.newDocumentBuilder();
                doc = db.parse(configResource.getInputStream());
            } catch (ParserConfigurationException e) {
                throw new ApplicationContextException("Error while reading " + configResource + ": " + e.getMessage(), e);
            } catch (SAXException e) {
                throw new ApplicationContextException("Error while reading " + configResource + ": " + e.getMessage(), e);
            }
            if (doc.getDocumentElement().getNamespaceURI() != null && doc.getDocumentElement().getNamespaceURI().equals("http://www.pustefix-framework.org/2008/namespace/project-config")) {
                pustefixReader.loadBeanDefinitions(configResource);
            } else {
                tryAddPropertyConfigurer(configLocation, beanFactory);
                springReader.loadBeanDefinitions(configResource);
            }

        }
   
        addAnnotationBeanDefinitionPostProcessor(beanFactory);
    }
    
    private void addAnnotationBeanDefinitionPostProcessor(BeanDefinitionRegistry registry) {
        BeanDefinitionBuilder beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(AnnotationBeanDefinitionPostProcessor.class);
        beanBuilder.setScope("singleton");
        BeanDefinition definition = beanBuilder.getBeanDefinition();
        DefaultBeanNameGenerator beanNameGenerator = new DefaultBeanNameGenerator();
        String name = beanNameGenerator.generateBeanName(definition, registry);
        registry.registerBeanDefinition(name, definition);
    }
    
    private void tryAddPropertyConfigurer(String configLocation, BeanDefinitionRegistry registry) {
        int ind = configLocation.lastIndexOf(".");
        if(ind > -1) {
            String propConfigLocation = configLocation.substring(0,ind) + "-properties" +
                configLocation.substring(ind);
            Resource propConfigResource = getResource(propConfigLocation);
            if(propConfigResource.exists()) {
                addPropertyConfigurer(PropertyPlaceholderConfigurer.class, propConfigResource, registry);
            }
            propConfigLocation = configLocation.substring(0,ind) + "-properties-override" +
                configLocation.substring(ind);
            propConfigResource = getResource(propConfigLocation);
            if(propConfigResource.exists()) {
                addPropertyConfigurer(PropertyOverrideConfigurer.class, propConfigResource, registry);
            }
        }
    }
    
    private void addPropertyConfigurer(Class<? extends PropertyResourceConfigurer> clazz, Resource location, BeanDefinitionRegistry registry) {
        BeanDefinitionBuilder beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(clazz);
        beanBuilder.setScope("singleton");
        beanBuilder.addPropertyValue("location", location);
        PustefixPropertiesPersister persister = new PustefixPropertiesPersister();
        try {
            persister.setLocation(location.getURI());
        } catch(IOException x) {
            //ignore
        }
        beanBuilder.addPropertyValue("propertiesPersister", persister);
        BeanDefinition definition = beanBuilder.getBeanDefinition();
        DefaultBeanNameGenerator beanNameGenerator = new DefaultBeanNameGenerator();
        String name = beanNameGenerator.generateBeanName(definition, registry);
        registry.registerBeanDefinition(name, definition);
    }
    
}
