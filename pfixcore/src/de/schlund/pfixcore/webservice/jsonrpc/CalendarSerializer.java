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

package de.schlund.pfixcore.webservice.jsonrpc;

import java.util.Calendar;
import java.util.GregorianCalendar;

import com.metaparadigm.jsonrpc.AbstractSerializer;
import com.metaparadigm.jsonrpc.MarshallException;
import com.metaparadigm.jsonrpc.ObjectMatch;
import com.metaparadigm.jsonrpc.SerializerState;
import com.metaparadigm.jsonrpc.UnmarshallException;
import com.metaparadigm.jsonrpc.org.json.JSONObject;


/**
 * @author mleidig@schlund.de
 */
public class CalendarSerializer extends AbstractSerializer {
    
    private static Class[] serClasses = new Class[] {GregorianCalendar.class,Calendar.class};
    private static Class[] jsonClasses = new Class[] {JSONObject.class};

    public Class[] getSerializableClasses() {
        return serClasses;
    }
    
    public Class[] getJSONClasses() { 
        return jsonClasses;
    }

    public ObjectMatch tryUnmarshall(SerializerState state,Class clazz,Object obj) throws UnmarshallException {
        JSONObject jsonObj=(JSONObject)obj;
        String javaClass=jsonObj.getString("javaClass");
        if(javaClass==null) throw new UnmarshallException("Type hint is missing");	
        if(!(javaClass.equals(Calendar.class.getName()))) throw new UnmarshallException("Type is no Calendar");
        return ObjectMatch.OKAY;
    }

    public Object unmarshall(SerializerState state,Class clazz,Object obj) throws UnmarshallException {
        JSONObject jsonObj=(JSONObject)obj;
        String javaClass=jsonObj.getString("javaClass");
        if(javaClass!=null&&!javaClass.equals(Calendar.class.getName())) throw new UnmarshallException("Type is no Calendar");
        long time=jsonObj.getLong("time"); 
        Calendar cal=Calendar.getInstance();
        cal.setTimeInMillis(time);
        return cal;
    }

    public Object marshall(SerializerState state,Object obj) throws MarshallException {
        if(obj instanceof Calendar) {
            String json="new Date("+((Calendar)obj).getTimeInMillis()+")";
        	return new CustomJSONObject(json);
        } else throw new MarshallException("Can't marshall object of instance "+obj.getClass().getName());
	}
        
}
