package de.schlund.pfixxml.xpath;

//import org.apache.xpath.XPathAPI;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *  <b>PFXPathEvaluatorImplXalan</b> evaluates XPath-expressions with the Xalan-XSLT processor.
 *  @author <a href="mailto:haecker@schlund.de">Joerg Haecker</a>
 */
public class PFXPathEvaluatorImplXalan implements PFXPathEvaluator {

    /**
     * @see de.schlund.pfixxml.PFXPathEvaluator#evaluate(Document, String)
     */
    public NodeList evaluate(Node node, String expression) throws Exception {
        NodeList nl = XPathAPI.selectNodeList(node, expression);
        
        return nl;
    }

}
