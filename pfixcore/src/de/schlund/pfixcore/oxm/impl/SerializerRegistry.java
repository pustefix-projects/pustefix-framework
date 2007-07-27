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
package de.schlund.pfixcore.oxm.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.schlund.pfixcore.beans.BeanDescriptorFactory;
import de.schlund.pfixcore.oxm.impl.serializers.ArraySerializer;
import de.schlund.pfixcore.oxm.impl.serializers.BeanSerializer;
import de.schlund.pfixcore.oxm.impl.serializers.CollectionSerializer;
import de.schlund.pfixcore.oxm.impl.serializers.MapSerializer;
import de.schlund.pfixcore.oxm.impl.serializers.ObjectToStringSerializer;

/**
 * @author mleidig@schlund.de
 */
public class SerializerRegistry {

    Map<Class<?>,ComplexTypeSerializer> serializers;
    Map<Class<?>,SimpleTypeSerializer> simpleSerializers;
    
    ArraySerializer arraySerializer;
    BeanSerializer beanSerializer;
    CollectionSerializer collSerializer;
    MapSerializer mapSerializer;
    
    public SerializerRegistry(BeanDescriptorFactory beanDescFactory) {
        
        serializers=new HashMap<Class<?>,ComplexTypeSerializer>();
        simpleSerializers=new HashMap<Class<?>,SimpleTypeSerializer>();
        
        arraySerializer=new ArraySerializer();
        beanSerializer=new BeanSerializer(beanDescFactory);
        collSerializer=new CollectionSerializer();
        mapSerializer=new MapSerializer();
        
        SimpleTypeSerializer ser=new ObjectToStringSerializer();
        simpleSerializers.put(String.class,ser);
        simpleSerializers.put(Boolean.class,ser);
        simpleSerializers.put(Byte.class,ser);
        simpleSerializers.put(Short.class,ser);
        simpleSerializers.put(Integer.class,ser);
        simpleSerializers.put(Long.class,ser);
        simpleSerializers.put(Float.class,ser);
        simpleSerializers.put(Double.class,ser);
        
    }
    
    public ComplexTypeSerializer getSerializer(Class<?> clazz) {
        ComplexTypeSerializer serializer=serializers.get(clazz);
        if(serializer==null) {
            if(clazz.isArray()) serializer=arraySerializer;
            else if(List.class.isAssignableFrom(clazz)) serializer=collSerializer;
            else if(Map.class.isAssignableFrom(clazz)) serializer=mapSerializer;
            else serializer=beanSerializer;
        }
        return serializer;
    }
    
    public SimpleTypeSerializer getSimpleTypeSerializer(Class<?> clazz) {
        SimpleTypeSerializer serializer=simpleSerializers.get(clazz);
        return serializer;
    }
    
}
