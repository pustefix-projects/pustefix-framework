package de.schlund.pfixxml.xpath;


import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.icl.saxon.Context;
import com.icl.saxon.expr.Expression;
import com.icl.saxon.expr.NodeSetExtent;
import com.icl.saxon.expr.NodeSetValue;
import com.icl.saxon.expr.StandaloneContext;
import com.icl.saxon.om.NamePool;
import com.icl.saxon.om.NodeInfo;

/**
 *  <b>PFXPathEvaluatorImplSaxon</b> evaluates XPath-expressions with Saxon-XSLT processor.
 *   @author <a href="mailto:haecker@schlund.de">Joerg Haecker</a>
 */
public class PFXPathEvaluatorImplSaxon implements PFXPathEvaluator {

    /**
     * @see de.schlund.pfixxml.PFXPathEvaluator#evaluate(Document, String)
     */
    public NodeList evaluate(Node node, String expression) throws Exception {
        Context context = new Context();
        context.setContextNode((NodeInfo)node);
        NamePool npool = NamePool.getDefaultNamePool();
        StandaloneContext stcontext = new StandaloneContext(npool);
        Expression expr = Expression.make(expression, stcontext);
        NodeSetValue nodeset= expr.evaluateAsNodeSet(context);
        NodeSetExtent set = new NodeSetExtent(nodeset.enumerate(), null);
        return set;
    }

}
