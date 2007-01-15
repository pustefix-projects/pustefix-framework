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

import de.schlund.pfixcore.generator.IWrapper;
import de.schlund.pfixxml.config.ContextXMLServletConfig;

public class PagerequestInputInterfaceRule extends CheckedRule {
    private ContextXMLServletConfig config;

    public PagerequestInputInterfaceRule(ContextXMLServletConfig config) {
        this.config = config;
    }

    public void begin(String namespace, String name, Attributes attributes) throws Exception {
        check(namespace, name, attributes);
        PageRequestConfigImpl pageConfig = (PageRequestConfigImpl) this.getDigester().peek();
        IWrapperConfigImpl wrapperConfig = new IWrapperConfigImpl();
        String prefix = attributes.getValue("prefix");
        if (prefix == null) {
            throw new SAXException("Mandatory attribute \"prefix\" is missing!");
        }
        wrapperConfig.setPrefix(prefix);
        String className = attributes.getValue("class");
        if (className == null) {
            throw new SAXException("Mandatory attribute \"class\" is missing!");
        }
        Class wrapperClass;
        try {
            wrapperClass = Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new SAXException("Could not load wrapper class \"" + className + "\"!");
        }
        if (!IWrapper.class.isAssignableFrom(wrapperClass)) {
            throw new SAXException("Input wrapper class " + wrapperClass + " on page " + pageConfig.getPageName() + " does not implement " + IWrapper.class + " interface!");
        }
        wrapperConfig.setWrapperClass(wrapperClass);
        String activeignore = attributes.getValue("activeignore");
        if (activeignore != null) {
            wrapperConfig.setActiveIgnore(Boolean.parseBoolean(activeignore));
        } else {
            wrapperConfig.setActiveIgnore(false);
        }
        String doContinue = attributes.getValue("continue");
        if (doContinue != null) {
            wrapperConfig.setContinue(Boolean.parseBoolean(doContinue));
        } else {
            wrapperConfig.setContinue(false);
        }
        String alwaysretrieve = attributes.getValue("alwaysretrieve");
        if (alwaysretrieve != null) {
            wrapperConfig.setAlwaysRetrieve(Boolean.parseBoolean(alwaysretrieve));
        } else {
            wrapperConfig.setAlwaysRetrieve(false);
        }
        String dologging = attributes.getValue("logging");
        if (dologging != null) {
            wrapperConfig.setLogging(Boolean.parseBoolean(dologging));
        } else {
            wrapperConfig.setLogging(false);
        }
        pageConfig.addIWrapper(wrapperConfig);
    }
    
    protected Map<String, Boolean> wantsAttributes() {
        HashMap<String, Boolean> atts = new HashMap<String, Boolean>();
        atts.put("class", true);
        atts.put("prefix", true);
        atts.put("activeignore", false);
        atts.put("continue", false);
        atts.put("alwaysretrieve", false);
        atts.put("logging", false);
        return atts;
    }
}
