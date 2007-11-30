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

import de.schlund.pfixcore.scripting.ScriptingState;
import de.schlund.pfixcore.workflow.State;
import de.schlund.pfixxml.config.ContextXMLServletConfig;

public class PagerequestStateRule extends CheckedRule {
    public PagerequestStateRule(ContextXMLServletConfig config) {
    }

    public void begin(String namespace, String name, Attributes attributes)
            throws Exception {
        check(namespace, name, attributes);
        PageRequestConfigImpl pageConfig = (PageRequestConfigImpl) this.getDigester()
                .peek();
        String className = attributes.getValue("class");
        if (className == null) {
            throw new SAXException("Mandatory attribute \"class\" is missing!");
        }
        Class clazz;
        if (className.startsWith("script:")) {
            String scriptName = className.substring(7);
            pageConfig.getProperties().setProperty(
                    ScriptingState.PROP_SCRIPT_PATH, scriptName);
            clazz = ScriptingState.class;
        } else {
            try {
                clazz = Class.forName(className);
            } catch (ClassNotFoundException e) {
                throw new SAXException("Could not load class \"" + className
                        + "\"!", e);
            }
        }
        if (!State.class.isAssignableFrom(clazz)) {
            throw new SAXException("State " + clazz + " for page " + pageConfig.getPageName() + " does not implement " + State.class + " interface!");
        }
        pageConfig.setState(clazz);
    }

    protected Map<String, Boolean> wantsAttributes() {
        HashMap<String, Boolean> atts = new HashMap<String, Boolean>();
        atts.put("class", true);
        return atts;
    }
}
