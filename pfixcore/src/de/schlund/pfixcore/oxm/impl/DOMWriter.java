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

import java.io.ByteArrayInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author mleidig@schlund.de
 * @author Stephan Schmidt <schst@stubbles.net>
 */
public class DOMWriter implements XMLWriter {

    private Node root;
    private Node current;

    public DOMWriter(Node node) {
        if (node.getNodeType() != Node.ELEMENT_NODE) throw new IllegalArgumentException("Illegal node type: must be element node");
        root = node;
        current = root;
    }

    public void writeStartElement(String localName) {
        Element elem = root.getOwnerDocument().createElement(localName);
        current.appendChild(elem);
        current = elem;
    }

    public void writeCharacters(String text) {
        Node node = root.getOwnerDocument().createTextNode(text);
        current.appendChild(node);
    }

    public void writeEndElement() {
        current = current.getParentNode();
    }

    public void writeAttribute(String localName, String value) {
        Element elem = (Element) current;
        elem.setAttribute(localName, value);
    }

    /**
     * Writes a character data section
     * 
     * @param cdata
     */
    public void writeCDataSection(String cdata) {
        Node node = root.getOwnerDocument().createCDATASection(cdata);
        current.appendChild(node);
    }

    /**
     * Writes an xml fragment to the document.
     * 
     * The fragment does not need a root element, but it must be well-formed
     * xml.
     * 
     * @param xmlFragment
     *            The fragment to be written to the document.
     */
    public void writeFragment(String xmlFragment) {
        try {
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = builderFactory.newDocumentBuilder();

            // make sure, that we have a root node
            xmlFragment = "<root>" + xmlFragment + "</root>";

            byte currentXMLBytes[] = xmlFragment.getBytes();
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(currentXMLBytes);

            Document doc = builder.parse(byteArrayInputStream);
            Element root = doc.getDocumentElement();

            Document originalDoc = this.root.getOwnerDocument();
            NodeList list = root.getChildNodes();
            for (int i = 0; i < list.getLength(); i++) {
                Node node = originalDoc.importNode(list.item(i), true);
                current.appendChild(node);
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to write XML fragment", e);
        }
    }

    public Node getNode() {
        return root;
    }

    public XPathPosition getCurrentPosition() {
        return new DOMXPathPosition(current);
    }
}