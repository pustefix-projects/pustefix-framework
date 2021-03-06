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
package org.pustefixframework.webservices.spring;

import org.pustefixframework.config.Constants;
import org.pustefixframework.config.generic.ParsingUtils;
import org.pustefixframework.config.project.ProjectInfo;
import org.pustefixframework.webservices.ServiceRuntime;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.ParsingHandler;
import com.marsching.flexiparse.parser.exception.ParserException;

import de.schlund.pfixcore.workflow.ContextImpl;
import de.schlund.pfixcore.workflow.context.ServerContextImpl;

/**
 * 
 * @author mleidig
 *
 */
public class WebServiceRequestHandlerParsingHandler implements ParsingHandler {
    
    public void handleNode(HandlerContext context) throws ParserException {
      
        Element serviceElement = (Element)context.getNode();
        
        Element configurationFileElement = (Element)serviceElement.getElementsByTagNameNS(Constants.NS_PROJECT,"config-file").item(0);
        if (configurationFileElement == null) throw new ParserException("Could not find expected <config-file> element");
        String configurationFile = configurationFileElement.getTextContent().trim();
        ProjectInfo projectInfo = ParsingUtils.getSingleTopObject(ProjectInfo.class, context);
        if(projectInfo.getDefiningModule() != null && !configurationFile.matches("^\\w+:.*")) {
            if(configurationFile.startsWith("/")) configurationFile = configurationFile.substring(1);
            configurationFile = "module://" + projectInfo.getDefiningModule() + "/" + configurationFile;
        }
        
        String handlerURI = "/webservice";
        NodeList pathElements = serviceElement.getElementsByTagNameNS(Constants.NS_PROJECT,"path");
        if(pathElements != null && pathElements.getLength() > 0) {
            handlerURI = pathElements.item(0).getTextContent();
        }
        
        BeanDefinitionBuilder beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(WebServiceHttpRequestHandler.class);
        beanBuilder.setScope("singleton");
        beanBuilder.addPropertyValue("configFile", configurationFile);
        beanBuilder.addPropertyValue("handlerURI", handlerURI);
        beanBuilder.addPropertyValue("serviceRuntime", new RuntimeBeanReference(ServiceRuntime.class.getName()));
        BeanDefinition beanDefinition = beanBuilder.getBeanDefinition();
        BeanDefinitionHolder beanHolder = new BeanDefinitionHolder(beanDefinition, WebServiceHttpRequestHandler.class.getName());
        context.getObjectTreeElement().addObject(beanHolder);
        
        beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(ServiceRuntime.class);
        beanBuilder.setScope("singleton");
        beanBuilder.addPropertyValue("serverContext", new RuntimeBeanReference(ServerContextImpl.class.getName()));
        beanBuilder.addPropertyValue("context", new RuntimeBeanReference(ContextImpl.class.getName()));
        beanDefinition = beanBuilder.getBeanDefinition();
        beanHolder = new BeanDefinitionHolder(beanDefinition, ServiceRuntime.class.getName());
        context.getObjectTreeElement().addObject(beanHolder);
        
    }

}
