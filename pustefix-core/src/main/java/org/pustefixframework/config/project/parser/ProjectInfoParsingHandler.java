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
import org.pustefixframework.config.generic.ParsingUtils;
import org.pustefixframework.config.project.ProjectInfo;
import org.pustefixframework.util.xml.DOMUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.exception.ParserException;

import de.schlund.pfixxml.LanguageInfo;

/**
 * 
 * @author mleidig@schlund.de
 *
 */
public class ProjectInfoParsingHandler extends CustomizationAwareParsingHandler {

    public void handleNodeIfActive(HandlerContext context) throws ParserException {
       
        ProjectInfo projectInfo = ParsingUtils.getSingleTopObject(ProjectInfo.class, context);

        Element projectElem = (Element)context.getNode();
        NodeList nameElems = projectElem.getElementsByTagNameNS(Constants.NS_PROJECT, "name");
        if(nameElems.getLength() == 1) {
            Element nameElem = (Element)nameElems.item(0);
            String name = nameElem.getTextContent().trim();
            if(!name.equals("")) projectInfo.setProjectName(name);
        }
      
        List<Element> langElems = DOMUtils.getChildElementsByTagNameNS((Element)context.getNode(), Constants.NS_PROJECT, "lang");
        for(Element langElem: langElems) {
            String lang = langElem.getTextContent().trim();
            if(lang.length() > 0) {
                projectInfo.addSupportedLanguage(lang);
                if(langElem.getAttribute("default").equalsIgnoreCase("true")) {
                    projectInfo.setDefaultLanguage(lang);
                }
            }
        }
        
        BeanDefinitionBuilder beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(LanguageInfo.class);
        beanBuilder.setScope("singleton");
        beanBuilder.addPropertyValue("supportedLanguages", projectInfo.getSupportedLanguages());
        beanBuilder.addPropertyValue("defaultLanguage", projectInfo.getDefaultLanguage());
        BeanDefinition beanDefinition = beanBuilder.getBeanDefinition();
        BeanDefinitionRegistry beanRegistry = ParsingUtils.getSingleTopObject(BeanDefinitionRegistry.class, context);
        beanRegistry.registerBeanDefinition(LanguageInfo.class.getName(), beanDefinition);
        beanRegistry.registerAlias(LanguageInfo.class.getName(), "pustefixLanguageInfo");
        
    }

}
