package org.pustefixframework.util.xml;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DOMUtils {
    
    public static List<Element> getChildElements(Element parent) {
        List<Element> elems = new ArrayList<Element>();
        NodeList nodes = parent.getChildNodes();
        for(int i=0; i<nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if(node.getNodeType() == Node.ELEMENT_NODE) {
                elems.add((Element)node);
            }
        }
        return elems;
    }
    
    public static List<Element> getChildElementsByTagName(Element parent, String tagName) {
        List<Element> elems = new ArrayList<Element>();
        NodeList nodes = parent.getChildNodes();
        for(int i=0; i<nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if(node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().equals(tagName)) {
                elems.add((Element)node);
            }
        }
        return elems;
    }
    
    public static List<Element> getChildElementsByLocalName(Element parent, String localName) {
        List<Element> elems = new ArrayList<Element>();
        NodeList nodes = parent.getChildNodes();
        for(int i=0; i<nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if(node.getNodeType() == Node.ELEMENT_NODE && node.getLocalName().equals(localName)) {
                elems.add((Element)node);
            }
        }
        return elems;
    }
    
    public static Element getFirstChildByTagName(Element parent, String tagName) {
        NodeList nodes = parent.getChildNodes();
        for(int i=0; i<nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if(node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().equals(tagName)) {
                return (Element)node;
            }
        }
        return null;
    }
    
    public static List<Element> getChildElementsByTagNameNS(Element parent, String namespaceURI, String localName) {
        List<Element> elems = new ArrayList<Element>();
        NodeList nodes = parent.getChildNodes();
        for(int i=0; i<nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if(node.getNodeType() == Node.ELEMENT_NODE && 
                    node.getNamespaceURI().equals(namespaceURI) && node.getLocalName().equals(localName)) {
                elems.add((Element)node);
            }
        }
        return elems;
    }
    
    public static Element cloneAndRename(Element element, String newName) {
        Element newElem = element.getOwnerDocument().createElement(newName);
        NamedNodeMap attrs = element.getAttributes();
        for(int i=0; i<attrs.getLength(); i++) {
            newElem.setAttribute(attrs.item(i).getNodeName(), attrs.item(i).getNodeValue());
        }
        NodeList nodes = element.getChildNodes();
        for(int i=0; i<nodes.getLength(); i++) {
            Node cloned = newElem.getOwnerDocument().importNode(nodes.item(i), true);
            newElem.appendChild(cloned);
        }
        return newElem;
    }

}
