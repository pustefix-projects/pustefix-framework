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
package org.pustefixframework.web.mvc;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.pustefixframework.web.mvc.filter.FilterResolver;
import org.springframework.web.bind.support.WebBindingInitializer;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;

/**
 * Configures the RequestMappingHandlerAdapter used by Pustefix States.
 * The Pustefix default configuration can be overridden by adding an own
 * bean definition of this type to the Spring configuration.
 */
public class RequestMappingHandlerAdapterConfig {

    private HandlerMethodArgumentResolver[] argumentResolvers;
    private WebBindingInitializer webBindingInitializer;

    public void setCustomArgumentResolver(HandlerMethodArgumentResolver argumentResolver) {
        this.argumentResolvers = new HandlerMethodArgumentResolver[] { argumentResolver };
    }

    public void setCustomArgumentResolvers(HandlerMethodArgumentResolver[] argumentResolvers) {
        this.argumentResolvers = argumentResolvers;
    }

    public HandlerMethodArgumentResolver[] getCustomArgumentResolvers() {
        return argumentResolvers;
    }

    public void setWebBindingInitializer(WebBindingInitializer webBindingInitializer) {
        this.webBindingInitializer = webBindingInitializer;
    }

    public WebBindingInitializer getWebBindingInitializer() {
        return webBindingInitializer;
    }

    public static RequestMappingHandlerAdapterConfig createDefaultConfig() {
        List<HandlerMethodArgumentResolver> resolvers = new ArrayList<>();
        try {
            Class<?> sortClass = Class.forName("org.springframework.data.web.SortHandlerMethodArgumentResolver");
            HandlerMethodArgumentResolver sortResolver = (HandlerMethodArgumentResolver)sortClass.newInstance();
            Method meth = sortClass.getMethod("setSortParameter", String.class);
            meth.invoke(sortResolver, "page.sort");
            Class<?> clazz = Class.forName("org.springframework.data.web.PageableHandlerMethodArgumentResolver");
            Constructor<?> con = clazz.getConstructor(sortClass);
            HandlerMethodArgumentResolver resolver = (HandlerMethodArgumentResolver)con.newInstance(sortResolver);
            meth = clazz.getMethod("setPageParameterName", String.class);
            meth.invoke(resolver, "page.page");
            meth = clazz.getMethod("setSizeParameterName", String.class);
            meth.invoke(resolver, "page.size");
            meth = clazz.getMethod("setOneIndexedParameters", boolean.class);
            meth.invoke(resolver, true);
            resolvers.add(resolver);
        } catch(NoSuchMethodException|InvocationTargetException|IllegalAccessException|InstantiationException x) {
            throw new RuntimeException("Error creating RequestMappingHandlerAdapter default configuration", x);
        } catch(ClassNotFoundException x) {
            //ignore optional resolver
        }
        resolvers.add(new FilterResolver());
        RequestMappingHandlerAdapterConfig adapterConfig = new RequestMappingHandlerAdapterConfig();
        adapterConfig.setCustomArgumentResolvers(resolvers.toArray(new HandlerMethodArgumentResolver[resolvers.size()]));
        return adapterConfig;
    }

}
