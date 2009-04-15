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

package org.pustefixframework.config.project.parser;

import org.pustefixframework.config.customization.CustomizationAwareParsingHandler;
import org.pustefixframework.config.generic.ParsingUtils;
import org.pustefixframework.config.project.XMLGeneratorInfo;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultBeanNameGenerator;
import org.w3c.dom.Element;

import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.exception.ParserException;

import de.schlund.pfixxml.targets.TargetGenerator;
import de.schlund.pfixxml.targets.TargetGeneratorFactoryBean;

public class XMLGeneratorInfoParsingHandler extends CustomizationAwareParsingHandler {

    @Override
    public void handleNodeIfActive(HandlerContext context) throws ParserException {
        
        Element root = (Element)context.getNode();

        if(root.getLocalName().equals("xml-generator")) {
            
            XMLGeneratorInfo info = new XMLGeneratorInfo();
            context.getObjectTreeElement().addObject(info);
        
        } else if(root.getLocalName().equals("config-file")) {
        
            XMLGeneratorInfo info = ParsingUtils.getSingleTopObject(XMLGeneratorInfo.class, context);
            
            String uri = root.getTextContent().trim();
            
            BeanDefinitionRegistry beanRegistry = ParsingUtils.getSingleTopObject(BeanDefinitionRegistry.class, context);
            DefaultBeanNameGenerator beanNameGenerator = new DefaultBeanNameGenerator();
        
            BeanDefinitionBuilder beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(TargetGeneratorFactoryBean.class);
            beanBuilder.setScope("singleton");
            beanBuilder.addPropertyValue("configFile", uri);
            BeanDefinition beanDefinition = beanBuilder.getBeanDefinition();
            String beanName = beanNameGenerator.generateBeanName(beanDefinition, beanRegistry);
            beanRegistry.registerBeanDefinition(beanName, beanDefinition);
            beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(TargetGenerator.class);
            beanDefinition = beanBuilder.getBeanDefinition();
            beanDefinition.setFactoryBeanName(beanName);
            beanDefinition.setFactoryMethodName("getObject");
            beanName = beanNameGenerator.generateBeanName(beanDefinition, beanRegistry);
            beanRegistry.registerBeanDefinition(beanName, beanDefinition);
        
            info.setConfigurationFile(uri);
            info.setTargetGeneratorBeanName(beanName);
            
        } else if(root.getLocalName().equals("check-modtime")) {
            
            XMLGeneratorInfo info = ParsingUtils.getSingleTopObject(XMLGeneratorInfo.class, context);
            boolean checkModtime = Boolean.parseBoolean(root.getTextContent().trim());
            info.setCheckModtime(checkModtime);
        }
        
    }

}
