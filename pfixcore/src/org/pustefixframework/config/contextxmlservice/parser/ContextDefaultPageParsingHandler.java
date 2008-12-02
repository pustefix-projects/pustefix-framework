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

package org.pustefixframework.config.contextxmlservice.parser;

import org.pustefixframework.config.Constants;
import org.pustefixframework.config.contextxmlservice.parser.internal.ContextConfigImpl;
import org.pustefixframework.config.generic.ParsingUtils;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.ParsingHandler;
import com.marsching.flexiparse.parser.exception.ParserException;


/**
 * 
 * @author mleidig
 *
 */
public class ContextDefaultPageParsingHandler implements ParsingHandler {
    
    public void handleNode(HandlerContext context) throws ParserException {
        
        Element element = (Element)context.getNode();
      
        ContextConfigImpl config = ParsingUtils.getSingleTopObject(ContextConfigImpl.class, context);
        
        NodeList nodes = element.getElementsByTagNameNS(Constants.NS_CONTEXT_XML_SERVICE,"variant");
        for(int i=0; i<nodes.getLength(); i++) {
            Element variantElement = (Element)nodes.item(i);
            String variantName = variantElement.getAttribute("name").trim();
            if(variantName.length()==0) throw new ParserException("Element 'variant' requires 'name' attribute.");
            String pageName = variantElement.getTextContent().trim();
            if(pageName.length()==0) throw new ParserException("Element 'variant' requires text content.");
            config.setDefaultPage(variantName, pageName);
        }
        
        nodes = element.getElementsByTagNameNS(Constants.NS_CONTEXT_XML_SERVICE,"default");
        if(nodes.getLength()==0) throw new ParserException("Element 'defaultpage' requires 'default' child element.");
        if(nodes.getLength()>1) throw new ParserException("Element 'defaultpage' doesn't allow multiple 'default' child elements.");
        Element defaultElement = (Element)nodes.item(0);
        String pageName = defaultElement.getTextContent().trim();
        if(pageName.length()==0) throw new ParserException("Element 'default' requires text content.");
        config.setDefaultPage(pageName);
        
    }
    
}
