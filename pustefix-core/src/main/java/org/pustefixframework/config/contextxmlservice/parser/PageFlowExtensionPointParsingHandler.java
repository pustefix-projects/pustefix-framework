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

import org.pustefixframework.config.contextxmlservice.parser.internal.PageFlowExtensionPointImpl;
import org.pustefixframework.config.generic.ParsingUtils;
import org.pustefixframework.extension.ExtensionPoint;
import org.pustefixframework.extension.PageFlowExtensionPoint;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultBeanNameGenerator;
import org.springframework.osgi.service.exporter.support.OsgiServiceFactoryBean;
import org.w3c.dom.Element;

import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.ParsingHandler;
import com.marsching.flexiparse.parser.exception.ParserException;


/**
 * Handles the declaration of a page flow extension point.
 */
public class PageFlowExtensionPointParsingHandler implements ParsingHandler {
    
    public void handleNode(HandlerContext context) throws ParserException {
       
        Element element = (Element)context.getNode();
        ParsingUtils.checkAttributes(element, new String[] {"id", "type"}, new String[] {"version", "cardinality"});
        
        String id = element.getAttribute("id").trim();
        String type = element.getAttribute("type").trim();
        String version = element.getAttribute("version").trim();
        if (version.length() == 0) {
            version = "0.0.0";
        }
        String cardinality = element.getAttribute("cardinality").trim();
        if (cardinality.length() == 0) {
            cardinality = "0..n";
        }
        
        PageFlowExtensionPointImpl extensionPoint = new PageFlowExtensionPointImpl();
        extensionPoint.setId(id);
        extensionPoint.setType(type);
        extensionPoint.setVersion(version);
        extensionPoint.setCardinality(cardinality);
        
        // Register extension point as a service
        BeanDefinitionRegistry beanRegistry = ParsingUtils.getSingleTopObject(BeanDefinitionRegistry.class, context);
        DefaultBeanNameGenerator beanNameGenerator = new DefaultBeanNameGenerator();
        String beanName;
        BeanDefinition beanDefinition;
        BeanDefinitionBuilder beanBuilder;
        
        beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(OsgiServiceFactoryBean.class);
        beanBuilder.setScope("singleton");
        beanBuilder.addPropertyValue("interfaces", new Class[] {ExtensionPoint.class, PageFlowExtensionPoint.class});
        //beanBuilder.addPropertyValue("serviceProperties", serviceProperties);
        beanBuilder.addPropertyValue("target", extensionPoint);
        beanDefinition = beanBuilder.getBeanDefinition();
        beanName = beanNameGenerator.generateBeanName(beanDefinition, beanRegistry);
        beanRegistry.registerBeanDefinition(beanName, beanDefinition);
        
        // Make extension point available in object tree
        context.getObjectTreeElement().addObject(extensionPoint);
    }

}
