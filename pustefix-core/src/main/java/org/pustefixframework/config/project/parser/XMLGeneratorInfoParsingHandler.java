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
import org.pustefixframework.config.project.ProjectInfo;
import org.pustefixframework.config.project.XMLGeneratorInfo;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultBeanNameGenerator;
import org.w3c.dom.Element;

import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.exception.ParserException;

import de.schlund.pfixxml.config.EnvironmentProperties;
import de.schlund.pfixxml.targets.SPCacheFactory;
import de.schlund.pfixxml.targets.TargetGenerator;
import de.schlund.pfixxml.targets.TargetGeneratorFactoryBean;
import de.schlund.pfixxml.targets.cachestat.CacheStatistic;

public class XMLGeneratorInfoParsingHandler extends CustomizationAwareParsingHandler {

	private BeanDefinitionBuilder cacheBeanBuilder;
	private BeanDefinitionBuilder statsBeanBuilder;
	
    @Override
    public void handleNodeIfActive(HandlerContext context) throws ParserException {
        
        Element root = (Element)context.getNode();

        if(root.getLocalName().equals("xml-generator")) {
            
        	cacheBeanBuilder = BeanDefinitionBuilder.genericBeanDefinition(SPCacheFactory.class);
        	cacheBeanBuilder.setScope("singleton");
        	cacheBeanBuilder.addPropertyValue("targetCacheCapacity", 30);
        	cacheBeanBuilder.addPropertyValue("targetCacheClass", "de.schlund.pfixxml.targets.LRUCache");
        	cacheBeanBuilder.addPropertyValue("includeCacheCapacity", 30);
        	cacheBeanBuilder.addPropertyValue("includeCacheClass", "de.schlund.pfixxml.targets.LRUCache");
        	cacheBeanBuilder.addPropertyValue("renderCacheCapacity", 150);
            cacheBeanBuilder.addPropertyValue("renderCacheClass", "de.schlund.pfixxml.targets.LRUCache");
        	cacheBeanBuilder.addPropertyReference("cacheStatistic", CacheStatistic.class.getName());
        	cacheBeanBuilder.setInitMethodName("init");
        	BeanDefinitionHolder beanHolder = new BeanDefinitionHolder(cacheBeanBuilder.getBeanDefinition(), SPCacheFactory.class.getName());
        	context.getObjectTreeElement().addObject(beanHolder);
        	
        	statsBeanBuilder = BeanDefinitionBuilder.genericBeanDefinition(CacheStatistic.class);
        	statsBeanBuilder.setScope("singleton");
        	int queueSize = 10;
        	int queueTicks = 10000;
        	if(EnvironmentProperties.getProperties().getProperty("mode").equals("prod")) {
        	    queueSize = 60;
        	    queueTicks = 60000;
        	}
        	statsBeanBuilder.addPropertyValue("queueSize", queueSize);
        	statsBeanBuilder.addPropertyValue("queueTicks", queueTicks);
        	ProjectInfo projectInfo = ParsingUtils.getSingleTopObject(ProjectInfo.class, context);
        	statsBeanBuilder.addPropertyValue("projectName", projectInfo.getProjectName());
        	beanHolder = new BeanDefinitionHolder(statsBeanBuilder.getBeanDefinition(), CacheStatistic.class.getName());
        	context.getObjectTreeElement().addObject(beanHolder);
        	
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
            beanBuilder.addPropertyReference("cacheFactory", SPCacheFactory.class.getName());
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
        
        } else if(root.getLocalName().equals("include-cache")) {
        	
        	String className = root.getAttribute("class").trim();
        	if(className.length() > 0) {
        		cacheBeanBuilder.addPropertyValue("includeCacheClass", className);
        	}
        	String capacity = root.getAttribute("capacity").trim();
        	if(capacity.length() > 0) {
        		cacheBeanBuilder.addPropertyValue("includeCacheCapacity", Integer.parseInt(capacity));
        	}
        	
        } else if(root.getLocalName().equals("target-cache")) {
        	
        	String className = root.getAttribute("class").trim();
        	if(className.length() > 0) {
        		cacheBeanBuilder.addPropertyValue("targetCacheClass", className);
        	}
        	String capacity = root.getAttribute("capacity").trim();
        	if(capacity.length() > 0) {
        		cacheBeanBuilder.addPropertyValue("targetCacheCapacity", Integer.parseInt(capacity));
        	}
        
        } else if(root.getLocalName().equals("render-cache")) {
            
            String className = root.getAttribute("class").trim();
            if(className.length() > 0) {
                cacheBeanBuilder.addPropertyValue("renderCacheClass", className);
            }
            String capacity = root.getAttribute("capacity").trim();
            if(capacity.length() > 0) {
                cacheBeanBuilder.addPropertyValue("renderCacheCapacity", Integer.parseInt(capacity));
            }
            
        } else if(root.getLocalName().equals("cache-statistic")) {
        	
        	String queueSize = root.getAttribute("queuesize").trim();
        	if(queueSize.length() > 0) {
        		statsBeanBuilder.addPropertyValue("queueSize", Integer.parseInt(queueSize));
        	}
        	String queueTicks = root.getAttribute("queueticks").trim();
        	if(queueTicks.length() > 0) {
        		statsBeanBuilder.addPropertyValue("queueTicks", Integer.parseInt(queueTicks));
        	}
        }
        
    }

}
