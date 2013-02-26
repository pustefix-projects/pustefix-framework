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

import java.util.List;
import java.util.Set;

import org.pustefixframework.config.Constants;
import org.pustefixframework.config.generic.ParsingUtils;
import org.pustefixframework.config.project.StaticPathInfo;
import org.pustefixframework.http.DocrootRequestHandler;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.marsching.flexiparse.configuration.RunOrder;
import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.ParsingHandler;
import com.marsching.flexiparse.parser.exception.ParserException;

import de.schlund.pfixcore.util.ModuleDescriptor;
import de.schlund.pfixcore.util.ModuleInfo;
import de.schlund.pfixxml.LanguageInfo;
import de.schlund.pfixxml.TenantInfo;
import de.schlund.pfixxml.config.EnvironmentProperties;

public class DocrootRequestHandlerParsingHandler implements ParsingHandler {
    
    public void handleNode(HandlerContext context) throws ParserException {
        
        if(context.getRunOrder() == RunOrder.START) {
        
            Element applicationElement = (Element) context.getNode();
           
            String defaultPath = null;
            NodeList defaultPathList = applicationElement.getElementsByTagNameNS(Constants.NS_PROJECT, "default-path");
            Element defaultPathElement = (Element) defaultPathList.item(0);
            if(defaultPathElement != null) defaultPath = defaultPathElement.getTextContent().trim(); 

            NodeList basePathList = applicationElement.getElementsByTagNameNS(Constants.NS_PROJECT, "docroot-path");
            if (basePathList.getLength() != 1) {
                throw new ParserException("Found " + basePathList.getLength() + " <docroot-path> elements but expected one.");
            }
            Element basePathElement = (Element)basePathList.item(0);
            String basePath = basePathElement.getTextContent();
            
            StaticPathInfo staticPathInfo = new StaticPathInfo();
            //Add pre-defined static paths
            staticPathInfo.addStaticPath("modules/pustefix-core/img");
            staticPathInfo.addStaticPath("modules/pustefix-core/script");
            staticPathInfo.addStaticPath("modules/pustefix-webservices-jaxws/script");
            staticPathInfo.addStaticPath("modules/pustefix-webservices-jsonws/script");
            staticPathInfo.addStaticPath("wsscript");
 
            Set<String> moduleNames = ModuleInfo.getInstance().getModules();
            for(String moduleName: moduleNames) {
                ModuleDescriptor moduleDesc = ModuleInfo.getInstance().getModuleDescriptor(moduleName);
                List<String> paths = moduleDesc.getStaticPaths();
                for(String path: paths) {
                    staticPathInfo.addStaticPath("modules/" + moduleName + path);
                }
            }
            
            staticPathInfo.setBasePath(basePath);
            staticPathInfo.setDefaultPath(defaultPath);
            
            context.getObjectTreeElement().addObject(staticPathInfo);
        
        } else {
            
            StaticPathInfo staticPathInfo = ParsingUtils.getSingleObject(StaticPathInfo.class, context);
            
            BeanDefinitionBuilder beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(DocrootRequestHandler.class);
            beanBuilder.setScope("singleton");
            beanBuilder.addPropertyValue("base", staticPathInfo.getBasePath());
            if(staticPathInfo.getDefaultPath() != null && !staticPathInfo.getDefaultPath().equals("")) beanBuilder.addPropertyValue("defaultPath", staticPathInfo.getDefaultPath());
            beanBuilder.addPropertyValue("passthroughPaths", staticPathInfo.getStaticPaths());
            beanBuilder.addPropertyValue("mode", EnvironmentProperties.getProperties().getProperty("mode"));
            beanBuilder.addPropertyReference("tenantInfo", TenantInfo.class.getName());
            beanBuilder.addPropertyReference("languageInfo", LanguageInfo.class.getName());
            
            context.getObjectTreeElement().addObject(new BeanDefinitionHolder(beanBuilder.getBeanDefinition(), "org.pustefixframework.http.DocrootRequestHandler"));
            
        }
    }

}
