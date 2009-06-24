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

package org.pustefixframework.config.application.parser;

import org.pustefixframework.config.application.ProjectInfo;
import org.pustefixframework.config.customization.CustomizationAwareParsingHandler;
import org.pustefixframework.config.generic.ParsingUtils;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;

import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.exception.ParserException;

import de.schlund.pfixxml.serverutil.SessionAdmin;

/**
 * 
 * @author mleidig@schlund.de
 *
 */
public class SessionAdminParsingHandler extends CustomizationAwareParsingHandler {

    @Override
    public void handleNodeIfActive(HandlerContext context) throws ParserException {
       
        BeanDefinitionBuilder beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(SessionAdmin.class);
        ProjectInfo projectInfo = ParsingUtils.getSingleTopObject(ProjectInfo.class, context);
        beanBuilder.addPropertyValue("projectName", projectInfo.getProjectName());
        beanBuilder.setScope("singleton");
        BeanDefinitionHolder beanHolder = new BeanDefinitionHolder(beanBuilder.getBeanDefinition(), SessionAdmin.class.getName());
        context.getObjectTreeElement().addObject(beanHolder);
       
    }

}
