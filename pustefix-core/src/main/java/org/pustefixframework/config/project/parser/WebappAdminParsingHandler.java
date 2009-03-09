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

import org.pustefixframework.admin.mbeans.WebappAdmin;
import org.pustefixframework.config.customization.CustomizationAwareParsingHandler;
import org.pustefixframework.config.generic.ParsingUtils;
import org.pustefixframework.config.project.ProjectInfo;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;

import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.exception.ParserException;

/**
 * 
 * @author mleidig@schlund.de
 *
 */
public class WebappAdminParsingHandler extends CustomizationAwareParsingHandler {

    @Override
    public void handleNodeIfActive(HandlerContext context) throws ParserException {
        
        BeanDefinitionBuilder beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(WebappAdmin.class);
        beanBuilder.setScope("singleton");
        ProjectInfo projectInfo = ParsingUtils.getSingleTopObject(ProjectInfo.class, context);
        beanBuilder.addPropertyValue("projectName", projectInfo.getProjectName());
        BeanDefinitionHolder beanHolder = new BeanDefinitionHolder(beanBuilder.getBeanDefinition(), WebappAdmin.class.getName());
        
        context.getObjectTreeElement().addObject(beanHolder);
       
    }

}
