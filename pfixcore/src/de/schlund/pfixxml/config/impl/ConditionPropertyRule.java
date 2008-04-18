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
 */

package de.schlund.pfixxml.config.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.xml.sax.Attributes;

import de.schlund.pfixcore.auth.Condition;

/**
 * @author mleidig@schlund.de
 */
public class ConditionPropertyRule extends CheckedRule {

    ContextXMLServletConfigImpl config;

    public ConditionPropertyRule(ContextXMLServletConfigImpl config) {
        this.config = config;
    }

    public void begin(String namespace, String name, Attributes attributes) throws Exception {
        check(namespace, name, attributes);
        String propName = attributes.getValue("name");
        String propValue = attributes.getValue("value");
        Object obj = getDigester().peek();
        if (obj instanceof Condition) {
            setProperty(obj, propName, propValue);
        } else throw new Exception("Illegal object: " + obj.getClass().getName());
    }

    public void end(String namespace, String name) throws Exception {
    }

    protected Map<String, Boolean> wantsAttributes() {
        HashMap<String, Boolean> atts = new HashMap<String, Boolean>();
        atts.put("name", true);
        atts.put("value", true);
        return atts;
    }

    private void setProperty(Object obj, String propName, String propValue) {
        String setter = "set" + Character.toUpperCase(propName.charAt(0)) + propName.substring(1);
        boolean found = false;
        Method[] methods = obj.getClass().getMethods();
        for (Method method : methods) {
            if (method.getName().equals(setter)) {
                Class<?>[] paramTypes = method.getParameterTypes();
                if (paramTypes.length == 1) {
                    Class<?> paramType = paramTypes[0];
                    if (paramType.isPrimitive()) {
                        if (paramType == boolean.class) paramType = Boolean.class;
                        else if (paramType == short.class) paramType = Short.class;
                        else if (paramType == int.class) paramType = Integer.class;
                        else if (paramType == long.class) paramType = Long.class;
                        else if (paramType == float.class) paramType = Float.class;
                        else if (paramType == double.class) paramType = Double.class;
                        else if (paramType == byte.class) paramType = Byte.class;
                    }
                    try {
                        Constructor<?> con = paramType.getConstructor(new Class[] { String.class });
                        if (con != null) {
                            Object param = con.newInstance(propValue);
                            method.invoke(obj, param);
                            found = true;
                            break;
                        }
                    } catch (NoSuchMethodException x) {
                        // String constructor not found, continue search
                    } catch (Exception x) {
                        throw new RuntimeException("Can't set property '" + propName + "' at instance of class '" + obj.getClass().getName() + "'.",
                                x);
                    }
                }
            }
        }
        if (!found)
            throw new IllegalArgumentException("Can't find appropriate setter method for property '" + propName + "' in class '"
                    + obj.getClass().getName() + "'. Currently only property types with String "
                    + "constructor or primitive types with according wrapper types are supported.");
    }

}
