/*
 * This file is part of Pustefix.
 *
 * Pustefix is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Pustefix is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Pustefix; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */

package de.schlund.pfixxml.util.xsltimpl;

import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.w3c.dom.Node;

import com.icl.saxon.Context;
import com.icl.saxon.expr.Expression;
import com.icl.saxon.expr.NodeSetValue;
import com.icl.saxon.expr.StandaloneContext;
import com.icl.saxon.om.NodeEnumeration;
import com.icl.saxon.om.NodeInfo;

import de.schlund.pfixxml.util.XPathSupport;

/**
 * @author mleidig@schlund.de
 */
public class XPathSaxon1 implements XPathSupport {

    public boolean isModelSupported(Node node) {
        if(node instanceof NodeInfo) return true;
        return false;
    }
    
    public List<Node> select(Node context, String xpath) throws TransformerException {
        List<Node> result;
        result = new ArrayList<Node>();
        NodeInfo   cNode   = (NodeInfo) context;
        Expression exp     = Expression.make(xpath, new StandaloneContext());
        Context    ctx = new Context();
        ctx.setContextNode(cNode);
        ctx.setPosition(1);
        ctx.setLast(1);
        NodeSetValue nodeSet = exp.evaluateAsNodeSet(ctx);
        NodeEnumeration enm = nodeSet.enumerate();
        while (enm.hasMoreElements()) {
            result.add((Node)enm.nextElement());
        }
        return result;
    }
    
    public boolean test(Node context, String test) throws TransformerException {
        NodeInfo   cNode   = (NodeInfo) context;
        Expression exp     = Expression.make(test, new StandaloneContext());
        Context    ctx = new Context();
        ctx.setContextNode(cNode);
        ctx.setPosition(1);
        ctx.setLast(1);
        return exp.evaluateAsBoolean(ctx);
    }
    
}
