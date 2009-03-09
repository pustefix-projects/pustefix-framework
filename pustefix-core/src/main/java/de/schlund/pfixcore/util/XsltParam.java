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
package de.schlund.pfixcore.util;

import org.apache.tools.ant.BuildException;

/**
 * @author adam
 *
 * Class used to store XSL parameters specified as nested 
 * <code>&lt;param&gt;</code>-Elements to the Xslt-Tasks.
 */
public class XsltParam {

    /** The parameter name */
    private String name;

    /** The parameter's XSL expression */
    private String expression;

    public XsltParam() {
        this(null, null);
    }
    
    public XsltParam(String name, String expression) {
        setName(name);
        setExpression(expression);
    }
    
    /**
     * Set the parameter name.
     *
     * @param name the name of the parameter.
     */
    public void setName(String name){
        this.name = name;
    }

    /**
     * The XSL expression for the parameter value
     *
     * @param expression the XSL expression representing the
     *   parameter's value.
     */
    public void setExpression(String expression){
        this.expression = expression;
    }

    /**
     * Get the parameter name
     *
     * @return the parameter name
     * @exception BuildException if the name is not set.
     */
    public String getName() throws BuildException{
        if (name == null) {
            throw new BuildException("Name attribute is missing.");
        }
        return name;
    }

    /**
     * Get the parameter expression
     *
     * @return the parameter expression
     * @exception BuildException if the expression is not set.
     */
    public String getExpression() throws BuildException {
        if (expression == null) {
            throw new BuildException("Expression attribute is missing.");
        }
        return expression;
    }
    

    public boolean equals(XsltParam that) {
        if (that == null) {
            return false;
        }
        if (this == that) {
            return true;
        }
        if (name == null) {
            if (that.name != null) {
                return false;
            }
        } else {
            if (name.equals(that.name) == false) {
                return false;
            }
        }
        if (expression == null) {
            if (that.expression != null) {
                return false;
            }
        } else {
            if (expression.equals(that.expression) == false) {
                return false;
            }
        }
        return true;
    }

    
    
    @Override
    public String toString() {
        return shortClassname(getClass().getName())+"[name="+name+"; expression="+expression+"]";
    }
    
    //
    //  Helper methods
    //

    /**
     * @return classname without package prefix
     */
    public static String shortClassname(String classname) {
        try {
            int idx = classname.lastIndexOf('.');
            if (idx >= 0) {
                classname = classname.substring(idx + 1, classname.length());
            }
        } catch (IndexOutOfBoundsException e) {
            // This should never happen
            e.printStackTrace();
        }
        return classname;
    }
    
}
