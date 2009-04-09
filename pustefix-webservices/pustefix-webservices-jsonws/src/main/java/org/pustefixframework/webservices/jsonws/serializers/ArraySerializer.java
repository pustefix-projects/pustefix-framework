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

package org.pustefixframework.webservices.jsonws.serializers;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Array;

import org.pustefixframework.webservices.json.JSONArray;
import org.pustefixframework.webservices.json.JSONValue;
import org.pustefixframework.webservices.jsonws.SerializationContext;
import org.pustefixframework.webservices.jsonws.SerializationException;
import org.pustefixframework.webservices.jsonws.Serializer;


public class ArraySerializer extends Serializer {
    
    @Override
    public Object serialize(SerializationContext ctx, Object obj) throws SerializationException {
        JSONArray jsonArray=new JSONArray();
        if(obj.getClass().isArray()) {
            int len=Array.getLength(obj);
            for(int i=0;i<len;i++) {
                Object item=Array.get(obj,i);
                if(item==null) {
                    jsonArray.add(JSONValue.NULL);
                } else {
                    Object serObj=ctx.serialize(item);
                    jsonArray.add(serObj);
                }
            }
        }
        return jsonArray;
    }
    
    @Override
    public void serialize(SerializationContext ctx, Object obj,Writer writer) throws SerializationException,IOException {
        writer.write("[");
        if(obj.getClass().isArray()) {
            int len=Array.getLength(obj);
            for(int i=0;i<len;i++) {
                if(i>0) writer.write(",");
                Object item=Array.get(obj,i);
                if(item==null) {
                    writer.write("null");
                } else {
                    ctx.serialize(item,writer);
                }
            }
        }
        writer.write("]");
    }

}
