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

import java.net.URI;
import java.net.URISyntaxException;

import org.pustefixframework.config.application.ProjectInfo;
import org.pustefixframework.config.application.XMLGeneratorInfo;
import org.pustefixframework.config.customization.CustomizationAwareParsingHandler;
import org.pustefixframework.config.generic.ParsingUtils;
import org.pustefixframework.resource.ResourceLoader;
import org.pustefixframework.resource.URLResource;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultBeanNameGenerator;
import org.w3c.dom.Element;

import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.exception.ParserException;

import de.schlund.pfixxml.targets.SPCacheFactory;
import de.schlund.pfixxml.targets.TargetGenerator;
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
        	cacheBeanBuilder.setFactoryMethod("getInstance");
        	cacheBeanBuilder.addPropertyValue("targetCacheCapacity", 30);
        	cacheBeanBuilder.addPropertyValue("targetCacheClass", "de.schlund.pfixxml.targets.LRUCache");
        	cacheBeanBuilder.addPropertyValue("includeCacheCapacity", 30);
        	cacheBeanBuilder.addPropertyValue("includeCacheClass", "de.schlund.pfixxml.targets.LRUCache");
        	cacheBeanBuilder.setInitMethodName("init");
        	BeanDefinitionHolder beanHolder = new BeanDefinitionHolder(cacheBeanBuilder.getBeanDefinition(), SPCacheFactory.class.getName());
        	context.getObjectTreeElement().addObject(beanHolder);
        	
        	statsBeanBuilder = BeanDefinitionBuilder.genericBeanDefinition(CacheStatistic.class);
        	statsBeanBuilder.setScope("singleton");
        	statsBeanBuilder.setFactoryMethod("getInstance");
        	statsBeanBuilder.addPropertyValue("queueSize", 60);
        	statsBeanBuilder.addPropertyValue("queueTicks", 60000);
        	ProjectInfo projectInfo = ParsingUtils.getSingleTopObject(ProjectInfo.class, context);
        	statsBeanBuilder.addPropertyValue("projectName", projectInfo.getProjectName());
        	statsBeanBuilder.setInitMethodName("init");
        	beanHolder = new BeanDefinitionHolder(statsBeanBuilder.getBeanDefinition(), CacheStatistic.class.getName());
        	context.getObjectTreeElement().addObject(beanHolder);
        	
            XMLGeneratorInfo info = new XMLGeneratorInfo();
            context.getObjectTreeElement().addObject(info);
        
        } else if(root.getLocalName().equals("config-file")) {
        
            XMLGeneratorInfo info = ParsingUtils.getSingleTopObject(XMLGeneratorInfo.class, context);
            ResourceLoader resourceLoader = ParsingUtils.getSingleTopObject(ResourceLoader.class, context);
            
            String uri = root.getTextContent().trim();
            
            BeanDefinitionRegistry beanRegistry = ParsingUtils.getSingleTopObject(BeanDefinitionRegistry.class, context);
            DefaultBeanNameGenerator beanNameGenerator = new DefaultBeanNameGenerator();
        
            BeanDefinitionBuilder beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(TargetGenerator.class);
            beanBuilder.setScope("singleton");
            
            URI confURI;
            try {
            	confURI = new URI(uri);
            } catch(URISyntaxException x) {
            	throw new ParserException("Illegal config-file URI: " + uri, x);
            }
            URLResource confRes = resourceLoader.getResource(confURI, URLResource.class);
            beanBuilder.addPropertyValue("configFile", confRes);
            beanBuilder.addPropertyValue("resourceLoader", resourceLoader);
            BeanDefinition beanDefinition = beanBuilder.getBeanDefinition();
            String beanName = beanNameGenerator.generateBeanName(beanDefinition, beanRegistry);
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
