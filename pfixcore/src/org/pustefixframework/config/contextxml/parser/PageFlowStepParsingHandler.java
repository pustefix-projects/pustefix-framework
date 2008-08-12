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

import de.schlund.pfixxml.config.impl.PageFlowConfigImpl;
import de.schlund.pfixxml.config.impl.PageFlowStepConfigImpl;

/**
 * 
 * @author mleidig
 *
 */
public class PageFlowStepParsingHandler implements ParsingHandler {

    public void handleNode(HandlerContext context) throws ParserException {
       
        Element element = (Element)context.getNode();
        ParsingUtils.checkAttributes(element, new String[] {"name"}, new String[] {"stophere"});
        
        PageFlowConfigImpl flowConfig = ParsingUtils.getFirstTopObject(PageFlowConfigImpl.class, context, true);
        
        String pageName = element.getAttribute("name").trim();
        PageFlowStepConfigImpl stepConfig = new PageFlowStepConfigImpl();
        stepConfig.setPage(pageName);
        String stophere = element.getAttribute("stophere").trim();
        if (stophere.length()>0) {
            stepConfig.setStopHere(Boolean.parseBoolean(stophere));
        } else {
            stepConfig.setStopHere(flowConfig.isStopNext());
        }
        flowConfig.addFlowStep(stepConfig);
        context.getObjectTreeElement().addObject(stepConfig);
       
    }

}
