package org.pustefixframework.config.customization;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.exception.ParserException;

/**
 * Provides some utility methods for working with DOM's containing
 * Pustefix's choose/when customization tags.
 *
 */
public class CustomizationDOMUtils {
    
    public static List<Element> getChildElementsByLocalName(HandlerContext handlerContext, String localName) throws ParserException {
        
        Element parent =  (Element)handlerContext.getNode();
        Collection<CustomizationInfo> infoCollection = handlerContext.getObjectTreeElement().getObjectsOfTypeFromTopTree(CustomizationInfo.class);
        if (infoCollection.isEmpty()) {
            throw new ParserException("Could not find instance of CustomizationInfo");
        }
        CustomizationInfo info = infoCollection.iterator().next();
        return getChildElementsByLocalName(parent, localName, info);
    }
        
    public static List<Element> getChildElementsByLocalName(Element rootElem, String localName, CustomizationInfo info) throws ParserException {
        
        List<Element> resultElems = new ArrayList<Element>();
        NodeList rootSubNodes = rootElem.getChildNodes();
        for(int i=0; i<rootSubNodes.getLength(); i++) {
            Node rootSubNode = rootSubNodes.item(i);
            if(rootSubNode.getNodeType() == Node.ELEMENT_NODE) {
                Element rootSubElem = (Element)rootSubNode;
                if(rootSubElem.getLocalName().equals("choose")) {
                    NodeList childNodes = rootSubElem.getChildNodes();
                    if(childNodes != null) {
                        for(int childCnt = 0; childCnt<childNodes.getLength(); childCnt++) {
                            Node childNode = childNodes.item(childCnt);
                            if(childNode.getNodeType() == Node.ELEMENT_NODE) {
                                Element childElem = (Element)childNode;
                                if(childElem.getLocalName().equals("when")) {
                                    String expression = childElem.getAttribute("test");
                                    try {
                                        boolean test = (Boolean)info.evaluateXPathExpression(expression, XPathConstants.BOOLEAN);
                                        if(test) {
                                            resultElems.addAll(getChildElementsByLocalName(childElem, localName, info));
                                            break;
                                        }
                                    } catch (XPathExpressionException e) {
                                        throw new ParserException("Error while evaluating XPath expression \"" + expression + "\": " + e.getMessage(), e);
                                    } 
                                } else if(childElem.getLocalName().equals("otherwise")) {
                                    resultElems.addAll(getChildElementsByLocalName(childElem, localName, info));
                                    break;
                                }
                            }
                        }
                    }
                    
                } else if(rootSubElem.getLocalName().equals(localName)) {
                    resultElems.add(rootSubElem);   
                }
            }
        }
        return resultElems;
    }

}
