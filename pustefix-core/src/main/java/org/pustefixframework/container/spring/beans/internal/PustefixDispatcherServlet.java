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

package org.pustefixframework.container.spring.beans.internal;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.pustefixframework.container.spring.beans.PustefixOsgiWebApplicationContext;
import org.pustefixframework.http.HttpRequestFilter;
import org.pustefixframework.http.HttpRequestFilterChain;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.SourceFilteringListener;
import org.springframework.core.OrderComparator;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.NestedServletException;

/**
 * Provides HTTP access to components within a 
 * {@link PustefixOsgiWebApplicationContext}. 
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class PustefixDispatcherServlet extends DispatcherServlet {

    private static final long serialVersionUID = -5527490492537021694L;

    private PustefixOsgiWebApplicationContext applicationContext;

    private List<HttpRequestFilter> filters;
    private HttpRequestFilter finalFilter;

    public PustefixDispatcherServlet(PustefixOsgiWebApplicationContext pustefixOsgiWebApplicationContext) {
        this.applicationContext = pustefixOsgiWebApplicationContext;
        this.applicationContext.addApplicationListener(new SourceFilteringListener(this.applicationContext, this));
    }

    @Override
    protected WebApplicationContext createWebApplicationContext(WebApplicationContext parent) throws BeansException {
        this.applicationContext.setServletContext(getServletContext());
        this.applicationContext.setServletConfig(getServletConfig());
        return this.applicationContext;
    }

    @Override
    protected void initStrategies(ApplicationContext context) {
        super.initStrategies(context);
        initFilters(context);
    }

    @SuppressWarnings("unchecked")
    protected void initFilters(ApplicationContext applicationContext) {
        LinkedList<HttpRequestFilter> filters = new LinkedList<HttpRequestFilter>();
        for (String beanName : applicationContext.getBeanDefinitionNames()) {
            Class<?> clazz = applicationContext.getType(beanName);
            if (clazz != null) {
                if (HttpRequestFilter.class.isAssignableFrom(clazz)) {
                    // Ignore scoped beans - there should be a scoped proxy that
                    // will be used instead.
                    if (!applicationContext.isPrototype(beanName) && !applicationContext.isSingleton(beanName)) {
                        continue;
                    }
                    Object bean = applicationContext.getBean(beanName);
                    filters.add((HttpRequestFilter) bean);
                }
            }
        }
        Collections.sort(filters, new OrderComparator());
        this.filters = filters;
        this.finalFilter = new HttpRequestFilter() {

            public void doFilter(HttpServletRequest request, HttpServletResponse response, HttpRequestFilterChain chain) throws IOException, ServletException {
                try {
                    PustefixDispatcherServlet.super.doDispatch(request, response);
                } catch (IOException e) {
                    throw e;
                } catch (ServletException e) {
                    throw e;
                } catch (Throwable e) {
                    throw new NestedServletException("Request processing failed", e);
                }
            }
            
        };
    }

    @Override
    protected void doService(HttpServletRequest req, HttpServletResponse res) throws Exception {
        // Store the application context in a thread local, so that it can
        // be retrieved by other components during request processing.
        try {
            WebApplicationContextStore.setApplicationContext(this.applicationContext);
            super.doService(req, res);
        } finally {
            WebApplicationContextStore.setApplicationContext(null);
        }
    }

    @Override
    protected void doDispatch(HttpServletRequest request, HttpServletResponse response) throws Exception {
        if(filters != null && filters.size() > 0) {
            HttpRequestFilterChain chain = new HttpRequestFilterChainImpl(filters, finalFilter);
            chain.doFilter(request, response);
        } else {
            super.doDispatch(request, response);
        }
    }

    @Override
    protected void onRefresh(ApplicationContext context) throws BeansException {
        if (applicationContext.isRefreshed()) {
            initStrategies(context);
            return;
        }
        applicationContext.addApplicationListener(new ApplicationListener() {

            public void onApplicationEvent(ApplicationEvent event) {
                if (event instanceof ContextRefreshedEvent) {
                    initStrategies(applicationContext);
                }
            }

        });
        // Check a second time: If application context has been refreshed
        // between the last check and registering the listener,
        // the listener will never get the event.
        if (applicationContext.isRefreshed()) {
            initStrategies(context);
        }
    }
    
    @Override
    public void destroy() {
        // Do nothing. Parent implementation destroys the ApplicationContext,
        // but our ApplicationContext is not tight to the servlet but to
        // to the bundle state.
    }

}
