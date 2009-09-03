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

import java.util.List;

import org.osgi.framework.BundleContext;
import org.pustefixframework.config.Constants;
import org.pustefixframework.config.generic.ParsingUtils;
import org.pustefixframework.resource.ResourceLoader;
import org.pustefixframework.xmlgenerator.config.model.Configuration;
import org.pustefixframework.xmlgenerator.config.model.SourceInfo;
import org.pustefixframework.xmlgenerator.config.model.TargetDef;
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
public class TargetDefParsingHandler implements ParsingHandler {
	
    public void handleNode(HandlerContext context) throws ParserException {
        
        Element element = (Element)context.getNode();
        ParsingUtils.checkAttributes(element, new String[] {"name","type"}, new String[] {"themes", "page", "variant"});
        
        TargetDef target = new TargetDef();
        
        BundleContext bundleContext = ParsingUtils.getSingleTopObject(BundleContext.class, context);
        String bundleName = bundleContext.getBundle().getSymbolicName();
        ResourceLoader resourceLoader = ParsingUtils.getSingleTopObject(ResourceLoader.class, context);
        
        SourceInfo sourceInfo = new SourceInfo(bundleName, resourceLoader);
        target.setSourceInfo(sourceInfo);
        
        String name = element.getAttribute("name").trim();
        target.setName(name);
        
        String type = ParsingUtils.getAttribute(element, "type", new String[] {"xml", "xsl"}, null);
        target.setType(TargetDef.Type.valueOf(type.toUpperCase()));
        
        String themes = element.getAttribute("themes").trim();
    	if(themes.length()>0) {
    		String[] themeList = themes.split("\\s");
    		target.setThemes(themeList);
    	}
        
    	String variant = element.getAttribute("variant").trim();
        if(variant.length()>0) {
        	target.setVariant(variant);
        }
        
        String page = element.getAttribute("page").trim();
        if(page.length()>0) {
        	target.setPage(page);
        }
        
        Element elem = ParsingUtils.getSingleChildElement(element, Constants.NS_XML_GENERATOR, "depxml", true);
        String nameVal = elem.getAttribute("name").trim();
        if(nameVal.length()>0) {
        	target.setXML(nameVal);
        } else {
        	throw new ParserException("Missing 'name' attribute at element 'depxml'.");
        }
        
        elem = ParsingUtils.getSingleChildElement(element, Constants.NS_XML_GENERATOR, "depxsl", true);
        nameVal = elem.getAttribute("name").trim();
        if(nameVal.length()>0) {
        	target.setXML(nameVal);
        } else {
        	throw new ParserException("Missing 'name' attribute at element 'depxsl'.");
        }
        
        List<Element> elems = ParsingUtils.getChildElements(element, Constants.NS_XML_GENERATOR, "depaux");
        for(Element subElem: elems) {
        	nameVal = subElem.getAttribute("name").trim();
            if(nameVal.length()>0) {
            	target.addAuxiliaryDependency(nameVal);
            } else {
            	throw new ParserException("Missing 'name' attribute at element 'depxsl'.");
            }
        }
        
        context.getObjectTreeElement().addObject(target);
        Configuration configuration = ParsingUtils.getFirstTopObject(Configuration.class, context, false);
        if(configuration!=null) {
        	configuration.addTargetDef(target);
        } else {
        	XMLExtension<TargetDef> ext = XMLExtensionParsingUtils.getListExtension(context);
    		ext.add(target);
        }
    }

}
