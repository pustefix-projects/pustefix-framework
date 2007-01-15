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

public class PagerequestVariantRule extends CheckedRule {
    private ContextXMLServletConfigImpl config;

    public PagerequestVariantRule(ContextXMLServletConfigImpl config) {
        this.config = config;
    }

    public void begin(String namespace, String name, Attributes attributes) throws Exception {
        check(namespace, name, attributes);
        ContextConfigImpl ctxConfig = config.getContextConfig();
        PageRequestConfigImpl defaultConfig = (PageRequestConfigImpl) this.getDigester().peek();
        PageRequestConfigImpl pageConfig = new PageRequestConfigImpl();
        String variantName = attributes.getValue("name");
        if (variantName == null) {
            throw new SAXException("Mandatory attribute \"name\" is missing!");
        }
        pageConfig.setPageName(defaultConfig.getPageName() + "::" + variantName);
        if (defaultConfig.getCopyFromPage() != null) {
            throw new SAXException("Page using \"copyfrom\" cannot define its own variants!");
        }
        pageConfig.setStoreXML(defaultConfig.isStoreXML());
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
        return atts;
    }
}
