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

import java.lang.reflect.Type;

import de.schlund.pfixcore.webservice.jsonws.DeserializationContext;
import de.schlund.pfixcore.webservice.jsonws.DeserializationException;
import de.schlund.pfixcore.webservice.jsonws.Deserializer;

/**
 * @author mleidig@schlund.de
 */
public class NumberDeserializer extends Deserializer {

    @Override
    public boolean canDeserialize(DeserializationContext ctx, Object jsonValue, Type targetType) {
        Class<?> targetClass=(Class<?>)targetType;
        if(jsonValue instanceof Number && Number.class.isAssignableFrom(targetClass)) return true;
        return false;
    }
    
    @Override
    public Object deserialize(DeserializationContext ctx,Object  jsonValue,Type targetType) throws DeserializationException {
        Class<?> targetClass=(Class<?>)targetType;
        if(jsonValue instanceof Number) {
            if(targetClass==int.class||targetClass==Integer.class) {
                if(jsonValue.getClass()==Integer.class) return jsonValue;
                else return new Integer(((Number)jsonValue).intValue());
            } else if(targetClass==float.class||targetClass==Float.class) {
                if(jsonValue.getClass()==Float.class) return jsonValue;
                else return new Float(((Number)jsonValue).floatValue());
            } else if(targetClass==long.class||targetClass==Long.class) {
                if(jsonValue.getClass()==Long.class) return jsonValue;
                else return new Long(((Number)jsonValue).longValue());
            } else if(targetClass==double.class||targetClass==Double.class) {
                if(jsonValue.getClass()==Double.class) return jsonValue;
                else return new Double(((Number)jsonValue).doubleValue());
            } else if(targetClass==byte.class||targetClass==Byte.class) {
                if(jsonValue.getClass()==Byte.class) return jsonValue;
                else return new Byte(((Number)jsonValue).byteValue());
            } else if(targetClass==short.class||targetClass==Short.class) {
                if(jsonValue.getClass()==Short.class) return jsonValue;
                else return new Short(((Number)jsonValue).shortValue());
            }
            return (Number)jsonValue;
        } else throw new DeserializationException("Wrong type: "+jsonValue.getClass().getName());
    }
    
}
