package de.schlund.pfixxml.util;

import java.util.List;
import javax.xml.transform.TransformerException;
import net.sf.saxon.dom.DocumentWrapper;
import net.sf.saxon.dom.NodeWrapper;
import net.sf.saxon.xpath.XPathEvaluator;
import org.w3c.dom.Document;
import org.w3c.dom.Node;


/**
 *  <b>PFXPathEvaluator</b> evaluates XPath-expressions. Currently saxon is used.
 *   @author <a href="mailto:haecker@schlund.de">Joerg Haecker</a>
 */
public final class XPath {
    public static List select(Node context, String xpath) throws TransformerException {
        List lst;
        XPathEvaluator evaluator;
        DocumentWrapper sourceWrapper;
        NodeWrapper contextWrapper;
        
        sourceWrapper = new DocumentWrapper(getDocument(context), "foo");
        contextWrapper = sourceWrapper.wrap(context);
        evaluator = new XPathEvaluator();
        evaluator.setSource(sourceWrapper);
        evaluator.setContextNode(contextWrapper);
        return evaluator.evaluate(xpath);
    }

    public static Node selectNode(Node context, String xpath) throws TransformerException {
        List result;
        
        result = select(context, xpath);
        if (result.size() == 0) {
            return null;
        } else {
            return (Node) result.get(0);
        }
    }

    /** computes the 'effective boolean value' **/
    public static boolean test(Node context, String test) throws TransformerException {
        List lst;
        
        lst = select(context, "boolean(" + test + ")");
        if (lst.size() != 1) {
            throw new RuntimeException();
        }
        return ((Boolean) lst.get(0)).booleanValue();
    }

    private static Document getDocument(Node node) {
        Document doc;
        
        if (node instanceof Document) {
            return (Document) node;        
        }
        doc = node.getOwnerDocument();
        if (doc == null) {
            throw new RuntimeException("unkown document");
        }
        return doc;
    }
}