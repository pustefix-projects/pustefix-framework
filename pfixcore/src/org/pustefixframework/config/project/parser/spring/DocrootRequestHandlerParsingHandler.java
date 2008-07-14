/*
 * Place license here
 */

package org.pustefixframework.config.project.parser.spring;

import java.util.ArrayList;

import org.pustefixframework.config.Constants;
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
        NodeList staticList = applicationElement.getElementsByTagNameNS(Constants.NS_PROJECT, "static");
        for (int i = 0; i < staticList.getLength(); i++) {
            Element staticElement = (Element) staticList.item(i);
            NodeList pathList = staticElement.getElementsByTagNameNS(Constants.NS_PROJECT, "path");
            for (int j = 0; j < pathList.getLength(); j++) {
                Element pathElement = (Element) pathList.item(j);
                String path = pathElement.getTextContent();
                paths.add(path);
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
