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
 *
 */
package de.schlund.pfixcore.oxm.impl;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * 
 * @author mleidig@schlund.de
 * 
 */
public class DOMXPathPosition implements XPathPosition {

    private Node node;

    public DOMXPathPosition(Node node) {
        this.node = node;
    }

    public String getXPath() {
        StringBuilder sb = new StringBuilder();
        buildXPath(node, sb);
        return sb.toString();
    }

    private void buildXPath(Node node, StringBuilder builder) {
        if (node != null) {
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element elem = (Element) node;
                Node parentNode = elem.getParentNode();
                if (parentNode != null && parentNode.getNodeType() == Node.ELEMENT_NODE) {
                    int pos = 1;
                    Node prevNode = elem.getPreviousSibling();
                    while (prevNode != null) {
                        if (prevNode.getNodeType() == Node.ELEMENT_NODE) {
                            if (prevNode.getNodeName().equals(elem.getNodeName())) pos++;
                        }
                        prevNode = prevNode.getPreviousSibling();
                    }
                    builder.insert(0, "/" + elem.getNodeName() + "[" + pos + "]");
                    buildXPath(parentNode, builder);
                } else {
                    builder.insert(0, "/" + elem.getNodeName());
                }
            } else if (node.getNodeType() == Node.ATTRIBUTE_NODE) {
                builder.insert(0, "/@" + node.getNodeName());
                buildXPath(node.getParentNode(), builder);
            }
        }
    }

}
