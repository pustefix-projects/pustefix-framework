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

package de.schlund.pfixcore.webservice.jsonws.deserializers;

import java.lang.reflect.Array;
import java.util.List;

import de.schlund.pfixcore.webservice.json.JSONArray;
import de.schlund.pfixcore.webservice.jsonws.DeserializationContext;
import de.schlund.pfixcore.webservice.jsonws.DeserializationException;
import de.schlund.pfixcore.webservice.jsonws.Deserializer;

public class ArrayDeserializer extends Deserializer {

    @Override
    public boolean canDeserialize(DeserializationContext ctx,Object jsonValue,Class<?> targetClass) throws DeserializationException {
        if(jsonValue instanceof JSONArray) {
            if(targetClass.isArray()) {
                Class compType=targetClass.getComponentType();
                JSONArray jsonArray=(JSONArray)jsonValue;
                if(jsonArray.size()==0) return true;
                ctx.canDeserialize(jsonArray.get(0),compType);
            } else if(List.class.isAssignableFrom(targetClass)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public Object deserialize(DeserializationContext ctx,Object jsonValue,Class targetClass) throws DeserializationException {
        if(jsonValue instanceof JSONArray) {
            JSONArray jsonArray=(JSONArray)jsonValue;
            if(targetClass.isArray()) {
                Class compType=targetClass.getComponentType();
                Object arrayObj=Array.newInstance(compType,jsonArray.size());
                for(int i=0;i<jsonArray.size();i++) {
                    Object item=jsonArray.get(i);
                    Object obj=ctx.deserialize(item,compType);
                    Array.set(arrayObj,i,obj);
                }
                return arrayObj;
            } else if(List.class.isAssignableFrom(targetClass)) {
                List list=null;
                try {
                    list=(List)targetClass.newInstance();
                } catch(Exception x) {
                    throw new DeserializationException("Can't instantiate array class '"+targetClass.getName()+"'.");
                }
                for(int i=0;i<jsonArray.size();i++) {
                    Object item=jsonArray.get(i);
                    Object obj=ctx.deserialize(item,Object.class);
                    list.add(obj);
                }
                return list;
            }
        } else throw new DeserializationException("Wrong type: "+jsonValue.getClass().getName());
        return null;
    }
    
}
