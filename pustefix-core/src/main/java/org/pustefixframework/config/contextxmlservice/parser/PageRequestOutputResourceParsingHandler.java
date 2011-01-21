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
import org.pustefixframework.config.contextxmlservice.ContextXMLServletConfig;
import org.pustefixframework.config.contextxmlservice.parser.internal.StateConfigImpl;
import org.pustefixframework.config.generic.ParsingUtils;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.w3c.dom.Element;

import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.ParsingHandler;
import com.marsching.flexiparse.parser.exception.ParserException;



/**
 * 
 * @author mleidig
 *
 */
public class PageRequestOutputResourceParsingHandler implements ParsingHandler {

    public void handleNode(HandlerContext context) throws ParserException {
       
        Element element = (Element)context.getNode();
        ParsingUtils.checkAttributes(element, new String[] {"node"}, new String[] {"class","bean-ref"});
        
        StateConfigImpl stateConfig = ParsingUtils.getFirstTopObject(StateConfigImpl.class, context, true);
        String node = element.getAttribute("node").trim();
       
        String className = element.getAttribute("class").trim();
        String beanRef = element.getAttribute("bean-ref").trim();
        if (className.length() == 0 && beanRef.length() == 0) {
            throw new ParserException("Either attribute 'class' or attribute 'bean-ref' required.");
        } else if (className.length() > 0 && beanRef.length() > 0) {
            throw new ParserException("Only one of the attributes 'class' or 'bean-ref' is allowed at a 'resource' element: " +
                    "class='" + className + "' bean-ref='" + beanRef +"'");
        } else if (className.length() > 0) {
            Class<?> clazz;
            try {
                clazz = Class.forName(className);
            } catch (ClassNotFoundException e) {
                throw new ParserException("Could not load resource interface \"" + className + "\"!");
            }
            ContextXMLServletConfig servletConfig = ParsingUtils.getSingleTopObject(ContextXMLServletConfig.class, context);
            ContextConfig contextConfig = servletConfig.getContextConfig();
            ContextResourceConfig resourceConfig = contextConfig.getContextResourceConfig(clazz);
            if (resourceConfig == null) {
                throw new ParserException("Could not find suitable context resource for class or interface \"" + className + "\"!");
            }
            stateConfig.addContextResource(node, new RuntimeBeanReference(resourceConfig.getBeanName()));
        } else if (beanRef.length() > 0) {
            stateConfig.addContextResource(node, new RuntimeBeanReference(beanRef));
        }
        
    }

}
