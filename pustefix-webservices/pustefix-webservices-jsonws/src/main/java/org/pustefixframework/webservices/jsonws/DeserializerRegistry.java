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

package org.pustefixframework.webservices.jsonws;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pustefixframework.webservices.jsonws.deserializers.ArrayDeserializer;
import org.pustefixframework.webservices.jsonws.deserializers.BeanDeserializer;
import org.pustefixframework.webservices.jsonws.deserializers.BooleanDeserializer;
import org.pustefixframework.webservices.jsonws.deserializers.CalendarDeserializer;
import org.pustefixframework.webservices.jsonws.deserializers.NumberDeserializer;
import org.pustefixframework.webservices.jsonws.deserializers.StringDeserializer;

import de.schlund.pfixcore.beans.BeanDescriptorFactory;

/**
 * @author mleidig@schlund.de
 */
public class DeserializerRegistry {

    Map<Class<?>,Deserializer> deserializers;
    Deserializer beanDeserializer;
    Deserializer arrayDeserializer;
    
    public DeserializerRegistry(BeanDescriptorFactory beanDescFactory) {
        
        deserializers=new HashMap<Class<?>,Deserializer>();
        
        beanDeserializer=new BeanDeserializer(beanDescFactory);
        arrayDeserializer=new ArrayDeserializer();
        
        deserializers.put(String.class,new StringDeserializer());
        Deserializer deser=new NumberDeserializer();
        deserializers.put(byte.class,deser);
        deserializers.put(Byte.class,deser);
        deserializers.put(short.class,deser);
        deserializers.put(Short.class,deser);
        deserializers.put(int.class,deser);
        deserializers.put(Integer.class,deser);
        deserializers.put(long.class,deser);
        deserializers.put(Long.class,deser);
        deserializers.put(float.class,deser);
        deserializers.put(Float.class,deser);
        deserializers.put(double.class,deser);
        deserializers.put(Double.class,deser);
        deser=new BooleanDeserializer();
        deserializers.put(boolean.class,deser);
        deserializers.put(Boolean.class,deser);
        deser=new CalendarDeserializer();
        deserializers.put(Calendar.class,deser);
        deserializers.put(Date.class,deser);
        
    }
    
    public Deserializer getDeserializer(Class<?> clazz) {
        Deserializer deser=deserializers.get(clazz);
        if(deser==null) {
            if(clazz.isArray()) deser=arrayDeserializer; 
            else if(List.class.isAssignableFrom(clazz)) deser=arrayDeserializer;
            else deser=beanDeserializer;
        }
        return deser;
    }
    
    public Deserializer getDeserializer(Type type) {
        Class<?> clazz=null;
        if(type instanceof Class) clazz=(Class<?>)type;
        else if(type instanceof ParameterizedType) {
            Type rawType=((ParameterizedType)type).getRawType();
            if(rawType instanceof Class) clazz=(Class<?>)rawType;
        }
        if(clazz!=null) return getDeserializer(clazz);
        else throw new RuntimeException("Type not supported: "+type.getClass()+" "+type);
    }
    
}
