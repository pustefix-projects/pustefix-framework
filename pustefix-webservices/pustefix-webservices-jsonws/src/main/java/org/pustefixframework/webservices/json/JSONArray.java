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

package org.pustefixframework.webservices.json;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class JSONArray implements JSONValue {

    final static char LSB='[';
    final static char RSB=']';
    final static char COM=',';
    
    List<Object> values;
    
    public void add(Object value) {
        if(values==null) values=new ArrayList<Object>();
        values.add(value);
    }
    
    public int size() {
        if(values==null) return 0;
        return values.size();
    }
    
    public Object get(int index) {
        if(values==null) throw new IllegalArgumentException("Can't get element from empty array.");
        return values.get(index);
    }
    
    public String toJSONString() {
        StringBuffer sb=new StringBuffer();
        sb.append(LSB);
        if(values!=null) {
            Iterator<Object> it=values.iterator();
            if(it.hasNext()) sb.append(ParserUtils.javaToJson(it.next()));
            while(it.hasNext()) {
                sb.append(COM);
                sb.append(ParserUtils.javaToJson(it.next()));
            }
        }
        sb.append(RSB);
        return sb.toString();
    }
    
}
