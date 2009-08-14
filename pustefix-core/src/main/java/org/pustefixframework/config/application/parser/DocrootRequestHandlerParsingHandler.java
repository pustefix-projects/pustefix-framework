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
import java.util.LinkedList;

import org.pustefixframework.config.Constants;
import org.pustefixframework.config.application.parser.internal.StaticResourceExtensionPointImpl;
import org.pustefixframework.config.generic.ParsingUtils;
import org.pustefixframework.http.DocrootRequestHandler;
import org.pustefixframework.resource.ResourceLoader;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
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

}
