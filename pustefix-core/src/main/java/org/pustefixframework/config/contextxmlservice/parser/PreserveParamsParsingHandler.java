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

import java.util.List;

import org.pustefixframework.config.contextxmlservice.PreserveParams;
import org.pustefixframework.config.contextxmlservice.parser.internal.ContextXMLServletConfigImpl;
import org.pustefixframework.config.generic.ParsingUtils;
import org.pustefixframework.util.xml.DOMUtils;
import org.w3c.dom.Element;

import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.ParsingHandler;
import com.marsching.flexiparse.parser.exception.ParserException;


public class PreserveParamsParsingHandler implements ParsingHandler {

    public void handleNode(HandlerContext context) throws ParserException {
        
        ContextXMLServletConfigImpl config = ParsingUtils.getSingleTopObject(ContextXMLServletConfigImpl.class, context);
        PreserveParams preserveParams = config.getContextConfig().getPreserveParams();
        Element element = (Element)context.getNode();
        List<Element> elems = DOMUtils.getChildElementsByLocalName(element, "param");
        for(Element elem: elems) {
            String name = elem.getAttribute("name").trim();
            if(name.isEmpty()) {
                throw new ParserException("Error while parsing preserve parameters: " +
                        "missing 'name' attribute value at element 'param'");
            }
            preserveParams.addParam(name);
        }
        
    }

}
