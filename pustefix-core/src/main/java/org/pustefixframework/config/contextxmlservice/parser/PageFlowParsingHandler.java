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

import org.pustefixframework.config.contextxmlservice.parser.internal.PageFlowConfigImpl;
import org.pustefixframework.config.contextxmlservice.parser.internal.PageFlowHolderImpl;
import org.pustefixframework.config.generic.ParsingUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultBeanNameGenerator;
import org.w3c.dom.Element;

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
       
        Element element = (Element)context.getNode();
        ParsingUtils.checkAttributes(element, new String[] {"name"}, new String[] {"final", "stopnext", "bean-ref"});
        
        String flowName = element.getAttribute("name").trim();
        String finalPage = element.getAttribute("final").trim();
        String stopnext = element.getAttribute("stopnext").trim();
        String beanRef = element.getAttribute("bean-ref").trim();
        
        if ((finalPage.length() != 0 || stopnext.length() != 0) 
                && beanRef.length() != 0) {
            throw new ParserException("Attribute bean-ref may not be used combinded with attribute final or stopnext on <pageflow> tag.");
        }
        
        if (beanRef.length() > 0) {
            context.getObjectTreeElement().addObject(new PageFlowHolderImpl(flowName, beanRef));
        } else {           
            PageFlowConfigImpl flowConfig = new PageFlowConfigImpl(flowName);
            
            if (finalPage.length()>0) {
                flowConfig.setFinalPage(finalPage);
            }
            
            if (stopnext.length()>0) {
                flowConfig.setStopNext(Boolean.parseBoolean(stopnext));
            }
            context.getObjectTreeElement().addObject(flowConfig);
            
            BeanDefinitionRegistry beanRegistry = ParsingUtils.getSingleTopObject(BeanDefinitionRegistry.class, context);
            DefaultBeanNameGenerator beanNameGenerator = new DefaultBeanNameGenerator();
            String beanName;
            BeanDefinition beanDefinition;
            BeanDefinitionBuilder beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(DataDrivenPageFlow.class);
            beanBuilder.setScope("singleton");
            beanBuilder.addConstructorArgValue(flowConfig);
            beanDefinition = beanBuilder.getBeanDefinition();
            beanName = beanNameGenerator.generateBeanName(beanDefinition, beanRegistry);
            beanRegistry.registerBeanDefinition(beanName, beanDefinition);
            
            context.getObjectTreeElement().addObject(new PageFlowHolderImpl(flowConfig.getFlowName(), beanName));
        }
    }

}
