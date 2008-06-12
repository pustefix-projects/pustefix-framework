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

import de.schlund.pfixcore.webservice.json.ParserUtils;
import de.schlund.pfixcore.webservice.jsonws.SerializationContext;
import de.schlund.pfixcore.webservice.jsonws.SerializationException;
import de.schlund.pfixcore.webservice.jsonws.Serializer;

/**
 * @author mleidig@schlund.de
 */
public class StringSerializer extends Serializer {
    
    @Override
    public Object serialize(SerializationContext ctx,Object obj) throws SerializationException {
        return obj;
    }
    
    @Override
    public void serialize(SerializationContext ctx,Object obj,Writer writer) throws SerializationException,IOException {
        writer.write(ParserUtils.jsonEscape((String)obj));
    }

}
