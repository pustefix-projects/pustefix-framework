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
package org.pustefixframework.config.contextxmlservice.parser;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.marsching.flexiparse.parser.exception.ParserException;

/**
 * Helper class to set simple bean-style properties at configuration object. 
 * 
 * @author mleidig
 *
 */
public class PropertyParsingUtils {
    
    public static void setProperty(Object targetObject, String propName, String propValue) throws ParserException {
        String setter = "set" + Character.toUpperCase(propName.charAt(0)) + propName.substring(1);
        boolean found = false;
        Method[] methods = targetObject.getClass().getMethods();
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
                            method.invoke(targetObject, param);
                            found = true;
                            break;
                        }
                    } catch (NoSuchMethodException x) {
                        // String constructor not found, continue search
                    } catch (Exception x) {
                        throw new ParserException("Can't set property '" + propName + "' at instance of class '" + 
                                targetObject.getClass().getName() + "'.", x);
                    }
                }
            }
        }
        if (!found)
            throw new IllegalArgumentException("Can't find appropriate setter method for property '" + propName + "' in class '"
                    + targetObject.getClass().getName() + "'. Currently only property types with String "
                    + "constructor or primitive types with according wrapper types are supported.");
    }
    
    public static void setProperties(Object targetObject, Element propertyParentElement) throws ParserException {
        NodeList propElems = propertyParentElement.getElementsByTagName("property");
        for(int i=0; i<propElems.getLength(); i++) {
            Element propElem = (Element)propElems.item(i);
            String propName = propElem.getAttribute("name").trim();
            if(propName.equals("")) throw new ParserException("Element 'property' requires 'name' attribute value.");
            String propValue = propElem.getAttribute("value").trim();
            if(propValue.equals("")) throw new ParserException("Element 'property' requires 'value' attribute value.");
            PropertyParsingUtils.setProperty(targetObject, propName, propValue);
        }
    }

}
