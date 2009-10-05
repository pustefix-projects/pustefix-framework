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
import org.pustefixframework.resource.support.DynamicResourceUtils;
import org.pustefixframework.xmlgenerator.config.model.IncludeConfig;
import org.pustefixframework.xmlgenerator.config.model.IncludeDef;
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
public class IncludeParsingHandler implements ParsingHandler {

    public void handleNode(HandlerContext context) throws ParserException {
        
        Element element = (Element)context.getNode();
        ParsingUtils.checkAttributes(element, new String[] {"stylesheet"}, null);
        
        BundleContext bundleContext = ParsingUtils.getSingleTopObject(BundleContext.class, context);
        String bundleName = bundleContext.getBundle().getSymbolicName();
        
        String stylesheet = element.getAttribute("stylesheet");
        URI uri;
        try {
        	uri = new URI(stylesheet);
        	if(uri.getScheme() == null && stylesheet.contains("/")) {
        		if(stylesheet.startsWith("/")) stylesheet = stylesheet.substring(1);
        		uri = new URI("bundle://" + bundleName + "/PUSTEFIX-INF/" + stylesheet);
        	} else if("dynamic".equals(uri.getScheme())) {
        	    stylesheet = DynamicResourceUtils.setBundleName(uri.toString(), bundleName);
        	    stylesheet = DynamicResourceUtils.setBasePath(stylesheet, "/PUSTEFIX-INF");
        	    uri = new URI(stylesheet);
        	}
        } catch(URISyntaxException x) {
        	throw new ParserException("Illegal URI: "+ stylesheet, x);
        }
        IncludeDef inc = new IncludeDef(uri);
        IncludeConfig config = ParsingUtils.getFirstTopObject(IncludeConfig.class, context, false);
        if(config != null) {
        	 config.addInclude(uri);
        } else {
        	XMLExtension<IncludeDef> ext = XMLExtensionParsingUtils.getListExtension(context);
        	ext.add(inc);
        }
        
    }

}
