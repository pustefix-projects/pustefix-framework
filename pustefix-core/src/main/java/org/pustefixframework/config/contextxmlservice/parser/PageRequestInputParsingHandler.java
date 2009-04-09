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

import org.pustefixframework.config.contextxmlservice.StateConfig.Policy;
import org.pustefixframework.config.contextxmlservice.parser.internal.StateConfigImpl;
import org.pustefixframework.config.generic.ParsingUtils;
import org.w3c.dom.Element;

import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.ParsingHandler;
import com.marsching.flexiparse.parser.exception.ParserException;

/**
 * 
 * @author mleidig@schlund.de
 *
 */
public class PageRequestInputParsingHandler implements ParsingHandler {

    public void handleNode(HandlerContext context) throws ParserException {
       
        Element element = (Element)context.getNode();
        ParsingUtils.checkAttributes(element, null, new String[] {"requirestoken", "policy"});
         
        StateConfigImpl stateConfig = ParsingUtils.getFirstTopObject(StateConfigImpl.class, context, true);
        
        String value = element.getAttribute("requirestoken").trim();
        if(value.length()>0) {
            if(value.equalsIgnoreCase("true")) {
                stateConfig.setRequiresToken(true);
            } else if(value.equalsIgnoreCase("false")) {
                stateConfig.setRequiresToken(false);
            } else {
                throw new ParserException("Illegal 'requirestoken' attribute value: "+value);
            }
        }
        
        value = element.getAttribute("policy").trim();
        if(value.length()>0) {
            if(value.equals("ALL")) {
                stateConfig.setIWrapperPolicy(Policy.ALL);
            } else if(value.equals("ANY")) {
                stateConfig.setIWrapperPolicy(Policy.ANY);
            } else if(value.equals("NONE")) {
                stateConfig.setIWrapperPolicy(Policy.NONE);
            } else {
                throw new ParserException("Illegal 'policy' attribute value: "+value);
            }
        }
        
    }

}
