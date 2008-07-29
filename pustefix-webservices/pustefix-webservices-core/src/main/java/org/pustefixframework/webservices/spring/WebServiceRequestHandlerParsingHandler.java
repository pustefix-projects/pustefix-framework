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
 *
 */
package org.pustefixframework.webservices.spring;

import org.pustefixframework.config.Constants;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.w3c.dom.Element;

import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.ParsingHandler;
import com.marsching.flexiparse.parser.exception.ParserException;

/**
 * 
 * @author mleidig
 *
 */
public class WebServiceRequestHandlerParsingHandler implements ParsingHandler {
    
    public void handleNode(HandlerContext context) throws ParserException {
      
        Element serviceElement = (Element)context.getNode();
        
        Element pathElement = (Element)serviceElement.getElementsByTagNameNS(Constants.NS_PROJECT,"path").item(0);
        if (pathElement == null) throw new ParserException("Could not find expected <path> element");
        String path = pathElement.getTextContent().trim();
        
        Element configurationFileElement = (Element)serviceElement.getElementsByTagNameNS(Constants.NS_PROJECT,"config-file").item(0);
        if (configurationFileElement == null) throw new ParserException("Could not find expected <config-file> element");
        String configurationFile = configurationFileElement.getTextContent().trim();
        
        BeanDefinitionBuilder beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(WebServiceHttpRequestHandler.class);
        beanBuilder.setScope("singleton");
        beanBuilder.addPropertyValue("configFile", configurationFile);
        beanBuilder.addPropertyValue("handlerURI", path + "/**");
     
        BeanDefinition beanDefinition = beanBuilder.getBeanDefinition();
        BeanDefinitionHolder beanHolder = new BeanDefinitionHolder(beanDefinition, WebServiceHttpRequestHandler.class.getName()+"#"+path);
        context.getObjectTreeElement().addObject(beanHolder);
        
    }

}
