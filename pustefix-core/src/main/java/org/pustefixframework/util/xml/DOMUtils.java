package org.pustefixframework.util.xml;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Helper class providing methods simplifying frequently used DOM operations.
 * 
 * @author mleidig@schlund.de
 *
 */
public class DOMUtils {

	/**
	 * Returns the first found direct child element with the specified node name.
	 * 
	 * @param parent - the parent element
	 * @param nodeName - the node name of the searched child element
	 * @return the first found child element or null if no matching element is found
	 */
	public static Element getChildElementByTagName(Element parent, String nodeName) {
		NodeList nodes = parent.getChildNodes();
		for(int i=0; i<nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if(node.getNodeType() == Node.ELEMENT_NODE) {
				Element element = (Element)node;
				if(element.getNodeName().equals(nodeName)) {
					return element;
				}
			}
		}
		return null;
	}
	
	/**
	 * Returns all direct child elements with the specified node name
	 * 
	 * @param parent - the parent element
	 * @param nodeName - the node name of the searched child elements 
	 * @return a list of all matching child elements or an empty list if none was found
	 */
	public static List<Element> getChildElementsByTagName(Element parent, String localName) {
		List<Element> elements = new ArrayList<Element>();
		NodeList nodes = parent.getChildNodes();
		for(int i=0; i<nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if(node.getNodeType() == Node.ELEMENT_NODE) {
				Element element = (Element)node;
				if(element.getNodeName().equals(localName)) {
					elements.add(element);
				}
			}
		}
		return elements;
	}
	
}
