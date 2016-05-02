package org.pustefixframework.ide.eclipse.plugin.util;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DOMUtils {

	public static List<Element> getElementsByTagAndAttr(Element parent,String elemName,String attrName,String attrVal) {
		List<Element> result=new ArrayList<Element>();
		NodeList nl=parent.getElementsByTagName(elemName);
		for(int i=0;i<nl.getLength();i++) {
			if(nl.item(i).getNodeType()==Node.ELEMENT_NODE) {
				Element elem=(Element)nl.item(i);
				String val=elem.getAttribute(attrName);
				if(val!=null && val.equals(attrVal)) result.add(elem);
			}
		}
		return result;
	}
	
	public static List<Element> getChildElementsByName(Element parent, String elemName) {
		List<Element> result = new ArrayList<Element>();
		NodeList nl = parent.getChildNodes();
		for(int i=0; i<nl.getLength(); i++) {
			if(nl.item(i).getNodeType()==Node.ELEMENT_NODE) {
				Element elem=(Element)nl.item(i);
				if(elem.getNodeName().equals(elemName)) result.add(elem);
			}
		}
		return result;
	}
	
	public static Element getChildElementByName(Element parent, String elemName) {
		NodeList nl = parent.getChildNodes();
		for(int i=0; i<nl.getLength(); i++) {
			if(nl.item(i).getNodeType()==Node.ELEMENT_NODE) {
				Element elem=(Element)nl.item(i);
				if(elem.getNodeName().equals(elemName)) return elem;
			}
		}
		return null;
	}
	
	public static void replaceTextPlaceHolders(Element element, String placeHolder, String replacement) {
        boolean hasSubElements = false;
        if(element.hasChildNodes()) {
            NodeList nodes = element.getChildNodes();
            for(int i=0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);
                if(node.getNodeType() == Node.ELEMENT_NODE) {
                    hasSubElements = true;
                    replaceTextPlaceHolders((Element)node, placeHolder, replacement);
                }
            }
        }
        if(!hasSubElements) {
            String text = element.getTextContent();
            if(text.contains(placeHolder)) {
                text = text.replace(placeHolder, replacement);
                element.setTextContent(text);
            }
        }
    }
	
    public static Document changeAttributes(Document domDoc, String tagName, String attribute, String newValue, boolean secNodeListItem) {
        NodeList nodeList = domDoc.getElementsByTagName(tagName);
        String myNodeName = null;
        Element element   = null;

        if (!secNodeListItem) {
        
            for (int i = 0; i < nodeList.getLength(); i++) {
                element = (Element)nodeList.item(i);
                myNodeName = element.getNodeName();            
                
                if (myNodeName.equals(tagName)) {
                    element.setAttribute(attribute, newValue);
                }
            }
        } else {
            element = (Element)nodeList.item(1);
            element.setAttribute(attribute, newValue);
        }
        return domDoc;
    }
}
