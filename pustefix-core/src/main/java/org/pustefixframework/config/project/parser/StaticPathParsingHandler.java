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

import org.pustefixframework.config.customization.CustomizationAwareParsingHandler;
import org.pustefixframework.config.generic.ParsingUtils;
import org.pustefixframework.config.project.StaticPathInfo;
import org.w3c.dom.Element;

import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.exception.ParserException;

/**
 * Handler reading /p:project-config/p:application/p:static//p:path elements
 * 
 * @author mleidig@schlund.de
 *
 */
public class StaticPathParsingHandler extends CustomizationAwareParsingHandler {
    
    @Override
    protected void handleNodeIfActive(HandlerContext context) throws ParserException {
        
        Element element = (Element)context.getNode();
        String path = element.getTextContent().trim();
        if(path.length() > 0) {
            StaticPathInfo staticPathInfo = ParsingUtils.getSingleTopObject(StaticPathInfo.class, context);
            staticPathInfo.addStaticPath(path);
        }
    }
    
}
