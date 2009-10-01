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
 */

package de.schlund.pfixxml.util;

import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathFactoryConfigurationException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author mleidig@schlund.de
 */
public class XPathDefault implements XPathSupport {

    XPathFactory xpathFactory;
    
    public XPathDefault() {
    	xpathFactory = XPathFactory.newInstance();
    }
    
    public boolean isModelSupported(Node node) {
        return true;
    }
    
    public List<Node> select(Node context, String xpath) throws TransformerException {
        NodeList nodes=selectNodes(context,xpath);
        List<Node> nodeList=new ArrayList<Node>();
        for(int i=0;i<nodes.getLength();i++) {
            nodeList.add(nodes.item(i));
        }
        return nodeList;
    }
 
    public boolean test(Node context, String test) throws TransformerException {
        try {
            javax.xml.xpath.XPath xp=createXPath();
            Boolean res=(Boolean)xp.evaluate(test,context,XPathConstants.BOOLEAN);
            return res.booleanValue();
        } catch(XPathException x) {
            throw new TransformerException("XPath error",x);
        }
    }
    
   
    private  NodeList selectNodes(Node context, String xpath) throws TransformerException {
        try {
            javax.xml.xpath.XPath xp=createXPath();
            NodeList res=(NodeList)xp.evaluate(xpath,context,XPathConstants.NODESET);
            return res;
        } catch(XPathException x) {
            throw new TransformerException("XPath error",x);
        }
    }
    
    private javax.xml.xpath.XPath createXPath() throws XPathFactoryConfigurationException {
        synchronized(xpathFactory) {
            return xpathFactory.newXPath();
        }
    }
    
}
