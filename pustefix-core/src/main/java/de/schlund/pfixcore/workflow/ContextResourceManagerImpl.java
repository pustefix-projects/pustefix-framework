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

package de.schlund.pfixcore.workflow;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.pustefixframework.config.contextxmlservice.ContextConfig;
import org.pustefixframework.config.contextxmlservice.ContextResourceConfig;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import de.schlund.pfixcore.beans.InitResource;
import de.schlund.pfixcore.exception.PustefixApplicationException;
import de.schlund.pfixcore.exception.PustefixCoreException;

/**
 * Implements the ability to store objects implementing a number of interfaces
 *
 * @author jtl, thomas
 *
 */

public class ContextResourceManagerImpl implements ContextResourceManager, ApplicationContextAware {
    
    private final static Logger LOG = Logger.getLogger(ContextResourceManagerImpl.class);
    private ApplicationContext applicationContext;
    private ContextConfig contextConfig;
    
    @SuppressWarnings("deprecation")
     public void init(Context context, ContextConfig config) throws PustefixApplicationException, PustefixCoreException {
        LOG.debug("Initializing Resources...");
        this.contextConfig = config;
        
        for (ContextResourceConfig resConfig : config.getContextResourceConfigs()) {
            Class<?> clazz = resConfig.getContextResourceClass();
            if(ContextResource.class.isAssignableFrom(clazz)) {
                try {
                    LOG.debug("***** Resource implements ContextResource => calling init(Context) of " + clazz.getName());
                    ContextResource resource = (ContextResource)applicationContext.getBean(resConfig.getBeanName());
                    resource.init(context);
                } catch (Exception e) {
                    throw new PustefixApplicationException("Exception while initializing context resource " + clazz, e);
                }
            } else {
                for (Method m : clazz.getMethods()) {
                    if (m.isAnnotationPresent(InitResource.class)) {
                        Object resource = applicationContext.getBean(resConfig.getBeanName());
                        try {
                            Class<?>[] params = m.getParameterTypes();
                            if (params.length == 0) {
                                LOG.debug("***** Found @InitResource for " + m.getName() + "() of " + resource.getClass().getName());
                                m.invoke(resource, new Object[]{});
                            } else if (params.length == 1 && (params[0].isAssignableFrom(Context.class))) {
                                LOG.debug("***** Found @InitResource for " + m.getName() + "(Context) of " + resource.getClass().getName());
                                m.invoke(resource, context);
                            } else {
                                throw new PustefixApplicationException("Annotated '@InitResource' method must either take " + 
                                        "no parameters or only one of type 'Context'or a superclass of it.");
                            } 
                        } catch (Exception e) {
                            throw new PustefixApplicationException("Exception while initializing context resource " + resource.getClass(), e);
                        }
                        break;
                    }
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see de.schlund.pfixcore.workflow.ContextResourceManager#getResource(java.lang.String)
     */
    public Object getResource(String name) {
        ContextResourceConfig conf = contextConfig.getContextResourceConfig(name);
        if(conf==null) throw new IllegalArgumentException("Resource not found: "+name);
        return applicationContext.getBean(conf.getBeanName());
    }

    /* (non-Javadoc)
     * @see de.schlund.pfixcore.workflow.ContextResourceManager#getResource(java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    public <T> T getResource(Class<T> clazz) {
        ContextResourceConfig conf = contextConfig.getContextResourceConfig(clazz.getName());
        return (T)applicationContext.getBean(conf.getBeanName());
    }
    
 
    
    /* (non-Javadoc)
     * @see de.schlund.pfixcore.workflow.ContextResourceManager#getResourceIterator()
     */
    public Iterator<Object> getResourceIterator() {
        List<Object> resources = new ArrayList<Object>();
        for (ContextResourceConfig resConfig : contextConfig.getContextResourceConfigs()) {
            resources.add(applicationContext.getBean(resConfig.getBeanName()));
        }
        return  resources.iterator();
    }
    
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
    
}
