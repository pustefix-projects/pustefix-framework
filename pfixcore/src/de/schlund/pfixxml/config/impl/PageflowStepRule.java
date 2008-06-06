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

import java.util.HashMap;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.schlund.pfixxml.config.ContextXMLServletConfig;

public class PageflowStepRule extends CheckedRule {
    
    public PageflowStepRule(ContextXMLServletConfig config) {}

    public void begin(String namespace, String name, Attributes attributes) throws Exception {
        check(namespace, name, attributes);
        PageFlowConfigImpl flowConfig = (PageFlowConfigImpl) this.getDigester().peek();
        String pageName = attributes.getValue("name");
        if (pageName == null) {
            throw new SAXException("Mandatory attribute \"name\" is missing!");
        }
        PageFlowStepConfigImpl stepConfig = new PageFlowStepConfigImpl();
        stepConfig.setPage(pageName);
        String stophere = attributes.getValue("stophere");
        if (stophere != null) {
            stepConfig.setStopHere(Boolean.parseBoolean(stophere));
        } else {
            stepConfig.setStopHere(flowConfig.isStopNext());
        }
        flowConfig.addFlowStep(stepConfig);
        this.getDigester().push(stepConfig);
    }
    
    public void end(String namespace, String name) throws Exception {
        this.getDigester().pop();
    }

    protected Map<String, Boolean> wantsAttributes() {
        HashMap<String, Boolean> atts = new HashMap<String, Boolean>();
        atts.put("name", true);
        atts.put("stophere", false);
        return atts;
    }
}
