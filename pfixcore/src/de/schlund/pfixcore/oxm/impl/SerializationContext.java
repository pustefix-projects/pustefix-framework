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

import org.apache.log4j.Logger;


/**
 * @author mleidig@schlund.de
 */
public class SerializationContext {

    private static Logger LOG=Logger.getLogger(SerializationContext.class);
    
    private SerializerRegistry registry;
    private Stack<Object> objectStack=new Stack<Object>();
    
    public SerializationContext(SerializerRegistry registry) {
        this.registry=registry;
    }
    
    public void serialize(Object obj, XMLWriter writer) throws SerializationException {
        SimpleTypeSerializer ss=registry.getSimpleTypeSerializer(obj.getClass());
        if(ss!=null) {
            String value=ss.serialize(obj,this);
            writer.writeCharacters(value);
        } else {
            ComplexTypeSerializer serializer=registry.getSerializer(obj.getClass());
            if(serializer!=null) {
                if(!objectStack.contains(obj)) { 
                    objectStack.push(obj);
                    serializer.serialize(obj,this,writer);
                    objectStack.pop();
                } else {
                    LOG.warn("Found circular reference to instance of class '"+
                            obj.getClass().getName()+"', skipping serialization");
                }
            }
        }
    }
    
    public String serialize(Object obj) throws SerializationException {
        SimpleTypeSerializer serializer=registry.getSimpleTypeSerializer(obj.getClass());
        return serializer.serialize(obj,this);
    }
    
    public boolean hasSimpleTypeSerializer(Class<?> clazz) {
        SimpleTypeSerializer serializer=registry.getSimpleTypeSerializer(clazz);
        return serializer!=null;
    }
    
}
