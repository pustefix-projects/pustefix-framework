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

import org.pustefixframework.config.contextxmlservice.parser.internal.ContextXMLServletConfigImpl;
import org.pustefixframework.config.generic.ParsingUtils;
import org.springframework.aop.scope.ScopedProxyUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultBeanNameGenerator;
import org.w3c.dom.Element;

import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.ParsingHandler;
import com.marsching.flexiparse.parser.exception.ParserException;

import de.schlund.pfixcore.workflow.ContextInterceptor;

public class ContextInterceptorParsingHandler implements ParsingHandler {

    public void handleNode(HandlerContext context) throws ParserException {
       
        Element element = (Element)context.getNode();
        ParsingUtils.checkAttributes(element, null, new String[] {"class","bean-ref"});
        
        ContextXMLServletConfigImpl config = ParsingUtils.getSingleTopObject(ContextXMLServletConfigImpl.class, context);     
        
        String beanName;
        
        String className = element.getAttribute("class").trim();
        String beanRef = element.getAttribute("bean-ref").trim();
        if(className.length()>0) {
            
            Class<?> clazz;
            try {
                clazz = Class.forName(className);
            } catch (ClassNotFoundException e) {
                throw new ParserException("Could not load interceptor class " + className, e);
            }
            if (!ContextInterceptor.class.isAssignableFrom(clazz)) {
                throw new ParserException("Context interceptor " + clazz + " does not implement " + ContextInterceptor.class + " interface!");
            }
            
            String scope = element.getAttribute("scope");
            if (scope == null || scope.length() == 0) {
                scope = "singleton";
            }
            
            BeanDefinitionRegistry beanRegistry = ParsingUtils.getSingleTopObject(BeanDefinitionRegistry.class, context);
            DefaultBeanNameGenerator beanNameGenerator = new DefaultBeanNameGenerator();
            BeanDefinitionBuilder beanBuilder;
            BeanDefinitionHolder beanHolder;
            BeanDefinition beanDefinition;
            
            beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(clazz);
            beanBuilder.setScope(scope);
            beanDefinition = beanBuilder.getBeanDefinition();
            beanName = beanNameGenerator.generateBeanName(beanDefinition, beanRegistry);
            beanHolder = new BeanDefinitionHolder(beanDefinition, beanName);
            if (!scope.equals("singleton") && !scope.equals("prototype")) {
                beanHolder = ScopedProxyUtils.createScopedProxy(beanHolder, beanRegistry, true);
            }
            beanRegistry.registerBeanDefinition(beanHolder.getBeanName(), beanHolder.getBeanDefinition());
            if (beanHolder.getAliases() != null) {
                for (String alias : beanHolder.getAliases()) {
                    beanRegistry.registerAlias(beanHolder.getBeanName(), alias);
                }
            }
            if(beanRef.length()>0) {
                throw new ParserException("Setting 'class' and 'bean-ref' attribute at 'interceptor' element isn't allowed.");
            }
        } else if(beanRef.length()>0) {
            beanName = beanRef;
        } else {
            throw new ParserException("No 'class' or 'bean-ref' attribute set at 'interceptor' element.");
        }
       
        
        Element parent = (Element)element.getParentNode();
        if (parent.getNodeName().equals("start")) {
            config.getContextConfig().addStartInterceptorBean(beanName);
        }
        if (parent.getNodeName().equals("end")) {
            config.getContextConfig().addEndInterceptorBean(beanName);
        }
        if (parent.getNodeName().equals("postrender")) {
            config.getContextConfig().addPostRenderInterceptorBean(beanName);
        }
        
    }

}
