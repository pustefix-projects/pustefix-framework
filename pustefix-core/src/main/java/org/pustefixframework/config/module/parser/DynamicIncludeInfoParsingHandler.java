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

package org.pustefixframework.config.module.parser;

import org.osgi.framework.BundleContext;
import org.pustefixframework.config.generic.ParsingUtils;
import org.pustefixframework.resource.internal.DynamicIncludeInfo;
import org.pustefixframework.resource.internal.DynamicIncludeInfoImpl;
import org.springframework.osgi.context.ConfigurableOsgiBundleApplicationContext;
import org.w3c.dom.Element;

import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.ParsingHandler;
import com.marsching.flexiparse.parser.exception.ParserException;


/**
 * 
 * @author mleidig@schlund.de
 *
 */
public class DynamicIncludeInfoParsingHandler implements ParsingHandler {

    public void handleNode(HandlerContext context) throws ParserException {
    	
    	Element element = (Element)context.getNode();
    	
    	ConfigurableOsgiBundleApplicationContext appContext = ParsingUtils.getSingleTopObject(ConfigurableOsgiBundleApplicationContext.class, context);
        BundleContext bundleContext = appContext.getBundleContext();
    	
        String moduleName = bundleContext.getBundle().getSymbolicName();
        DynamicIncludeInfoImpl dynInfo = DynamicIncludeInfoImpl.create(moduleName, element);
		bundleContext.registerService(DynamicIncludeInfo.class.getName(), dynInfo, null);
		
    }

}
