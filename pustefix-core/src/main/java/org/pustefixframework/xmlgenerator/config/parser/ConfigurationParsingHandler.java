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
import org.w3c.dom.Element;

import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.ParsingHandler;
import com.marsching.flexiparse.parser.exception.ParserException;

import de.schlund.pfixxml.util.XsltVersion;

/**
 * 
 * @author mleidig@schlund.de
 *
 */
public class ConfigurationParsingHandler implements ParsingHandler {
	
    public void handleNode(HandlerContext context) throws ParserException {
        
    	Element element = (Element)context.getNode();
    	ParsingUtils.checkAttributes(element, new String[] {"project", "lang", "xmlns"}, new String[] {"themes", "xsltversion"});    	
    	
     	Configuration configuration = ParsingUtils.getSingleTopObject(Configuration.class, context);
    	
    	String language = element.getAttribute("lang").trim();
    	configuration.setLanguage(language);
    	
    	String themes = element.getAttribute("themes").trim();
    	if(themes.length()>0) {
    		String[] themeList = themes.split("\\s");
    		if(themeList.length>0) {
    			configuration.setThemes(themeList);
    		}
    	}
    	
    	XsltVersion version = XsltVersion.XSLT1;
    	String xsltVersion = element.getAttribute("xsltversion").trim();
    	if(xsltVersion.length()>0) {
    		version = XsltVersion.valueOf(xsltVersion);
    	}
    	configuration.setXsltVersion(version);
    	
    	context.getObjectTreeElement().addObject(configuration);
        
    }

}
