package de.schlund.pfixxml.xpath;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *  <b>PFXPathEvaluator</b> defines functionality for evaluation XPath expression.
 *  Different XSLT processor can be used by writing warpper classes
 *  which implement this interface.
 *  @author <a href="mailto:haecker@schlund.de">Joerg Haecker</a>
 */
public interface PFXPathEvaluator {

    /**
     *  Evaluate an XPath expression.
     */

    public NodeList evaluate(Node node, String expression) throws Exception;
   
}
