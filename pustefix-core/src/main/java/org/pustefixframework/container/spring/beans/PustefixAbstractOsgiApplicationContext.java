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

import org.osgi.framework.BundleContext;
import org.pustefixframework.container.spring.beans.internal.DelegatedEntityResolver;
import org.pustefixframework.container.spring.beans.internal.DelegatedNamespaceHandlerResolver;
import org.pustefixframework.container.spring.beans.internal.TrackingUtil;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.DefaultNamespaceHandlerResolver;
import org.springframework.beans.factory.xml.DelegatingEntityResolver;
import org.springframework.beans.factory.xml.NamespaceHandlerResolver;
import org.springframework.osgi.context.support.OsgiBundleXmlApplicationContext;
import org.springframework.osgi.util.OsgiStringUtils;
import org.springframework.util.Assert;
import org.xml.sax.EntityResolver;


/**
 * Abstract <code>ApplicationContext</code> implementation that is used a base
 * for the application context implemenentations for Pustefix applications and
 * modules. This implementation is OSGi-aware and thus can be used to 
 * import or export services from or to other bundles. Child implementations
 * have to implement the 
 * {@link #loadBeanDefinitions(DefaultListableBeanFactory)} method, which should
 * call the {@link #afterLoadBeanDefinitions(DefaultListableBeanFactory)} 
 * method.
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public abstract class PustefixAbstractOsgiApplicationContext extends OsgiBundleXmlApplicationContext {
    
    protected abstract void loadBeanDefinitions(DefaultListableBeanFactory beanFactory) throws IOException, BeansException;/**

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
     * Creates a {@link NamespaceHandlerResolver} that delegates to 
     * corresponding OSGi services.
     * 
     * @param bundleContext bundle context of the bundle this ApplicationContext
     *  is being instantiated for
     * @param bundleClassLoader the bundle's class loader
     * @return delegating NamespaceHandlerResolver
     */
    protected NamespaceHandlerResolver createNamespaceHandlerResolver(BundleContext bundleContext, ClassLoader bundleClassLoader)
    {
        Assert.notNull(bundleContext, "bundleContext is required");
        NamespaceHandlerResolver localNamespaceResolver = new DefaultNamespaceHandlerResolver(bundleClassLoader);
        NamespaceHandlerResolver osgiServiceNamespaceResolver = lookupNamespaceHandlerResolver(bundleContext, localNamespaceResolver);
        DelegatedNamespaceHandlerResolver delegate = new DelegatedNamespaceHandlerResolver();
        delegate.addNamespaceHandler(localNamespaceResolver, "LocalNamespaceResolver for bundle " + OsgiStringUtils.nullSafeNameAndSymName(bundleContext.getBundle()));
        delegate.addNamespaceHandler(osgiServiceNamespaceResolver, "OSGi Service resolver");
        return delegate;
    }

    /**
     * Creates an {@link EntityResolver} that delegates to corresponding
     * OSGi services.
     * 
     * @param bundleContext bundle context of the bundle this ApplicationContext
     *  is being intantiated for
     * @param bundleClassLoader the bundle's class loader
     * @return delegating EntityResolver
     */
    protected EntityResolver createEntityResolver(BundleContext bundleContext, ClassLoader bundleClassLoader)
    {
        Assert.notNull(bundleContext, "bundleContext is required");
        EntityResolver localEntityResolver = new DelegatingEntityResolver(bundleClassLoader);
        EntityResolver osgiServiceEntityResolver = lookupEntityResolver(bundleContext, localEntityResolver);
        DelegatedEntityResolver delegate = new DelegatedEntityResolver();
        delegate.addEntityResolver(localEntityResolver, "LocalEntityResolver for bundle " + OsgiStringUtils.nullSafeNameAndSymName(bundleContext.getBundle()));
        delegate.addEntityResolver(osgiServiceEntityResolver, "OSGi Service resolver");
        return delegate;
    }

    private NamespaceHandlerResolver lookupNamespaceHandlerResolver(BundleContext bundleContext, Object fallbackObject)
    {
        return (NamespaceHandlerResolver)TrackingUtil.getService(new Class[] {
            org.springframework.beans.factory.xml.NamespaceHandlerResolver.class
        }, null, (org.springframework.beans.factory.xml.NamespaceHandlerResolver.class).getClassLoader(), bundleContext, fallbackObject);
    }

    private EntityResolver lookupEntityResolver(BundleContext bundleContext, Object fallbackObject)
    {
        return (EntityResolver)TrackingUtil.getService(new Class[] {
            org.xml.sax.EntityResolver.class
        }, null, (org.xml.sax.EntityResolver.class).getClassLoader(), bundleContext, fallbackObject);
    }
}
