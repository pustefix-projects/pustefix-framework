package de.schlund.pfixxml.xpath;

import com.icl.saxon.Context;
import com.icl.saxon.expr.Expression;
import com.icl.saxon.expr.NodeSetValue;
import com.icl.saxon.expr.StandaloneContext;
import com.icl.saxon.om.NodeInfo;
import org.apache.log4j.Category;
import org.w3c.dom.Node;


/**
 *  <b>PFXPathEvaluator</b> evaluates XPath-expressions. Currently saxon is used.
 *   @author <a href="mailto:haecker@schlund.de">Joerg Haecker</a>
 */
public final class PFXPathEvaluator {

    //~ Instance/static variables ..................................................................

    private static Category CAT = Category.getInstance(PFXPathEvaluator.class.getName());

    //~ Methods ....................................................................................

    /**
     * Evaluate an xpath expression and return its result.
     * Note: Only documents that implement saxons NodeInfo interface can currently be
     * passed. Attempts to pass others documents will cause an exception.
     * @param expression the xpath expression to be evaluated
     * @param contextNode the context on which the xpath expression will work on
     * @return the result of the evaluation in form of a NodeSetValue
     * @throws exception throws exception when passing wrong document or on all errors
     */
    public static final NodeSetValue evaluateAsNodeSetValue(String expression, Node contextNode)
                                                     throws Exception {
        if (! (contextNode instanceof NodeInfo)) {
            StringBuffer sb = new StringBuffer(100);
            sb.append("Given Node is not an instance of NodeInfo! It's a ").append(contextNode.getClass().getName());
            throw new Exception(sb.toString());
        }
        NodeInfo   cNode   = (NodeInfo) contextNode;
        Expression exp     = Expression.make(expression, new StandaloneContext());
        Context    context = new Context();
        context.setContextNode(cNode);
        context.setPosition(1);
        context.setLast(1);
        NodeSetValue nodeSet = exp.evaluateAsNodeSet(context);
        return nodeSet;
    }
}// end of class PFXPathEvaluator