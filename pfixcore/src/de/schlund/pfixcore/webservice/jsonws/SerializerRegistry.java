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
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import de.schlund.pfixcore.webservice.jsonws.serializers.BeanSerializer;
import de.schlund.pfixcore.webservice.jsonws.serializers.BooleanSerializer;
import de.schlund.pfixcore.webservice.jsonws.serializers.CalendarSerializer;
import de.schlund.pfixcore.webservice.jsonws.serializers.NumberSerializer;
import de.schlund.pfixcore.webservice.jsonws.serializers.StringSerializer;

/**
 * @author mleidig@schlund.de
 */
public class SerializerRegistry {

    Map<Class,Serializer> serializers;
    BeanSerializer beanSerializer;
    
    public SerializerRegistry(BeanDescriptorFactory beanDescFactory) {
        serializers=new HashMap<Class,Serializer>();
        
      
        beanSerializer=new BeanSerializer(beanDescFactory);
        
        serializers.put(String.class,new StringSerializer());
        Serializer ser=new NumberSerializer();
        serializers.put(Byte.class,ser);
        serializers.put(Short.class,ser);
        serializers.put(Integer.class,ser);
        serializers.put(Long.class,ser);
        serializers.put(Float.class,ser);
        serializers.put(Double.class,ser);
        serializers.put(Boolean.class,new BooleanSerializer());
        ser=new CalendarSerializer();
        serializers.put(Calendar.class,ser);
        serializers.put(GregorianCalendar.class,ser);
        serializers.put(Date.class,ser);
      
    }
    
    public Serializer getSerializer(Object obj) {
        Serializer ser=null;
        ser=serializers.get(obj.getClass());
        if(ser==null) ser=beanSerializer;
        return ser;
    }
    
}
