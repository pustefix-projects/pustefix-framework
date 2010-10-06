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

package org.pustefixframework.config.customization;

import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathFunction;
import javax.xml.xpath.XPathFunctionException;
import javax.xml.xpath.XPathFunctionResolver;
import javax.xml.xpath.XPathVariableResolver;

import org.w3c.dom.Document;

import com.marsching.flexiparse.util.MapBasedNamespaceContext;


/**
 * {@link CustomizationInfo} implementation that uses a list of 
 * {@link Properties} to resolve variables.
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class PropertiesBasedCustomizationInfo implements CustomizationInfo {
    
    private class MyVariableResolver implements XPathVariableResolver {

        public Object resolveVariable(QName variableName) {
            return properties.getProperty(variableName.getLocalPart());
        }
        
    }
    
    private class MyFunctionResolver implements XPathFunctionResolver {

        public XPathFunction resolveFunction(QName functionName, int arity) {
            if (functionName.getNamespaceURI().equals("http://www.pustefix-framework.org/2008/namespace/xpath-functions") && functionName.getLocalPart().equalsIgnoreCase("isSet") && arity == 1) {
                return isSetFunction;
            } else {
                return null;
            }
        }
        
    }
    
    private class IsSetFunction implements XPathFunction {
        @SuppressWarnings("rawtypes")
        public Object evaluate(List args) throws XPathFunctionException {
            String varName = (String) args.get(0);
            return (properties.getProperty(varName) != null);
        }        
    }
    
    private XPathFunction isSetFunction = new IsSetFunction();
    private Properties properties;
    private NamespaceContext namespaceContext;
    private Document dummyDoc;
    private XPath xpath;
    
    /**
     * Creates a new customization info using the supplied 
     * {@link Properties} object to resolve variables.
     * 
     * @param properties contains variables and their values
     */
    public PropertiesBasedCustomizationInfo(Properties properties) {
        this.properties = properties;
        XPathFactory xpfac = XPathFactory.newInstance();
        xpfac.setXPathVariableResolver(new MyVariableResolver());
        xpfac.setXPathFunctionResolver(new MyFunctionResolver());
        HashMap<String, String> namespacePrefixes = new HashMap<String, String>();
        namespacePrefixes.put("pfx", "http://www.pustefix-framework.org/2008/namespace/xpath-functions");
        namespaceContext = new MapBasedNamespaceContext(namespacePrefixes);
        xpath = xpfac.newXPath();
        xpath.setNamespaceContext(namespaceContext);
        try {
            dummyDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException("Can't create dummy document for XPath evaluation", e);
        }
    }
    
    public Object evaluateXPathExpression(String expression, QName returnType) throws XPathExpressionException {
        return xpath.evaluate(expression, dummyDoc, returnType);
    }
    
    public boolean evaluateXPathExpression(String expression) throws XPathExpressionException {
        return (Boolean)evaluateXPathExpression(expression, XPathConstants.BOOLEAN);
    }

    public String replaceVariables(String expression) {
        StringBuffer str = new StringBuffer(expression);
        StringBuffer result = new StringBuffer();
        
        while (str.length() > 0) {
            char c = str.charAt(0);
            if (c == '$') {
                if (str.length() >= 2 && str.charAt(1) == '$') {
                    result.append('$');
                    str.delete(0, 2);
                } else if (str.length() >= 2 && str.charAt(1) == '{') {
                    int end = str.indexOf("}");
                    if (end == -1) {
                        throw new IllegalArgumentException("Expression \"" + expression + "\" contains unended variable reference");
                    }
                    String name = str.substring(2, end);
                    String value = resolveVariable(name);
                    if (value == null) {
                        value = "";
                    }
                    result.append(value);
                    str.delete(0, end + 1);
                } else {
                    result.append('$');
                    str.deleteCharAt(0);
                }
            } else {
                result.append(c);
                str.deleteCharAt(0);
            }
        }
        
        return result.toString();
    }

    public String resolveVariable(String name) {
        return properties.getProperty(name);
    }
    
    public Properties getProperties() {
        return properties;
    }

}
