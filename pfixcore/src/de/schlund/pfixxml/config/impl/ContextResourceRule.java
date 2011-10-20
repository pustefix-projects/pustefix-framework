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

import de.schlund.pfixcore.workflow.ContextResource;

public class ContextResourceRule extends CheckedRule {

    private ContextXMLServletConfigImpl config;

    public ContextResourceRule(ContextXMLServletConfigImpl config) {
        this.config = config;
    }

    public void begin(String namespace, String name, Attributes attributes) throws Exception {
        check(namespace, name, attributes);
        ContextConfigImpl ctxConfig = (ContextConfigImpl) this.getDigester().peek();
        String className = attributes.getValue("class");
        if (className == null) {
            throw new SAXException("Mandatory attribute \"class\" is missing!");
        }
        Class clazz = null;
        try {
            clazz = Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new SAXException("Could not load class \"" + className + "\"!", e);
        }
        if (!ContextResource.class.isAssignableFrom(clazz)) {
            throw new SAXException("ContextResource " + clazz + " does not implement " + ContextResource.class + " interface!");
        }
        ContextResourceConfigImpl crConfig = new ContextResourceConfigImpl(clazz, config.getContextConfig());
        ctxConfig.addContextResource(crConfig);
        this.getDigester().push(crConfig);        
    }
    
    public void end(String namespace, String name) throws Exception {
        this.getDigester().pop();
    }
    
    protected Map<String, Boolean> wantsAttributes() {
        HashMap<String, Boolean> atts = new HashMap<String, Boolean>();
        atts.put("class", true);
        return atts;
    }
    
}