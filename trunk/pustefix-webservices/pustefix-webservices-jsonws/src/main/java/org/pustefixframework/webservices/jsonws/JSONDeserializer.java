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

import java.lang.reflect.Type;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author mleidig@schlund.de
 */
public class JSONDeserializer {

    DeserializerRegistry registry;
    List<Pattern> deserializationWhiteList;
    
    public JSONDeserializer(DeserializerRegistry registry) {
        this.registry=registry;
    }
    
    public JSONDeserializer(DeserializerRegistry registry, List<Pattern> deserializationWhiteList) {
        this.registry=registry;
        this.deserializationWhiteList = deserializationWhiteList;
    }
    
    public boolean canDeserialize(Object jsonObj,Type targetType)  throws DeserializationException {
        DeserializationContext ctx=new DeserializationContext(registry, deserializationWhiteList); 
        return ctx.canDeserialize(jsonObj, targetType);
    }
    
    public Object deserialize(Object jsonObj,Type targetType) throws DeserializationException {
        DeserializationContext ctx=new DeserializationContext(registry, deserializationWhiteList); 
        return ctx.deserialize(jsonObj,targetType);
    }
    
}
