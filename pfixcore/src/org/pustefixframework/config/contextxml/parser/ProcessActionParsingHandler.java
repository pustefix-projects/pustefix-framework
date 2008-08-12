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

import de.schlund.pfixxml.config.impl.PageRequestConfigImpl;
import de.schlund.pfixxml.config.impl.ProcessActionConfigImpl;

/**
 * 
 * @author mleidig
 *
 */
public class ProcessActionParsingHandler implements ParsingHandler {

    public void handleNode(HandlerContext context) throws ParserException {
       
        Element element = (Element)context.getNode();
        ParsingUtils.checkAttributes(element, new String[] {"name"}, new String[] {"pageflow", "jumptopage", "jumptopageflow", "forcestop"});
         
        PageRequestConfigImpl pageConfig = ParsingUtils.getFirstTopObject(PageRequestConfigImpl.class, context, true);
        ProcessActionConfigImpl actionConfig = new ProcessActionConfigImpl();
        
        String actionname = element.getAttribute("name").trim();
        actionConfig.setName(actionname);
        
        String pageflow = element.getAttribute("pageflow").trim();
        if (pageflow.length()>0) actionConfig.setPageflow(pageflow);
        
        String forcestop = element.getAttribute("forcestop").trim();
        if (forcestop.length()>0) {
            if (forcestop.equals("true") || forcestop.equals("false") || forcestop.equals("step")) {
                actionConfig.setForceStop(forcestop);
            } else  {
                throw new ParserException("Value \"" + forcestop +  "\" is no valid value for 'forcestop'!");
            }
        } else {
            actionConfig.setForceStop("false");
        }
        
        String jumptopage = element.getAttribute("jumptopage").trim();
        if (jumptopage.length()>0) actionConfig.setJumpToPage(jumptopage);
        
        String jumptopageflow = element.getAttribute("jumptopageflow").trim();
        if (jumptopageflow.length()>0) actionConfig.setJumpToPageflow(jumptopageflow);
        
        pageConfig.addProcessAction(actionname, actionConfig);
        
        context.getObjectTreeElement().addObject(actionConfig);
        
    }

}
