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

import javax.servlet.ServletContext;

import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ProtocolResolver;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.StringUtils;
import org.springframework.web.context.ServletContextAware;

import de.schlund.pfixxml.resources.ResourceUtil;

/**
 *  Creates all required bean definitions for bootstrapping Pustefix using a standard
 *  ApplicationContext implementation, i.e. without using the PustefixWebApplicationContext implementation.
 *
 *  Therefore this BeanDefinitionRegistryPostProcessor has to be registered either as
 *  XML bean definition or using Spring Java Config. A Pustefix project configuration file
 *  has to be present in order to set up the required Pustefix bean definitions. The config
 *  location of this file or additional Spring configuration files can be configured.
 *  By default the configuration will be read from "WEB-INF/project.xml".
 */
public class PustefixConfigPostProcessor implements BeanDefinitionRegistryPostProcessor,
                                ServletContextAware, ResourceLoaderAware, EnvironmentAware {

    private ServletContext servletContext;
    private ResourceLoader resourceLoader;
    private Environment environment;

    private String[] configLocations = { "WEB-INF/project.xml" };

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {

        ((DefaultResourceLoader)resourceLoader).addProtocolResolver(new PustefixProtocolResolver());
        try {
            DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory)registry;
            PustefixWebApplicationContext.load(beanFactory, servletContext,
                    (ConfigurableEnvironment)environment, configLocations, resourceLoader);
        } catch(IOException | BeansException x) {
            throw new FatalBeanException("Error loading bean definitions", x);
        }
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory factory) throws BeansException {
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    /**
     * Set the config location.
     *
     * If not set, the mandatory Pustefix project configuration will be read from "WEB-INF/project.xml".
     * Optionally you can specify additional Spring XML configuration files to be read.
     *
     * @param location - the config location
     */
    public void setConfigLocation(String location) {
        setConfigLocations(StringUtils.tokenizeToStringArray(location, ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS));
    }

    /**
     * Set the config locations.
     *
     * If not set, the mandatory Pustefix project configuration will be read from "WEB-INF/project.xml",
     * Optionally you can specify additional Spring XML configuration files to be read.
     *
     * @param locations - the config locations
     */
    public void setConfigLocations(String... locations) {
        this.configLocations = locations;
    }


    private class PustefixProtocolResolver implements ProtocolResolver {

        @Override
        public Resource resolve(String location, ResourceLoader loader) {
            if(location.startsWith("module:") || location.startsWith("dynamic:")) {
                return ResourceUtil.getResource(location);
            }
            return null;
        }

    }

}
