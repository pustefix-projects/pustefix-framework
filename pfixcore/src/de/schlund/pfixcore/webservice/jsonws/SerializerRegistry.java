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

package de.schlund.pfixcore.webservice.jsonws;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.schlund.pfixcore.webservice.beans.BeanDescriptorFactory;
import de.schlund.pfixcore.webservice.jsonws.serializers.ArraySerializer;
import de.schlund.pfixcore.webservice.jsonws.serializers.BeanSerializer;
import de.schlund.pfixcore.webservice.jsonws.serializers.BooleanSerializer;
import de.schlund.pfixcore.webservice.jsonws.serializers.CalendarSerializer;
import de.schlund.pfixcore.webservice.jsonws.serializers.ListSerializer;
import de.schlund.pfixcore.webservice.jsonws.serializers.MapSerializer;
import de.schlund.pfixcore.webservice.jsonws.serializers.NumberSerializer;
import de.schlund.pfixcore.webservice.jsonws.serializers.StringSerializer;

/**
 * @author mleidig@schlund.de
 */
public class SerializerRegistry {

    Map<Class,Serializer> serializers;
    BeanSerializer beanSerializer;
    ArraySerializer arraySerializer;
    ListSerializer listSerializer;
    MapSerializer mapSerializer;
    
    public SerializerRegistry(BeanDescriptorFactory beanDescFactory) {
        
        serializers=new HashMap<Class,Serializer>();
        
        beanSerializer=new BeanSerializer(beanDescFactory);
        arraySerializer=new ArraySerializer();
        listSerializer=new ListSerializer();
        mapSerializer=new MapSerializer();
        
        serializers.put(String.class,new StringSerializer());
        Serializer ser=new NumberSerializer();
        serializers.put(Byte.class,ser);
        serializers.put(Short.class,ser);
        serializers.put(Integer.class,ser);
        serializers.put(Long.class,ser);
        serializers.put(Float.class,ser);
        serializers.put(Double.class,ser);
        serializers.put(Boolean.class,new BooleanSerializer());
        ser=new CalendarSerializer();
        serializers.put(Calendar.class,ser);
        serializers.put(GregorianCalendar.class,ser);
        serializers.put(Date.class,ser);
      
    }
    
    public Serializer getSerializer(Class clazz) {
        Serializer ser=serializers.get(clazz);
        if(ser==null) {
            if(clazz.isArray()) ser=arraySerializer;
            else if(List.class.isAssignableFrom(clazz)) ser=listSerializer;
            else if(Map.class.isAssignableFrom(clazz)) ser=mapSerializer;
            else ser=beanSerializer;
        }
        return ser;
    }
    
    public Serializer getSerializer(Type type) {
        Class clazz=null;
        if(type instanceof Class) clazz=(Class)type;
        else if(type instanceof ParameterizedType) {
            Type rawType=((ParameterizedType)type).getRawType();
            if(rawType instanceof Class) clazz=(Class)rawType;
        }
        if(clazz!=null) return getSerializer(clazz);
        else throw new RuntimeException("Type not supported: "+type.getClass()+" "+type);
    }
    
}
