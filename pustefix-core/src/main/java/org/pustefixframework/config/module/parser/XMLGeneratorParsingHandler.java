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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Properties;

import org.osgi.framework.BundleContext;
import org.pustefixframework.config.Constants;
import org.pustefixframework.config.customization.CustomizationInfo;
import org.pustefixframework.config.customization.PropertiesBasedCustomizationInfo;
import org.pustefixframework.config.customization.RuntimeProperties;
import org.pustefixframework.config.generic.ParsingUtils;
import org.pustefixframework.resource.InputStreamResource;
import org.pustefixframework.resource.ResourceLoader;
import org.pustefixframework.xmlgenerator.config.model.XMLExtension;
import org.pustefixframework.xmlgenerator.config.model.XMLExtensions;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultBeanNameGenerator;
import org.springframework.osgi.context.ConfigurableOsgiBundleApplicationContext;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import com.marsching.flexiparse.objecttree.ObjectTreeElement;
import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.OSGiAwareParser;
import com.marsching.flexiparse.parser.Parser;
import com.marsching.flexiparse.parser.ParsingHandler;
import com.marsching.flexiparse.parser.exception.ParserException;

/**
 * 
 * @author mleidig@schlund.de
 *
 */
public class XMLGeneratorParsingHandler implements ParsingHandler {

    public void handleNode(HandlerContext context) throws ParserException {
    	
    	Element element = (Element)context.getNode();
    	Element configElem = ParsingUtils.getSingleChildElement(element, Constants.NS_MODULE, "config-file", true);
    	String uriStr = ParsingUtils.getTextContent(configElem, true);
    	URI uri;
    	try {
    		uri = new URI(uriStr);
		} catch (URISyntaxException x) {
			throw new ParserException("Element 'config-file' has illegal content: " + uriStr, x);
		}
    	
        ResourceLoader resourceLoader = ParsingUtils.getSingleTopObject(ResourceLoader.class, context);
        InputStreamResource resource = resourceLoader.getResource(uri, InputStreamResource.class);
        if(resource==null) throw new ParserException("Configuration resource can't be found: " + uri.toString());
        
        Properties runtimeProperties = RuntimeProperties.getProperties();
        CustomizationInfo cusInfo = new PropertiesBasedCustomizationInfo(runtimeProperties);
        ConfigurableOsgiBundleApplicationContext appContext = ParsingUtils.getSingleTopObject(ConfigurableOsgiBundleApplicationContext.class, context);
        BundleContext bundleContext = appContext.getBundleContext();
        BeanDefinitionRegistry beanReg = ParsingUtils.getSingleTopObject(BeanDefinitionRegistry.class, context);
        
        Parser parser = new OSGiAwareParser(appContext.getBundleContext(),"META-INF/org/pustefixframework/xmlgenerator/config/parser/xml-generator-config-module.xml");
        Collection<XMLExtension> extensions;
        try {
        	InputSource source = new InputSource(resource.getInputStream());
        	source.setSystemId(resource.getURI().toASCIIString());
        	ObjectTreeElement objectTree = parser.parse(source, runtimeProperties, cusInfo, beanReg, appContext, bundleContext, resourceLoader);
        	extensions = objectTree.getObjectsOfTypeFromSubTree(XMLExtension.class);
        } catch(IOException x) {
        	throw new ParserException("Error while reading configuration: " + uri.toString(), x);
        }
        
        BeanDefinitionBuilder beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(XMLExtensions.class);
        beanBuilder.addPropertyValue("extensions", extensions);
        BeanDefinition beanDefinition = beanBuilder.getBeanDefinition();
        DefaultBeanNameGenerator beanNameGenerator = new DefaultBeanNameGenerator();
        BeanDefinitionRegistry beanRegistry = ParsingUtils.getSingleTopObject(BeanDefinitionRegistry.class, context);
        String beanName = beanNameGenerator.generateBeanName(beanDefinition, beanRegistry);
        beanRegistry.registerBeanDefinition(beanName, beanDefinition);
    }

}
