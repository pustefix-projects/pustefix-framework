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

package org.pustefixframework.webservices.jsonws.deserializers;

import java.lang.reflect.Type;

import org.pustefixframework.webservices.jsonws.DeserializationContext;
import org.pustefixframework.webservices.jsonws.DeserializationException;
import org.pustefixframework.webservices.jsonws.Deserializer;


/**
 * @author mleidig@schlund.de
 */
public class EnumDeserializer extends Deserializer {

    @Override
    public boolean canDeserialize(DeserializationContext ctx, Object jsonValue, Type targetType) {
        if(jsonValue instanceof String) return true;
        return false;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public Object deserialize(DeserializationContext ctx,Object jsonValue,Type targetType) throws DeserializationException {
        if(jsonValue instanceof String) {
        	Class<? extends Enum> targetClass=(Class<? extends Enum>)targetType;
        	return Enum.valueOf(targetClass, (String)jsonValue);
        } else throw new DeserializationException("Wrong type: "+jsonValue.getClass().getName());
    }
    
}
