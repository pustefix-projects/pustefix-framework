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
package de.schlund.pfixcore.oxm.impl;

import javax.xml.bind.annotation.XmlRootElement;

import net.sf.cglib.proxy.Enhancer;
import de.schlund.pfixcore.beans.BeanDescriptorFactory;
import de.schlund.pfixcore.oxm.Marshaller;

public class MarshallerFactory {

    private static Marshaller defaultMarshaller;
    private static Marshaller jaxbMarshaller;

    static {
        BeanDescriptorFactory factory = new BeanDescriptorFactory();
        SerializerRegistry registry = new SerializerRegistry(factory);
        defaultMarshaller = new MarshallerImpl(registry);
        jaxbMarshaller = new de.schlund.pfixcore.oxm.impl.JAXBMarshaller();
    }
    
    public static Marshaller getMarshaller(Object object) {
    	
    	Class<?> objectClass = object.getClass();
    	if(Enhancer.isEnhanced(objectClass)) {
    		objectClass = objectClass.getSuperclass();
    	}
    	XmlRootElement jaxbElem = objectClass.getAnnotation(XmlRootElement.class);
    	Marshaller marshaller;
    	if(jaxbElem == null) {
    		marshaller = defaultMarshaller;
    	} else {
    		marshaller = jaxbMarshaller;
    	}
    	return marshaller;
    	
    }

}
