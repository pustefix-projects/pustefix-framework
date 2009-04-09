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
package de.schlund.pfixcore.oxm.impl.serializers;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Set;

import de.schlund.pfixcore.beans.BeanDescriptor;
import de.schlund.pfixcore.beans.BeanDescriptorFactory;
import de.schlund.pfixcore.oxm.impl.ComplexTypeSerializer;
import de.schlund.pfixcore.oxm.impl.SerializationContext;
import de.schlund.pfixcore.oxm.impl.XMLWriter;

/**
 * @author mleidig@schlund.de
 */
public class ComplexEnumSerializer implements ComplexTypeSerializer {

    private BeanDescriptorFactory beanDescFactory;

    public ComplexEnumSerializer(BeanDescriptorFactory beanDescFactory) {
        this.beanDescFactory = beanDescFactory;
    }

    public void serialize(Object obj, SerializationContext ctx, XMLWriter writer) {
        Enum<?> e = (Enum<?>) obj;
        writer.writeAttribute("name", e.name());
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
                    if (field != null) {
                        val = field.get(obj);
                    } else {
                        throw new RuntimeException("Enum of type '" + obj.getClass().getName() + "' doesn't "
                                + " have getter method or direct access to property '" + prop + "'.");
                    }
                }
                if (val != null) {
                    if (ctx.hasSimpleTypeSerializer(val.getClass())) {
                        writer.writeAttribute(prop, ctx.serialize(val));
                    } else {
                        writer.writeStartElement(prop);
                        ctx.serialize(val, writer);
                        writer.writeEndElement();
                    }
                }
            } catch (Exception x) {
                throw new RuntimeException("Error during serialization.", x);
            }
        }
    }
}