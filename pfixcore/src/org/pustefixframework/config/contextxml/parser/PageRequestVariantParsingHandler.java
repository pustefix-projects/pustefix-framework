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

import org.pustefixframework.config.generic.ParsingUtils;
import org.w3c.dom.Element;

import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.ParsingHandler;
import com.marsching.flexiparse.parser.exception.ParserException;

import de.schlund.pfixxml.config.impl.ContextConfigImpl;
import de.schlund.pfixxml.config.impl.ContextXMLServletConfigImpl;
import de.schlund.pfixxml.config.impl.PageRequestConfigImpl;

/**
 * 
 * @author mleidig
 *
 */
public class PageRequestVariantParsingHandler implements ParsingHandler {

    public void handleNode(HandlerContext context) throws ParserException {
       
        Element element = (Element)context.getNode();
        ParsingUtils.checkAttributes(element, new String[] {"name"}, null);
        
        ContextConfigImpl ctxConfig = ParsingUtils.getSingleSubObjectFromRoot(ContextConfigImpl.class, context);
        ContextXMLServletConfigImpl config = ParsingUtils.getSingleTopObject(ContextXMLServletConfigImpl.class, context);     
        
        PageRequestConfigImpl defaultConfig = ParsingUtils.getSingleTopObject(PageRequestConfigImpl.class, context);
        PageRequestConfigImpl pageConfig = new PageRequestConfigImpl();
        String variantName = element.getAttribute("name").trim();
        
        pageConfig.setPageName(defaultConfig.getPageName() + "::" + variantName);
        if (defaultConfig.getCopyFromPage() != null) {
            throw new ParserException("Page using \"copyfrom\" cannot define its own variants!");
        }
        pageConfig.setDefaultStaticState(config.getDefaultStaticState());
        pageConfig.setDefaultIHandlerState(config.getDefaultIHandlerState());
        ctxConfig.addPageRequest(pageConfig);
        context.getObjectTreeElement().addObject(pageConfig);
        
    }

}
