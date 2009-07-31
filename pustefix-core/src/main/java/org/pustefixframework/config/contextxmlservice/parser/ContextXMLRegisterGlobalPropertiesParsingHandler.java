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

import java.util.Collection;
import java.util.Enumeration;
import java.util.Properties;

import org.pustefixframework.config.contextxmlservice.parser.internal.ContextConfigImpl;
import org.pustefixframework.config.contextxmlservice.parser.internal.PustefixContextXMLRequestHandlerConfigImpl;
import org.pustefixframework.config.customization.CustomizationAwareParsingHandler;
import org.pustefixframework.config.generic.ParsingUtils;

import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.exception.ParserException;


public class ContextXMLRegisterGlobalPropertiesParsingHandler extends CustomizationAwareParsingHandler {

    @Override
    protected void handleNodeIfActive(HandlerContext context) throws ParserException {
        PustefixContextXMLRequestHandlerConfigImpl servletConfig = ParsingUtils.getFirstTopObject(PustefixContextXMLRequestHandlerConfigImpl.class, context, true);
        ContextConfigImpl contextConfig = servletConfig.getContextConfig();
        
        Properties servletProperties = servletConfig.getProperties();
        Properties contextProperties = contextConfig.getProperties();
        
        Collection<Properties> propertiesCollection = context.getObjectTreeElement().getObjectsOfType(Properties.class);
        for (Properties p : propertiesCollection) {
            Enumeration<?> en = p.propertyNames();
            while (en.hasMoreElements()) {
                String propName = (String) en.nextElement();
                String propValue = p.getProperty(propName);
                servletProperties.setProperty(propName, propValue);
                contextProperties.setProperty(propName, propValue);
            }
        }
        servletConfig.setProperties(servletProperties);
        
        //Add global properties to context properties for backwards compatibility
        Enumeration<?> en = servletProperties.propertyNames();
        while (en.hasMoreElements()) {
            String propName = (String) en.nextElement();
            if(!contextProperties.containsKey(propName)) {
                String propValue = servletProperties.getProperty(propName);
                contextProperties.setProperty(propName, propValue);
            }
        }
        contextConfig.setProperties(contextProperties);
    }

}
