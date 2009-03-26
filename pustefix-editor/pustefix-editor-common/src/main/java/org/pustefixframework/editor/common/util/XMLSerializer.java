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

package org.pustefixframework.editor.common.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.schlund.pfixxml.util.Xml;


/**
 * Provides serialization for DOM nodes, so that they can be transferred over
 * a network connection or made persistent in a file.  
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class XMLSerializer {
    
    private final static String WRAP_NS = "http://pustefixframework.org/2009/org.pustefixframework.editor.util.XMLSerializer/special-namespace-only-used-for-wrapping-serialized-nodes";
    private final static String WRAP_ELEMENT_NAME = "document";
    
    /**
     * Creates a string representation of a XML Node. At the moment, only
     * the Document and Element node types are supported.
     * 
     * @param node DOM node instance
     * @return string representation only understood by the
     *  {@link #deserializeNode(String)}} method of this class.
     */
    public String serializeNode(Node node) {
        switch (node.getNodeType()) {
        
        case Node.DOCUMENT_NODE:
            return Xml.serialize(node, false, true, "UTF-8");
            
        case Node.ELEMENT_NODE:
            Document doc = wrapElement((Element) node);
            return Xml.serialize(doc, false, true, "UTF-8");
        
        default:
            throw new IllegalArgumentException("Node type not supported by serializer");
        
        }
    }

    private Document wrapElement(Element element) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db;
        try {
            db = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException("Unexpected error while creating new DocumentBuilder", e);
        }
        Document doc = db.newDocument();
        Element rootElement = doc.createElementNS(WRAP_NS, WRAP_ELEMENT_NAME);
        doc.appendChild(rootElement);
        rootElement.appendChild(doc.importNode(element, true));
        return doc;
    }
    
    /**
     * Creates a DOM node from its string representation created by
     * {@link #serializeNode(Node)}.
     * 
     * @param xml string representation of the node
     * @return DOM Node instance
     */
    public Node deserializeNode(String xml) {
        DocumentBuilder db = createDocumentBuilder();
        Document doc;
        try {
            doc = db.parse(new ByteArrayInputStream(xml.getBytes("UTF-8")));
        } catch (SAXException e) {
            throw new IllegalArgumentException("Error while parsing string representation of node", e);
        } catch (IOException e) {
            throw new RuntimeException("Unexpected IOException while reading from a ByteArrayInputStream", e);
        }
        Element rootElement = doc.getDocumentElement();
        if (rootElement.getNamespaceURI().equals(WRAP_NS)
                && rootElement.getLocalName().equals(WRAP_ELEMENT_NAME)) {
            NodeList nl = rootElement.getChildNodes();
            if (nl.getLength() != 1) {
                throw new IllegalArgumentException("String is not a valid serialized node representation");
            }
            return nl.item(0);
        } else {
            return doc;
        }
    }
    
    private DocumentBuilder createDocumentBuilder() {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        try {
            return dbf.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException("Unexpected error while creating new DocumentBuilder", e);
        }
    }
}
