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

package org.pustefixframework.config.contextxmlservice.parser;

import org.pustefixframework.config.contextxmlservice.ContextConfig;
import org.pustefixframework.config.contextxmlservice.ContextResourceConfig;
import org.pustefixframework.config.contextxmlservice.JSONOutputResourceHolder;
import org.pustefixframework.config.contextxmlservice.PustefixContextXMLRequestHandlerConfig;
import org.pustefixframework.config.generic.ParsingUtils;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.osgi.context.ConfigurableOsgiBundleApplicationContext;
import org.w3c.dom.Element;

import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.ParsingHandler;
import com.marsching.flexiparse.parser.exception.ParserException;



/**
 * 
 * @author mleidig
 *
 */
public class JSONOutputResourceParsingHandler implements ParsingHandler {

    public void handleNode(HandlerContext context) throws ParserException {
       
        Element element = (Element)context.getNode();
        ParsingUtils.checkAttributes(element, new String[] {"alias"}, new String[] {"class","bean-ref"});
        
        ConfigurableOsgiBundleApplicationContext appContext = ParsingUtils.getSingleTopObject(ConfigurableOsgiBundleApplicationContext.class, context);

        final String alias = element.getAttribute("alias").trim();
       
        String className = element.getAttribute("class").trim();
        String beanRef = element.getAttribute("bean-ref").trim();
        if (className.length() == 0 && beanRef.length() == 0) {
            throw new ParserException("Either attribute 'class' or attribute 'bean-ref' required.");
        }
        if (className.length() > 0) {
            Class<?> clazz;
            try {
                clazz = Class.forName(className, true, appContext.getClassLoader());
            } catch (ClassNotFoundException e) {
                throw new ParserException("Could not load resource interface \"" + className + "\"!");
            }
            PustefixContextXMLRequestHandlerConfig servletConfig = ParsingUtils.getSingleTopObject(PustefixContextXMLRequestHandlerConfig.class, context);
            ContextConfig contextConfig = servletConfig.getContextConfig();
            ContextResourceConfig resourceConfig = contextConfig.getContextResourceConfig(clazz);
            if (resourceConfig == null) {
                throw new ParserException("Could not find suitable context resource for class or interface \"" + className + "\"!");
            }
            beanRef = resourceConfig.getBeanName();
        }

        final RuntimeBeanReference reference = new RuntimeBeanReference(beanRef);
        JSONOutputResourceHolder holder = new JSONOutputResourceHolder() {

            public String getName() {
                return alias;
            }

            public Object getJSONOutputResource() {
                return reference;
            }
            
        };
        context.getObjectTreeElement().addObject(holder);
    }

}
