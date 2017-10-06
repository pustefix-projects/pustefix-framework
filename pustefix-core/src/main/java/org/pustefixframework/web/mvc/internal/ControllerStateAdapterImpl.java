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
package org.pustefixframework.web.mvc.internal;

import java.util.Arrays;

import org.pustefixframework.web.mvc.RequestMappingHandlerAdapterConfig;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import de.schlund.pfixxml.PfixServletRequest;

/**
 * Bridge from Pustefix States to Spring MVC's RequestMappingHandlerAdapter.
 */
public class ControllerStateAdapterImpl implements ControllerStateAdapter, InitializingBean, ApplicationContextAware {

    private StateRequestMappingHandlerMapping mapping;
    private RequestMappingHandlerAdapter adapter;

    private RequestMappingHandlerAdapterConfig adapterConfig;
    private ApplicationContext applicationContext;

    public void setAdapterConfig(RequestMappingHandlerAdapterConfig adapterConfig) {
        this.adapterConfig = adapterConfig;
    }

    /**
     * Call RequestMappingHandlerAdapter if State class contains request mappings.
     */
    @Override
    public ModelAndView tryHandle(PfixServletRequest request, Object handler, String pageName) throws Exception {
        if(mapping.hasRequestMapping(handler.getClass())) {
            ControllerRequestWrapper wrappedRequest = new ControllerRequestWrapper(request, pageName);
            ControllerResponseWrapper response = new ControllerResponseWrapper();
            HandlerExecutionChain chain = mapping.getHandler(wrappedRequest);
            if(chain != null && chain.getHandler() != null) {
                return adapter.handle(wrappedRequest, response, chain.getHandler());
            }
        }
        return null;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        mapping = new StateRequestMappingHandlerMapping();
        mapping.setApplicationContext(applicationContext);
        mapping.afterPropertiesSet();
        adapter = new RequestMappingHandlerAdapter();
        adapter.setApplicationContext(applicationContext);
        adapter.setCustomArgumentResolvers(Arrays.asList(adapterConfig.getCustomArgumentResolvers()));
        adapter.setWebBindingInitializer(adapterConfig.getWebBindingInitializer());
        adapter.afterPropertiesSet();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

}
