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

package de.schlund.pfixxml.config;

import java.util.HashMap;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class ContextInterceptorRule extends CheckedRule {

    private ContextXMLServletConfig config;
    private String type;
    private int count;

    public ContextInterceptorRule(ContextXMLServletConfig config, String type) {
        this.config = config;
        this.type = type;
        this.count = 0;
    }

    public void begin(String namespace, String name, Attributes attributes) throws Exception {
        check(namespace, name, attributes);
        // Use properties until interceptors have been added to XSD
        String propvalue = attributes.getValue("class");
        if (propvalue == null) {
            throw new SAXException("Mandatory attribute \"class\" is missing!");
        }
        count++;
        String propname = "context." + type + "interceptor." + count;
        config.getProperties().setProperty(propname, propvalue);
    }

    protected Map<String, Boolean> wantsAttributes() {
        HashMap<String, Boolean> atts = new HashMap<String, Boolean>();
        atts.put("class", true);
        return atts;
    }
    
}
