package org.pustefixframework.util.xml;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

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
	
	/**
	 * Returns all direct child elements
	 * 
	 * @param parent - the parent element
	 * @return a list of all child elements
	 */
	public static List<Element> getChildElements(Element parent) {
		List<Element> elements = new ArrayList<Element>();
		NodeList nodes = parent.getChildNodes();
		for(int i=0; i<nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if(node.getNodeType() == Node.ELEMENT_NODE) {
				Element element = (Element)node;
				elements.add(element);
			}
		}
		return elements;
	}
	

	/**
	 * Adds whitespace text nodes to a DOM fragment
	 * required for indentation and line breaks 
	 * 
	 * @param element - the root element of the DOM fragment
	 * @param startIndent - the indentation for the root element
	 * @param incIndent - the additional indentation per element level
	 */
	public static void format(Element element, int startIndent, int incIndent) {
		
		if(element.getOwnerDocument().getDocumentElement() != element) {
			Node previous = element.getPreviousSibling();
			if(previous != null) {
				if(previous.getNodeType() == Node.TEXT_NODE) {
					String str = getTextContent(previous);
					int pos = str.lastIndexOf('\n');
					if(pos > -1) {
						str = str.substring(pos + 1);
						if(str.trim().equals("")) {
							int restIndent = startIndent;
							if(str.length() <= startIndent) restIndent = startIndent - str.length();
							Text text = element.getOwnerDocument().createTextNode(createIndent(restIndent));
							element.getParentNode().insertBefore(text, element);
						} else {
							Text text = element.getOwnerDocument().createTextNode("\n" + createIndent(startIndent));
							element.getParentNode().insertBefore(text, element);
						}
					} else {
						Text text = element.getOwnerDocument().createTextNode("\n" + createIndent(startIndent));
						element.getParentNode().insertBefore(text, element);
					}
				} else if(previous.getNodeType() == Node.ELEMENT_NODE) {
					Text text = element.getOwnerDocument().createTextNode("\n" + createIndent(startIndent));
					element.getParentNode().insertBefore(text, element);
				}
			} else {
				Text text = element.getOwnerDocument().createTextNode("\n" + createIndent(startIndent));
				element.getParentNode().insertBefore(text, element);
			}
		}
		
		List<Element> childElements = DOMUtils.getChildElements(element);
		for(Element child: childElements) {
			format(child, startIndent + incIndent, incIndent);
		}
		
		Node lastChild = element.getLastChild();
		if(lastChild != null) {
			if(lastChild.getNodeType() == Node.TEXT_NODE) {
				String str = getTextContent(lastChild);
				int pos = str.lastIndexOf('\n');
				if(pos > -1) {
					str = str.substring(pos + 1);
					if(str.trim().equals("")) {
						int restIndent = startIndent - str.length();
						Text text = element.getOwnerDocument().createTextNode(createIndent(restIndent));
						element.appendChild(text);
					} else {
						Text text = element.getOwnerDocument().createTextNode("\n" + createIndent(startIndent));
						element.appendChild(text);
					}
				} 
			} else if(lastChild.getNodeType() == Node.ELEMENT_NODE) {
				Text text = element.getOwnerDocument().createTextNode("\n" + createIndent(startIndent));
				element.appendChild(text);
			}
		}
		
		if(element.getOwnerDocument().getDocumentElement() != element) {
			Element parent = (Element)element.getParentNode();
			Node lastParentChild = parent.getLastChild();
			if(lastParentChild != null) {
				if(lastParentChild.getNodeType() == Node.TEXT_NODE) {
					String str = getTextContent(lastParentChild);
					int pos = str.lastIndexOf('\n');
					if(pos > -1) {
						str = str.substring(pos + 1);
						if(str.trim().equals("")) {
							int restIndent = startIndent - incIndent - str.length();
							Text text = parent.getOwnerDocument().createTextNode(createIndent(restIndent));
							parent.appendChild(text);
						} else {
							Text text = parent.getOwnerDocument().createTextNode("\n" + createIndent(startIndent - incIndent));
							parent.appendChild(text);
						}
					} 
				} else if(lastParentChild.getNodeType() == Node.ELEMENT_NODE) {
					Text text = parent.getOwnerDocument().createTextNode("\n" + createIndent(startIndent - incIndent));
					parent.appendChild(text);
				}
			}
		}
	}
	
	private static String createIndent(int indent) {
		String indentStr = "";
		for(int ind = 0; ind < indent; ind++) indentStr += " ";
		return indentStr;
	}
	
	private static String getTextContent(Node node) {
		String text = "";
		if(node != null && node.getNodeType() == Node.TEXT_NODE) {
			text = getTextContent(node.getPreviousSibling()) + ((Text)node).getTextContent();
		}
		return text;
	}
	
}
