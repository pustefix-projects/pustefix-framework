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

import java.util.Stack;

/**
 * @author mleidig@schlund.de
 */
public class SerializationContext {

    private SerializerRegistry   registry;
    private Stack<Object>        objectStack   = new Stack<Object>();
    private Stack<XPathPosition> positionStack = new Stack<XPathPosition>();

    public SerializationContext(SerializerRegistry registry) {
        this.registry = registry;
    }

    public void serialize(Object obj, XMLWriter writer) throws SerializationException {
        SimpleTypeSerializer simpleSerializer = registry.getSimpleTypeSerializer(obj.getClass());
        if (simpleSerializer != null) {
            String value = serialize(obj, simpleSerializer);
            writer.writeCharacters(value);
        } else {
            ComplexTypeSerializer complexSerializer = registry.getSerializer(obj.getClass());
            if (complexSerializer != null) {
                serialize(obj, writer, complexSerializer);
            }
        }
    }

    public void serialize(Object obj, XMLWriter writer, ComplexTypeSerializer serializer) throws SerializationException {
        if (!objectStack.contains(obj)) {
            objectStack.push(obj);
            positionStack.push(writer.getCurrentPosition());
            serializer.serialize(obj, this, writer);
            objectStack.pop();
            positionStack.pop();
        } else {
            int ind = objectStack.indexOf(obj);
            XPathPosition p = positionStack.get(ind);
            writer.writeAttribute("xpathref", p.getXPath());
        }
    }

    public String serialize(Object obj) throws SerializationException {
        SimpleTypeSerializer serializer = registry.getSimpleTypeSerializer(obj.getClass());
        return serialize(obj, serializer);
    }

    public String serialize(Object obj, SimpleTypeSerializer serializer) throws SerializationException {
        return serializer.serialize(obj, this);
    }

    public boolean hasSimpleTypeSerializer(Class<?> clazz) {
        SimpleTypeSerializer serializer = registry.getSimpleTypeSerializer(clazz);
        return serializer != null;
    }

    public String mapClassName(Object obj) {
        return registry.mapClassName(obj);
    }

}
