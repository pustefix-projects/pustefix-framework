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


public class DirectForeignContextRule extends CheckedRule {

    private DirectOutputServletConfigImpl config;

    public DirectForeignContextRule(DirectOutputServletConfigImpl config) {
        this.config = config;
    }

    /* (non-Javadoc)
     * @see org.apache.commons.digester.Rule#begin(java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    public void begin(String namespace, String name, Attributes attributes) throws Exception {
        check(namespace, name, attributes);
        String servletName = attributes.getValue("externalservletname");
        if (servletName == null) {
            throw new SAXException("Mandatory attribute \"externalservletname\" is missing!");
        }
        this.config.setExternalServletName(servletName);
        String syncStr = attributes.getValue("synchronized");
        if (syncStr != null && !Boolean.parseBoolean(syncStr)) {
            this.config.setSynchronized(false);
        } else {
            this.config.setSynchronized(true);
        }
    }
    
    protected Map<String, Boolean> wantsAttributes() {
        HashMap<String, Boolean> atts = new HashMap<String, Boolean>();
        atts.put("externalservletname", true);
        atts.put("synchronized", false);
        return atts;
    }
}
