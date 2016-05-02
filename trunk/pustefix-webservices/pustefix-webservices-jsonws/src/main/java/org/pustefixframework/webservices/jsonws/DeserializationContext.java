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

package org.pustefixframework.webservices.jsonws;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.regex.Pattern;

public class DeserializationContext {

    DeserializerRegistry deserReg;
    List<Pattern> deserializationWhiteList;
    
    public DeserializationContext(DeserializerRegistry deserReg, List<Pattern> deserializationWhiteList) {
        this.deserReg=deserReg;
        this.deserializationWhiteList = deserializationWhiteList;
    }
   
    public boolean canDeserialize(Object jsonObj,Type targetType) throws DeserializationException {
        Class<?> targetClass=null;
        if(targetType instanceof Class) targetClass=(Class<?>)targetType;
        else if(targetType instanceof ParameterizedType) {
            Type rawType=((ParameterizedType)targetType).getRawType();
            if(rawType instanceof Class) targetClass=(Class<?>)rawType;
            else return false;
        } else return false;
        if(jsonObj==null) {
            if(targetClass.isPrimitive()) return false;
            return true;
        }
        Deserializer deser=deserReg.getDeserializer(targetClass);
        return deser.canDeserialize(this,jsonObj,targetType);
    }
    
    public Object deserialize(Object jsonObj,Type targetType) throws DeserializationException {
        if(jsonObj==null) return null;
        Class<?> targetClass=null;
        if(targetType instanceof Class) targetClass=(Class<?>)targetType;
        else if(targetType instanceof ParameterizedType) {
            Type rawType=((ParameterizedType)targetType).getRawType();
            if(rawType instanceof Class) targetClass=(Class<?>)rawType;
            else throw new DeserializationException("Unsupported type: "+targetType);
        } else throw new DeserializationException("Unsupported type: "+targetType);
        Deserializer deser=deserReg.getDeserializer(targetClass);
        return deser.deserialize(this,jsonObj,targetType);
    }
    
    public boolean isWhiteListedForDeserialization(String className) {
        if(deserializationWhiteList != null) {
            for(Pattern pattern: deserializationWhiteList) {
                if(pattern.matcher(className).matches()) {
                    return true;
                }
            }
        }
        return false;
    }
    
}
