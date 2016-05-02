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

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;


/**
 * Provides information about customization options. An instance of this
 * type is usually attached to the root node of a configuration tree prior
 * to the start of the actual parsing process. This object can be used
 * in order to get information about compile-time options.
 *  
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public interface CustomizationInfo {
    
    /**
     * Compiles an XPath expression using a variable resolver and optionally
     * a function resolver that provides information about compile-time 
     * options. 
     * 
     * @param expression a valid XPath expression
     * @param returnType one of the return types defined in {@link XPathConstants}
     * @return result object whose type is determined by the returnType parameter
     */
    Object evaluateXPathExpression(String expression, QName returnType) throws XPathExpressionException;
    
    /**
     * Performs a lookup for the specified variable name and returns the 
     * corresponding compile-time value if defined.
     * 
     * @param name name of the variable which is looked up
     * @return compile-time value of the variable or <code>null</code> if
     *   no variable with this name is defined
     */
    String resolveVariable(String name);
    
    /**
     * Replaces variables in the form ${variable-name} occuring within
     * the given string by their values. Not existing variables are replaced
     * with the empty string.
     * 
     * @param expression string containing variables that should be replaced
     * @return string with resolved variables
     */
    String replaceVariables(String expression);
    
}
