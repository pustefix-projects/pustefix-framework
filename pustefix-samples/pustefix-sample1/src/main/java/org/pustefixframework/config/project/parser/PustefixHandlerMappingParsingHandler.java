/*
 * Place license here
 */

package org.pustefixframework.config.project.parser;

import org.pustefixframework.container.spring.http.PustefixHandlerMapping;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;

import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.ParsingHandler;
import com.marsching.flexiparse.parser.exception.ParserException;

public class PustefixHandlerMappingParsingHandler implements ParsingHandler {

    public void handleNode(HandlerContext context) throws ParserException {
        BeanDefinitionBuilder beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(PustefixHandlerMapping.class);
        beanBuilder.setScope("singleton");
        BeanDefinitionHolder beanHolder = new BeanDefinitionHolder(beanBuilder.getBeanDefinition(), PustefixHandlerMapping.class.getName());
        context.getObjectTreeElement().addObject(beanHolder);
    }

}
