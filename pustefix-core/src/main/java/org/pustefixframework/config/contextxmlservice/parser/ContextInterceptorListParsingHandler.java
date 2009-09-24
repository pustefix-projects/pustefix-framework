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

import java.util.List;

import org.pustefixframework.config.contextxmlservice.ContextInterceptorHolder;
import org.pustefixframework.config.contextxmlservice.ContextInterceptorListHolder;
import org.pustefixframework.config.contextxmlservice.parser.internal.ContextInterceptorExtensionPointImpl;
import org.pustefixframework.config.contextxmlservice.parser.internal.ContextInterceptorList;
import org.pustefixframework.config.generic.ParsingUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultBeanNameGenerator;
import org.springframework.beans.factory.support.ManagedList;
import org.w3c.dom.Element;

import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.ParsingHandler;
import com.marsching.flexiparse.parser.exception.ParserException;

/**
 * Handles the declaration of a set of context interceptors (start, end or
 * postrender).  
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class ContextInterceptorListParsingHandler implements ParsingHandler {

    public void handleNode(HandlerContext context) throws ParserException {
        Element element = (Element) context.getNode();

        @SuppressWarnings("unchecked")
        List<Object> contextInterceptorObjects = new ManagedList();

        for (Object o : context.getObjectTreeElement().getObjectsOfTypeFromSubTree(Object.class)) {
            if (o instanceof ContextInterceptorExtensionPointImpl) {
                contextInterceptorObjects.add(o);
            } else if (o instanceof ContextInterceptorHolder) {
                ContextInterceptorHolder holder = (ContextInterceptorHolder) o;
                contextInterceptorObjects.add(holder.getContextInterceptorObject());
            }
        }

        BeanDefinitionRegistry beanRegistry = ParsingUtils.getSingleTopObject(BeanDefinitionRegistry.class, context);
        DefaultBeanNameGenerator beanNameGenerator = new DefaultBeanNameGenerator();
        BeanDefinitionBuilder beanBuilder;
        BeanDefinition beanDefinition;
        String beanName;

        beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(ContextInterceptorList.class);
        beanBuilder.setScope("singleton");
        beanBuilder.addPropertyValue("contextInterceptorObjects", contextInterceptorObjects);
        beanDefinition = beanBuilder.getBeanDefinition();
        beanName = beanNameGenerator.generateBeanName(beanDefinition, beanRegistry);
        beanRegistry.registerBeanDefinition(beanName, beanDefinition);

        final RuntimeBeanReference beanReference = new RuntimeBeanReference(beanName);
        final String listType = element.getNodeName();

        context.getObjectTreeElement().addObject(new ContextInterceptorListHolder() {

            public Object getContextInterceptorListObject() {
                return beanReference;
            }

            public String getListType() {
                return listType;
            }
        });
    }

}
