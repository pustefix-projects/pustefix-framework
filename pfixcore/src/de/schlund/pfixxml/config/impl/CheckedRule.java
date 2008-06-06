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
import java.util.Set;

import org.apache.commons.digester.Rule;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public abstract class CheckedRule extends Rule {
    protected abstract Map<String, Boolean> wantsAttributes();

    protected void check(String namespace, String name, Attributes attributes)
            throws Exception {
        Map<String, Boolean> atts = wantsAttributes();
        if (atts == null) {
            atts = new HashMap<String, Boolean>();
        }

        Set<String> allowedAttributes = atts.keySet();

        for (String attName : allowedAttributes) {
            if (atts.get(attName).booleanValue()) {
                if (attributes.getValue(attName) == null)
                    throw new SAXException("Required attribute \"" + attName
                            + "\" missing on element \"" + name + "\" ["
                            + namespace + "]");
            }
        }

        for (int i = 0; i < attributes.getLength(); i++) {
            String attName = attributes.getLocalName(i);
            if (!allowedAttributes.contains(attName)) {
                throw new SAXException("Unknown attribute \"" + attName
                        + "\" specified on element \"" + name + "\" ["
                        + namespace + "]");
            }
        }

    }

    public void body(String namespace, String name, String text)
            throws Exception {
        if (text != null && !isWhitespace(text)) {
            throw new SAXException("Got text below element \"" + name + "\" ["
                    + namespace + "] although not allowed here");
        }
    }

    private boolean isWhitespace(String text) {
        if (text.length() == 0) {
            return true;
        }
        return text.matches("\\s*");
    }

}
