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

import org.pustefixframework.config.contextxmlservice.parser.internal.PageFlowConfigImpl;
import org.pustefixframework.config.generic.ParsingUtils;
import org.w3c.dom.Element;

import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.ParsingHandler;
import com.marsching.flexiparse.parser.exception.ParserException;

/**
 * Handles the <pageflow> tag within a page flow variant extension. Does
 * not really create a page flow. Instead a dummy object is attached to the
 * object tree, to make the page flow name available to handlers for child
 * elements. 
 */
public class PageFlowDummyParsingHandler implements ParsingHandler {

    public void handleNode(HandlerContext context) throws ParserException {

        Element element = (Element) context.getNode();
        ParsingUtils.checkAttributes(element, new String[] { "name" }, new String[] { "final", "stopnext" });

        String flowName = element.getAttribute("name").trim();
        String finalPage = element.getAttribute("final").trim();
        String stopnext = element.getAttribute("stopnext").trim();

        PageFlowConfigImpl flowConfig = new PageFlowConfigImpl(flowName);
        if (finalPage.length() > 0) {
            flowConfig.setFinalPage(finalPage);
        }

        if (stopnext.length() > 0) {
            flowConfig.setStopNext(Boolean.parseBoolean(stopnext));
        }

        context.getObjectTreeElement().addObject(flowConfig);
    }

}
