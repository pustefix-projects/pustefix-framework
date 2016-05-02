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
package org.pustefixframework.container.spring.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import javax.servlet.ServletContext;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.web.context.ServletContextAware;

/**
 * PropertyPlaceholderConfigurer implementation, which additionally searches for mode 
 * or other context init parameter specific versions of a property file.
 * 
 * By default "location" will be set to "WEB-INF/spring.properties" and the context
 * init parameter "mode" will be used as file suffix. Having set "mode" to "test" the
 * following files will try to be loaded (same properties in later file will precede):
 * 
 *   WEB-INF/spring.properties
 *   WEB-INF/spring-test.properties
 *   
 * You can also use this implementation to load properties from the classpath. In the following
 * example "location" was set to "classpath*:spring.properties", "params" to "mode" and "locale".
 * Configured with the parameters "mode" set to "test" and "locale" set to "en_US" it will try to 
 * load properties from the following locations:
 * 
 *   classpath*:spring.properties
 *   classpath*:spring-en_US.properties
 *   classpath*:spring-test.properties
 *   classpath*:spring-test-en_US.properties
 * 
 */
public class EnvironmentPropertyPlaceholderConfigurer extends PropertyPlaceholderConfigurer 
                implements InitializingBean, ApplicationContextAware, ServletContextAware {

    private ServletContext servletContext;
    private ApplicationContext appContext;
    private String[] locations;
    private String[] params;
    
    private boolean ignoreResourceNotFoundSet;
    private boolean orderSet;
    private boolean ignoreUnresolvablePlaceholdersSet;
    
    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }  
  
    @Override
    public void setApplicationContext(ApplicationContext appContext) {
        this.appContext = appContext;
    }
    
    @Override
    /**
     * Default for this implementation is "true".
     */
    public void setIgnoreResourceNotFound(boolean ignoreResourceNotFound) {
        super.setIgnoreResourceNotFound(ignoreResourceNotFound);
        ignoreResourceNotFoundSet = true;
    }
    
    @Override
    /**
     * Default for this implementation is "10".
     */
    public void setOrder(int order) {
        super.setOrder(order);
        orderSet = true;
    }

    @Override
    /**
     * Default for this implementation is "true".
     */
    public void setIgnoreUnresolvablePlaceholders(boolean ignoreUnresolvablePlaceholders) {
        super.setIgnoreUnresolvablePlaceholders(ignoreUnresolvablePlaceholders);
        ignoreUnresolvablePlaceholdersSet = true;
    }
    
    /**
     * Set property file location.
     * Will be set to "WEB-INF/spring.properties" by default.
     */
    public void setLocation(String location) {
        this.locations = new String[] {location};
    }
    
    /**
     * Set multiple property file locations.
     */
    public void setLocations(String[] locations) {
        this.locations = locations;
    }
    
    /**
     * Set parameter whose value should be used as property filename suffix.
     * Will be set to "mode" by default.
     */
    public void setParameter(String param) {
        this.params = new String[] {param};
    }
    
    /**
     * Set parameters whose values should be used as property filename suffix.
     */
    public void setParameters(String[] params) {
        this.params = params;
    }
    
    @Override
    public void afterPropertiesSet() {
        
        //Don't fail when placeholders can't be resolved and give other PropertyPlaceholders
        //the chance to resolve them.
        if(!ignoreUnresolvablePlaceholdersSet) {
            setIgnoreUnresolvablePlaceholders(true);
        }
        
        //Ensure placeholder is called before Pustefix's default implementation, which should
        //run as the last one because it's failing when placeholders can't be resolved.
        if(!orderSet) {
            setOrder(10);
        }
        
        //Ignore if property file can't be found by default
        if(!ignoreResourceNotFoundSet) {
            setIgnoreResourceNotFound(true);
        }
        
        //Set sensible default values if bean properties aren't set
        if(locations == null) {
            locations = new String[] {"/WEB-INF/spring.properties"};
        }
        if(params == null) {
            params = new String[] {"mode"};
        }
        
        //Resolve locations to Spring Resources and set them at underlying class
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(appContext);
        List<Resource> resourceList = new ArrayList<Resource>();
        List<String> envLocations = getEnvironmentLocations(locations);
        for(String envLocation: envLocations) {
            Resource[] resources;
            try {
                resources = resolver.getResources(envLocation);
                if(resources != null) {
                    for(Resource resource: resources) {
                        resourceList.add(resource);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException("Error loading Spring placeholder properties", e);
            }   
        }
        if(!resourceList.isEmpty()) {
            removeDuplicates(resourceList);
            setLocations(resourceList.toArray(new Resource[resourceList.size()]));
        }
    }
    
    /**
     * Extend location paths with mode and locale parameter values.
     */
    private List<String> getEnvironmentLocations(String[] locations) {
        
        List<String> envLocations = new ArrayList<String>();
        if(locations != null) {
            List<String> paramValues = new ArrayList<String>();
            if(params != null) {
                for(String param: params) {
                    String value = servletContext.getInitParameter(param);
                    if(value != null) {
                        paramValues.add(value);
                    }
                }
            }
            if(paramValues.isEmpty()) {   
                for(String location: locations) {
                    envLocations.add(location);
                }
            } else {
                List<String> envSuffixes = getEnvSuffixes(paramValues.toArray(new String[paramValues.size()]));
                for(String location: locations) {
                    int ind = location.lastIndexOf('.');
                    if(ind > -1) {
                        String prefix = location.substring(0, ind);
                        String suffix = location.substring(ind);
                        for(String envSuffix: envSuffixes) {
                            envLocations.add(prefix + envSuffix + suffix);
                        }
                    }
                }
            }
        }
        return envLocations;
    }
    
    /**
     * Remove all duplicate entries with lower precedence, i.e. keep
     * only the last occurring entry.
     */
    private void removeDuplicates(List<Resource> resources) {
        
        Set<Resource> uniqueResources = new HashSet<Resource>();
        ListIterator<Resource> it = resources.listIterator(resources.size());
        while(it.hasPrevious()) {
            Resource resource = it.previous();
            if(uniqueResources.contains(resource)) {
                it.remove();
            } else {
                uniqueResources.add(resource);
            }
        }
    }
    
    private List<String> getEnvSuffixes(String[] params) {
        
        List<String> suffixes = new ArrayList<String>();
        getEnvSuffixes(params, 0, "", suffixes);
        Collections.reverse(suffixes);
        return suffixes;
    }
    
    private void getEnvSuffixes(String[] params, int index, String suffix, List<String> suffixes) {
        
        if(index < params.length) {
            getEnvSuffixes(params, index + 1, suffix + "-" + params[index], suffixes);
            getEnvSuffixes(params, index + 1, suffix, suffixes);
        } else {
            suffixes.add(suffix);
        }   
    }

}