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

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.Currency;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import de.schlund.pfixcore.beans.BeanDescriptor;
import de.schlund.pfixcore.beans.BeanDescriptorFactory;
import de.schlund.pfixcore.oxm.impl.serializers.ArraySerializer;
import de.schlund.pfixcore.oxm.impl.serializers.BeanSerializer;
import de.schlund.pfixcore.oxm.impl.serializers.ClassSerializer;
import de.schlund.pfixcore.oxm.impl.serializers.CollectionSerializer;
import de.schlund.pfixcore.oxm.impl.serializers.ComplexEnumSerializer;
import de.schlund.pfixcore.oxm.impl.serializers.DateSerializer;
import de.schlund.pfixcore.oxm.impl.serializers.MapSerializer;
import de.schlund.pfixcore.oxm.impl.serializers.ObjectToStringSerializer;
import de.schlund.pfixcore.oxm.impl.serializers.PropertiesSerializer;
import de.schlund.pfixcore.oxm.impl.serializers.SimpleEnumSerializer;

/**
 * @author mleidig@schlund.de
 */
public class SerializerRegistry {

    BeanDescriptorFactory beanDescFactory;
    ClassNameMapping classNameMapping;

    Map<Class<?>, ComplexTypeSerializer> complexSerializers;
    Map<Class<?>, SimpleTypeSerializer> simpleSerializers;

    ArraySerializer arraySerializer;
    BeanSerializer beanSerializer;
    CollectionSerializer collSerializer;
    MapSerializer mapSerializer;
    SimpleEnumSerializer simpleEnumSerializer;
    ComplexEnumSerializer complexEnumSerializer;

    public SerializerRegistry(BeanDescriptorFactory beanDescFactory) {

        this.beanDescFactory = beanDescFactory;
        classNameMapping = new ClassNameMapping();

        complexSerializers = new HashMap<Class<?>, ComplexTypeSerializer>();
        simpleSerializers = new HashMap<Class<?>, SimpleTypeSerializer>();

        arraySerializer = new ArraySerializer();
        beanSerializer = new BeanSerializer(beanDescFactory);
        collSerializer = new CollectionSerializer();
        mapSerializer = new MapSerializer();
        simpleEnumSerializer = new SimpleEnumSerializer();
        complexEnumSerializer = new ComplexEnumSerializer(beanDescFactory);

        SimpleTypeSerializer ser = new ObjectToStringSerializer();
        simpleSerializers.put(String.class, ser);
        simpleSerializers.put(Character.class, ser);
        simpleSerializers.put(Boolean.class, ser);
        simpleSerializers.put(Byte.class, ser);
        simpleSerializers.put(Short.class, ser);
        simpleSerializers.put(Integer.class, ser);
        simpleSerializers.put(Long.class, ser);
        simpleSerializers.put(Float.class, ser);
        simpleSerializers.put(Double.class, ser);
        simpleSerializers.put(Currency.class, ser);
        simpleSerializers.put(URL.class, ser);
        simpleSerializers.put(URI.class, ser);
        simpleSerializers.put(File.class, ser);

        ser = new DateSerializer();
        simpleSerializers.put(Date.class, ser);
        simpleSerializers.put(GregorianCalendar.class, ser);

        simpleSerializers.put(Class.class, new ClassSerializer());

        complexSerializers.put(Properties.class, new PropertiesSerializer());

    }

    public ComplexTypeSerializer getSerializer(Class<?> clazz) {
        ComplexTypeSerializer serializer = complexSerializers.get(clazz);
        if (serializer == null) {
            if (clazz.isArray()) serializer = arraySerializer;
            else if (List.class.isAssignableFrom(clazz)) serializer = collSerializer;
            else if (Map.class.isAssignableFrom(clazz)) serializer = mapSerializer;
            else if (Enum.class.isAssignableFrom(clazz)) serializer = complexEnumSerializer;
            else serializer = beanSerializer;
        }
        return serializer;
    }

    public SimpleTypeSerializer getSimpleTypeSerializer(Class<?> clazz) {
        SimpleTypeSerializer serializer = simpleSerializers.get(clazz);
        if (Enum.class.isAssignableFrom(clazz)) {
            BeanDescriptor beanDesc = beanDescFactory.getBeanDescriptor(clazz);
            if (beanDesc.getReadableProperties().isEmpty()) serializer = simpleEnumSerializer;
        }
        return serializer;
    }

    public String mapClassName(Object obj) {
        if (obj == null) return "null";
        return classNameMapping.mapClassName(obj.getClass());
    }

}
