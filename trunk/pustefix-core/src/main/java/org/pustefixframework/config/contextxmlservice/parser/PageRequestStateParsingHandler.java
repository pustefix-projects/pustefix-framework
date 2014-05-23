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

package org.pustefixframework.config.contextxmlservice.parser;

import org.pustefixframework.config.contextxmlservice.parser.internal.PageRequestConfigImpl;
import org.pustefixframework.config.contextxmlservice.parser.internal.StateConfigImpl;
import org.pustefixframework.config.customization.CustomizationAwareParsingHandler;
import org.pustefixframework.config.generic.ParsingUtils;
import org.w3c.dom.Element;

import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.exception.ParserException;

import de.schlund.pfixcore.workflow.ConfigurableState;

public class PageRequestStateParsingHandler extends CustomizationAwareParsingHandler {

    @Override
    protected void handleNodeIfActive(HandlerContext context) throws ParserException {
        PageRequestConfigImpl pageConfig = ParsingUtils.getSingleTopObject(PageRequestConfigImpl.class, context);
        StateConfigImpl stateConfig = ParsingUtils.getSingleTopObject(StateConfigImpl.class, context);
        
        Element stateElement = (Element) context.getNode();
        String stateClassName = stateElement.getAttribute("class");
        String beanRef = stateElement.getAttribute("bean-ref");
        String parentBeanRef = stateElement.getAttribute("parent-bean-ref");
        String beanName = stateElement.getAttribute("bean-name");
        String scope = stateElement.getAttribute("scope");
        
        if (stateClassName.length() == 0 && beanRef.length() == 0) {
            throw new ParserException("One of the \"class\" or \"bean-ref\" attributes must be specified for the <state> tag.");
        }
        
        if (stateClassName.length() > 0 && beanRef.length() > 0) {
            throw new ParserException("Only one of the \"class\" or \"bean-ref\" attributes may be specified for the <state> tag.");
        }
        
        if (beanRef.length() > 0 && parentBeanRef.length() > 0) {
            throw new ParserException("Only one of the \"bean-ref\" or \"parent-bean-ref\" attributes may be specified for the <state> tag.");
        }
        
        if (beanName.length() > 0 && stateClassName.length() == 0) {
            throw new ParserException("\"bean-name\" attribute may only be specified in combination with \"class\" attribute for the <state> tag.");
        }
        
        if (scope.length() > 0 && stateClassName.length() == 0) {
            throw new ParserException("\"scope\" attribute may only be specified in combination with \"class\" attribute for the <state> tag.");
        }
        
        if (beanRef.length() > 0) {
            stateConfig.setExternalBean(true);
            pageConfig.setBeanName(beanRef);
        }
        
        if (parentBeanRef.length() > 0) {
            stateConfig.setParentBeanName(parentBeanRef);
        }
        
        if (stateClassName.length() > 0) {
        	Class<?> clazz;
        	try {
        		clazz = Class.forName(stateClassName);
        	} catch (ClassNotFoundException e) {
        		throw new ParserException("Could not load state class " + stateClassName, e);
        	}
        	if (!ConfigurableState.class.isAssignableFrom(clazz)) {
        		throw new ParserException("State class " + stateClassName + " does not implement ConfigurableState.");
        	}
        	stateConfig.setState(clazz.asSubclass(ConfigurableState.class));
        	if (beanName.length() > 0) {
        		pageConfig.setBeanName(beanName);
        	}
        }
        
        if (scope.length() > 0) {
            stateConfig.setScope(scope);
        }
    }

}
