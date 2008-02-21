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

import java.util.List;

import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;


/**
 *  Evaluates XPath-expressions. 
 */
public class XPath {
    
    final static Logger LOG=Logger.getLogger(XPath.class);
    
    static XPathSupport defaultSupport=new XPathDefault();
       
    static XPathSupport getXPathSupport(Node context) throws TransformerException {
        for(XPathSupport xps:XsltProvider.getXpathSupport().values()) {
            if(xps.isModelSupported(context)) return xps;
        }
        return defaultSupport;
    }
    
    public static List<Node> select(Node context, String xpath) throws TransformerException {
        XPathSupport xps=getXPathSupport(context);
        return xps.select(context,xpath);
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
        List<Node> result;
        XPathSupport xps=getXPathSupport(context);
        result = xps.select(context,xpath);
        if (result.size() == 0) {
            return null;
        } else {
            return result.get(0);
        }
    }

    /** computes the 'effective boolean value' **/
    public static boolean test(Node context, String test) throws TransformerException {
        XPathSupport xps=getXPathSupport(context);
        return xps.test(context,test);
    }

}