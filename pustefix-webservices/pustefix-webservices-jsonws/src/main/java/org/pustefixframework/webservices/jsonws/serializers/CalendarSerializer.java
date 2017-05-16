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

package org.pustefixframework.webservices.jsonws.serializers;

import java.io.IOException;
import java.io.Writer;
import java.util.Calendar;
import java.util.Date;

import org.pustefixframework.webservices.json.JSONObject;
import org.pustefixframework.webservices.jsonws.SerializationContext;
import org.pustefixframework.webservices.jsonws.SerializationException;
import org.pustefixframework.webservices.jsonws.Serializer;


public class CalendarSerializer extends Serializer {

    @Override
    public Object serialize(SerializationContext ctx, Object obj) throws SerializationException {
        if(obj instanceof Calendar) {
            JSONObject jsonObj = new JSONObject();
            jsonObj.putMember("__time__", ((Calendar)obj).getTimeInMillis());
            return jsonObj;
        } else if(obj instanceof Date) {
            JSONObject jsonObj = new JSONObject();
            jsonObj.putMember("__time__", ((Date)obj).getTime());
            return jsonObj;
        } else throw new SerializationException("Can't serialize object of instance "+obj.getClass().getName());
    }
    
    @Override
    public void serialize(SerializationContext ctx, Object obj, Writer writer) throws SerializationException, IOException {
        if(obj instanceof Calendar) {
            writer.write("{\"__time__\":" + ((Calendar)obj).getTimeInMillis() + "}");
        } else if(obj instanceof Date) {
            writer.write("{\"__time__\":" + ((Date)obj).getTime() + "}");
        } else throw new SerializationException("Can't serialize object of instance "+obj.getClass().getName());
    }
    
}
