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

public class PagerequestRule extends CheckedRule {
    private ContextXMLServletConfigImpl config;

    public PagerequestRule(ContextXMLServletConfigImpl config) {
        this.config = config;
    }

    public void begin(String namespace, String name, Attributes attributes) throws Exception {
        check(namespace, name, attributes);
        ContextConfigImpl ctxConfig = config.getContextConfig();
        PageRequestConfigImpl pageConfig = new PageRequestConfigImpl();
        String pageName = attributes.getValue("name");
        if (pageName == null) {
            throw new SAXException("Mandatory attribute \"name\" is missing!");
        }
        pageConfig.setPageName(pageName);
        String copyfrom = attributes.getValue("copyfrom");
        if (copyfrom != null) {
            pageConfig.setCopyFromPage(copyfrom);
        }
        String nostore = attributes.getValue("nostore");
        if (nostore != null) {
            pageConfig.setStoreXML(!Boolean.parseBoolean(nostore));
        } else {
            pageConfig.setStoreXML(true);
        }
        pageConfig.setDefaultStaticState(config.getDefaultStaticState());
        pageConfig.setDefaultIHandlerState(config.getDefaultIHandlerState());
        ctxConfig.addPageRequest(pageConfig);
        this.getDigester().push(pageConfig);
    }
    
    public void end(String namespace, String name) throws Exception {
        this.getDigester().pop();
    }

    protected Map<String, Boolean> wantsAttributes() {
        HashMap<String, Boolean> atts = new HashMap<String, Boolean>();
        atts.put("name", true);
        atts.put("nostore", false);
        atts.put("copyfrom", false);
        return atts;
    }
}
