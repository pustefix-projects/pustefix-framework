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
package org.pustefixframework.config.project.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.pustefixframework.config.Constants;
import org.pustefixframework.config.customization.CustomizationAwareParsingHandler;
import org.pustefixframework.config.generic.ParsingUtils;
import org.pustefixframework.util.i18n.POMessageSource;
import org.pustefixframework.util.xml.DOMUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultBeanNameGenerator;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.w3c.dom.Element;

import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.exception.ParserException;

import de.schlund.pfixxml.config.EnvironmentProperties;

/**
 * Creates BeanDefinitions for MessageSources defined in the project configuration. 
 */
public class MessageSourcesParsingHandler extends CustomizationAwareParsingHandler {

    public void handleNodeIfActive(HandlerContext context) throws ParserException {

        BeanDefinitionRegistry beanRegistry = ParsingUtils.getSingleTopObject(BeanDefinitionRegistry.class, context);
        DefaultBeanNameGenerator nameGenerator = new DefaultBeanNameGenerator();
        String lastMsgSrcName = null;
        List<Element> msgSrcElems = DOMUtils.getChildElementsByTagNameNS((Element)context.getNode(), Constants.NS_PROJECT, "messagesource");
        
        for(ListIterator<Element> it = msgSrcElems.listIterator(msgSrcElems.size()); it.hasPrevious();) {
            Element msgSrcElem = it.previous();
            String type = msgSrcElem.getAttribute("type").trim();
            Class<? extends MessageSource> msgSrcClass;
            if(type.equals("po")) {
                msgSrcClass = POMessageSource.class;
            } else if(type.isEmpty() || type.equals("properties")) {
                msgSrcClass = ReloadableResourceBundleMessageSource.class;
            } else {
                throw new ParserException("Illegal messagesource type: " + type);
            }
            List<String> baseNames = new ArrayList<>();
            String baseName = msgSrcElem.getAttribute("basename").trim();
            if(!baseName.isEmpty()) {
                baseNames.add(baseName);
            }
            List<Element> baseNameElems = DOMUtils.getChildElementsByTagNameNS(msgSrcElem, Constants.NS_PROJECT, "basename");
            for(Element baseNameElem: baseNameElems) {
                baseName = baseNameElem.getTextContent().trim();
                if(!baseName.isEmpty()) {
                    baseNames.add(baseName);
                }
            }
            
            long cacheSeconds = "prod".equals(EnvironmentProperties.getProperties().getProperty("mode")) ? -1 : 10;
            
            BeanDefinitionBuilder beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(msgSrcClass);
            beanBuilder.setScope("singleton");
            if(!baseNames.isEmpty()) {
                beanBuilder.addPropertyValue("basenames", baseNames.toArray());
            }
            beanBuilder.addPropertyValue("cacheSeconds", cacheSeconds);
            if(lastMsgSrcName != null) {
                beanBuilder.addPropertyReference("parentMessageSource", lastMsgSrcName);
            }

            BeanDefinition beanDef = beanBuilder.getBeanDefinition();
            String beanName;
            if(!it.hasPrevious()) {
                beanName = "messageSource";
            } else {
                beanName = nameGenerator.generateBeanName(beanDef, beanRegistry);
            }
            beanRegistry.registerBeanDefinition(beanName, beanBuilder.getBeanDefinition());
            lastMsgSrcName = beanName;
        }
    }

}

