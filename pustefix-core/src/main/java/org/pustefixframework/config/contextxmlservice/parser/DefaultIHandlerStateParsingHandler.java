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

import org.pustefixframework.config.contextxmlservice.parser.internal.PustefixContextXMLRequestHandlerConfigImpl;
import org.pustefixframework.config.generic.ParsingUtils;
import org.springframework.osgi.context.ConfigurableOsgiBundleApplicationContext;
import org.w3c.dom.Element;

import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.ParsingHandler;
import com.marsching.flexiparse.parser.exception.ParserException;

import de.schlund.pfixcore.workflow.ConfigurableState;

/**
 * 
 * @author mleidig
 *
 */
public class DefaultIHandlerStateParsingHandler implements ParsingHandler {

    public void handleNode(HandlerContext context) throws ParserException {
        
        Element element = (Element)context.getNode();
        ParsingUtils.checkAttributes(element, new String[] {"class"}, new String[] {"parent-bean-ref"});

        ConfigurableOsgiBundleApplicationContext appContext = ParsingUtils.getSingleTopObject(ConfigurableOsgiBundleApplicationContext.class, context);

        PustefixContextXMLRequestHandlerConfigImpl config = ParsingUtils.getSingleTopObject(PustefixContextXMLRequestHandlerConfigImpl.class, context);     
   
        String className = element.getAttribute("class");
        Class<?> clazz;
        try {
            clazz = Class.forName(className, true, appContext.getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new ParserException("Could not load class \"" + className + "\"!", e);
        }
        if (!ConfigurableState.class.isAssignableFrom(clazz)) {
            throw new ParserException("Default IHandler state class " + clazz + " does not implement " + ConfigurableState.class + " interface!");
        }
        config.setDefaultIHandlerState(clazz.asSubclass(ConfigurableState.class));
        
        String parentRef = element.getAttribute("parent-bean-ref").trim();
        if(parentRef.length() > 0) {
            config.setDefaultIHandlerStateParentBeanName(parentRef);
        }
        
    }

}
