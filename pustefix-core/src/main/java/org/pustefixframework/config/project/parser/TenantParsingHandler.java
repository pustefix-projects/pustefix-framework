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

package org.pustefixframework.config.project.parser;

import java.util.List;

import org.pustefixframework.config.Constants;
import org.pustefixframework.config.customization.CustomizationAwareParsingHandler;
import org.pustefixframework.util.xml.DOMUtils;
import org.w3c.dom.Element;

import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.exception.ParserException;

import de.schlund.pfixxml.Tenant;

/**
 * 
 * @author mleidig@schlund.de
 *
 */
public class TenantParsingHandler extends CustomizationAwareParsingHandler {

    public void handleNodeIfActive(HandlerContext context) throws ParserException {

        Element elem = (Element)context.getNode();
        String name = elem.getAttribute("name").trim();
        if(name.length() == 0) throw new ParserException("Attribute '/project/tenant/@name' must not be empty!");
        Tenant tenant = new Tenant(name);
        
        List<Element> langElems = DOMUtils.getChildElementsByTagNameNS(elem, Constants.NS_PROJECT, "lang");
        for(Element langElem: langElems) {
            String lang = langElem.getTextContent().trim();
            if(lang.length() == 0) throw new ParserException("Element '/project/tenant/lang' must not be empty!");
            if(tenant.getSupportedLanguages().contains(lang)) throw new ParserException("Element '/project/tenant/lang'" + 
                    " with content '" + lang + "' was found multiple times.");
            tenant.addSupportedLanguage(lang);
        }
        
        context.getObjectTreeElement().addObject(tenant);
    }

}
