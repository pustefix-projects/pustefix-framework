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

package org.pustefixframework.config.contextxml.parser;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Properties;

import org.pustefixframework.config.contextxml.parser.internal.PageRequestConfigImpl;
import org.pustefixframework.config.contextxml.parser.internal.StateConfigImpl;
import org.pustefixframework.config.customization.CustomizationAwareParsingHandler;
import org.pustefixframework.config.generic.ParsingUtils;

import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.exception.ParserException;

public class PageRequestRegisterPropertiesParsingHandler extends CustomizationAwareParsingHandler {

    @Override
    protected void handleNodeIfActive(HandlerContext context) throws ParserException {
        PageRequestConfigImpl pageConfig = ParsingUtils.getFirstTopObject(PageRequestConfigImpl.class, context, true);
        StateConfigImpl stateConfig = ParsingUtils.getFirstTopObject(StateConfigImpl.class, context, false);
        
        Properties pageProperties = pageConfig.getProperties();
        Properties stateProperties = null;
        if (stateConfig != null) {
            stateProperties = stateConfig.getProperties();
        }

        Collection<Properties> propertiesCollection = context.getObjectTreeElement().getObjectsOfType(Properties.class);
        for (Properties p : propertiesCollection) {
            Enumeration<?> en = p.propertyNames();
            while (en.hasMoreElements()) {
                String propName = (String) en.nextElement();
                String propValue = p.getProperty(propName);
                pageProperties.setProperty(propName, propValue);
                if (stateProperties != null) {
                    stateProperties.setProperty(propName, propValue);
                }
            }
        }
        pageConfig.setProperties(pageProperties);
        if (stateConfig != null) {
            stateConfig.setProperties(stateProperties);
        }
    }

}
