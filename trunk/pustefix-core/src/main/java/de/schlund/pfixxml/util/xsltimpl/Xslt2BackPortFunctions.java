package de.schlund.pfixxml.util.xsltimpl;

import com.icl.saxon.Context;
import com.icl.saxon.expr.XPathException;
import com.icl.saxon.om.Axis;
import com.icl.saxon.om.Builder;
import com.icl.saxon.om.DocumentInfo;
import com.icl.saxon.om.NamePool;
import com.icl.saxon.om.NodeEnumeration;
import com.icl.saxon.pattern.AnyNodeTest;
import com.icl.saxon.tree.AttributeCollection;

import de.schlund.pfixxml.util.ExtensionFunctionUtils;

/**
 * Some useful extensions function not available until XSLT 2 "backported" to XSLT 1
 *
 */
public class Xslt2BackPortFunctions {

    public static boolean endsWith(String str, String end) {
        return str.endsWith(end);
    }

    public static String upperCase(String str) {
        return str.toUpperCase();
    }

    public static String lowerCase(String str) {
        return str.toLowerCase();
    }

    public static NodeEnumeration tokenize(Context context, String str, String pattern) throws XPathException {
        try {
            Builder builder = context.getController().makeBuilder();
            NamePool pool = context.getController().getNamePool();
            builder.setNamePool(pool);
            builder.startDocument();
            int name = pool.allocate("", "", "token");
            AttributeCollection emptyAtts = new AttributeCollection(pool);
            String[] tokens = str.split(pattern);
            for(String token: tokens) {
                builder.startElement(name, emptyAtts, new int[0], 0);
                builder.characters(token.toCharArray(), 0, token.length());
                builder.endElement(name);
            }
            builder.endDocument();
            DocumentInfo doc = builder.getCurrentDocument();
            return doc.getEnumeration(Axis.CHILD, AnyNodeTest.getInstance());
        } catch (Exception err) {
            ExtensionFunctionUtils.setExtensionFunctionError(err);
            throw new XPathException(err);
        }
    }

}
