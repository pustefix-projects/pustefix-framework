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

package org.pustefixframework.config.application.parser;

import org.pustefixframework.config.generic.ParsingUtils;
import org.pustefixframework.http.internal.ContextStoreWebRequestInterceptor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultBeanNameGenerator;

import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.ParsingHandler;
import com.marsching.flexiparse.parser.exception.ParserException;

import de.schlund.pfixcore.workflow.Context;

/**
 * Parsing handler that registers a web request interceptor, which takes care
 * of storing the current {@link Context} instance in a thread local in order
 * to make it available to other bundles.
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class RegisterContextStoreWebRequestInterceptorParsingHandler implements ParsingHandler {

    public void handleNode(HandlerContext context) throws ParserException {
        BeanDefinitionRegistry beanRegistry = ParsingUtils.getSingleTopObject(BeanDefinitionRegistry.class, context);
        DefaultBeanNameGenerator beanNameGenerator = new DefaultBeanNameGenerator();
        String beanName;
        BeanDefinition beanDefinition;
        BeanDefinitionBuilder beanBuilder;

        beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(ContextStoreWebRequestInterceptor.class);
        beanBuilder.setScope("singleton");
        beanBuilder.addPropertyReference("context", "pustefixContext");
        beanDefinition = beanBuilder.getBeanDefinition();
        beanName = beanNameGenerator.generateBeanName(beanDefinition, beanRegistry);
        beanRegistry.registerBeanDefinition(beanName, beanDefinition);
    }

}
