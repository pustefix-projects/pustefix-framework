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

import de.schlund.pfixcore.workflow.ContextInterceptor;

public class ContextInterceptorRule extends CheckedRule {

    private ContextXMLServletConfigImpl config;
    private String type;

    public ContextInterceptorRule(ContextXMLServletConfigImpl config, String type) {
        this.config = config;
        if (!(type.equals("start") || type.equals("end"))) {
            throw new IllegalArgumentException("\"" + type + "\" is not a valid context interceptor type!");
        }
        this.type = type;
    }

    public void begin(String namespace, String name, Attributes attributes) throws Exception {
        check(namespace, name, attributes);
        // Use properties until interceptors have been added to XSD
        String classname = attributes.getValue("class");
        Class clazz;
        try {
            clazz = Class.forName(classname);
        } catch (ClassNotFoundException e) {
            throw new SAXException("Could not load interceptor class " + classname, e);
        }
        if (!ContextInterceptor.class.isAssignableFrom(clazz)) {
            throw new SAXException("Context interceptor " + clazz + " does not implmement " + ContextInterceptor.class + " interface!");
        }
        if (type.equals("start")) {
            config.getContextConfig().addStartInterceptor(clazz);
        }
        if (type.equals("end")) {
            config.getContextConfig().addEndInterceptor(clazz);
        }
    }

    protected Map<String, Boolean> wantsAttributes() {
        HashMap<String, Boolean> atts = new HashMap<String, Boolean>();
        atts.put("class", true);
        return atts;
    }
    
}
