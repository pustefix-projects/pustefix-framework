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
package de.schlund.pfixcore.oxm.impl.serializers;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import de.schlund.pfixcore.beans.BeanDescriptor;
import de.schlund.pfixcore.beans.BeanDescriptorFactory;
import de.schlund.pfixcore.oxm.impl.AnnotationAware;
import de.schlund.pfixcore.oxm.impl.ComplexTypeSerializer;
import de.schlund.pfixcore.oxm.impl.SerializationContext;
import de.schlund.pfixcore.oxm.impl.SimpleTypeSerializer;
import de.schlund.pfixcore.oxm.impl.XMLWriter;

/**
 * @author mleidig@schlund.de
 * @author Stephan Schmidt <schst@stubbles.net>
 */
public class BeanSerializer implements ComplexTypeSerializer {

    BeanDescriptorFactory beanDescFactory;
    Map<Class<?>, Map<String, SimpleTypeSerializer>> customSimpleSerializerCache;
    Map<Class<?>, Map<String, ComplexTypeSerializer>> customComplexSerializerCache;

    public BeanSerializer(BeanDescriptorFactory beanDescFactory) {
        this.beanDescFactory = beanDescFactory;
        customSimpleSerializerCache = new HashMap<Class<?>, Map<String, SimpleTypeSerializer>>();
        customComplexSerializerCache = new HashMap<Class<?>, Map<String, ComplexTypeSerializer>>();
    }

    public void serialize(Object obj, SerializationContext ctx, XMLWriter writer) {
        BeanDescriptor bd = beanDescFactory.getBeanDescriptor(obj.getClass());
        readCustomSerializers(obj.getClass(), bd);
        Set<String> props = bd.getReadableProperties();
        Iterator<String> it = props.iterator();
        while (it.hasNext()) {
            String prop = it.next();
            try {
                Object val = null;
                Method meth = bd.getGetMethod(prop);
                if (meth != null) {
                    val = meth.invoke(obj, new Object[0]);
                } else {
                    Field field = bd.getDirectAccessField(prop);
                    if (field != null) val = field.get(obj);
                    else throw new RuntimeException("Bean of type '" + obj.getClass().getName() + "' doesn't "
                            + " have getter method or direct access to property '" + prop + "'.");
                }
                if (val != null) {
                    SimpleTypeSerializer simpleSerializer = getCustomSimpleTypeSerializer(obj.getClass(), prop);
                    if (simpleSerializer != null) {
                        String value = ctx.serialize(val, simpleSerializer);
                        ComplexTypeSerializer complexSerializer = getCustomComplexTypeSerializer(obj.getClass(), prop);
                        if (complexSerializer != null) {
                            writer.writeStartElement(prop);
                            ctx.serialize(value, writer, complexSerializer);
                            writer.writeEndElement(prop);
                        } else {
                            writer.writeAttribute(prop, value);
                        }
                    } else {
                        ComplexTypeSerializer complexSerializer = getCustomComplexTypeSerializer(obj.getClass(), prop);
                        if (complexSerializer != null) {
                            writer.writeStartElement(prop);
                            ctx.serialize(val, writer, complexSerializer);
                            writer.writeEndElement(prop);
                        } else if (ctx.hasSimpleTypeSerializer(val.getClass())) {
                            writer.writeAttribute(prop, ctx.serialize(val));
                        } else {
                            writer.writeStartElement(prop);
                            ctx.serialize(val, writer);
                            writer.writeEndElement(prop);
                        }
                    }
                }
            } catch (Exception x) {
                throw new RuntimeException("Error during serialization.", x);
            }
        }
    }

    private SimpleTypeSerializer getCustomSimpleTypeSerializer(Class<?> clazz, String prop) {
        Map<String, SimpleTypeSerializer> simpleSerializers = customSimpleSerializerCache.get(clazz);
        if (simpleSerializers != null && !simpleSerializers.isEmpty()) {
            return simpleSerializers.get(prop);
        }
        return null;
    }

    private ComplexTypeSerializer getCustomComplexTypeSerializer(Class<?> clazz, String prop) {
        Map<String, ComplexTypeSerializer> complexSerializers = customComplexSerializerCache.get(clazz);
        if (complexSerializers != null && !complexSerializers.isEmpty()) {
            return complexSerializers.get(prop);
        }
        return null;
    }

    private void readCustomSerializers(Class<?> clazz, BeanDescriptor beanDesc) {
        if (customSimpleSerializerCache.containsKey(clazz)) return;
        Map<String, SimpleTypeSerializer> simpleSerializers = new HashMap<String, SimpleTypeSerializer>();
        Map<String, ComplexTypeSerializer> complexSerializers = new HashMap<String, ComplexTypeSerializer>();
        Set<String> props = beanDesc.getReadableProperties();
        Iterator<String> it = props.iterator();
        while (it.hasNext()) {
            String prop = it.next();
            try {
                Method meth = beanDesc.getGetMethod(prop);
                if (meth != null) {
                    Annotation[] annos = meth.getAnnotations();
                    for (Annotation anno : annos) {
                        if (anno.annotationType().isAnnotationPresent(de.schlund.pfixcore.oxm.impl.annotation.SimpleTypeSerializerClass.class)) {
                            de.schlund.pfixcore.oxm.impl.annotation.SimpleTypeSerializerClass serAnno = anno.annotationType().getAnnotation(
                                    de.schlund.pfixcore.oxm.impl.annotation.SimpleTypeSerializerClass.class);
                            Class<? extends SimpleTypeSerializer> serClass = serAnno.value();
                            SimpleTypeSerializer serializer = serClass.newInstance();
                            if (AnnotationAware.class.isAssignableFrom(serClass)) {
                                AnnotationAware aa = (AnnotationAware) serializer;
                                aa.setAnnotation(anno);
                            }
                            simpleSerializers.put(prop, serializer);
                        } else if (anno.annotationType()
                                .isAnnotationPresent(de.schlund.pfixcore.oxm.impl.annotation.ComplexTypeSerializerClass.class)) {
                            de.schlund.pfixcore.oxm.impl.annotation.ComplexTypeSerializerClass serAnno = anno.annotationType().getAnnotation(
                                    de.schlund.pfixcore.oxm.impl.annotation.ComplexTypeSerializerClass.class);
                            Class<? extends ComplexTypeSerializer> serClass = serAnno.value();
                            ComplexTypeSerializer serializer = serClass.newInstance();
                            if (AnnotationAware.class.isAssignableFrom(serClass)) {
                                AnnotationAware aa = (AnnotationAware) serializer;
                                aa.setAnnotation(anno);
                            }
                            complexSerializers.put(prop, serializer);
                        }
                    }
                }
                Field field = beanDesc.getDirectAccessField(prop);
                if (field != null) {
                    Annotation[] annos = field.getAnnotations();
                    for (Annotation anno : annos) {
                        if (anno.annotationType().isAnnotationPresent(de.schlund.pfixcore.oxm.impl.annotation.SimpleTypeSerializerClass.class)) {
                            de.schlund.pfixcore.oxm.impl.annotation.SimpleTypeSerializerClass serAnno = anno.annotationType().getAnnotation(
                                    de.schlund.pfixcore.oxm.impl.annotation.SimpleTypeSerializerClass.class);
                            Class<? extends SimpleTypeSerializer> serClass = serAnno.value();
                            SimpleTypeSerializer serializer = serClass.newInstance();
                            if (AnnotationAware.class.isAssignableFrom(serClass)) {
                                AnnotationAware aa = (AnnotationAware) serializer;
                                aa.setAnnotation(anno);
                            }
                            simpleSerializers.put(prop, serializer);
                        } else if (anno.annotationType()
                                .isAnnotationPresent(de.schlund.pfixcore.oxm.impl.annotation.ComplexTypeSerializerClass.class)) {
                            de.schlund.pfixcore.oxm.impl.annotation.ComplexTypeSerializerClass serAnno = anno.annotationType().getAnnotation(
                                    de.schlund.pfixcore.oxm.impl.annotation.ComplexTypeSerializerClass.class);
                            Class<? extends ComplexTypeSerializer> serClass = serAnno.value();
                            ComplexTypeSerializer serializer = serClass.newInstance();
                            if (AnnotationAware.class.isAssignableFrom(serClass)) {
                                AnnotationAware aa = (AnnotationAware) serializer;
                                aa.setAnnotation(anno);
                            }
                            complexSerializers.put(prop, serializer);
                        }
                    }
                }
            } catch (Exception x) {
                throw new RuntimeException("Error during serialization.", x);
            }
        }
        customSimpleSerializerCache.put(clazz, simpleSerializers);
        customComplexSerializerCache.put(clazz, complexSerializers);
    }

}
