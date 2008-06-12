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

package de.schlund.pfixcore.webservice.jsonqx;

import java.io.IOException;
import java.io.Writer;
import java.util.Calendar;
import java.util.Date;

import de.schlund.pfixcore.webservice.jsonws.CustomJSONObject;
import de.schlund.pfixcore.webservice.jsonws.SerializationContext;
import de.schlund.pfixcore.webservice.jsonws.SerializationException;
import de.schlund.pfixcore.webservice.jsonws.Serializer;

public class CalendarSerializer extends Serializer {

    @Override
    public Object serialize(SerializationContext ctx, Object obj) throws SerializationException {
        if(obj instanceof Calendar) {
            String json=createUTCFuncDate((Calendar)obj);
            return new CustomJSONObject(json);
        } else if(obj instanceof Date) {
            String json=createUTCFuncDate((Date)obj);
            return new CustomJSONObject(json);
        } else throw new SerializationException("Can't serialize object of instance "+obj.getClass().getName());
    }
    
    @Override
    public void serialize(SerializationContext ctx, Object obj, Writer writer) throws SerializationException, IOException {
        if(obj instanceof Calendar) {
            writer.write(createUTCFuncDate((Calendar)obj));
        } else if(obj instanceof Date) {
            writer.write(createUTCFuncDate((Date)obj));
        } else throw new SerializationException("Can't serialize object of instance "+obj.getClass().getName());
    }
    
    private String createUTCFuncDate(Calendar cal) {
        StringBuilder sb=new StringBuilder();
        sb.append("new Date(Date.UTC(");
        sb.append(cal.get(Calendar.YEAR));
        sb.append(",");
        sb.append(cal.get(Calendar.MONTH));
        sb.append(",");
        sb.append(cal.get(Calendar.DATE));
        sb.append(",");
        sb.append(cal.get(Calendar.HOUR_OF_DAY));
        sb.append(",");
        sb.append(cal.get(Calendar.MINUTE));
        sb.append(",");
        sb.append(cal.get(Calendar.SECOND));
        sb.append(",");
        sb.append(cal.get(Calendar.MILLISECOND));
        sb.append("))");
        return sb.toString();
    }
    
    private String createUTCFuncDate(Date date) {
        Calendar cal=Calendar.getInstance();
        cal.setTime(date);
        return createUTCFuncDate(cal);
    }
    
}
