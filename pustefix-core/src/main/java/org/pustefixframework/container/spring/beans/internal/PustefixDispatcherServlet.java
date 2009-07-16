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

import org.pustefixframework.container.spring.beans.PustefixOsgiWebApplicationContext;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.SourceFilteringListener;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;


/**
 * Provides HTTP access to components within a 
 * {@link PustefixOsgiWebApplicationContext}. 
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class PustefixDispatcherServlet extends DispatcherServlet {
    private static final long serialVersionUID = -5527490492537021694L;
    
    private PustefixOsgiWebApplicationContext applicationContext;
    
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
