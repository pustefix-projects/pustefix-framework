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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.pustefixframework.config.Constants;
import org.pustefixframework.config.application.parser.internal.StaticResourceExtensionPointImpl;
import org.pustefixframework.config.generic.ParsingUtils;
import org.pustefixframework.extension.ExtensionPoint;
import org.pustefixframework.extension.StaticResourceExtensionPoint;
import org.pustefixframework.http.DocrootRequestHandler;
import org.pustefixframework.resource.ResourceLoader;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultBeanNameGenerator;
import org.springframework.osgi.service.exporter.support.OsgiServiceFactoryBean;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.ParsingHandler;
import com.marsching.flexiparse.parser.exception.ParserException;

/**
 * Handles configuration and creation of static resource request handler.  
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class DocrootRequestHandlerParsingHandler implements ParsingHandler {
    
    public void handleNode(HandlerContext context) throws ParserException {
        Element applicationElement = (Element) context.getNode();
        
        NodeList defaultPathList = applicationElement.getElementsByTagNameNS(Constants.NS_APPLICATION, "default-path");
        if (defaultPathList.getLength() != 1) {
            throw new ParserException("Found " + defaultPathList.getLength() + " <default-path> elements but expected one.");
        }
        Element defaultPathElement = (Element) defaultPathList.item(0);
        String defaultPath = defaultPathElement.getTextContent();

        NodeList basePathList = applicationElement.getElementsByTagNameNS(Constants.NS_APPLICATION, "docroot-path");
        if (basePathList.getLength() > 1) {
            throw new ParserException("Found " + basePathList.getLength() + " <docroot-path> elements but expected one.");
        }
        Element basePathElement = basePathList.getLength() > 0 ? (Element)basePathList.item(0) : null;
        String basePath = basePathElement != null ? basePathElement.getTextContent() : null;
        
        ArrayList<String> paths = new ArrayList<String>();
        
        //Add pre-defined static paths
        paths.add("wsscript");
        
        NodeList staticList = applicationElement.getElementsByTagNameNS(Constants.NS_APPLICATION, "static");
        for (int i = 0; i < staticList.getLength(); i++) {
            Element staticElement = (Element) staticList.item(i);
            NodeList pathList = staticElement.getElementsByTagNameNS(Constants.NS_APPLICATION, "path");
            for (int j = 0; j < pathList.getLength(); j++) {
                Element pathElement = (Element) pathList.item(j);
                String path = pathElement.getTextContent();
                if (!paths.contains(path)) {
                    paths.add(path);
                }
            }
        }
        
        LinkedList<StaticResourceExtensionPointImpl> extensionPoints = new LinkedList<StaticResourceExtensionPointImpl>();
        extensionPoints.add(createDefaultStaticResourceExtensionPoint(context));
        extensionPoints.addAll(context.getObjectTreeElement().getObjectsOfTypeFromSubTree(StaticResourceExtensionPointImpl.class));
        
        BeanDefinitionBuilder beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(DocrootRequestHandler.class);
        beanBuilder.setScope("singleton");
        beanBuilder.addPropertyValue("base", basePath);
        beanBuilder.addPropertyValue("defaultPath", defaultPath);
        beanBuilder.addPropertyValue("applicationPathPrefixes", paths);
        beanBuilder.addPropertyValue("staticResourceExtensionPoints", extensionPoints);
        
        ResourceLoader resourceLoader = ParsingUtils.getSingleTopObject(ResourceLoader.class, context);
        beanBuilder.addPropertyValue("resourceLoader", resourceLoader);
        
        context.getObjectTreeElement().addObject(new BeanDefinitionHolder(beanBuilder.getBeanDefinition(), "org.pustefixframework.http.DocrootRequestHandler"));
    }

    private StaticResourceExtensionPointImpl createDefaultStaticResourceExtensionPoint(HandlerContext context) throws ParserException {
        StaticResourceExtensionPointImpl extensionPoint = new StaticResourceExtensionPointImpl();
        extensionPoint.setCardinality("0..n");
        extensionPoint.setId(Constants.EXTENSION_POINT_DEFAULT_STATIC_RESOURCES);
        extensionPoint.setType("application.static");
        extensionPoint.setVersion("0.0.0");
        
        BeanDefinitionRegistry beanRegistry = ParsingUtils.getSingleTopObject(BeanDefinitionRegistry.class, context);
        DefaultBeanNameGenerator beanNameGenerator = new DefaultBeanNameGenerator();
        String beanName;
        BeanDefinition beanDefinition;
        BeanDefinitionBuilder beanBuilder;
        
        Map<String, String> serviceProperties = new HashMap<String, String>();
        serviceProperties.put("extension-point", Constants.EXTENSION_POINT_DEFAULT_STATIC_RESOURCES);
        serviceProperties.put("type", "application.static");
        serviceProperties.put("version", "0.0.0");
        
        beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(OsgiServiceFactoryBean.class);
        beanBuilder.setScope("singleton");
        beanBuilder.addPropertyValue("interfaces", new Class[] {ExtensionPoint.class, StaticResourceExtensionPoint.class});
        beanBuilder.addPropertyValue("serviceProperties", serviceProperties);
        beanBuilder.addPropertyValue("target", extensionPoint);
        beanDefinition = beanBuilder.getBeanDefinition();
        beanName = beanNameGenerator.generateBeanName(beanDefinition, beanRegistry);
        beanRegistry.registerBeanDefinition(beanName, beanDefinition);

        return extensionPoint;
    }
}
