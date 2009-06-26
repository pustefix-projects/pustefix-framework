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

import org.pustefixframework.config.contextxmlservice.parser.internal.PageFlowStepActionConditionConfigImpl;
import org.pustefixframework.config.contextxmlservice.parser.internal.PageFlowStepActionConfigImpl;
import org.pustefixframework.config.generic.ParsingUtils;
import org.springframework.osgi.context.ConfigurableOsgiBundleApplicationContext;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.ParsingHandler;
import com.marsching.flexiparse.parser.exception.ParserException;

import de.schlund.pfixcore.workflow.FlowStepAction;
import de.schlund.pfixcore.workflow.FlowStepJumpToAction;

/**
 * 
 * @author mleidig
 *
 */
public class PageFlowStepActionParsingHandler implements ParsingHandler {

    public void handleNode(HandlerContext context) throws ParserException {
       
        Element element = (Element)context.getNode();
        
        ConfigurableOsgiBundleApplicationContext appContext = ParsingUtils.getSingleTopObject(ConfigurableOsgiBundleApplicationContext.class, context);

        PageFlowStepActionConditionConfigImpl condConfig = ParsingUtils.getFirstTopObject(PageFlowStepActionConditionConfigImpl.class, context, true);
        
        Class<?> clazz = null;
        String type = element.getAttribute("type").trim();
        if (type.length()==0) {
            throw new ParserException("Mandatory attribute \"type\" is missing!");
        }
        if (type.equals("jumpto")) {
            clazz = FlowStepJumpToAction.class;
        }
        try {
            if (clazz == null) {
                clazz = Class.forName(type, true, appContext.getClassLoader());
            }
        } catch (ClassNotFoundException e) {
            throw new ParserException("Could not load class \"" + type + "\"!", e);
        }
        if (!FlowStepAction.class.isAssignableFrom(clazz)) {
            throw new ParserException("Pageflow step action " + clazz + " does not implmenent " + FlowStepAction.class + " interface!");
        }
        PageFlowStepActionConfigImpl actionConfig = new PageFlowStepActionConfigImpl();
        actionConfig.setActionType(clazz.asSubclass(FlowStepAction.class));
        NamedNodeMap attrs = element.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            actionConfig.setParam(attrs.item(i).getLocalName(), attrs.item(i).getNodeValue());
        }
        condConfig.addAction(actionConfig);
       
    }

}
