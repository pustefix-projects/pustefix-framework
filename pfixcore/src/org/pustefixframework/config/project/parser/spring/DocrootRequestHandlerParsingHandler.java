/*
 * This file is part of PFIXCORE.
 *
 * PFIXCORE is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * PFIXCORE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with PFIXCORE; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.pustefixframework.config.project.parser.spring;

import java.util.ArrayList;

import org.pustefixframework.config.Constants;
import org.pustefixframework.config.global.GlobalConfigurationHolder;
import org.pustefixframework.config.global.StaticPathInfo;
import org.pustefixframework.http.DocrootRequestHandler;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.ParsingHandler;
import com.marsching.flexiparse.parser.exception.ParserException;

public class DocrootRequestHandlerParsingHandler implements ParsingHandler {
    
    public void handleNode(HandlerContext context) throws ParserException {
        Element applicationElement = (Element) context.getNode();
        
        NodeList defaultPathList = applicationElement.getElementsByTagNameNS(Constants.NS_PROJECT, "default-path");
        if (defaultPathList.getLength() != 1) {
            throw new ParserException("Found " + defaultPathList.getLength() + " <default-path> elements but expected one.");
        }
        Element defaultPathElement = (Element) defaultPathList.item(0);
        String defaultPath = defaultPathElement.getTextContent();

        NodeList basePathList = applicationElement.getElementsByTagNameNS(Constants.NS_PROJECT, "docroot-path");
        if (basePathList.getLength() != 1) {
            throw new ParserException("Found " + basePathList.getLength() + " <docroot-path> elements but expected one.");
        }
        Element basePathElement = (Element)basePathList.item(0);
        String basePath = basePathElement.getTextContent();
        
        ArrayList<String> paths = new ArrayList<String>();
        
        GlobalConfigurationHolder gch = context.getObjectTreeElement().getRoot().getObjectsOfTypeFromSubTree(GlobalConfigurationHolder.class).iterator().next();
        for (StaticPathInfo pathInfo : gch.getConfigurationRoot().getObjectsOfTypeFromSubTree(StaticPathInfo.class)) {
            String path = pathInfo.getPath();
            if (!paths.contains(path)) {
                paths.add(path);
            }
        }
        
        NodeList staticList = applicationElement.getElementsByTagNameNS(Constants.NS_PROJECT, "static");
        for (int i = 0; i < staticList.getLength(); i++) {
            Element staticElement = (Element) staticList.item(i);
            NodeList pathList = staticElement.getElementsByTagNameNS(Constants.NS_PROJECT, "path");
            for (int j = 0; j < pathList.getLength(); j++) {
                Element pathElement = (Element) pathList.item(j);
                String path = pathElement.getTextContent();
                if (!paths.contains(path)) {
                    paths.add(path);
                }
            }
        }
        
        BeanDefinitionBuilder beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(DocrootRequestHandler.class);
        beanBuilder.setScope("singleton");
        beanBuilder.addPropertyValue("base", basePath);
        beanBuilder.addPropertyValue("defaultPath", defaultPath);
        beanBuilder.addPropertyValue("passthroughPaths", paths);
        
        context.getObjectTreeElement().addObject(new BeanDefinitionHolder(beanBuilder.getBeanDefinition(), "org.pustefixframework.http.DocrootRequestHandler"));
    }

}
