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
package de.schlund.pfixcore.oxm.impl.serializers;

import java.util.Collection;
import java.util.Iterator;

import de.schlund.pfixcore.oxm.impl.ComplexTypeSerializer;
import de.schlund.pfixcore.oxm.impl.SerializationContext;
import de.schlund.pfixcore.oxm.impl.SerializationException;
import de.schlund.pfixcore.oxm.impl.XMLWriter;

/**
 * @author mleidig@schlund.de
 */
public class CollectionSerializer implements ComplexTypeSerializer {

    public void serialize(Object obj, SerializationContext context, XMLWriter writer) throws SerializationException {
        if(obj instanceof Collection) {
            Collection<?> col = (Collection<?>)obj;
            Iterator<?> it = col.iterator();
            while (it.hasNext()) {
                Object item = it.next();
                String elementName = context.mapClassName(item);
                writer.writeStartElement(elementName);
                if(item != null) {
                    context.serialize(item, writer);
                }
                writer.writeEndElement();
            }
        } else throw new SerializationException("Illegal type: "+obj.getClass().getName());
    }   
}