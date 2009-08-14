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

package org.pustefixframework.xmlgenerator.view;

import java.util.Hashtable;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.pustefixframework.config.generic.ParsingUtils;
import org.pustefixframework.resource.ResourceLoader;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.osgi.context.ConfigurableOsgiBundleApplicationContext;

import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.ParsingHandler;
import com.marsching.flexiparse.parser.exception.ParserException;

/**
 * Configuration handler which triggers view extension and extension point parsing.
 * 
 * @author mleidig@schlund.de
 *
 */
public class ViewExtensionsParsingHandler implements ParsingHandler {

	private Log logger = LogFactory.getLog(ViewExtensionsParsingHandler.class);
	
    public void handleNode(HandlerContext context) throws ParserException {
    	
        ResourceLoader resourceLoader = ParsingUtils.getSingleTopObject(ResourceLoader.class, context);
        ConfigurableOsgiBundleApplicationContext appContext = ParsingUtils.getSingleTopObject(ConfigurableOsgiBundleApplicationContext.class, context);
        BundleContext bundleContext = appContext.getBundleContext();
       
        long t1 = System.currentTimeMillis();
        
        ViewExtensionParser parser = new ViewExtensionParser();
        parser.parse(bundleContext, resourceLoader);
        List<ViewExtensionPoint> extensionPoints = parser.getExtensionPoints();
        List<ViewExtension> extensions = parser.getExtensions();
        
        long t2 = System.currentTimeMillis();
        
        if(logger.isDebugEnabled()) {
        	logger.debug("Parsed " + parser.getFileCount() + " include part files in " + (t2-t1) + "ms");
        }
        
        for(ViewExtensionPoint extensionPoint:extensionPoints) {
        	registerExtensionPoint(extensionPoint, bundleContext);
        }
        
        BeanDefinitionBuilder beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(ViewExtensions.class);
        beanBuilder.addPropertyValue("extensions", extensions);
        BeanDefinition beanDefinition = beanBuilder.getBeanDefinition();
        BeanDefinitionRegistry beanRegistry = ParsingUtils.getSingleTopObject(BeanDefinitionRegistry.class, context);
        beanRegistry.registerBeanDefinition(ViewExtensions.class.getName(), beanDefinition);
        
        if(logger.isDebugEnabled()) {
        	logger.debug("Registered " + extensions.size() + " view extensions from bundle " + 
        			bundleContext.getBundle().getSymbolicName());
        }
        
    }
    
    private void registerExtensionPoint(ViewExtensionPoint extensionPoint, BundleContext bundleContext) {
		
		Hashtable<String,String> properties = new Hashtable<String,String>();
		properties.put("extension-point", extensionPoint.getId());
        properties.put("type", extensionPoint.getType());
        properties.put("version", extensionPoint.getVersion());
		
		bundleContext.registerService(ViewExtensionPoint.class.getName(), extensionPoint, properties);
    }

}
