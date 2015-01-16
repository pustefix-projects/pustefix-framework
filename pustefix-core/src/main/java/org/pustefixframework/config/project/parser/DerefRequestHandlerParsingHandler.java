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

import org.pustefixframework.config.Constants;
import org.pustefixframework.config.contextxmlservice.ServletManagerConfig;
import org.pustefixframework.config.derefservice.internal.DerefServiceConfig;
import org.pustefixframework.config.generic.ParsingUtils;
import org.pustefixframework.config.project.SessionTrackingStrategyInfo;
import org.pustefixframework.http.dereferer.DerefRequestHandler;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.w3c.dom.Element;

import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.ParsingHandler;
import com.marsching.flexiparse.parser.exception.ParserException;

import de.schlund.pfixcore.workflow.PageMap;
import de.schlund.pfixcore.workflow.SiteMap;
import de.schlund.pfixxml.LanguageInfo;
import de.schlund.pfixxml.TenantInfo;
import de.schlund.pfixxml.serverutil.SessionAdmin;

public class DerefRequestHandlerParsingHandler implements ParsingHandler {
    
    private BeanDefinitionBuilder beanBuilder;
    
    public void handleNode(HandlerContext context) throws ParserException {
        
        Element root = (Element) context.getNode();
        
        if(root.getLocalName().equals("application")) {
            
            ServletManagerConfig config = new DerefServiceConfig();
            context.getObjectTreeElement().addObject(config);

            SessionTrackingStrategyInfo strategyInfo = ParsingUtils.getSingleSubObjectFromRoot(SessionTrackingStrategyInfo.class, context);
            
            beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(DerefRequestHandler.class);
            beanBuilder.setScope("singleton");
            beanBuilder.setInitMethodName("init");
            beanBuilder.addPropertyValue("handlerURI", "/deref");
            beanBuilder.addPropertyValue("validTime", 1000 * 60 * 60);
            beanBuilder.addPropertyValue("mustSign", true);
            beanBuilder.addPropertyValue("configuration", config);
            beanBuilder.addPropertyValue("sessionAdmin", new RuntimeBeanReference(SessionAdmin.class.getName()));
            beanBuilder.addPropertyValue("sessionTrackingStrategy", strategyInfo.getSessionTrackingStrategyInstance());
            beanBuilder.addPropertyValue("tenantInfo", new RuntimeBeanReference(TenantInfo.class.getName()));
            beanBuilder.addPropertyValue("languageInfo", new RuntimeBeanReference(LanguageInfo.class.getName()));
            beanBuilder.addPropertyValue("siteMap", new RuntimeBeanReference(SiteMap.class.getName()));
            beanBuilder.addPropertyValue("pageMap", new RuntimeBeanReference(PageMap.class.getName()));
            BeanDefinition beanDefinition = beanBuilder.getBeanDefinition();
            BeanDefinitionHolder beanHolder = new BeanDefinitionHolder(beanDefinition, DerefRequestHandler.class.getName());
            context.getObjectTreeElement().addObject(beanHolder);
            
        } else if(root.getLocalName().equals("deref-service")) {
        
            Element serviceElement = (Element) context.getNode();
            
            String path="/deref";
            Element element = (Element) serviceElement.getElementsByTagNameNS(Constants.NS_PROJECT, "path").item(0);
            if (element != null) path = element.getTextContent().trim();
            
            long validTime = 1000 * 60 * 60;
            element = (Element) serviceElement.getElementsByTagNameNS(Constants.NS_PROJECT, "validtime").item(0);
            if (element != null) validTime = Long.parseLong(element.getTextContent().trim()) * 1000;
            
            boolean mustSign = true;
            element = (Element) serviceElement.getElementsByTagNameNS(Constants.NS_PROJECT, "mustsign").item(0);
            if (element != null) mustSign = Boolean.parseBoolean(element.getTextContent().trim());
            
            beanBuilder.addPropertyValue("handlerURI", path);
            beanBuilder.addPropertyValue("validTime", validTime);
            beanBuilder.addPropertyValue("mustSign", mustSign);
        }
    }

}
