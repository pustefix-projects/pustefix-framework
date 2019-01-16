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

import org.pustefixframework.config.contextxmlservice.ContextXMLServletConfig;
import org.pustefixframework.config.customization.CustomizationAwareParsingHandler;
import org.pustefixframework.config.generic.ParsingUtils;
import org.pustefixframework.http.ErrorFilter;
import org.pustefixframework.http.PustefixInitFilter;
import org.pustefixframework.web.servlet.i18n.PustefixLocaleResolverPostProcessor;
import org.pustefixframework.web.servlet.view.XsltView;
import org.pustefixframework.web.servlet.view.XsltViewResolver;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;

import com.marsching.flexiparse.configuration.RunOrder;
import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.exception.ParserException;

import de.schlund.pfixxml.LanguageInfo;
import de.schlund.pfixxml.Tenant;
import de.schlund.pfixxml.TenantInfo;
import de.schlund.pfixxml.exceptionprocessor.ExceptionProcessingConfiguration;

public class ProjectParsingHandler extends CustomizationAwareParsingHandler {

    public void handleNodeIfActive(HandlerContext context) throws ParserException {
       
        if(context.getRunOrder() != RunOrder.START) {

        	List<Tenant> tenants = new ArrayList<Tenant>();
            Collection<Tenant> tenantCollection = context.getObjectTreeElement().getObjectsOfTypeFromSubTree(Tenant.class);
            tenants.addAll(tenantCollection);
        
            BeanDefinitionBuilder beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(TenantInfo.class);
            beanBuilder.setScope("singleton");
            beanBuilder.addPropertyValue("tenants", tenants);
            BeanDefinition beanDefinition = beanBuilder.getBeanDefinition();
            BeanDefinitionRegistry beanRegistry = ParsingUtils.getSingleTopObject(BeanDefinitionRegistry.class, context);
            beanRegistry.registerBeanDefinition(TenantInfo.class.getName(), beanDefinition);
            beanRegistry.registerAlias(TenantInfo.class.getName(), "pustefixTenantInfo");

            beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(PustefixLocaleResolverPostProcessor.class);
            beanBuilder.setScope("singleton");
            beanRegistry.registerBeanDefinition(PustefixLocaleResolverPostProcessor.class.getName(), beanBuilder.getBeanDefinition());

            beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(XsltViewResolver.class);
            beanBuilder.setScope("singleton");
            beanBuilder.addPropertyValue("viewClass", XsltView.class);
            beanRegistry.registerBeanDefinition(XsltViewResolver.class.getName(), beanBuilder.getBeanDefinition());

            beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(PustefixInitFilter.class);
            beanBuilder.setScope("singleton");
            beanBuilder.addPropertyReference("tenantInfo", TenantInfo.class.getName());
            beanBuilder.addPropertyReference("languageInfo", LanguageInfo.class.getName());
            beanRegistry.registerBeanDefinition(PustefixInitFilter.class.getName(), beanBuilder.getBeanDefinition());

            beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(ErrorFilter.class);
            beanBuilder.setScope("singleton");
            beanBuilder.addPropertyReference("exceptionProcessingConfiguration", ExceptionProcessingConfiguration.class.getName());
            ContextXMLServletConfig config = context.getObjectTreeElement().getObjectsOfTypeFromSubTree(ContextXMLServletConfig.class).iterator().next();
            beanBuilder.addPropertyValue("properties", config.getProperties());
            beanRegistry.registerBeanDefinition(ErrorFilter.class.getName(), beanBuilder.getBeanDefinition());
        }
    }

}
