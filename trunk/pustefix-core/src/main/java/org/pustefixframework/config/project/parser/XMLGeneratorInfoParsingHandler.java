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
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.w3c.dom.Element;

import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.exception.ParserException;

import de.schlund.pfixcore.workflow.SiteMap;
import de.schlund.pfixxml.TenantInfo;
import de.schlund.pfixxml.config.EnvironmentProperties;
import de.schlund.pfixxml.resources.Resource;
import de.schlund.pfixxml.resources.ResourceUtil;
import de.schlund.pfixxml.targets.SPCacheFactory;
import de.schlund.pfixxml.targets.TargetGenerator;
import de.schlund.pfixxml.targets.cachestat.CacheStatistic;

public class XMLGeneratorInfoParsingHandler extends CustomizationAwareParsingHandler {

	private BeanDefinitionBuilder cacheBeanBuilder;
	private BeanDefinitionBuilder statsBeanBuilder;
	private BeanDefinitionBuilder targetBeanBuilder;
	private BeanDefinitionBuilder siteBeanBuilder;
	
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
        	
        	targetBeanBuilder = BeanDefinitionBuilder.genericBeanDefinition(TargetGenerator.class);
        	siteBeanBuilder = BeanDefinitionBuilder.genericBeanDefinition(SiteMap.class);
        	
        } else if(root.getLocalName().equals("config-file")) {
        
            String uri = root.getTextContent().trim();
            ProjectInfo projectInfo = ParsingUtils.getSingleTopObject(ProjectInfo.class, context);
            if(projectInfo.getDefiningModule() != null && !uri.matches("^\\w+:.*")) {
                if(uri.startsWith("/")) uri = uri.substring(1);
                uri = "module://" + projectInfo.getDefiningModule() + "/" + uri;
            }
            Resource res = ResourceUtil.getResource(uri);
            
            targetBeanBuilder.addConstructorArgValue(res);
            targetBeanBuilder.addConstructorArgReference(SPCacheFactory.class.getName());
            targetBeanBuilder.addConstructorArgReference(SiteMap.class.getName());
            targetBeanBuilder.addPropertyReference("tenantInfo", TenantInfo.class.getName());
            BeanDefinition beanDefinition = targetBeanBuilder.getBeanDefinition();
            String beanName = TargetGenerator.class.getName();
            BeanDefinitionRegistry beanRegistry = ParsingUtils.getSingleTopObject(BeanDefinitionRegistry.class, context);
            beanRegistry.registerBeanDefinition(beanName, beanDefinition);
            
            siteBeanBuilder.addConstructorArgValue(res);
            beanDefinition = siteBeanBuilder.getBeanDefinition();
            beanName = SiteMap.class.getName();
            beanRegistry.registerBeanDefinition(beanName, beanDefinition);
            
        } else if(root.getLocalName().equals("check-modtime")) {

            boolean checkModtime = Boolean.parseBoolean(root.getTextContent().trim());
            targetBeanBuilder.addPropertyValue("isGetModTimeMaybeUpdateSkipped", !checkModtime);
        
        } else if(root.getLocalName().equals("tooling-extensions")) {

            boolean toolingExtensions = Boolean.parseBoolean(root.getTextContent().trim());
            targetBeanBuilder.addPropertyValue("toolingExtensions", toolingExtensions);
            
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
