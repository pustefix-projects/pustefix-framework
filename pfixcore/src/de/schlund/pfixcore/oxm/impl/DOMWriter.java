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
 * @author mleidig@schlund.de
 */
public class DOMWriter implements XMLWriter {

    private Node root;
    private Node current;
    
    public DOMWriter(Node node) {
        if(node.getNodeType()!=Node.ELEMENT_NODE) 
            throw new IllegalArgumentException("Illegal node type: must be element node");
        root=node;
        current=root;
    }
    
    public void writeStartElement(String localName) {
        Element elem=root.getOwnerDocument().createElement(localName);
        current.appendChild(elem);
        current=elem;
    }
    
    public void writeCharacters(String text) {
        Node node=root.getOwnerDocument().createTextNode(text);
        current.appendChild(node);
    }
    
    public void writeEndElement(String localName) {
        current=current.getParentNode();
    }
    
    public void writeAttribute(String localName, String value) {
        Element elem=(Element)current;
        elem.setAttribute(localName, value);
    }
    
    public Node getNode() {
        return root;
    }
    
    public XPathPosition getCurrentPosition() {
       return new DOMXPathPosition(current);
    }
    
}
