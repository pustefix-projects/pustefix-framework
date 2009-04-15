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

import java.util.HashMap;
import java.util.Map;

import org.pustefixframework.config.Constants;
import org.pustefixframework.config.customization.CustomizationAwareParsingHandler;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.exception.ParserException;

import de.schlund.pfixxml.exceptionprocessor.ExceptionConfig;
import de.schlund.pfixxml.exceptionprocessor.ExceptionProcessingConfiguration;
import de.schlund.pfixxml.exceptionprocessor.ExceptionProcessor;
import de.schlund.pfixxml.exceptionprocessor.PageForwardingExceptionProcessor;
import de.schlund.pfixxml.exceptionprocessor.UniversalExceptionProcessor;

/**
 * 
 * @author mleidig@schlund.de
 *
 */
public class ExceptionProcessingParsingHandler extends CustomizationAwareParsingHandler {

    private BeanDefinitionBuilder beanBuilder;
    
    @Override
    public void handleNodeIfActive(HandlerContext context) throws ParserException {
        
        Element element = (Element)context.getNode();
        
        if(element.getLocalName().equals("application")) {
            
            beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(ExceptionProcessingConfiguration.class);
            beanBuilder.setScope("singleton");
            Map<Class<?>, ExceptionConfig> map = new HashMap<Class<?>, ExceptionConfig>();
            beanBuilder.addPropertyValue("exceptionConfigs", map);
            BeanDefinition beanDefinition = beanBuilder.getBeanDefinition();
            BeanDefinitionHolder beanHolder = new BeanDefinitionHolder(beanDefinition, ExceptionProcessingConfiguration.class.getName());
            context.getObjectTreeElement().addObject(beanHolder);
            
        } else if(element.getLocalName().equals("exception-processing")) {
        
            Map<Class<?>, ExceptionConfig> map = new HashMap<Class<?>, ExceptionConfig>();
            
            NodeList nodes = element.getElementsByTagNameNS(Constants.NS_PROJECT, "process");
            for(int i=0; i<nodes.getLength(); i++) {
                Element mapElem = (Element)nodes.item(i);
                String type = mapElem.getAttribute("type").trim();
                if(type.equals("")) throw new ParserException("Missing type attribute at exception processing mapping");
                String processor = mapElem.getAttribute("processor").trim();
                String page = mapElem.getAttribute("page").trim();
                String forward = mapElem.getAttribute("forward").trim();
                boolean isForward = false;
                if(!forward.equals("")) isForward = Boolean.parseBoolean(forward);
                Class<? extends ExceptionProcessor> clazz = null;
                if(processor.equals("")) {
                    if(isForward) clazz = PageForwardingExceptionProcessor.class;
                    else clazz = UniversalExceptionProcessor.class;
                } else {
                    try {
                        clazz = Class.forName(processor).asSubclass(ExceptionProcessor.class);
                    } catch(ClassNotFoundException x) {
                        throw new ParserException("Can't get exception processor class", x);
                    }
                }
                
                ExceptionConfig config = new ExceptionConfig();
                ExceptionProcessor instance;
                try {
                    instance = clazz.newInstance();
                } catch (Exception x) {
                    throw new ParserException("Can't get exception processor instance", x);
                } 
                config.setProcessor(instance);
                config.setPage(page);
                config.setForward(isForward);
                config.setType(type);
                
                Class<?> exClass = null;
                try {
                    exClass = Class.forName(type);
                } catch(ClassNotFoundException x) {
                    throw new ParserException("Can't get exception class: " + type);
                }
                map.put(exClass.asSubclass(Throwable.class), config);      
            }
        
            beanBuilder.addPropertyValue("exceptionConfigs", map);
        
        }
    }

}
