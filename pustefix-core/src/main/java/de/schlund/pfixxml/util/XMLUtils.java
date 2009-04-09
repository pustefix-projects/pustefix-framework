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
 *
 */
package de.schlund.pfixxml.util;

import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;

/**
 * This class contains some utility methods for working with w3c DOMs: 
 * - creation of a XPath expression denoting the absolute node position
 * - assertEquals JUnit test implementation for comparison of DOM nodes
 * 
 * @author mleidig
 * 
 */
public class XMLUtils {

    /**
     * Create a XPath expression denoting the node's absolute position.
     */
    public static String getXPath(Node node) {
        StringBuilder sb = new StringBuilder();
        buildXPath(node, sb);
        return sb.toString();
    }

    /**
     * Build XPath expression by bottom-up traversing the node's ancestors.
     */
    private static void buildXPath(Node node, StringBuilder builder) {
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
                buildXPath(((Attr) node).getOwnerElement(), builder);
            } else if (node.getNodeType() == Node.TEXT_NODE) {
                int pos = 1;
                Node prevNode = node.getPreviousSibling();
                while (prevNode != null) {
                    if (prevNode.getNodeType() == Node.TEXT_NODE) pos++;
                    prevNode = prevNode.getPreviousSibling();
                }
                builder.insert(0, "/text()[" + pos + "]");
                buildXPath(node.getParentNode(), builder);
            }
        }
    }
    
    /**
     * Strips whitespace (empty text nodes) from DOM
     */
    public static void stripWhitespace(Node node) {
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            Element elem = (Element) node;
            NodeList nl = elem.getChildNodes();
            List<Node> nodes = new ArrayList<Node>();
            for (int i = 0; i < nl.getLength(); i++)
                nodes.add(nl.item(i));
            for (Node n : nodes)
                stripWhitespace(n);
        } else if (node.getNodeType() == Node.TEXT_NODE) {
            Text text = (Text) node;
            String content = text.getTextContent();
            if (content.trim().equals("")) node.getParentNode().removeChild(node);
        } else if (node.getNodeType() == Node.DOCUMENT_NODE) {
            stripWhitespace(node.getFirstChild());
        }
    }
    
    public static Document parse(String xml) throws RuntimeException {
        try {
            DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            return db.parse(new InputSource(new StringReader(xml)));
        } catch(Exception x) {
            throw new RuntimeException("Error parsing XML string.", x);
        }
    }
    
    public static Document parse(InputStream in) throws RuntimeException {
        try {
            DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            return db.parse(in);
        } catch(Exception x) {
            throw new RuntimeException("Error parsing XML string.", x);
        }
    }
    
    public static void serialize(Document doc) throws Exception {
        TransformerFactory tf=TransformerFactory.newInstance();
        Transformer t=tf.newTransformer();
        DOMSource src=new DOMSource(doc);
        StreamResult res=new StreamResult(System.out);
        t.transform(src,res); 
    }

    public static String serializeToString(Document doc) throws Exception {
        StringWriter sw=new StringWriter();
        TransformerFactory tf=TransformerFactory.newInstance();
        Transformer t=tf.newTransformer();
        DOMSource src=new DOMSource(doc);
        StreamResult res=new StreamResult(sw);
        t.transform(src,res); 
        return sw.toString();
    }


}
