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

package org.pustefixframework.webservices.jsonws.deserializers;

import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.pustefixframework.webservices.json.JSONArray;
import org.pustefixframework.webservices.jsonws.DeserializationContext;
import org.pustefixframework.webservices.jsonws.DeserializationException;
import org.pustefixframework.webservices.jsonws.Deserializer;


public class ArrayDeserializer extends Deserializer {

    @Override
    public boolean canDeserialize(DeserializationContext ctx, Object jsonValue, Type targetType) throws DeserializationException {
        if (jsonValue instanceof JSONArray) {
            Class<?> targetClass = null;
            if (targetType instanceof Class)
                targetClass = (Class<?>) targetType;
            else if (targetType instanceof ParameterizedType) {
                Type rawType = ((ParameterizedType) targetType).getRawType();
                if (rawType instanceof Class)
                    targetClass = (Class<?>) rawType;
                else
                    return false;
            }
            if (targetClass.isArray() || List.class.isAssignableFrom(targetClass)) return true;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object deserialize(DeserializationContext ctx, Object jsonValue, Type targetType) throws DeserializationException {

        if (jsonValue instanceof JSONArray) {

            JSONArray jsonArray = (JSONArray) jsonValue;

            Class<?> targetClass = null;
            if (targetType instanceof Class)
                targetClass = (Class<?>) targetType;
            else if (targetType instanceof ParameterizedType) {
                Type rawType = ((ParameterizedType) targetType).getRawType();
                if (rawType instanceof Class)
                    targetClass = (Class<?>) rawType;
                else
                    throw new DeserializationException("Type not supported: " + targetType);
            }

            if (targetClass.isArray()) {

                Class<?> compType = targetClass.getComponentType();
                Object arrayObj = Array.newInstance(compType, jsonArray.size());
                for (int i = 0; i < jsonArray.size(); i++) {
                    Object item = jsonArray.get(i);
                    Object obj = ctx.deserialize(item, compType);
                    Array.set(arrayObj, i, obj);
                }
                return arrayObj;

            } else if (List.class.isAssignableFrom(targetClass)) {

                Type argType = null;
                if (targetType instanceof ParameterizedType) {
                    ParameterizedType paramType = (ParameterizedType) targetType;
                    Type[] argTypes = paramType.getActualTypeArguments();
                    if (argTypes.length == 1) {
                        argType = argTypes[0];
                    } else
                        throw new DeserializationException("Type not supported: " + targetType);
                } else
                    throw new DeserializationException("Deserialization of unparameterized List types isn't supported: " + targetType);

                List list = null;
                if (!targetClass.isInterface()) {
                    try {
                        list = (List) targetClass.newInstance();
                    } catch (Exception x) {}
                }
                if (list == null) {
                    if (targetClass.isAssignableFrom(ArrayList.class)) {
                        list = new ArrayList<Object>();
                    } else
                        throw new DeserializationException("Can't create instance of class '" + targetClass.getName() + "'.");
                }

                for (int i = 0; i < jsonArray.size(); i++) {
                    Object item = jsonArray.get(i);
                    Object obj = ctx.deserialize(item, argType);
                    list.add(obj);
                }
                return list;

            } else
                throw new DeserializationException("Type not supported: " + targetType);

        } else
            throw new DeserializationException("Wrong type: " + jsonValue.getClass().getName());

    }

}
