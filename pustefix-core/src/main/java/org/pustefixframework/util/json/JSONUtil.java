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

package org.pustefixframework.util.json;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Utility functions for the JSON protocol.  
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public abstract class JSONUtil {

    /**
     * Creates JSON code from an XML element.
     * XML attributes are stored in a map within the object
     * attribute "_attr", text and CDATA content is stored within
     * the object attribute "_text". XML child elements are stored
     * within arrays, where one array is created for every element
     * name and store in the object using the XML element name as 
     * the object attribute name. If the XML element name starts with
     * an underscore, an extra underscore is prepended.
     * XML namespace information is completely ignored.
     * The "_attr" and "_text" attributes are not created, if there is
     * no data to store within this attributes. 
     * 
     * @param element XML element whose content should be rendered
     * @return a valid JSON string representing the data stored
     *  within the XML element
     */
    public static String xmlElementToJSON(Element element) {
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        if (element.hasAttributes()) {
            sb.append("\"_attr\":");
            sb.append(attributesToJson(element));
            sb.append(',');
        }
        Map<String, List<String>> elements = new LinkedHashMap<String, List<String>>();
        StringBuilder text = new StringBuilder();
        NodeList children = element.getChildNodes();
        int numChildren = children.getLength();
        for (int i = 0; i < numChildren; i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                Element childElement = (Element) child;
                List<String> codeList = elements.get(childElement.getTagName());
                if (codeList == null) {
                    codeList = new LinkedList<String>();
                    elements.put(childElement.getTagName(), codeList);
                }
                codeList.add(xmlElementToJSON(childElement));
            } else if (child.getNodeType() == Node.TEXT_NODE) {
                String nodeValue = child.getNodeValue();
                if (nodeValue.trim().length() != 0) {
                    // Only append text nodes,
                    // which are not pure white-space
                    text.append(child.getNodeValue());
                }
            } else if (child.getNodeType() == Node.CDATA_SECTION_NODE) {
                text.append(child.getNodeValue());
            }
        }
        for (String elementName : elements.keySet()) {
            List<String> elementsCode = elements.get(elementName);
            if (elementName.startsWith("_")) {
                elementName = "_" + elementName;
            }
            sb.append(escapeAndQuoteString(elementName));
            sb.append(':');
            sb.append('[');
            boolean firstElement = true;
            for (String elementCode : elementsCode) {
                if (!firstElement) {
                    sb.append(',');
                } else {
                    firstElement = false;
                }
                sb.append(elementCode);
            }
            sb.append(']');
            sb.append(',');
        }
        if (text.length() != 0) {
            sb.append("\"_text\":");
            sb.append(escapeAndQuoteString(text.toString()));
            sb.append(',');
        }
        // Remove trailing comma
        if (sb.charAt(sb.length() - 1) == ',') {
            sb.deleteCharAt(sb.length() - 1);
        }
        sb.append('}');
        return sb.toString();
    }

    private static String attributesToJson(Element element) {
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        NamedNodeMap attributeMap = element.getAttributes();
        int numAttributes = attributeMap.getLength();
        for (int i = 0; i < numAttributes; i++) {
            Attr attribute = (Attr) attributeMap.item(i);
            if (i > 0) {
                sb.append(',');
            }
            sb.append(escapeAndQuoteString(attribute.getName()));
            sb.append(':');
            sb.append(escapeAndQuoteString(attribute.getValue()));
        }
        sb.append('}');
        return sb.toString();
    }

    private static String escapeAndQuoteString(String string) {
        StringBuilder sb = new StringBuilder();
        sb.append('"');
        sb.append(escapeString(string));
        sb.append('"');
        return sb.toString();
    }

    private static String escapeString(String string) {
        StringBuilder sb = new StringBuilder();
        int strLen = string.length();
        for (int i = 0; i < strLen; i++) {
            int c = string.codePointAt(i);
            if (c >= Character.MIN_SUPPLEMENTARY_CODE_POINT) {
                i++;
                sb.appendCodePoint(c);
            } else {
                if (c == '\\' || c == '"') {
                    sb.append('\\');
                    sb.appendCodePoint(c);
                } else if (c < 0x20) {
                    sb.append("\\u00");
                    String hexString = Integer.toHexString(c);
                    if (hexString.length() == 1) {
                        sb.append('0');
                    }
                    sb.append(hexString);
                } else {
                    sb.appendCodePoint(c);
                }
            }
        }
        return sb.toString();
    }
}
