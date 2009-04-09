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
package de.schlund.pfixcore.oxm.impl;

import javax.xml.transform.Result;
import javax.xml.transform.dom.DOMResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import de.schlund.pfixcore.oxm.Marshaller;
import de.schlund.pfixcore.oxm.MarshallingException;

/**
 * 
 * Marshaller implementation which serializes an object into a W3C DOM
 * 
 * @author mleidig@schlund.de
 */
public class MarshallerImpl implements Marshaller {

    SerializerRegistry registry;

    public MarshallerImpl(SerializerRegistry registry) {
        this.registry = registry;
    }

    public void marshal(Object obj, Result result) throws MarshallingException {
        if (!(result instanceof DOMResult)) throw new IllegalArgumentException("Result must be of type: " + DOMResult.class.getName());
        DOMResult domResult = (DOMResult) result;
        Node node = domResult.getNode();
        if (node instanceof Document) node = ((Document) node).getDocumentElement();
        XMLWriter xmlWriter = new DOMWriter(node);
        SerializationContext context = new SerializationContext(registry);
        try {
            context.serialize(obj, xmlWriter);
        } catch (SerializationException x) {
            throw new MarshallingException("Error while marshalling object.", x);
        }
    }

}
