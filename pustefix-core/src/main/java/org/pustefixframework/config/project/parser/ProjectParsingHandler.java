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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.pustefixframework.config.customization.CustomizationAwareParsingHandler;
import org.pustefixframework.config.generic.ParsingUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;

import com.marsching.flexiparse.configuration.RunOrder;
import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.exception.ParserException;

import de.schlund.pfixxml.Tenant;
import de.schlund.pfixxml.TenantInfo;

/**
 * 
 * @author mleidig@schlund.de
 *
 */
public class ProjectParsingHandler extends CustomizationAwareParsingHandler {

    public void handleNodeIfActive(HandlerContext context) throws ParserException {
       
        if(context.getRunOrder() == RunOrder.START) {
            
            
        } else {
            
            List<Tenant> tenants = new ArrayList<Tenant>();
            Collection<Tenant> tenantCollection = context.getObjectTreeElement().getObjectsOfTypeFromSubTree(Tenant.class);
            tenants.addAll(tenantCollection);
        
            BeanDefinitionBuilder beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(TenantInfo.class);
            beanBuilder.setScope("singleton");
            beanBuilder.addPropertyValue("tenants", tenants);
            BeanDefinition beanDefinition = beanBuilder.getBeanDefinition();
            BeanDefinitionRegistry beanRegistry = ParsingUtils.getSingleTopObject(BeanDefinitionRegistry.class, context);
            beanRegistry.registerBeanDefinition(TenantInfo.class.getName(), beanDefinition);
            
        }  
      
    }

}
