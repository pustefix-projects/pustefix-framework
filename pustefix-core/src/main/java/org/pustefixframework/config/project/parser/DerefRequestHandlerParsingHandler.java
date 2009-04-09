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
            PropertyFileReader.read(ResourceUtil.getFileResourceFromDocroot("WEB-INF/pustefix.xml"), properties);
        } catch (ParserException e) {
            throw new ParserException("Error while reading WEB-INF/pustefix.xml", e);
        } catch (IOException e) {
            throw new ParserException("Error while reading WEB-INF/pustefix.xml", e);
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
