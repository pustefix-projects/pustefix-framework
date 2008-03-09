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
import java.util.Iterator;
import java.util.Map;

import de.schlund.pfixcore.oxm.impl.AnnotationAware;
import de.schlund.pfixcore.oxm.impl.ComplexTypeSerializer;
import de.schlund.pfixcore.oxm.impl.SerializationContext;
import de.schlund.pfixcore.oxm.impl.SerializationException;
import de.schlund.pfixcore.oxm.impl.XMLWriter;

/**
 * @author mleidig@schlund.de
 */
public class MapSerializer implements ComplexTypeSerializer, AnnotationAware {

    private String elementName = "entry"; 
    
    public void serialize(Object obj, SerializationContext context, XMLWriter writer) throws SerializationException {
        if(obj instanceof Map) {
            Map<?,?> map = (Map<?,?>)obj;
            Iterator<?> it = map.keySet().iterator();
            while(it.hasNext()) {
                writer.writeStartElement(this.elementName);
                Object key = it.next();
                String elementName = context.mapClassName(key);
                writer.writeStartElement(elementName);
                if (key != null) {
                    context.serialize(key, writer);
                }
                writer.writeEndElement();
                Object val = map.get(key);
                elementName = context.mapClassName(val);
                writer.writeStartElement(elementName);
                if (val != null) {
                    context.serialize(val, writer);
                }
                writer.writeEndElement();

                writer.writeEndElement();
            }
        } else throw new SerializationException("Illegal type: "+obj.getClass().getName());
    }

    public void setAnnotation(Annotation annotation) {
        if (annotation instanceof de.schlund.pfixcore.oxm.impl.annotation.MapSerializer) {
            de.schlund.pfixcore.oxm.impl.annotation.MapSerializer mapAnno = (de.schlund.pfixcore.oxm.impl.annotation.MapSerializer)annotation;
            this.elementName = mapAnno.elementName();
        }
    }   
}