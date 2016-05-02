/*
 * This file is part of Pustefix.
 *
 * Pustefix is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Pustefix is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Pustefix; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package de.schlund.pfixcore.example;

import java.util.HashMap;

import org.w3c.dom.Element;

import de.schlund.pfixcore.beans.InsertStatus;

/**
 * Describe class ContextSimpleDataImpl here.
 *
 *
 * Created: Tue Jun 13 12:13:57 2006
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 * @version 1.0
 */
public class ContextSimpleData {

    private HashMap<String, String> data = new HashMap<String, String>();
    
    /**
     * Serialize the data of the model (a simple hashmap) into XML
     * @param element an <code>Element</code> value
     * @exception Exception if an error occurs
     */
    @InsertStatus
    public void serializeDataToXML(Element element) throws Exception {
        for (String key: data.keySet()) {
            String  value = data.get(key);
            Element sub   = element.getOwnerDocument().createElement(key);
            sub.setAttribute("value" , value);
            element.appendChild(sub);
        }

    }

    public void reset() {
        data.clear();
    }

    // Implementation of de.schlund.pfixcore.example.ContextSimpleData

    /**
     * Describe <code>getValue</code> method here.
     *
     * @param string a <code>String</code> value
     * @return a <code>String</code> value
     */
    public String getValue(final String string) {
        return data.get(string);
    }

    /**
     * Describe <code>setValue</code> method here.
     *
     * @param string a <code>String</code> value
     * @param string1 a <code>String</code> value
     */
    public void setValue(final String key, final String value) {
        data.put(key, value);
    }

}
