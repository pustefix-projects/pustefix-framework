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

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.osgi.util.tracker.ServiceTracker;
import org.pustefixframework.container.spring.beans.internal.PustefixDispatcherServlet;
import org.pustefixframework.container.spring.beans.internal.ServletConfigProxy;
import org.pustefixframework.container.spring.beans.internal.ServletContextProxy;
import org.pustefixframework.http.ExtendedHttpService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.context.ServletConfigAware;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.context.support.ServletContextAwareProcessor;


/**
 * <code>ApplicationContext</code> implementation that is used for Pustefix
 * web applications. This implementation is OSGi-aware and thus can be used to 
 * import or export services from or to other bundles. It automatically 
 * registers an instance of {@link PustefixDispatcherServlet} with a
 * {@link HttpService} or {@link ExtendedHttpService} in order to dispatch
 * HTTP requests to the corresponding components within this ApplicationContext
 * instance.
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class PustefixOsgiWebApplicationContext extends PustefixAbstractOsgiApplicationContext
    implements ConfigurableWebApplicationContext {

    private ServletContextProxy servletContextProxy = new ServletContextProxy();
    private ServletConfigProxy servletConfigProxy = new ServletConfigProxy();
    private HttpServiceTracker httpServiceTracker;
    private ExtendedHttpServiceTracker extendedHttpServiceTracker;
    private String namespace;
    private boolean refreshed = false;
    private final Object refreshLock = new Object();
    
    private Log logger = LogFactory.getLog(this.getClass());
    
    private void initTrackers() {
        httpServiceTracker = new HttpServiceTracker(getBundleContext(), this);
        extendedHttpServiceTracker = new ExtendedHttpServiceTracker(getBundleContext(), this);
        httpServiceTracker.open(true);
        extendedHttpServiceTracker.open(true);
    }
    
    private void closeTrackers() {
        if (httpServiceTracker != null) {
            httpServiceTracker.close();
            httpServiceTracker = null;
        }
        if (extendedHttpServiceTracker != null) {
            extendedHttpServiceTracker.close();
            extendedHttpServiceTracker = null;
        }
    }

    protected void loadBeanDefinitions(DefaultListableBeanFactory beanFactory) throws IOException, BeansException {
        String configLocations[] = getConfigLocations();
        if (configLocations == null) {
            configLocations = getDefaultConfigLocations();
            if (configLocations == null) {
                return;
            }
        }
        
        try {
            PustefixApplicationBeanDefinitionReader reader = new PustefixApplicationBeanDefinitionReader(beanFactory, this);
            
            reader.setResourceLoader(this);
            reader.setEntityResolver(createEntityResolver(getBundleContext(), getClassLoader()));
            reader.setNamespaceHandlerResolver(createNamespaceHandlerResolver(getBundleContext(), getClassLoader()));
            reader.loadBeanDefinitions(configLocations);
            
            afterLoadBeanDefinitions(beanFactory);
        } catch (RuntimeException e) {
            // TODO: This is just a workaround for a bug in Spring DM 1.2.0,
            // which causes exceptions thrown here to be lost
            logger.error(e);
            throw e;
        }
    }
    
    /**
     * Set servlet config for this web application context. This method
     * is called by the {@link PustefixDispatcherServlet} and thus does
     * not have to be invoked manually.
     * 
     * @param servletConfig servlet config for this application context
     */
    public void setServletConfig(ServletConfig servletConfig) {
        this.servletConfigProxy.setServletConfig(servletConfig);
    }

    /**
     * Set servlet context for this web application context. This method
     * is called by the {@link PustefixDispatcherServlet} and thus does
     * not have to be invoked manually.
     * 
     * @param servletConfig servlet context for this application context
     */
    public void setServletContext(ServletContext servletContext) {
        this.servletContextProxy.setServletContext(servletContext);
    }

    /**
     * This implementation takes care of closing the service trackers registered
     * by this class. Should be called from child implementations overriding
     * this method.
     */
    @Override
    protected void onClose() {
        closeTrackers();
        super.onClose();
    }

    public String getNamespace() {
        return namespace;
    }

    public ServletConfig getServletConfig() {
        return servletConfigProxy;
    }

    public void setConfigLocation(String configLocation) {
        setConfigLocations(StringUtils.tokenizeToStringArray(configLocation, CONFIG_LOCATION_DELIMITERS));
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public ServletContext getServletContext() {
        return servletContextProxy;
    }

    @Override
    protected void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        super.postProcessBeanFactory(beanFactory);

        beanFactory.addBeanPostProcessor(new ServletContextAwareProcessor(this.servletContextProxy, this.servletConfigProxy));
        beanFactory.ignoreDependencyInterface(ServletContextAware.class);
        beanFactory.ignoreDependencyInterface(ServletConfigAware.class);
        beanFactory.registerResolvableDependency(ServletContext.class, this.servletContextProxy);
        beanFactory.registerResolvableDependency(ServletConfig.class, this.servletConfigProxy);
        
        registerScopes(beanFactory);
    }

    @Override
    public void refresh() throws BeansException, IllegalStateException {
        
        try {
            super.refresh();
        } catch (RuntimeException e) {
            logger.error(e);
            throw e;
        } catch (Error e) {
            logger.error(e);
            throw e;
        }
        
        // Now that the ApplicationContext has been initialized,
        // we can perform the second stage of the servlet initialization
        synchronized (refreshLock) {
            refreshed = true;
        }
    }
    
    @Override
    public void completeRefresh() {
        super.completeRefresh();
        initTrackers();
    }
    
    /**
     * Returns true if and only if this ApplicationContext's {@link #refresh()} 
     * method has been called successfully at least one time.
     * 
     * @return flag indicating whehter this instance has been refreshed at
     *  least once
     */
    public boolean isRefreshed() {
        synchronized (refreshLock) {
            return refreshed;
        }
    }

    @Override
    protected void doClose() {
        closeTrackers();
        super.doClose();
    }

    private static class HttpServiceTracker extends ServiceTracker {
        private Log logger = LogFactory.getLog(HttpServiceTracker.class);
        private PustefixOsgiWebApplicationContext applicationContext;
        
        HttpServiceTracker(BundleContext bundleContext, PustefixOsgiWebApplicationContext applicationContext) {
            super(bundleContext, HttpService.class.getName(), null);
            this.applicationContext = applicationContext;
        }

        @Override
        public Object addingService(ServiceReference reference) {
            HttpService httpService = (HttpService) super.addingService(reference);
            try {
                httpService.registerServlet("/", new PustefixDispatcherServlet(applicationContext), null, null);
            } catch (ServletException e) {
                logger.fatal("Exception while trying to register dispatcher servlet", e);
            } catch (NamespaceException e) {
                logger.fatal("Exception while trying to register dispatcher servlet", e);
            }
            return httpService;
        }

        @Override
        public void removedService(ServiceReference reference, Object service) {
            HttpService httpService = (HttpService) service;
            httpService.unregister("/");
            super.removedService(reference, service);
        }
        
    }

    private static class ExtendedHttpServiceTracker extends ServiceTracker {
        private Log logger = LogFactory.getLog(ExtendedHttpServiceTracker.class);
        private BundleContext bundleContext;
        private PustefixOsgiWebApplicationContext applicationContext;
        
        ExtendedHttpServiceTracker(BundleContext bundleContext, PustefixOsgiWebApplicationContext applicationContext) {
            super(bundleContext, ExtendedHttpService.class.getName(), null);
            this.bundleContext = bundleContext;
            this.applicationContext = applicationContext;
        }

        @Override
        public Object addingService(ServiceReference reference) {
            ExtendedHttpService httpService = (ExtendedHttpService) super.addingService(reference);
            try {
                httpService.registerServlet("/", new PustefixDispatcherServlet(applicationContext), null, bundleContext.getBundle());
            } catch (ServletException e) {
                logger.fatal("Exception while trying to register dispatcher servlet", e);
            } catch (NamespaceException e) {
                logger.fatal("Exception while trying to register dispatcher servlet", e);
            }
            return httpService;
        }

        @Override
        public void removedService(ServiceReference reference, Object service) {
            ExtendedHttpService httpService = (ExtendedHttpService) service;
            httpService.unregister("/", bundleContext.getBundle());
            super.removedService(reference, service);
        }
        
    }

}
