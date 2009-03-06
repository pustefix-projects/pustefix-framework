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
package de.schlund.pfixcore.generator.iwrpgen;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.schlund.pfixcore.generator.IWrapper;

/**
 * Helper class to populate IWrapper instances with data from Java beans.
 * 
 * @author mleidig@schlund.de
 */
public class BeanToIWrapper {

    private static Map<Class<?>, BeanDescriptor> descriptors = new HashMap<Class<?>, BeanDescriptor>();

    private static synchronized <T> BeanDescriptor getBeanDescriptor(Class<T> clazz) {
        BeanDescriptor desc = descriptors.get(clazz);
        if (desc == null) {
            desc = new BeanDescriptor(clazz);
            descriptors.put(clazz, desc);
        }
        return desc;
    }

    /**
     * Populates an IWrapper instance with data from a Java bean.
     * 
     * @param obj -
     *            the Java bean to populate the IWrapper
     * @param wrapper -
     *            the IWrapper to be populated
     */
    public static void populateIWrapper(Object obj, IWrapper wrapper) {
        Class<?> clazz = obj.getClass();
        BeanDescriptor beanDesc = getBeanDescriptor(clazz);
        BeanDescriptor wrapperBeanDesc = getBeanDescriptor(wrapper.getClass());
        Set<String> properties = wrapperBeanDesc.getProperties();
        for (String property : properties) {
            Object res = null;
            Method getter = beanDesc.getGetMethod(property);
            if (getter != null) {
                try {
                    res = getter.invoke(obj, new Object[0]);
                } catch (Exception x) {
                    throw new RuntimeException("Can't get property: " + property, x);
                }
            } else {
                Field field = beanDesc.getDirectAccessField(property);
                if (field == null) throw new RuntimeException("Can't find bean property: " + property);
                try {
                    res = field.get(obj);
                } catch (Exception x) {
                    throw new RuntimeException("Can't get property: " + property, x);
                }
            }
            Method setter = null;
            try {
                setter = wrapperBeanDesc.getSetMethod(property);
                Type type = beanDesc.getPropertyType(property);
                if (res != null && isList(type)) {
                    Class<?> compType = null;
                    Class<?>[] paramTypes = setter.getParameterTypes();
                    if (paramTypes.length == 1 && paramTypes[0].isArray()) compType = paramTypes[0].getComponentType();
                    if (compType == null) throw new RuntimeException("Can't get array component type for property: " + property);
                    List<?> list = (List<?>) res;
                    Object array = Array.newInstance(compType, list.size());
                    for (int ind = 0; ind < list.size(); ind++) {
                        Object item = list.get(ind);
                        Array.set(array, ind, item);
                    }
                    res = array;
                }
                setter.invoke(wrapper, res);
            } catch (Exception x) {
                throw new RuntimeException("Can't set param: " + property, x);
            }
        }
    }

    private static boolean isList(Type type) {
        if (type instanceof ParameterizedType) {
            ParameterizedType ptype = (ParameterizedType) type;
            Type rawType = ptype.getRawType();
            if (rawType instanceof Class) {
                Class<?> c = (Class<?>) rawType;
                if (List.class.isAssignableFrom(c)) return true;
            }
        }
        return false;
    }

}
