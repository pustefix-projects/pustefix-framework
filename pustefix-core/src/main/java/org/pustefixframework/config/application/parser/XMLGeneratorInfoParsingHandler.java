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
import org.pustefixframework.xmlgenerator.cachestat.CacheStatistic;
import org.pustefixframework.xmlgenerator.targets.LRUCache;
import org.pustefixframework.xmlgenerator.targets.TargetGenerator;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultBeanNameGenerator;
import org.w3c.dom.Element;

import com.marsching.flexiparse.configuration.RunOrder;
import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.exception.ParserException;


public class XMLGeneratorInfoParsingHandler extends CustomizationAwareParsingHandler {

	private final String TARGETGENERATOR_BEANNAME = "org.pustefixframework.xmlgenerator";
	private final String TARGETCACHE_BEANNAME = "org.pustefixframework.xmlgenerator.targetcache";
	private final String INCLUDECACHE_BEANNAME = "org.pustefixframework.xmlgenerator.includecache";
	private final String CACHESTATISTIC_BEANNAME = "org.pustefixframework.xmlgenerator.cachestatistic";
	
	private BeanDefinitionBuilder targetCacheBeanBuilder;
	private BeanDefinitionBuilder includeCacheBeanBuilder;
	private BeanDefinitionBuilder statsBeanBuilder;
	
    @Override
    public void handleNodeIfActive(HandlerContext context) throws ParserException {
        
        Element root = (Element)context.getNode();

        if(context.getRunOrder() == RunOrder.START) {
        
        if(root.getLocalName().equals("xml-generator")) {
        	
        	statsBeanBuilder = BeanDefinitionBuilder.genericBeanDefinition(CacheStatistic.class);
        	statsBeanBuilder.setScope("singleton");
        	statsBeanBuilder.addPropertyValue("queueSize", 60);
        	statsBeanBuilder.addPropertyValue("queueTicks", 60000);
        	ProjectInfo projectInfo = ParsingUtils.getSingleTopObject(ProjectInfo.class, context);
        	statsBeanBuilder.addPropertyValue("projectName", projectInfo.getProjectName());
        	statsBeanBuilder.addPropertyReference("targetGenerator", TARGETGENERATOR_BEANNAME);
        	BeanDefinitionHolder beanHolder = new BeanDefinitionHolder(statsBeanBuilder.getBeanDefinition(), CACHESTATISTIC_BEANNAME);
        	context.getObjectTreeElement().addObject(beanHolder);
        	
            XMLGeneratorInfo info = new XMLGeneratorInfo();
            context.getObjectTreeElement().addObject(info);
        
        } else if(root.getLocalName().equals("config-file")) {
        
            XMLGeneratorInfo info = ParsingUtils.getSingleTopObject(XMLGeneratorInfo.class, context);
            ResourceLoader resourceLoader = ParsingUtils.getSingleTopObject(ResourceLoader.class, context);
            
            String uri = root.getTextContent().trim();
            
            BeanDefinitionRegistry beanRegistry = ParsingUtils.getSingleTopObject(BeanDefinitionRegistry.class, context);
        
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
            beanBuilder.addPropertyReference("targetCache", TARGETCACHE_BEANNAME);
            beanBuilder.addPropertyReference("includeCache", INCLUDECACHE_BEANNAME);
            beanBuilder.addPropertyReference("cacheStatistic", CACHESTATISTIC_BEANNAME);
            BeanDefinition beanDefinition = beanBuilder.getBeanDefinition();
            beanRegistry.registerBeanDefinition(TARGETGENERATOR_BEANNAME, beanDefinition);
            info.setConfigurationFile(uri);           
            info.setTargetGeneratorBeanName(TARGETGENERATOR_BEANNAME);
            
        } else if(root.getLocalName().equals("check-modtime")) {
            
            XMLGeneratorInfo info = ParsingUtils.getSingleTopObject(XMLGeneratorInfo.class, context);
            boolean checkModtime = Boolean.parseBoolean(root.getTextContent().trim());
            info.setCheckModtime(checkModtime);
        
        } else if(root.getLocalName().equals("include-cache")) {
        	
        	String className = root.getAttribute("class").trim();
        	Class<?> clazz = LRUCache.class;
        	if(className.length() > 0) {
        		try {
					clazz = Class.forName(className);
				} catch (ClassNotFoundException e) {
					throw new ParserException("Can't get cache class", e);
				}
        	}
        	includeCacheBeanBuilder = BeanDefinitionBuilder.genericBeanDefinition(clazz);
    		includeCacheBeanBuilder.setScope("singleton");
    		int capacity = 30;
        	String capacityStr = root.getAttribute("capacity").trim();
        	if(capacityStr.length() > 0) {
        		capacity = Integer.parseInt(capacityStr);
        	}
        	includeCacheBeanBuilder.addConstructorArgValue(capacity);
        	
        } else if(root.getLocalName().equals("target-cache")) {
        	
        	String className = root.getAttribute("class").trim();
        	Class<?> clazz = LRUCache.class;
        	if(className.length() > 0) {
        		try {
					clazz = Class.forName(className);
				} catch (ClassNotFoundException e) {
					throw new ParserException("Can't get cache class", e);
				}
        	}
        	targetCacheBeanBuilder = BeanDefinitionBuilder.genericBeanDefinition(clazz);
    		targetCacheBeanBuilder.setScope("singleton");
    		int capacity = 30;
    		String capacityStr = root.getAttribute("capacity").trim();
        	if(capacityStr.length() > 0) {
        		capacity = Integer.parseInt(capacityStr);
        	}
    		targetCacheBeanBuilder.addConstructorArgValue(capacity);
        	
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
        
        } else {
        	
        	 if(root.getLocalName().equals("xml-generator")) {
        	
        		 if(targetCacheBeanBuilder == null) {
        			 targetCacheBeanBuilder = BeanDefinitionBuilder.genericBeanDefinition(LRUCache.class);
        			 targetCacheBeanBuilder.setScope("singleton");
        			 targetCacheBeanBuilder.addConstructorArgValue(30);
        		 }
        		 BeanDefinitionHolder beanHolder = new BeanDefinitionHolder(targetCacheBeanBuilder.getBeanDefinition(), TARGETCACHE_BEANNAME);
        		 context.getObjectTreeElement().addObject(beanHolder);
	        	
        		 if(includeCacheBeanBuilder == null) {
        			 includeCacheBeanBuilder = BeanDefinitionBuilder.genericBeanDefinition(LRUCache.class);
        			 includeCacheBeanBuilder.setScope("singleton");
        			 includeCacheBeanBuilder.addConstructorArgValue(30);
        		 }
        		 beanHolder = new BeanDefinitionHolder(includeCacheBeanBuilder.getBeanDefinition(), INCLUDECACHE_BEANNAME);
        		 context.getObjectTreeElement().addObject(beanHolder);
        	
        	 }
        	
        }
        
    }

}
