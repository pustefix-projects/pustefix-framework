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
import org.pustefixframework.config.project.Expires;
import org.pustefixframework.config.project.StaticPathInfo;
import org.w3c.dom.Element;

import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.exception.ParserException;

/**
 * Handler reading /p:project-config/p:application/p:static//p:expires elements
 */
public class ExpiresParsingHandler extends CustomizationAwareParsingHandler {

    @Override
    protected void handleNodeIfActive(HandlerContext context) throws ParserException {
        StaticPathInfo staticPathInfo = ParsingUtils.getSingleTopObject(StaticPathInfo.class, context);
        Element element = (Element)context.getNode();
        if(element.getLocalName().equals("static")) {
            String defaultExpires = element.getAttribute("expires").trim();
            if(!defaultExpires.isEmpty()) {
                staticPathInfo.setDefaultExpires(Expires.valueOf(defaultExpires));
            }
        } else {
            String type = element.getAttribute("type").trim();
            String expires = element.getTextContent().trim();
            if(!type.isEmpty() && !expires.isEmpty()) {
                staticPathInfo.addExpires(type, Expires.valueOf(expires));
            }
        }
    }

}
