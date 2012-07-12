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

import org.pustefixframework.config.generic.ParsingUtils;
import org.pustefixframework.config.project.ProjectInfo;
import org.pustefixframework.http.SiteMapRequestHandler;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.w3c.dom.Element;

import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.ParsingHandler;
import com.marsching.flexiparse.parser.exception.ParserException;

import de.schlund.pfixcore.workflow.SiteMap;
import de.schlund.pfixxml.TenantInfo;

public class SiteMapRequestHandlerParsingHandler implements ParsingHandler {
    
    public void handleNode(HandlerContext context) throws ParserException {

    	Element element = (Element)context.getNode();
        ParsingUtils.checkAttributes(element, null, new String[] {"type"});
    	
        String typeAttr = element.getAttribute("type").trim();
        SiteMapRequestHandler.SiteMapType siteMapType = null;
        if(typeAttr.length() > 0) {
        	try {
        		siteMapType = SiteMapRequestHandler.SiteMapType.valueOf(typeAttr.toUpperCase());
        	} catch(IllegalArgumentException x) {
        		throw new ParserException("Searchengine sitemap type '" + typeAttr + "' in 'project.xml' not supported.");
        	}
        }
        
        BeanDefinitionBuilder beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(SiteMapRequestHandler.class);
        beanBuilder.setScope("singleton");
        beanBuilder.addPropertyValue("siteMap", new RuntimeBeanReference(SiteMap.class.getName()));
        beanBuilder.addPropertyValue("tenantInfo", new RuntimeBeanReference(TenantInfo.class.getName()));
        ProjectInfo projectInfo = ParsingUtils.getSingleTopObject(ProjectInfo.class, context);
        beanBuilder.addPropertyValue("projectInfo", projectInfo);
        beanBuilder.addPropertyValue("pustefixContext", new RuntimeBeanReference("pustefixContext"));
        if(siteMapType != null) {
        	beanBuilder.addPropertyValue("siteMapType", siteMapType);
        }
        context.getObjectTreeElement().addObject(new BeanDefinitionHolder(beanBuilder.getBeanDefinition(), SiteMapRequestHandler.class.getName()));

    }

}
