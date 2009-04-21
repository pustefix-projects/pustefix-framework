/*
 * Place license here
 */

package org.pustefixframework.config.project.parser;

import org.pustefixframework.config.customization.CustomizationAwareParsingHandler;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.w3c.dom.Element;

import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.exception.ParserException;

import de.schlund.pfixxml.SessionCleaner;

/**
 * 
 * @author mleidig@schlund.de
 *
 */
public class SessionCleanerParsingHandler extends CustomizationAwareParsingHandler {

    private BeanDefinitionBuilder beanBuilder;
    
    @Override
    public void handleNodeIfActive(HandlerContext context) throws ParserException {
        Element element = (Element)context.getNode();
        if(element.getLocalName().equals("context-xml-service")) {
            beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(SessionCleaner.class);
            beanBuilder.setScope("singleton");
            BeanDefinitionHolder beanHolder = new BeanDefinitionHolder(beanBuilder.getBeanDefinition(), SessionCleaner.class.getName());
            context.getObjectTreeElement().addObject(beanHolder);
        } else if(element.getLocalName().equals("session-cleaner-timeout")) {
            String value = element.getTextContent().trim();
            beanBuilder.addPropertyValue("timeout", Integer.parseInt(value));
        }
    }

}
