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

import org.pustefixframework.config.customization.CustomizationAwareParsingHandler;
import org.pustefixframework.config.generic.ParsingUtils;
import org.pustefixframework.config.project.ProjectInfo;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.w3c.dom.Element;

import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.exception.ParserException;

import de.schlund.pfixxml.perflogging.PerfLogging;

/**
 * 
 * @author mleidig@schlund.de
 *
 */
public class PerfLoggingParsingHandler extends CustomizationAwareParsingHandler {

    public void handleNodeIfActive(HandlerContext context) throws ParserException {
        
        Element element = (Element)context.getNode();
        ParsingUtils.checkAttributes(element, null, new String[] {"enabled", "buffersize", "autostart", "offermaxwait"});
        
        BeanDefinitionBuilder beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(PerfLogging.class);
       
        String value = element.getAttribute("enabled").trim();
        if(value.length() > 0) beanBuilder.addPropertyValue("enabled", Boolean.parseBoolean(value));
        value = element.getAttribute("buffersize");
        if(value.length() > 0) beanBuilder.addPropertyValue("bufferSize", Integer.parseInt(value));
        value = element.getAttribute("autostart");
        if(value.length() > 0) beanBuilder.addPropertyValue("autoStart", Boolean.parseBoolean(value));
        value = element.getAttribute("offermaxwait");
        if(value.length() > 0) beanBuilder.addPropertyValue("offerMaxWait", Integer.parseInt(value));
        
        ProjectInfo projectInfo = ParsingUtils.getSingleTopObject(ProjectInfo.class, context);
        beanBuilder.addPropertyValue("projectName", projectInfo.getProjectName());
        
        String beanName = PerfLogging.class.getName();
        BeanDefinition beanDefinition = beanBuilder.getBeanDefinition();
        BeanDefinitionRegistry beanRegistry = ParsingUtils.getSingleTopObject(BeanDefinitionRegistry.class, context);
        beanRegistry.registerBeanDefinition(beanName, beanDefinition);
        
    }

}
