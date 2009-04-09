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
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathFactoryConfigurationException;

import net.sf.saxon.Configuration;
import net.sf.saxon.dom.NodeOverNodeInfo;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.xpath.XPathEvaluator;

import org.w3c.dom.Node;

import de.schlund.pfixxml.util.XPathSupport;

/**
 * @author mleidig@schlund.de
 */
public class XPathSaxon2 implements XPathSupport {

    public boolean isModelSupported(Node node) {
        return NodeOverNodeInfo.class.isAssignableFrom(node.getClass());
    }
    
    public List<Node> select(Node context, String xpath) throws TransformerException {
        try {
            XPathEvaluator xpe=createXPath(context);
            List<Node> nodeList=new ArrayList<Node>();
            List<?> list=(List<?>)xpe.evaluate(xpath,context,XPathConstants.NODESET);
            for(Object obj:list) {
                Item item=(Item)obj;
                if(item instanceof NodeInfo) nodeList.add(NodeOverNodeInfo.wrap((NodeInfo)item));
            }
            return nodeList;
        } catch(XPathException x) {
            throw new TransformerException("XPath error",x);
        }
    }
    
    public boolean test(Node context, String test) throws TransformerException {
        try {
            XPathEvaluator xpe=createXPath(context);
            Boolean res=(Boolean)xpe.evaluate(test,context,XPathConstants.BOOLEAN);
            return res.booleanValue();
        } catch(XPathException x) {
            throw new TransformerException("XPath error",x);
        }
    }
    
    private XPathEvaluator createXPath(Node context) throws XPathFactoryConfigurationException {
        NodeOverNodeInfo info=(NodeOverNodeInfo)context;
        Configuration config=info.getUnderlyingNodeInfo().getConfiguration();
        XPathEvaluator xpe=new XPathEvaluator(config);
        return xpe;
    }
    
}
