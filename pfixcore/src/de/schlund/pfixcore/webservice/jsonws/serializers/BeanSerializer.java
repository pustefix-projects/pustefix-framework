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

package de.schlund.pfixcore.webservice.jsonws.serializers;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Set;

import de.schlund.pfixcore.webservice.json.JSONObject;
import de.schlund.pfixcore.webservice.jsonws.BeanDescriptor;
import de.schlund.pfixcore.webservice.jsonws.BeanDescriptorFactory;
import de.schlund.pfixcore.webservice.jsonws.SerializationContext;
import de.schlund.pfixcore.webservice.jsonws.SerializationException;
import de.schlund.pfixcore.webservice.jsonws.Serializer;

public class BeanSerializer extends Serializer {

    BeanDescriptorFactory beanDescFactory;
    
    public BeanSerializer(BeanDescriptorFactory beanDescFactory) {
        this.beanDescFactory=beanDescFactory;
    }
    
    public Object serialize(SerializationContext ctx,Object obj) throws SerializationException {
        JSONObject jsonObj=new JSONObject();
        if(ctx.doClassHinting()) {
            jsonObj.putMember(ctx.getClassHintPropertyName(),obj.getClass().getName());
        }
        BeanDescriptor bd=beanDescFactory.getBeanDescriptor(obj.getClass());
        Set<String> props=bd.getReadableProperties();
        Iterator<String> it=props.iterator();
        while(it.hasNext()) {
            String prop=it.next();
            Method meth=bd.getGetMethod(prop);
            try {
                Object val=meth.invoke(obj,new Object[0]);
                if(val==null) {
                    jsonObj.putMember(prop,JSONObject.NULL);
                } else {
                    Object serObj=ctx.serialize(val);
                    jsonObj.putMember(prop,serObj);
                }
            } catch (Exception x) {
                throw new SerializationException("Error during serialization.",x);
            }
        }
        return jsonObj;
    }
    
    public void serialize(SerializationContext ctx,Object obj,Writer writer) throws SerializationException,IOException {
        writer.write("{");
        if(ctx.doClassHinting()) {
            writer.write("\"");
            writer.write(ctx.getClassHintPropertyName());
            writer.write("\":\"");
            writer.write(obj.getClass().getName());
            writer.write("\",");
        }
        BeanDescriptor bd=beanDescFactory.getBeanDescriptor(obj.getClass());
        Set<String> props=bd.getReadableProperties();
        Iterator<String> it=props.iterator();
        while(it.hasNext()) {
            String prop=it.next();
            Method meth=bd.getGetMethod(prop);
            try {
                Object val=meth.invoke(obj,new Object[0]);
                if(val==null) {
                    writer.write("\"");
                    writer.write(prop);
                    writer.write("\":null");
                } else {
                    writer.write("\"");
                    writer.write(prop);
                    writer.write("\":");
                    ctx.serialize(val,writer);
                }
                if(it.hasNext()) writer.write(",");
            } catch (Exception x) {
                throw new SerializationException("Error during serialization.",x);
            }
        }
        writer.write("}");
    }
    
}
