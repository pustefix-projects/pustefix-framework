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

import de.schlund.pfixxml.AppVariant;
import de.schlund.pfixxml.AppVariantInfo;

/**
 * 
 * @author mleidig@schlund.de
 *
 */
public class ProjectParsingHandler extends CustomizationAwareParsingHandler {

    public void handleNodeIfActive(HandlerContext context) throws ParserException {
       
        if(context.getRunOrder() == RunOrder.START) {
            
            
        } else {
            
            List<AppVariant> appVariants = new ArrayList<AppVariant>();
            Collection<AppVariant> appVarCollection = context.getObjectTreeElement().getObjectsOfTypeFromSubTree(AppVariant.class);
            System.out.println("AAAAAAAAAAAAAACCCCCCCCCCCC: "+appVarCollection.size());
            appVariants.addAll(appVarCollection);
        
            BeanDefinitionBuilder beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(AppVariantInfo.class);
            beanBuilder.setScope("singleton");
            beanBuilder.addPropertyValue("appVariants", appVariants);
            BeanDefinition beanDefinition = beanBuilder.getBeanDefinition();
            BeanDefinitionRegistry beanRegistry = ParsingUtils.getSingleTopObject(BeanDefinitionRegistry.class, context);
            beanRegistry.registerBeanDefinition(AppVariantInfo.class.getName(), beanDefinition);
            
        }  
      
    }

}
