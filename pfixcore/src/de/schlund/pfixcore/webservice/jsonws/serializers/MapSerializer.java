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

package de.schlund.pfixcore.webservice.jsonws.serializers;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.Map;

import de.schlund.pfixcore.webservice.json.JSONObject;
import de.schlund.pfixcore.webservice.json.JSONValue;
import de.schlund.pfixcore.webservice.json.ParserUtils;
import de.schlund.pfixcore.webservice.jsonws.SerializationContext;
import de.schlund.pfixcore.webservice.jsonws.SerializationException;
import de.schlund.pfixcore.webservice.jsonws.Serializer;

/**
 * @author mleidig@schlund.de
 */
public class MapSerializer extends Serializer {

    public MapSerializer() {}

    public Object serialize(SerializationContext ctx, Object obj) throws SerializationException {
        JSONObject jsonObj = new JSONObject();
        if (obj instanceof Map) {
            if (ctx.doClassHinting()) {
                jsonObj.putMember(ctx.getClassHintPropertyName(), obj.getClass().getName());
            }
            Map<?, ?> map = (Map<?, ?>) obj;
            Iterator<?> it = map.keySet().iterator();
            while (it.hasNext()) {
                Object key = it.next();
                if (key instanceof String) {
                    Object val = map.get(key);
                    if (val == null) {
                        jsonObj.putMember(key.toString(), JSONValue.NULL);
                    } else {
                        Object serObj = ctx.serialize(val);
                        jsonObj.putMember(key.toString(), serObj);
                    }
                } else
                    throw new SerializationException("Unsupported Map key type (must be java.lang.String): " + key.getClass().getName());
            }
        }
        return jsonObj;
    }

    public void serialize(SerializationContext ctx, Object obj, Writer writer) throws SerializationException, IOException {
        writer.write("{");
        if (obj instanceof Map) {
            if (ctx.doClassHinting()) {
                writer.write("\"");
                writer.write(ctx.getClassHintPropertyName());
                writer.write("\":\"");
                writer.write(obj.getClass().getName());
                writer.write("\",");
            }
            Map<?, ?> map = (Map<?, ?>) obj;
            Iterator<?> it = map.keySet().iterator();
            while (it.hasNext()) {
                Object key = it.next();
                if (key instanceof String) {
                    Object val = map.get(key);
                    String keyStr = ParserUtils.jsonEscape(key.toString());
                    if (val == null) {
                        writer.write(keyStr);
                        writer.write(":null");
                    } else {
                        writer.write(keyStr);
                        writer.write(":");
                        ctx.serialize(val, writer);
                    }
                    if (it.hasNext()) writer.write(",");
                } else
                    throw new SerializationException("Unsupported Map key type (must be java.lang.String): " + key.getClass().getName());
            }
        }
        writer.write("}");
    }

}
