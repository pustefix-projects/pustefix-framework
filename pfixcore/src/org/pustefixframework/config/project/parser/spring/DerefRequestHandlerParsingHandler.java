/*
 * Place license here
 */

package org.pustefixframework.config.project.parser.spring;

import org.pustefixframework.http.dereferer.DerefRequestHandler;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;

import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.ParsingHandler;
import com.marsching.flexiparse.parser.exception.ParserException;

public class DerefRequestHandlerParsingHandler implements ParsingHandler {
    
    public void handleNode(HandlerContext context) throws ParserException {
        BeanDefinitionBuilder beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(DerefRequestHandler.class);
        beanBuilder.setScope("singleton");
        beanBuilder.addPropertyValue("handlerURI", "/xml/deref/**");
        beanBuilder.addPropertyValue("commonPropFile", "pfixroot:/common/conf/pustefix.xml");
        BeanDefinition beanDefinition = beanBuilder.getBeanDefinition();
        BeanDefinitionHolder beanHolder = new BeanDefinitionHolder(beanDefinition, DerefRequestHandler.class.getName());
        context.getObjectTreeElement().addObject(beanHolder);    }

}
