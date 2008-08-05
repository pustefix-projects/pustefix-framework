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

package org.pustefixframework.config.directoutputservice.parser;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Properties;

import org.pustefixframework.config.Constants;
import org.pustefixframework.config.customization.CustomizationAwareParsingHandler;
import org.pustefixframework.config.directoutputservice.parser.internal.DirectOutputPageRequestConfigImpl;
import org.pustefixframework.config.generic.ParsingUtils;
import org.springframework.aop.scope.ScopedProxyUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.beans.factory.support.DefaultBeanNameGenerator;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.marsching.flexiparse.configuration.RunOrder;
import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.exception.ParserException;

import de.schlund.pfixcore.workflow.DirectOutputState;

public class DirectOutputPageRequestParsingHandler extends CustomizationAwareParsingHandler {

    @Override
    protected void handleNodeIfActive(HandlerContext context) throws ParserException {
        if (context.getRunOrder() == RunOrder.START) {
            Element elem = (Element) context.getNode();
            String pageName = elem.getAttribute("name");
            if (pageName.length() == 0) {
                throw new ParserException("Mandatory attribute \"name\" is missing!");
            }
            DirectOutputPageRequestConfigImpl reqConfig = new DirectOutputPageRequestConfigImpl();
            reqConfig.setPageName(pageName);
            
            NodeList stateElemList = elem.getElementsByTagNameNS(Constants.NS_DIRECT_OUTPUT_SERVICE, "directoutputstate");
            if (stateElemList.getLength() != 1) {
                throw new ParserException("\"directoutputpagerequest\" has to have exactly one \"directoutputstate\" child");
            }
            Element stateElem = (Element) stateElemList.item(0);
            String scope = stateElem.getAttribute("scope");
            String className = stateElem.getAttribute("class");
            String beanRef = stateElem.getAttribute("bean-ref");
            String beanAlias = null;
            if (className.length() == 0 && beanRef.length() == 0) {
                throw new ParserException("One of attributes \"class\" or \"bean-ref\" must be present!");
            }
            if (className.length() != 0 && beanRef.length() != 0) {
                throw new ParserException("Only one of attributes \"class\" or \"bean-ref\" may be specified!");
            }
            if (beanRef.length() != 0 && scope.length() != 0) {
                throw new ParserException("\"scope\" attribute must not be used with \"bean-ref\" attribute!");
            }
            if (className.length() != 0) {
                beanAlias = stateElem.getAttribute("bean-name");
            }
            if (className.length() != 0) {
                Class<?> clazz;
                try {
                    clazz = Class.forName(className);
                } catch (ClassNotFoundException e) {
                    throw new ParserException("Could not load class \"" + className + "\"!", e);
                }
                if (!DirectOutputState.class.isAssignableFrom(clazz)) {
                    throw new ParserException("Direct output state " + clazz + " for page " + reqConfig.getPageName() + " does not implmenent " + DirectOutputState.class + " interface!");
                }
                if (scope.length() == 0) {
                    scope = "singleton";
                }
                beanRef = createBeanDefinition(context, clazz.asSubclass(DirectOutputState.class), beanAlias, pageName, scope);
            }
            reqConfig.setBeanName(beanRef);
            context.getObjectTreeElement().addObject(reqConfig);
        } else if (context.getRunOrder() == RunOrder.END) {
            DirectOutputPageRequestConfigImpl reqConfig = context.getObjectTreeElement().getObjectsOfType(DirectOutputPageRequestConfigImpl.class).iterator().next();
            Collection<Properties> propertiesCollection = context.getObjectTreeElement().getObjectsOfTypeFromSubTree(Properties.class);
            Properties properties = new Properties();
            for (Properties p : propertiesCollection) {
                Enumeration<?> en = p.propertyNames();
                while (en.hasMoreElements()) {
                    String propName = (String) en.nextElement();
                    String propValue = p.getProperty(propName);
                    properties.setProperty(propName, propValue);
                }
            }
            reqConfig.setProperties(properties);
        }
    }
    
    private String createBeanDefinition(HandlerContext context, Class<? extends DirectOutputState> beanClass, String beanAlias, String pageName, String scope) throws ParserException {
        String beanName;
        BeanNameGenerator nameGenerator = new DefaultBeanNameGenerator();
        BeanDefinitionRegistry registry = ParsingUtils.getSingleTopObject(BeanDefinitionRegistry.class, context);
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(beanClass);
        builder.setScope(scope);
        BeanDefinition beanDefinition = builder.getBeanDefinition();
        if (beanAlias != null && beanAlias.length() != 0) {
            beanName = beanAlias;
        } else {
            beanName = nameGenerator.generateBeanName(beanDefinition, registry);
        }
        BeanDefinitionHolder beanHolder = new BeanDefinitionHolder(beanDefinition, beanName);
        if (!scope.equals("singleton") && !scope.equals("prototype")) {
            beanHolder = ScopedProxyUtils.createScopedProxy(beanHolder, registry, true);
        }
        registry.registerBeanDefinition(beanHolder.getBeanName(), beanHolder.getBeanDefinition());
        return beanName;
    }
}
