package org.pustefixframework.test;

import java.util.HashSet;
import java.util.Set;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * Assertions for XML/DOM nodes, which can be used within JUnit tests.
 * 
 * 
 * @author mleidig@schlund.de
 *
 */
public class XmlAssert {
    
    /**
     * Compare expected and actual node in-depth and throw AssertionError if a
     * difference is found.
     */
    public static void assertEquals(Node expected, Node actual) throws AssertionError {
        if (expected == null && actual == null) return;

        int expNodeType = expected.getNodeType();
        int actNodeType = actual.getNodeType();
        if (expNodeType != actNodeType) fail("Different node type.", getXPath(expected), getXPath(actual));
        if (expNodeType == actNodeType) {
            if (expNodeType == Node.ATTRIBUTE_NODE) {
                String expNodeName = expected.getNodeName();
                String actNodeName = actual.getNodeName();
                if (!expNodeName.equals(actNodeName)) fail("Different attribute node name.", expected, actual);
            } else if (expNodeType == Node.ELEMENT_NODE) {
                Element expElem = (Element) expected;
                Element actElem = (Element) actual;
                String expNodeName = expected.getNodeName();
                String actNodeName = actual.getNodeName();
                if (!expNodeName.equals(actNodeName)) fail("Different element node name.", expected, actual);
                NamedNodeMap expAttrMap = expElem.getAttributes();
                NamedNodeMap actAttrMap = actElem.getAttributes();
                for (int i = 0; i < expAttrMap.getLength(); i++) {
                    Attr expAttr = (Attr) expAttrMap.item(i);
                    Attr actAttr = (Attr) actAttrMap.getNamedItem(expAttr.getName());
                    if (actAttr == null) fail("Missing attribute.", getXPath(expAttr), null);
                    String expVal = expAttr.getValue();
                    String actVal = actAttr.getValue();
                    if (!expVal.equals(actVal))
                        fail("Different attribute values", getXPath(expAttr) + "='" + expVal + "'", getXPath(actAttr) + "='" + actVal + "'");
                }
                for (int i = 0; i < actAttrMap.getLength(); i++) {
                    Attr actAttr = (Attr) actAttrMap.item(i);
                    Attr expAttr = (Attr) expAttrMap.getNamedItem(actAttr.getName());
                    if (expAttr == null) fail("Additional attribute.", null, getXPath(actAttr));
                }
                NodeList expChildren = expected.getChildNodes();
                NodeList actChildren = actual.getChildNodes();
                if (expChildren.getLength() != actChildren.getLength())
                    fail("Different number of children.", "count(" + getXPath(expected) + "/child::node())=" + expected.getChildNodes().getLength(),
                            "count(" + getXPath(actual) + "/child::node())=" + actual.getChildNodes().getLength());
                for (int i = 0; i < expChildren.getLength(); i++) {
                    assertEquals(expChildren.item(i), actChildren.item(i));
                }
            } else if (expNodeType == Node.TEXT_NODE) {
                Text expTextNode = (Text) expected;
                Text actTextNode = (Text) actual;
                String expText = expTextNode.getTextContent();
                String actText = actTextNode.getTextContent();
                if (!expText.equals(actText))
                    fail("Different text content.", getXPath(expected) + "='" + getTextContent(expTextNode, 20) + "'", getXPath(actual) + "='"
                            + getTextContent(actTextNode, 20) + "'");
            } else if (expNodeType == Node.DOCUMENT_NODE) {
                Document expDoc = (Document) expected;
                Document actDoc = (Document) actual;
                Element expElem = expDoc.getDocumentElement();
                Element actElem = actDoc.getDocumentElement();
                assertEquals(expElem, actElem);
            }
        }
    }
    
    /**
     * Compare expected and actual node in-depth permitting arbitrarily ordered
     * child elements and throw AssertionError if a difference is found.
     */
    public static void assertEqualsUnordered(Node expected, Node actual) throws AssertionError {
        if (expected == null && actual == null) return;
        int expNodeType = expected.getNodeType();
        int actNodeType = actual.getNodeType();
        if (expNodeType != actNodeType) fail("Different node type.", getXPath(expected), getXPath(actual));
        if (expNodeType == actNodeType) {
            if (expNodeType == Node.ATTRIBUTE_NODE) {
                String expNodeName = expected.getNodeName();
                String actNodeName = actual.getNodeName();
                if (!expNodeName.equals(actNodeName)) fail("Different attribute node name.", expected, actual);
            } else if (expNodeType == Node.ELEMENT_NODE) {
                Element expElem = (Element) expected;
                Element actElem = (Element) actual;
                String expNodeName = expected.getNodeName();
                String actNodeName = actual.getNodeName();
                if (!expNodeName.equals(actNodeName)) fail("Different element node name.", expected, actual);
                NamedNodeMap expAttrMap = expElem.getAttributes();
                NamedNodeMap actAttrMap = actElem.getAttributes();
                for (int i = 0; i < expAttrMap.getLength(); i++) {
                    Attr expAttr = (Attr) expAttrMap.item(i);
                    Attr actAttr = (Attr) actAttrMap.getNamedItem(expAttr.getName());
                    if (actAttr == null) fail("Missing attribute.", getXPath(expAttr), null);
                    String expVal = expAttr.getValue();
                    String actVal = actAttr.getValue();
                    if (!expVal.equals(actVal))
                        fail("Different attribute values", getXPath(expAttr) + "='" + expVal + "'", getXPath(actAttr) + "='" + actVal + "'");
                }
                for (int i = 0; i < actAttrMap.getLength(); i++) {
                    Attr actAttr = (Attr) actAttrMap.item(i);
                    Attr expAttr = (Attr) expAttrMap.getNamedItem(actAttr.getName());
                    if (expAttr == null) fail("Additional attribute.", null, getXPath(actAttr));
                }
                NodeList expChildren = expected.getChildNodes();
                NodeList actChildren = actual.getChildNodes();
                if (expChildren.getLength() != actChildren.getLength())
                    fail("Different number of children.", "count(" + getXPath(expected) + "/child::node())=" + expected.getChildNodes().getLength(),
                            "count(" + getXPath(actual) + "/child::node())=" + actual.getChildNodes().getLength());
                Set<Node> processedNodes = new HashSet<Node>();
                for (int i = 0; i < expChildren.getLength(); i++) {
                    boolean found = false;
                    AssertionError deepestError = null;
                    for(int j = 0; !found && (j < actChildren.getLength()); j++) {
                        if(!processedNodes.contains(actChildren.item(j))) {
                            try {
                                assertEqualsUnordered(expChildren.item(i), actChildren.item(j));
                                processedNodes.add(actChildren.item(j));
                                found = true;
                            } catch(AssertionError error) {
                                if(deepestError==null || getPathDepth(deepestError)<getPathDepth(error)) {
                                    deepestError = error;
                                }
                            }
                        }
                    }
                    if(!found) {
                        throw deepestError;
                    }
                }
            } else if (expNodeType == Node.TEXT_NODE) {
                Text expTextNode = (Text) expected;
                Text actTextNode = (Text) actual;
                String expText = expTextNode.getTextContent();
                String actText = actTextNode.getTextContent();
                if (!expText.equals(actText))
                    fail("Different text content.", getXPath(expected) + "='" + getTextContent(expTextNode, 20) + "'", getXPath(actual) + "='"
                            + getTextContent(actTextNode, 20) + "'");
            } else if (expNodeType == Node.DOCUMENT_NODE) {
                Document expDoc = (Document) expected;
                Document actDoc = (Document) actual;
                Element expElem = expDoc.getDocumentElement();
                Element actElem = actDoc.getDocumentElement();
                assertEqualsUnordered(expElem, actElem);
            }
        }
    }
    
    private static void fail(String message, Node expected, Node actual) throws AssertionError {
        fail(message, getXPath(expected), getXPath(actual));
    }
    
    private static void fail(String message, String expected, String actual) throws AssertionError {
        StringBuilder sb = new StringBuilder();
        sb.append(message);
        sb.append(" Expected: \"");
        sb.append(expected);
        sb.append("\" but was: \"");
        sb.append(actual);
        sb.append("\"");
        throw new AssertionError(sb.toString());
    }
    
    private static String getTextContent(Text text, int maxLen) {
        String str = text.getTextContent();
        str = str.replaceAll("\n", "\\\\n");
        str = str.replaceAll("\r", "\\\\r");
        if (str.length() > maxLen) str = str.substring(0, maxLen - 3) + "...";
        return str;
    }

    private static int getPathDepth(AssertionError error) {
        String msg = error.getMessage();
        int cnt = 0;
        for(int i=0; i<msg.length(); i++) {
            if(msg.charAt(i)=='/') cnt++; 
        }
        return cnt;
    }
    
    /**
     * Create a XPath expression denoting the node's absolute position.
     */
    private static String getXPath(Node node) {
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
    
}
