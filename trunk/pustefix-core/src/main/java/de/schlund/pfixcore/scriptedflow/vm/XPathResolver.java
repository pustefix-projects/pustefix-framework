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

package de.schlund.pfixcore.scriptedflow.vm;




import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathVariableResolver;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import de.schlund.pfixxml.SPDocument;
import de.schlund.pfixxml.util.Xml;

/**
 * Used by the VM to evaluate XPath expressions for different instructions.  
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class XPathResolver {
    private class MyXPathVariableResolver implements XPathVariableResolver {
        public Object resolveVariable(QName variableName) {
            String value = null;
            if (variableName.getLocalPart().equals("__pagename") && spdoc != null) {
                value = spdoc.getPagename();
                if (value == null) {
                    return "";
                } else {
                    return value;
                }
            }
            
            if (params != null && variableName.getLocalPart().startsWith("__param_")) {
                value = params.get(variableName.getLocalPart().substring(8));
                if (value == null) {
                    return "";
                } else {
                    return value;
                }
            }
            
            if (vars != null) {
                value = vars.get(variableName.getLocalPart());
                if (value == null) {
                    return "";
                } else {
                    return value;
                }
            }
            
            return "";
        }
    }

    private XPath xpath;

    private MyXPathVariableResolver resolver;

    private SPDocument spdoc;

    private Map<String, String> params;

    private Map<String, String> vars;

    public XPathResolver() {
        resolver = new MyXPathVariableResolver();
        XPathFactory xpfac = XPathFactory.newInstance();
        xpfac.setXPathVariableResolver(resolver);
        xpath = xpfac.newXPath();
    }
    
    public void setSPDocument(SPDocument spdoc) {
        this.spdoc = spdoc;
    }
    
    public void setParams(Map<String, String> params) {
        this.params = params;
    }
    
    public void setVariables(Map<String, String> params) {
        this.vars = params;
    }
    
    public boolean evalXPathBoolean(String expr) {
        Document doc;
        if (spdoc == null) {
            doc = Xml.createDocument();
        } else {
            doc = spdoc.getDocument();
        }
        try {
            Boolean ret = (Boolean) xpath.evaluate(expr, doc,
                    XPathConstants.BOOLEAN);
            return ret.booleanValue();
        } catch (XPathExpressionException e) {
            throw new RuntimeException("Invalid XPath expression: \"" + expr
                    + "\"", e);
        }
    }

    public String evalXPathString(String expr) {
        Document doc;
        if (spdoc == null) {
            doc = Xml.createDocument();
        } else {
            doc = spdoc.getDocument();
        }
        try {
            String ret = (String) xpath.evaluate(expr, doc,
                    XPathConstants.STRING);
            return ret;
        } catch (XPathExpressionException e) {
            throw new RuntimeException("Invalid XPath expression: \"" + expr
                    + "\"", e);
        }
    }


    public Node evalXPathNode(String expr) {
        Document doc;
        if (spdoc == null) {
            doc = Xml.createDocument();
        } else {
            doc = spdoc.getDocument();
        }
        try {
            Node ret = (Node) xpath.evaluate(expr, doc, XPathConstants.NODE);
            return ret;
        } catch (XPathExpressionException e) {
            throw new RuntimeException("Invalid XPath expression: \"" + expr + "\"", e);
        }
    }

}
