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

package org.pustefixframework.webservices.jsonws.serializers;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Set;

import org.pustefixframework.webservices.json.JSONObject;
import org.pustefixframework.webservices.json.JSONValue;
import org.pustefixframework.webservices.jsonws.SerializationContext;
import org.pustefixframework.webservices.jsonws.SerializationException;
import org.pustefixframework.webservices.jsonws.Serializer;

import de.schlund.pfixcore.beans.BeanDescriptor;
import de.schlund.pfixcore.beans.BeanDescriptorFactory;

/**
 * @author mleidig@schlund.de
 */
public class BeanSerializer extends Serializer {

    BeanDescriptorFactory beanDescFactory;

    public BeanSerializer(BeanDescriptorFactory beanDescFactory) {
        this.beanDescFactory = beanDescFactory;
    }

    @Override
    public Object serialize(SerializationContext ctx, Object obj) throws SerializationException {
        JSONObject jsonObj = new JSONObject();
        if (ctx.doClassHinting()) {
            jsonObj.putMember(ctx.getClassHintPropertyName(), obj.getClass().getName());
        }
        BeanDescriptor bd = beanDescFactory.getBeanDescriptor(obj.getClass());
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
                    if (field != null)
                        val = field.get(obj);
                    else
                        throw new SerializationException("Bean of type '" + obj.getClass().getName() + "' doesn't " + " have getter method or direct access to property '" + prop
                                + "'.");
                }
                if (val == null) {
                    jsonObj.putMember(prop, JSONValue.NULL);
                } else {
                    Object serObj = ctx.serialize(val);
                    jsonObj.putMember(prop, serObj);
                }
            } catch (Exception x) {
                throw new SerializationException("Error during serialization.", x);
            }
        }
        return jsonObj;
    }

    @Override
    public void serialize(SerializationContext ctx, Object obj, Writer writer) throws SerializationException, IOException {
        writer.write("{");
        if (ctx.doClassHinting()) {
            writer.write("\"");
            writer.write(ctx.getClassHintPropertyName());
            writer.write("\":\"");
            writer.write(obj.getClass().getName());
            writer.write("\",");
        }
        BeanDescriptor bd = beanDescFactory.getBeanDescriptor(obj.getClass());
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
                    if (field != null)
                        val = field.get(obj);
                    else
                        throw new SerializationException("Bean of type '" + obj.getClass().getName() + "' doesn't " + " have getter method or direct access to property '" + prop
                                + "'.");
                }
                if (val == null) {
                    writer.write("\"");
                    writer.write(prop);
                    writer.write("\":null");
                } else {
                    writer.write("\"");
                    writer.write(prop);
                    writer.write("\":");
                    ctx.serialize(val, writer);
                }
                if (it.hasNext()) writer.write(",");
            } catch (Exception x) {
                throw new SerializationException("Error during serialization.", x);
            }
        }
        writer.write("}");
    }

}
