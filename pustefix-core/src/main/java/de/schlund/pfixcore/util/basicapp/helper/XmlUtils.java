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

package de.schlund.pfixcore.util.basicapp.helper;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * Like @see de.schlund.pfixcore.util.basicapp.helper.StringUtils 
 * there also exists a XmlUtils Object with static methods 
 * (it's this one ;o) to convert the current dom 
 * 
 * @author <a href="mailto:rapude@schlund.de">Ralf Rapude</a>
 * @version $Id$
 */
public class XmlUtils {
    
    /**
     * Changing the attribute of a document in order to apply
     * the changes made by the user
     * @param domDoc The current document
     * @param tagName The name of the tag changing the related attribute
     * @param attribute The attribtute to change
     * @param secNodeListItem
     * @param docName The name of the document in order to
     * avoid problems if a node occurs in different files with the
     * same name
     */
    public static Document changeAttributes(Document domDoc, String tagName, String attribute, String newValue, boolean secNodeListItem) {
        System.out.println("Changing the attribute " + attribute);
        
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
        
        System.out.println("Changing attribute has been successfull");
        return domDoc;
    }
    
    
    /**
     * Changing the text nodes of the config files
     * @param domDoc a Document containing the dom
     * @param tagName the tag to change
     * @param value The nodes new content
     */
    public static Document changeTextValues(Document domDoc, String tagName,
            String value, boolean firstChild) {
        System.out.println("Changing the value of the tag " + tagName);
        
        NodeList nodeList = domDoc.getElementsByTagName(tagName);
        String myNodeName = null;
        Node oldValue     = null;
        Node newValue     = domDoc.createTextNode(value);
        
        // running through the doms nodes
        for (int i = 0; i < nodeList.getLength(); i++) {
            Element element   = (Element)nodeList.item(i);
            myNodeName        = element.getNodeName();
            
            // pick the right one out
            if (myNodeName.equals(tagName)) {               
                // firstChild is a boolean given as an argument
                if (firstChild) {                    
                    if (element.getFirstChild() != null) {
                        oldValue = element.getFirstChild();
                    } else {
                        oldValue = element.getNextSibling();
                    }                    
                } else {
                    oldValue = element.getLastChild();
                }
                element.replaceChild(newValue, oldValue);
            }
            
        }
        System.out.println("Changing Textnode has been successfull\n");
        return domDoc;
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
    
}
