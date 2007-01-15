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

package de.schlund.pfixxml.config.impl;

import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.schlund.pfixcore.workflow.FlowStepAction;
import de.schlund.pfixxml.config.ContextXMLServletConfig;

public class PageflowStepActionRule extends CheckedRule {
    private ContextXMLServletConfig config;

    public PageflowStepActionRule(ContextXMLServletConfig config) {
        this.config = config;
    }

    public void begin(String namespace, String name, Attributes attributes) throws Exception {
       // DO NOT call check because we want arbitrary attributes to be passed
       // to the action config 

        PageFlowStepActionConditionConfigImpl condConfig = (PageFlowStepActionConditionConfigImpl) this.getDigester().peek();
        String type = attributes.getValue("type");
        if (type == null) {
            throw new SAXException("Mandatory attribute \"type\" is missing!");
        }
        if (type.equals("jumpto")) {
            type = "de.schlund.pfixcore.workflow.FlowStepJumpToAction";
        }
        Class clazz;
        try {
            clazz = Class.forName(type);
        } catch (ClassNotFoundException e) {
            throw new SAXException("Could not load class \"" + type + "\"!", e);
        }
        if (!FlowStepAction.class.isAssignableFrom(clazz)) {
            throw new SAXException("Pageflow step action " + clazz + " does not implmenent " + FlowStepAction.class + " interface!");
        }
        PageFlowStepActionConfigImpl actionConfig = new PageFlowStepActionConfigImpl();
        actionConfig.setActionType(clazz);
        for (int i = 0; i < attributes.getLength(); i++) {
            actionConfig.setParam(attributes.getLocalName(i), attributes.getValue(i));
        }
        condConfig.addAction(actionConfig);
    }

    protected Map<String, Boolean> wantsAttributes() {
        // This is not used here, so return null
        return null;
    }    
}
