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

import java.util.Enumeration;
import java.util.Properties;

import de.schlund.pfixcore.oxm.impl.ComplexTypeSerializer;
import de.schlund.pfixcore.oxm.impl.SerializationContext;
import de.schlund.pfixcore.oxm.impl.SerializationException;
import de.schlund.pfixcore.oxm.impl.XMLWriter;

/**
 * @author mleidig@schlund.de
 */
public class PropertiesSerializer implements ComplexTypeSerializer {

    public void serialize(Object obj, SerializationContext context, XMLWriter writer) throws SerializationException {
        if (obj instanceof Properties) {
            Properties props = (Properties) obj;
            Enumeration<?> e = props.propertyNames();
            while (e.hasMoreElements()) {
                String name = (String) e.nextElement();
                writer.writeStartElement("property");
                writer.writeAttribute("key", name);
                String value = props.getProperty(name);
                writer.writeAttribute("value", value);
                writer.writeEndElement();
            }
        } else throw new SerializationException("Illegal type: " + obj.getClass().getName());
    }
}