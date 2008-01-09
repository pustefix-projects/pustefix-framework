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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import de.schlund.pfixcore.generator.IWrapper;
import de.schlund.pfixxml.config.GlobalConfigurator;

/**
 * 
 * @author mleidig@schlund.de
 *
 */
public class IWrapperToBean {

    private static Map<Class<?>, BeanDescriptor> descriptors = new HashMap<Class<?>, BeanDescriptor>();

    private static synchronized <T> BeanDescriptor getBeanDescriptor(Class<T> clazz) {
        BeanDescriptor desc = descriptors.get(clazz);
        if (desc == null) {
            desc = new BeanDescriptor(clazz);
            descriptors.put(clazz, desc);
        }
        return desc;
    }

    public static <T> T createBean(IWrapper wrapper, Class<T> beanClass) {
        try {
            T instance = beanClass.newInstance();
            populateBean(wrapper, instance);
            return instance;
        } catch (Exception x) {
            throw new RuntimeException("Can't instantiate bean: " + beanClass.getName(), x);
        }
    }

    public static void populateBean(IWrapper wrapper, Object obj) {
        Class<?> clazz = obj.getClass();
        BeanDescriptor beanDesc = getBeanDescriptor(clazz);
        BeanDescriptor wrapperBeanDesc = getBeanDescriptor(wrapper.getClass());
        Set<String> properties = wrapperBeanDesc.getProperties();
        for (String property : properties) {
            String getterName = createGetterName(property);
            Method getter = null;
            try {
                getter = wrapper.getClass().getMethod(getterName, new Class[0]);
            } catch (NoSuchMethodException x) {
                throw new RuntimeException("Can't find getter: " + getterName, x);
            }
            Object res = null;
            try {
                res = getter.invoke(wrapper, new Object[0]);
            } catch (Exception x) {
                throw new RuntimeException("Can't get property: " + property, x);
            }
            Method setter = beanDesc.getSetMethod(property);
            if (setter == null) {
                Field field = null;
                field = beanDesc.getDirectAccessField(property);
                if (field == null) throw new RuntimeException("Can't find bean property: " + property);
                try {
                    field.set(obj, res);
                } catch (Exception x) {
                    throw new RuntimeException("Can't set property: " + property, x);
                }
            } else {
                try {
                    setter.invoke(obj, res);
                } catch (Exception x) {
                    throw new RuntimeException("Can't set property: " + property, x);
                }
            }
        }
    }

    private static String createGetterName(String propName) {
        return "get" + Character.toUpperCase(propName.charAt(0)) + propName.substring(1);
    }

}
