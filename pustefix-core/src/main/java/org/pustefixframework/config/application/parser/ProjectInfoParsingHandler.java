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

package org.pustefixframework.config.application.parser;

import org.pustefixframework.config.Constants;
import org.pustefixframework.config.application.ProjectInfo;
import org.pustefixframework.config.customization.CustomizationAwareParsingHandler;
import org.pustefixframework.config.generic.ParsingUtils;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.exception.ParserException;

/**
 * 
 * @author mleidig@schlund.de
 *
 */
public class ProjectInfoParsingHandler extends CustomizationAwareParsingHandler {

    public void handleNodeIfActive(HandlerContext context) throws ParserException {
       
        ProjectInfo projectInfo = ParsingUtils.getSingleTopObject(ProjectInfo.class, context);

        Element projectElem = (Element)context.getNode();
        NodeList nameElems = projectElem.getElementsByTagNameNS(Constants.NS_APPLICATION, "name");
        if(nameElems.getLength() == 1) {
            Element nameElem = (Element)nameElems.item(0);
            String name = nameElem.getTextContent().trim();
            if(!name.equals("")) projectInfo.setProjectName(name);
        }
      
    }

}
