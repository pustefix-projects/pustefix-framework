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

package org.pustefixframework.container.spring.http;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.request.WebRequestInterceptor;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.handler.AbstractDetectingUrlHandlerMapping;

public class PustefixHandlerMapping extends AbstractDetectingUrlHandlerMapping {

    private ConcurrentHashMap<String, Object> handlerCache = new ConcurrentHashMap<String, Object>();
    private Object handlerCacheNull = new Object();
    
    public PustefixHandlerMapping() {
        super();
        setAlwaysUseFullPath(true);
    }

    @Override
    protected String[] determineUrlsForHandler(String beanName) {
        
        Class<?> beanClass = getApplicationContext().getType(beanName);
        if (UriProvidingHttpRequestHandler.class.isAssignableFrom(beanClass)) {
            Object bean = getApplicationContext().getBean(beanName);
            UriProvidingHttpRequestHandler handler = (UriProvidingHttpRequestHandler) bean;
            return handler.getRegisteredURIs();
        }
        return null;
    }

    @Override
    protected Object lookupHandler(String urlPath, HttpServletRequest request) throws Exception {
        //cache handler lookup, because lookup can be expensive when having a lot of url paths,
        //which are checked repeatedly by Spring
        Object obj = handlerCache.get(urlPath);
        if(obj == null) {
            obj = super.lookupHandler(urlPath, request);
            handlerCache.putIfAbsent(urlPath, obj == null ? handlerCacheNull : obj);
        } else if(obj == handlerCacheNull) {
            obj = null;
        }
        return obj;
    }
    
    @Override
    @SuppressWarnings({"unchecked","rawtypes"})
    protected void extendInterceptors(List interceptors) {
        // Find all interceptors in the ApplicationContext and add them
        ApplicationContext applicationContext = getApplicationContext();
        for (String beanName : applicationContext.getBeanDefinitionNames()) {
            Class clazz = applicationContext.getType(beanName);
            if(clazz != null) {
                if (HandlerInterceptor.class.isAssignableFrom(clazz)
                        || WebRequestInterceptor.class.isAssignableFrom(clazz)) {
                    // Ignore scoped beans - there should be a scoped proxy that
                    // will be used instead.
                    if (!applicationContext.isPrototype(beanName)
                            && !applicationContext.isSingleton(beanName)) {
                        continue;
                    }
                    Object bean = applicationContext.getBean(beanName);
                    if (!interceptors.contains(bean)) {
                        interceptors.add(bean);
                    }
                }
            }
        }
    }
    
    public void reload() {
        initApplicationContext();
    }
    
}
