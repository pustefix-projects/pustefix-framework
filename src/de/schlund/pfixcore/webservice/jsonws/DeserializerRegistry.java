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

package de.schlund.pfixcore.webservice.jsonws;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.schlund.pfixcore.webservice.jsonws.deserializers.ArrayDeserializer;
import de.schlund.pfixcore.webservice.jsonws.deserializers.BeanDeserializer;
import de.schlund.pfixcore.webservice.jsonws.deserializers.BooleanDeserializer;
import de.schlund.pfixcore.webservice.jsonws.deserializers.CalendarDeserializer;
import de.schlund.pfixcore.webservice.jsonws.deserializers.NumberDeserializer;
import de.schlund.pfixcore.webservice.jsonws.deserializers.StringDeserializer;


public class DeserializerRegistry {

    Map<Class,Deserializer> deserializers;
    Deserializer beanDeserializer;
    Deserializer arrayDeserializer;
    
    public DeserializerRegistry() {
        deserializers=new HashMap<Class,Deserializer>();
        beanDeserializer=new BeanDeserializer();
        
        arrayDeserializer=new ArrayDeserializer();
        deserializers.put(List.class,arrayDeserializer);
        
        deserializers.put(String.class,new StringDeserializer());
        
        Deserializer deser=new NumberDeserializer();
        deserializers.put(byte.class,deser);
        deserializers.put(Byte.class,deser);
        deserializers.put(short.class,deser);
        deserializers.put(Short.class,deser);
        deserializers.put(int.class,deser);
        deserializers.put(Integer.class,deser);
        deserializers.put(long.class,deser);
        deserializers.put(Long.class,deser);
        deserializers.put(float.class,deser);
        deserializers.put(Float.class,deser);
        deserializers.put(double.class,deser);
        deserializers.put(Double.class,deser);
        
        deser=new BooleanDeserializer();
        deserializers.put(boolean.class,deser);
        deserializers.put(Boolean.class,deser);
        
        deser=new CalendarDeserializer();
        deserializers.put(Calendar.class,deser);
        deserializers.put(Date.class,deser);
        
        
    }
    
    public Deserializer getDeserializer(Class clazz) throws DeserializationException {
        Deserializer deser=null;
        if(clazz!=null) deser=deserializers.get(clazz);
        if(clazz.isArray()) return arrayDeserializer; 
        if(List.class.isAssignableFrom(clazz)) return arrayDeserializer;
        if(deser==null) deser=beanDeserializer;
        if(deser==null) throw new DeserializationException("No Deserializer found for "+clazz.getName());
        return deser;
    }
    
}
