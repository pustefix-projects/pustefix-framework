/*
 * Place license here
 */

package org.pustefixframework.config.project.parser;

import java.io.IOException;
import java.util.Properties;

import org.pustefixframework.config.contextxmlservice.ServletManagerConfig;
import org.pustefixframework.config.generic.PropertyFileReader;
import org.pustefixframework.http.dereferer.DerefRequestHandler;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;

import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.ParsingHandler;
import com.marsching.flexiparse.parser.exception.ParserException;

import de.schlund.pfixxml.resources.ResourceUtil;
import de.schlund.pfixxml.serverutil.SessionAdmin;

public class DerefRequestHandlerParsingHandler implements ParsingHandler {
    
    public void handleNode(HandlerContext context) throws ParserException {
        final Properties properties = new Properties(System.getProperties());
        try {
            PropertyFileReader.read(ResourceUtil.getFileResourceFromDocroot("common/conf/pustefix.xml"), properties);
        } catch (ParserException e) {
            throw new ParserException("Error while reading common/conf/pustefix.xml", e);
        } catch (IOException e) {
            throw new ParserException("Error while reading common/conf/pustefix.xml", e);
        }
        ServletManagerConfig config = new ServletManagerConfig() {

            public Properties getProperties() {
                return properties;
            }

            public boolean isSSL() {
                return false;
            }

            public boolean needsReload() {
                return false;
            }
            
        };
        BeanDefinitionBuilder beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(DerefRequestHandler.class);
        beanBuilder.setScope("singleton");
        beanBuilder.setInitMethodName("init");
        beanBuilder.addPropertyValue("handlerURI", "/xml/deref/**");
        beanBuilder.addPropertyValue("configuration", config);
        beanBuilder.addPropertyValue("sessionAdmin", new RuntimeBeanReference(SessionAdmin.class.getName()));
        BeanDefinition beanDefinition = beanBuilder.getBeanDefinition();
        BeanDefinitionHolder beanHolder = new BeanDefinitionHolder(beanDefinition, DerefRequestHandler.class.getName());
        context.getObjectTreeElement().addObject(beanHolder);
    }

}
