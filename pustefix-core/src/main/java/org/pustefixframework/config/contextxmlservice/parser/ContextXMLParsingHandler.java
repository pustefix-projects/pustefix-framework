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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.pustefixframework.config.contextxmlservice.ContextConfigHolder;
import org.pustefixframework.config.contextxmlservice.ContextInterceptorListHolder;
import org.pustefixframework.config.contextxmlservice.PageFlowHolder;
import org.pustefixframework.config.contextxmlservice.PageRequestConfigHolder;
import org.pustefixframework.config.contextxmlservice.parser.internal.AuthConstraintExtensionPointImpl;
import org.pustefixframework.config.contextxmlservice.parser.internal.AuthConstraintMap;
import org.pustefixframework.config.contextxmlservice.parser.internal.AuthConstraintRef;
import org.pustefixframework.config.contextxmlservice.parser.internal.ContextConfigImpl;
import org.pustefixframework.config.contextxmlservice.parser.internal.PageFlowExtensionPointImpl;
import org.pustefixframework.config.contextxmlservice.parser.internal.PageFlowMap;
import org.pustefixframework.config.contextxmlservice.parser.internal.PageFlowVariantExtensionPointImpl;
import org.pustefixframework.config.contextxmlservice.parser.internal.PageRequestConfigExtensionPointImpl;
import org.pustefixframework.config.contextxmlservice.parser.internal.PageRequestConfigMap;
import org.pustefixframework.config.contextxmlservice.parser.internal.PageRequestConfigVariantExtensionPointImpl;
import org.pustefixframework.config.contextxmlservice.parser.internal.PustefixContextXMLRequestHandlerConfigImpl;
import org.pustefixframework.config.contextxmlservice.parser.internal.RoleExtensionPointImpl;
import org.pustefixframework.config.contextxmlservice.parser.internal.RoleMap;
import org.pustefixframework.config.generic.ParsingUtils;
import org.pustefixframework.container.spring.beans.support.ScopedProxyUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultBeanNameGenerator;
import org.springframework.beans.factory.support.ManagedList;

import com.marsching.flexiparse.configuration.RunOrder;
import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.ParsingHandler;
import com.marsching.flexiparse.parser.exception.ParserException;

import de.schlund.pfixcore.auth.AuthConstraint;
import de.schlund.pfixcore.auth.Role;
import de.schlund.pfixcore.workflow.ContextImpl;
import de.schlund.pfixcore.workflow.ContextResourceManagerImpl;
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
            
            PustefixContextXMLRequestHandlerConfigImpl ctxConfig = new PustefixContextXMLRequestHandlerConfigImpl();
            context.getObjectTreeElement().addObject(ctxConfig);
            
        } else {
            ContextConfigImpl contextConfig = ParsingUtils.getSingleSubObjectFromRoot(ContextConfigImpl.class, context);
            
            BeanDefinitionBuilder beanBuilder;
            BeanDefinition beanDefinition;
            BeanDefinitionHolder beanHolder;
            DefaultBeanNameGenerator beanNameGenerator = new DefaultBeanNameGenerator();
            BeanDefinitionRegistry beanRegistry = ParsingUtils.getSingleTopObject(BeanDefinitionRegistry.class, context);
            
            @SuppressWarnings("unchecked")
            List<Object> pageList = new ManagedList();
            for (Object o : context.getObjectTreeElement().getObjectsOfTypeFromSubTree(Object.class)) {
                if (o instanceof PageRequestConfigHolder) {
                    PageRequestConfigHolder holder = (PageRequestConfigHolder) o;
                    pageList.add(holder.getPageRequestConfigObject());
                } else if (o instanceof PageRequestConfigExtensionPointImpl) {
                    pageList.add(o);
                } else if (o instanceof PageRequestConfigVariantExtensionPointImpl) {
                    pageList.add(o);
                }
            }
            
            beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(PageRequestConfigMap.class);
            beanBuilder.setScope("singleton");
            beanBuilder.addPropertyValue("pageRequestConfigObjects", pageList);
            beanDefinition = beanBuilder.getBeanDefinition();
            String pageMapBeanName = beanNameGenerator.generateBeanName(beanDefinition, beanRegistry);
            beanRegistry.registerBeanDefinition(pageMapBeanName, beanDefinition);

            Object startInterceptors = Collections.emptyList();
            Object endInterceptors = Collections.emptyList();
            Object postRenderInterceptors = Collections.emptyList();

            for (ContextInterceptorListHolder holder : context.getObjectTreeElement().getObjectsOfTypeFromSubTree(ContextInterceptorListHolder.class)) {
                if (holder.getListType().equals("start")) {
                    startInterceptors = holder.getContextInterceptorListObject();
                } else if (holder.getListType().equals("end")) {
                    endInterceptors = holder.getContextInterceptorListObject();
                } else if (holder.getListType().equals("postrender")) {
                    postRenderInterceptors = holder.getContextInterceptorListObject();
                } else {
                    throw new ParserException("Found ContextInterceptorListHolder with unknown type \"" + holder.getListType() + "\".");
                }
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
            
            List<Object> roleObjectList = new ArrayList<Object>();
            List<Object> authConstraintObjectList = new ArrayList<Object>();
            for (Object obj : context.getObjectTreeElement().getObjectsOfTypeFromSubTree(Object.class)) {
                if (obj instanceof AuthConstraint) {
                	AuthConstraint con = (AuthConstraint)obj;
                	if(!(con instanceof AuthConstraintRef)) {
                		if(!con.getId().equals("anonymous")) {
                			authConstraintObjectList.add(con);
                		}
                	}
                } else if (obj instanceof Role) {
                	roleObjectList.add(obj);
                } else if (obj instanceof AuthConstraintExtensionPointImpl) {
                	authConstraintObjectList.add(obj);
                } else if (obj instanceof RoleExtensionPointImpl) {
                	roleObjectList.add(obj);
                }
            }
            RoleMap roles = new RoleMap();
            roles.setRoleObjects(roleObjectList);
            AuthConstraintMap authConstraints = new AuthConstraintMap();
            authConstraints.setAuthConstraintObjects(authConstraintObjectList);
            
            beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(ContextConfigImpl.class);
            beanBuilder.setScope("singleton");
            beanBuilder.addConstructorArgValue(contextConfig);
            beanBuilder.addPropertyValue("roles", roles);
            beanBuilder.addPropertyValue("authConstraints", authConstraints);
            beanBuilder.addPropertyValue("startInterceptors", startInterceptors);
            beanBuilder.addPropertyValue("endInterceptors", endInterceptors);
            beanBuilder.addPropertyValue("postRenderInterceptors", postRenderInterceptors);
            beanBuilder.addPropertyReference("pageRequestConfigMap", pageMapBeanName);
            beanBuilder.addPropertyReference("pageFlowMap", pageFlowMapBeanName);
            beanDefinition = beanBuilder.getBeanDefinition();
            String contextConfigBeanName = beanNameGenerator.generateBeanName(beanDefinition, beanRegistry);
            beanRegistry.registerBeanDefinition(contextConfigBeanName, beanDefinition);
            final RuntimeBeanReference contextConfigReference = new RuntimeBeanReference(contextConfigBeanName);
            context.getObjectTreeElement().addObject(new ContextConfigHolder() {

                public Object getContextConfigObject() {
                    return contextConfigReference;
                }
                
            });
            
            beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(ServerContextImpl.class);
            beanBuilder.setScope("singleton");
            beanBuilder.setInitMethodName("init");
            beanBuilder.addPropertyReference("config", contextConfigBeanName);
            beanDefinition = beanBuilder.getBeanDefinition();
            beanHolder = new BeanDefinitionHolder(beanDefinition, ServerContextImpl.class.getName() );
            context.getObjectTreeElement().addObject(beanHolder);
            
            beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(ContextResourceManagerImpl.class);
            beanBuilder.setScope("session");
            beanDefinition = beanBuilder.getBeanDefinition();
            beanHolder = new BeanDefinitionHolder(beanDefinition, ContextResourceManagerImpl.class.getName());
            beanHolder = ScopedProxyUtils.createScopedProxy(beanHolder, beanRegistry, true);
            context.getObjectTreeElement().addObject(beanHolder); 
            
            beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(ContextImpl.class);
            beanBuilder.setScope("session");
            beanBuilder.setInitMethodName("init");
            beanBuilder.addPropertyReference("serverContext", ServerContextImpl.class.getName());
            beanBuilder.addPropertyReference("contextResourceManager", ContextResourceManagerImpl.class.getName());
            
            if (beanRegistry.isBeanNameInUse(PerfLogging.class.getName())) {
                beanBuilder.addPropertyReference("perfLogging", PerfLogging.class.getName());
            }
            
            beanDefinition = beanBuilder.getBeanDefinition();
            beanHolder = new BeanDefinitionHolder(beanDefinition, ContextImpl.class.getName(), new String[] {"pustefixContext"});
            beanHolder = ScopedProxyUtils.createScopedProxy(beanHolder, beanRegistry, true);
            context.getObjectTreeElement().addObject(beanHolder); 
        }
    }

}
