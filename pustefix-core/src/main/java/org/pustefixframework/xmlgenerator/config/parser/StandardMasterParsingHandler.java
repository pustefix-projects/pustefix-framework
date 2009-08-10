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
package org.pustefixframework.xmlgenerator.config.parser;

import org.pustefixframework.config.generic.ParsingUtils;
import org.pustefixframework.xmlgenerator.config.model.Configuration;
import org.pustefixframework.xmlgenerator.config.model.StandardMaster;
import org.pustefixframework.xmlgenerator.config.model.XMLExtension;
import org.w3c.dom.Element;

import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.ParsingHandler;
import com.marsching.flexiparse.parser.exception.ParserException;

/**
 * 
 * @author mleidig@schlund.de
 *
 */
public class StandardMasterParsingHandler implements ParsingHandler {
	
    public void handleNode(HandlerContext context) throws ParserException {
        
        Element element = (Element)context.getNode();
        ParsingUtils.checkAttributes(element, null, new String[] {"name"});
        
        StandardMaster master = new StandardMaster();
        
        String name = element.getAttribute("name").trim();
        if(name.length()>0) {
        	master.setName(name);
        }
        
        context.getObjectTreeElement().addObject(master);
        Configuration configuration = ParsingUtils.getFirstTopObject(Configuration.class, context, false);
        if(configuration!=null) {
        	configuration.addStandardMaster(master);
        } else {
        	XMLExtension<StandardMaster> ext = XMLExtensionParsingUtils.getListExtension(context);
    		ext.add(master);
        }
    }

}
