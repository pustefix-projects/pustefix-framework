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
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.exception.ParserException;

import de.schlund.pfixcore.util.ModuleInfo;

/**
 * 
 * @author mleidig@schlund.de
 *
 */
public class DynamicIncludesParsingHandler extends CustomizationAwareParsingHandler {

    @Override
    public void handleNodeIfActive(HandlerContext context) throws ParserException {
    
            Element element = (Element)context.getNode();
            
            NodeList nodeList = element.getElementsByTagName("default-search");
            if(nodeList.getLength() > 0) {
                if(nodeList.getLength() > 1) throw new ParserException("Found multiple 'default-search' elements.");
                nodeList = element.getElementsByTagName("module");
                ModuleInfo moduleInfo = ModuleInfo.getInstance();
                for(int i=0; i<nodeList.getLength(); i++) {
                    Element moduleElem = (Element)nodeList.item(i);
                    String moduleName = moduleElem.getTextContent().trim();
                    if(moduleName.equals("")) throw new ParserException("Element 'module' must not be empty.");
                    moduleInfo.addDefaultSearchModule(moduleName);
                }
            }
        
    }

}
