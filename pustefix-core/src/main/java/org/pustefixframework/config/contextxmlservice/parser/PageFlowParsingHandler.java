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

import org.pustefixframework.config.contextxmlservice.PageFlowConfig;
import org.pustefixframework.config.contextxmlservice.PageFlowStepHolder;
import org.pustefixframework.config.contextxmlservice.parser.internal.PageFlowConfigImpl;
import org.pustefixframework.config.contextxmlservice.parser.internal.PageFlowHolderImpl;
import org.pustefixframework.config.contextxmlservice.parser.internal.PageFlowStepExtensionPointImpl;
import org.pustefixframework.config.contextxmlservice.parser.internal.PageFlowStepList;
import org.pustefixframework.config.generic.ParsingUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultBeanNameGenerator;
import org.springframework.beans.factory.support.ManagedList;
import org.w3c.dom.Element;

import com.marsching.flexiparse.configuration.RunOrder;
import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.ParsingHandler;
import com.marsching.flexiparse.parser.exception.ParserException;

import de.schlund.pfixcore.workflow.context.DataDrivenPageFlow;

/**
 * 
 * @author mleidig
 *
 */
public class PageFlowParsingHandler implements ParsingHandler {

    public void handleNode(HandlerContext context) throws ParserException {
        if (context.getRunOrder() == RunOrder.START) {
            handleNodeStart(context);
        } else if (context.getRunOrder() == RunOrder.END) {
            handleNodeEnd(context);
        }
    }

    public void handleNodeStart(HandlerContext context) throws ParserException {

        Element element = (Element) context.getNode();
        ParsingUtils.checkAttributes(element, new String[] { "name" }, new String[] { "final", "stopnext", "bean-ref" });

        String flowName = element.getAttribute("name").trim();
        String finalPage = element.getAttribute("final").trim();
        String stopnext = element.getAttribute("stopnext").trim();
        String beanRef = element.getAttribute("bean-ref").trim();

        if ((finalPage.length() != 0 || stopnext.length() != 0) && beanRef.length() != 0) {
            throw new ParserException("Attribute bean-ref may not be used combinded with attribute final or stopnext on <pageflow> tag.");
        }

        if (beanRef.length() > 0) {
            context.getObjectTreeElement().addObject(new PageFlowHolderImpl(flowName, beanRef));
        } else {
            PageFlowConfigImpl flowConfig = new PageFlowConfigImpl(flowName);

            if (finalPage.length() > 0) {
                flowConfig.setFinalPage(finalPage);
            }

            if (stopnext.length() > 0) {
                flowConfig.setStopNext(Boolean.parseBoolean(stopnext));
            }
            context.getObjectTreeElement().addObject(flowConfig);

            
        }
    }

    public void handleNodeEnd(HandlerContext context) throws ParserException {
        createAndRegisterBeans(context);
    }

    static void createAndRegisterBeans(HandlerContext context) throws ParserException {
        @SuppressWarnings("unchecked")
        List<Object> flowStepObjects = new ManagedList();
        for (Object o : context.getObjectTreeElement().getObjectsOfTypeFromSubTree(Object.class)) {
            if (o instanceof PageFlowStepHolder) {
                PageFlowStepHolder holder = (PageFlowStepHolder) o;
                flowStepObjects.add(holder.getPageFlowStepObject());
            } else if (o instanceof PageFlowStepExtensionPointImpl) {
                flowStepObjects.add(o);
            }
        }

        PageFlowConfig flowConfig = ParsingUtils.getSingleObject(PageFlowConfig.class, context);
        
        BeanDefinitionRegistry beanRegistry = ParsingUtils.getSingleTopObject(BeanDefinitionRegistry.class, context);
        DefaultBeanNameGenerator beanNameGenerator = new DefaultBeanNameGenerator();
        String beanName;
        BeanDefinition beanDefinition;
        BeanDefinitionBuilder beanBuilder;

        beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(PageFlowStepList.class);
        beanBuilder.setScope("singleton");
        beanBuilder.addPropertyValue("flowStepObjects", flowStepObjects);
        beanDefinition = beanBuilder.getBeanDefinition();
        beanName = beanNameGenerator.generateBeanName(beanDefinition, beanRegistry);
        beanRegistry.registerBeanDefinition(beanName, beanDefinition);

        beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(DataDrivenPageFlow.class);
        beanBuilder.setScope("singleton");
        beanBuilder.addPropertyValue("name", flowConfig.getFlowName());
        beanBuilder.addPropertyValue("finalPage", flowConfig.getFinalPage());
        beanBuilder.addPropertyReference("flowSteps", beanName);
        beanDefinition = beanBuilder.getBeanDefinition();
        beanName = beanNameGenerator.generateBeanName(beanDefinition, beanRegistry);
        beanRegistry.registerBeanDefinition(beanName, beanDefinition);

        context.getObjectTreeElement().addObject(new PageFlowHolderImpl(flowConfig.getFlowName(), beanName));
    }
}
