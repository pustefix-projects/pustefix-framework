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

package org.pustefixframework.config.module.parser;

import org.pustefixframework.config.generic.ParsingUtils;
import org.pustefixframework.http.internal.ContextStore;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultBeanNameGenerator;

import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.ParsingHandler;
import com.marsching.flexiparse.parser.exception.ParserException;

import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixcore.workflow.context.AbstractContextProxy;

/**
 * Handler that registers a proxy object implementing {@link Context) in
 * the module's application context.  
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class RegisterContextParsingHandler implements ParsingHandler {

    public void handleNode(HandlerContext context) throws ParserException {
        BeanDefinitionRegistry beanRegistry = ParsingUtils.getSingleTopObject(BeanDefinitionRegistry.class, context);
        DefaultBeanNameGenerator beanNameGenerator = new DefaultBeanNameGenerator();
        String beanName;
        BeanDefinition beanDefinition;
        BeanDefinitionBuilder beanBuilder;

        beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(ContextStoreContextProxy.class);
        beanBuilder.setScope("singleton");
        beanDefinition = beanBuilder.getBeanDefinition();
        beanName = beanNameGenerator.generateBeanName(beanDefinition, beanRegistry);
        beanRegistry.registerBeanDefinition(beanName, beanDefinition);
        beanRegistry.registerAlias(beanName, "pustefixContext");
    }

    public static class ContextStoreContextProxy extends AbstractContextProxy {

        @Override
        protected Context getContext() {
            return ContextStore.getContextForCurrentThread();
        }

    }
}
