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
package de.schlund.pfixxml.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeIterator;

import com.icl.saxon.Context;
import com.icl.saxon.expr.Expression;
import com.icl.saxon.expr.NodeSetValue;
import com.icl.saxon.expr.StandaloneContext;
import com.icl.saxon.om.NodeEnumeration;
import com.icl.saxon.om.NodeInfo;

import de.schlund.pfixxml.FactoryInitServlet;


/**
 *  Evaluates XPath-expressions. Some ugly reflection stuff to work with jdk 1.4 and 1.5.
 */
public class XPath {
    // TODO: use java.xml.xpath if we've completed transition to java 1.5 ...
    public static List select(Node context, String xpath) throws TransformerException {
        // TODO: xalan cannot evaluable saxon trees an vice versa ...

        if (context instanceof NodeInfo) {
            return saxonSelect(context, xpath);
    	} else {
            return xalanSelect(context, xpath);
    	}
    }

    public static Node selectOne(Node context, String xpath) throws TransformerException {
        Node node;

        node = selectNode(context, xpath);
        if (node == null) {
            throw new TransformerException("xpath '" + xpath + "' not found in "
                    + context.getClass().getName() + " " + Xml.serialize(context, true, false));
        }
        return node;
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
        if (context instanceof NodeInfo) {
        	NodeInfo   cNode   = (NodeInfo) context;
        	Expression exp     = Expression.make(test, new StandaloneContext());
        	Context    ctx = new Context();
        	ctx.setContextNode(cNode);
        	ctx.setPosition(1);
        	ctx.setLast(1);
        	return exp.evaluateAsBoolean(ctx);
        } else {
            return xalanTest(context, test);
        }
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
    
    //-- xalan xpath
    
    //--

    private static List saxonSelect(Node context, String xpath) throws TransformerException {
        List result;
        result = new ArrayList();
        NodeInfo   cNode   = (NodeInfo) context;
        Expression exp     = Expression.make(xpath, new StandaloneContext());
        Context    ctx = new Context();
        ctx.setContextNode(cNode);
        ctx.setPosition(1);
        ctx.setLast(1);
        NodeSetValue nodeSet = exp.evaluateAsNodeSet(ctx);
        NodeEnumeration enm = nodeSet.enumerate();
        while (enm.hasMoreElements()) {
            result.add(enm.nextElement());
        }
        return result;
        
    }
    /**
     * @return &lt;{@link Node}&gt;List
     */
    public static List xalanSelect(Node context, String xpath) throws TransformerException {
        List result;
        NodeIterator iter;
        Node node;

        result = new ArrayList();
        iter = (NodeIterator) doInvoke(SELECT, context, xpath);
        while (true) {
            node = iter.nextNode();
            if (node == null) {
                break;
            }
            result.add(node);
        }
        return result;
    }

    //-- jdk 1.4/1.5 switch

    private static final Method SELECT;
    private static final Method EVAL;
    private static final Method BOOL;

    static {
        Class cls;
        Class xobject;
        
        try {
            // jdk 1.5
            cls = Class.forName("com.sun.org.apache.xpath.internal.XPathAPI");
            xobject = Class.forName("com.sun.org.apache.xpath.internal.objects.XObject");
        } catch (ClassNotFoundException e) {
            try {
                // jdk 1.4
                cls = Class.forName("org.apache.xpath.XPathAPI");
                xobject = Class.forName("org.apache.xpath.internal.objects.XObject");
            } catch (ClassNotFoundException f) {
                throw new RuntimeException(f);
            }
        }
        try {
            SELECT = cls.getMethod("selectNodeIterator", new Class[] { Node.class, String.class });
            EVAL = cls.getMethod("eval", new Class[] { Node.class, String.class });
            BOOL = xobject.getMethod("bool", FactoryInitServlet.NO_CLASSES);
        } catch (SecurityException g) {
            throw new RuntimeException(g);
        } catch (NoSuchMethodException g) {
            throw new RuntimeException(g);
        }
    }

    public static boolean xalanTest(Node context, String test) throws TransformerException {
        Object xobject;
        
        xobject = doInvoke(EVAL, context, test);
        return ((Boolean) doInvoke(BOOL, xobject, FactoryInitServlet.NO_OBJECTS)).booleanValue();
    }
    
    private static Object doInvoke(Method meth, Node node, String xpath) throws TransformerException {
        return doInvoke(meth, null, new Object[] { node, xpath });
    }
    
    private static Object doInvoke(Method meth, Object obj, Object[] args) throws TransformerException {
        Throwable t;

        try {
            return meth.invoke(obj, args);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            t = e.getTargetException();
            if (t instanceof TransformerException) {
                throw (TransformerException) t;
            } else {
                throw new RuntimeException(t);
            }
        }
    }
}