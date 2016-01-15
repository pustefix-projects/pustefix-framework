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
package org.pustefixframework.config.directoutputservice.parser;

import org.pustefixframework.config.directoutputservice.parser.internal.DirectOutputServiceConfigImpl;
import org.pustefixframework.config.generic.ParsingUtils;
import org.w3c.dom.Element;

import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.ParsingHandler;
import com.marsching.flexiparse.parser.exception.ParserException;


public class DirectOutputAuthConstraintParsingHandler implements ParsingHandler {

    public void handleNode(HandlerContext context) throws ParserException {

        Element element = (Element)context.getNode();
        ParsingUtils.checkAttributes(element, new String[] {"ref"}, null);
        
        String constraintRef = element.getAttribute("ref").trim();
        if (!constraintRef.equals("")) {
            DirectOutputServiceConfigImpl config = ParsingUtils.getSingleTopObject(DirectOutputServiceConfigImpl.class, context);
            config.setAuthConstraintRef(constraintRef);
        }
    }

}
