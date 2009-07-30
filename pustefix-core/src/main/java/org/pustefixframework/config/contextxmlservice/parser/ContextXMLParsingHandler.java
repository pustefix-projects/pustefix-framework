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

package org.pustefixframework.config.contextxmlservice.parser;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.pustefixframework.config.contextxmlservice.PageFlowHolder;
import org.pustefixframework.config.contextxmlservice.PageRequestConfig;
import org.pustefixframework.config.contextxmlservice.parser.internal.ContextConfigImpl;
import org.pustefixframework.config.contextxmlservice.parser.internal.ContextXMLServletConfigImpl;
import org.pustefixframework.config.contextxmlservice.parser.internal.PageFlowExtensionPointImpl;
import org.pustefixframework.config.contextxmlservice.parser.internal.PageFlowMap;
import org.pustefixframework.config.contextxmlservice.parser.internal.PageFlowVariantExtensionPointImpl;
import org.pustefixframework.config.generic.ParsingUtils;
import org.pustefixframework.container.spring.beans.support.ScopedProxyUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultBeanNameGenerator;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.support.ManagedMap;

import com.marsching.flexiparse.configuration.RunOrder;
import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.ParsingHandler;
import com.marsching.flexiparse.parser.exception.ParserException;

import de.schlund.pfixcore.workflow.ContextImpl;
import de.schlund.pfixcore.workflow.ContextResourceManagerImpl;
import de.schlund.pfixcore.workflow.PageMap;
import de.schlund.pfixcore.workflow.context.ServerContextImpl;
import de.schlund.pfixxml.perflogging.PerfLogging;

/**
 * 
 * @author mleidig
 *
 */
public class ContextXMLParsingHandler implements ParsingHandler {

    public void handleNode(HandlerContext context) throws ParserException {
        
        if (context.getRunOrder() == RunOrder.START) {
            
            ContextXMLServletConfigImpl ctxConfig = new ContextXMLServletConfigImpl();
            context.getObjectTreeElement().addObject(ctxConfig);
            
        } else {
            ContextConfigImpl contextConfig = ParsingUtils.getSingleSubObjectFromRoot(ContextConfigImpl.class, context);
            
            try {
                contextConfig.checkAuthConstraints();
            } catch(Exception x) {
                throw new ParserException("Authconstraints are invalid", x);
            }
            
            BeanDefinitionBuilder beanBuilder;
            BeanDefinition beanDefinition;
            BeanDefinitionHolder beanHolder;
            DefaultBeanNameGenerator beanNameGenerator = new DefaultBeanNameGenerator();
            BeanDefinitionRegistry beanRegistry = ParsingUtils.getSingleTopObject(BeanDefinitionRegistry.class, context);
            
            @SuppressWarnings("unchecked")
            Map<String, Object> pageMap = new ManagedMap();
            Collection<PageRequestConfig> pageCollection = context.getObjectTreeElement().getObjectsOfTypeFromSubTree(PageRequestConfig.class);
            for (PageRequestConfig pageConfig : pageCollection) {
                pageMap.put(pageConfig.getPageName(), new RuntimeBeanReference(pageConfig.getBeanName()));
            }
            
            beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(PageMap.class);
            beanBuilder.setScope("singleton");
            beanBuilder.addPropertyValue("map", pageMap);
            beanDefinition = beanBuilder.getBeanDefinition();
            String pageMapBeanName = beanNameGenerator.generateBeanName(beanDefinition, beanRegistry);
            beanRegistry.registerBeanDefinition(pageMapBeanName, beanDefinition);
            
            @SuppressWarnings("unchecked")
            List<Object> startInterceptors = new ManagedList();
            for (String interceptorBeanName : contextConfig.getStartInterceptorBeans()) {
                startInterceptors.add(new RuntimeBeanReference(interceptorBeanName));
            }
            @SuppressWarnings("unchecked")
            List<Object> endInterceptors = new ManagedList();
            for (String interceptorBeanName : contextConfig.getEndInterceptorBeans()) {
                endInterceptors.add(new RuntimeBeanReference(interceptorBeanName));
            }
            @SuppressWarnings("unchecked")
            List<Object> postRenderInterceptors = new ManagedList();
            for (String interceptorBeanName : contextConfig.getPostRenderInterceptorBeans()) {
                postRenderInterceptors.add(new RuntimeBeanReference(interceptorBeanName));
            }
            
            @SuppressWarnings("unchecked")
            List<Object> pageFlowObjectList = new ManagedList();
            for (Object o : context.getObjectTreeElement().getObjectsOfTypeFromSubTree(Object.class)) {
                if (o instanceof PageFlowHolder) {
                    PageFlowHolder pageFlowholder = (PageFlowHolder) o;
                    pageFlowObjectList.add(pageFlowholder.getPageFlowObject());
                } else if (o instanceof PageFlowExtensionPointImpl) {
                    pageFlowObjectList.add(o);
                } else if (o instanceof PageFlowVariantExtensionPointImpl) {
                    pageFlowObjectList.add(o);
                }
            }
            
            beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(PageFlowMap.class);
            beanBuilder.setScope("singleton");
            beanBuilder.addPropertyValue("pageFlowObjects", pageFlowObjectList);
            beanDefinition = beanBuilder.getBeanDefinition();
            String pageFlowMapBeanName = beanNameGenerator.generateBeanName(beanDefinition, beanRegistry);
            beanRegistry.registerBeanDefinition(pageFlowMapBeanName, beanDefinition);
            
            beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(ContextConfigImpl.class);
            beanBuilder.setScope("singleton");
            beanBuilder.addConstructorArgValue(contextConfig);
            beanBuilder.addPropertyValue("startInterceptors", startInterceptors);
            beanBuilder.addPropertyValue("endInterceptors", endInterceptors);
            beanBuilder.addPropertyValue("postRenderInterceptors", postRenderInterceptors);
            beanBuilder.addPropertyReference("pageFlowMap", pageFlowMapBeanName);
            beanDefinition = beanBuilder.getBeanDefinition();
            String contextConfigBeanName = beanNameGenerator.generateBeanName(beanDefinition, beanRegistry);
            beanRegistry.registerBeanDefinition(contextConfigBeanName, beanDefinition);
            
            beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(ServerContextImpl.class);
            beanBuilder.setScope("singleton");
            beanBuilder.setInitMethodName("init");
            beanBuilder.addPropertyReference("config", contextConfigBeanName);
            beanBuilder.addPropertyReference("pageMap", pageMapBeanName);
            beanDefinition = beanBuilder.getBeanDefinition();
            beanHolder = new BeanDefinitionHolder(beanDefinition, ServerContextImpl.class.getName() );
            context.getObjectTreeElement().addObject(beanHolder);
            
            BeanDefinitionRegistry beanReg = ParsingUtils.getSingleTopObject(BeanDefinitionRegistry.class, context);
            
            beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(ContextResourceManagerImpl.class);
            beanBuilder.setScope("session");
            beanDefinition = beanBuilder.getBeanDefinition();
            beanHolder = new BeanDefinitionHolder(beanDefinition, ContextResourceManagerImpl.class.getName());
            beanHolder = ScopedProxyUtils.createScopedProxy(beanHolder, beanReg, true);
            context.getObjectTreeElement().addObject(beanHolder); 
            
            beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(ContextImpl.class);
            beanBuilder.setScope("session");
            beanBuilder.setInitMethodName("init");
            beanBuilder.addPropertyReference("serverContext", ServerContextImpl.class.getName());
            beanBuilder.addPropertyReference("contextResourceManager", ContextResourceManagerImpl.class.getName());
            
            if(beanReg.isBeanNameInUse(PerfLogging.class.getName())) {
                beanBuilder.addPropertyReference("perfLogging", PerfLogging.class.getName());
            }
            
            beanDefinition = beanBuilder.getBeanDefinition();
            beanHolder = new BeanDefinitionHolder(beanDefinition, ContextImpl.class.getName(), new String[] {"pustefixContext"});
            beanHolder = ScopedProxyUtils.createScopedProxy(beanHolder, beanReg, true);
            context.getObjectTreeElement().addObject(beanHolder); 
            
        }
    }

}
