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

package de.schlund.pfixcore.webservice.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class JSONObject implements JSONValue {

    final static char LCB='{';
    final static char RCB='}';
    final static char COL=':';
    final static char COM=',';
    
    final static List<String> emptyIteratorList=new ArrayList<String>();
    
    Map<String,Object> members;
    
    public boolean hasMember(String memberName) {
        if(members==null) return false;
        return members.get(memberName)!=null;
    }
    
    public Iterator<String> getMemberNames() {
        if(members==null) return emptyIteratorList.iterator();
        return members.keySet().iterator();
    }
    
    public void putMember(String memberName,Object memberValue) {
        if(members==null) members=new HashMap<String,Object>();
        members.put(memberName,memberValue);
    }
    
    public Object getMember(String memberName) {
        if(members==null) return null;
        return members.get(memberName);
    }
    
    public String getStringMember(String memberName) {
        Object obj=members.get(memberName);
        if(obj!=null)  {
            if(!(obj instanceof String)) 
                throw new IllegalArgumentException("Member '"+memberName+"' has wrong type: "+obj.getClass().getName());
            return (String)obj;
        }
        return null;
    }
    
    public Boolean getBooleanMember(String memberName) {
        Object obj=members.get(memberName);
        if(obj!=null) {
            if(!(obj instanceof Boolean)) 
                throw new IllegalArgumentException("Member '"+memberName+"' has wrong type: "+obj.getClass().getName());
            return (Boolean)obj;
        }
        return null;
    }
    
    public Number getNumberMember(String memberName) {
        Object obj=members.get(memberName);
        if(obj!=null) {
            if(!(obj instanceof Number)) 
                throw new IllegalArgumentException("Member '"+memberName+"' has wrong type: "+obj.getClass().getName());
            return (Number)obj;
        }
        return null;
    }
    
    public JSONArray getArrayMember(String memberName) {
        Object obj=members.get(memberName);
        if(obj!=null) {
            if(!(obj instanceof JSONArray))
                throw new IllegalArgumentException("Member '"+memberName+"' has wrong type: "+obj.getClass().getName());
            return (JSONArray)obj;
        }
        return null;
    }
    
    public String toJSONString() {
        StringBuffer sb=new StringBuffer();
        sb.append(LCB);
        if(members!=null) {
            Iterator<String> it=getMemberNames();
            while(it.hasNext()) {
                String memberName=it.next();
                sb.append(ParserUtils.jsonEscape(memberName));
                sb.append(COL);
                Object memberValue=getMember(memberName);
                sb.append(ParserUtils.javaToJson(memberValue));
                if(it.hasNext()) sb.append(COM);
            }
        }
        sb.append(RCB);
        return sb.toString();
    }
    
}
