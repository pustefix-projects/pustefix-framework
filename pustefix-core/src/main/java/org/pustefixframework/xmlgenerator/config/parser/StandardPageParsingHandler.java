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

import java.net.URI;
import java.net.URISyntaxException;

import org.osgi.framework.BundleContext;
import org.pustefixframework.config.generic.ParsingUtils;
import org.pustefixframework.resource.ResourceLoader;
import org.pustefixframework.xmlgenerator.config.model.Configuration;
import org.pustefixframework.xmlgenerator.config.model.SourceInfo;
import org.pustefixframework.xmlgenerator.config.model.StandardPage;
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
public class StandardPageParsingHandler implements ParsingHandler {
	
    public void handleNode(HandlerContext context) throws ParserException {
    	
        Element element = (Element)context.getNode();
        ParsingUtils.checkAttributes(element, new String[] {"name","xml"}, new String[] {"themes", "variant", "master", "metatags"});
        
        StandardPage page = new StandardPage();
        
        BundleContext bundleContext = ParsingUtils.getSingleTopObject(BundleContext.class, context);
        String bundleName = bundleContext.getBundle().getSymbolicName();
        ResourceLoader resourceLoader = ParsingUtils.getSingleTopObject(ResourceLoader.class, context);
        
        SourceInfo sourceInfo = new SourceInfo(bundleName, resourceLoader);
        page.setSourceInfo(sourceInfo);
        
        String name = element.getAttribute("name").trim();
        page.setName(name);
        
        String xml = element.getAttribute("xml").trim();
        
        URI uri;
        try {
        	uri = new URI(xml);
        	if(uri.getScheme()==null && xml.contains("/")) {
        		if(xml.startsWith("/")) xml=xml.substring(1);
        		uri = new URI("bundle://"+bundleName+"/PUSTEFIX-INF/"+xml);
        	}
        } catch(URISyntaxException x) {
        	throw new ParserException("Illegal URI: "+xml, x);
        }
        page.setXML(uri.toString());
        
        String themes = element.getAttribute("themes").trim();
    	if(themes.length()>0) {
    		String[] themeList = themes.split("\\s");
    		if(themeList.length>0) {
    			page.setThemes(themeList);
    		}
    	}
        
    	String variant = element.getAttribute("variant").trim();
        if(variant.length()>0) {
        	page.setVariant(variant);
        }
        
        String master = element.getAttribute("master").trim();
        if(master.length()>0) {
        	page.setMaster(master);
        }
        
        String metatags = element.getAttribute("metatags").trim();
        if(metatags.length()>0) {
        	page.setMetatags(metatags);
        }
        
        context.getObjectTreeElement().addObject(page);
        Configuration configuration = ParsingUtils.getFirstTopObject(Configuration.class, context, false);
        if(configuration != null) {
        	configuration.getStandardPages().add(page);
        } else {
        	XMLExtension<StandardPage> ext = XMLExtensionParsingUtils.getListExtension(context);
    		ext.add(page);
        }
    }

}
