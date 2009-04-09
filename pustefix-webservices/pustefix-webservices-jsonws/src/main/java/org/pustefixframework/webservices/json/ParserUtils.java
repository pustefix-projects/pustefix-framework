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

import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.pustefixframework.webservices.json.parser.ParseException;


/**
 * @author mleidig@schlund.de
 */
public class ParserUtils {

    final static char[] ESC_CHARS={'b','f','n','r','t','\\','"'};
    final static char[] ESC_MAP={'\b','\f','\n','\r','\t','\\','"'};
    
    final static Pattern DATE_PATTERN=Pattern.compile("new Date\\((0|[1-9][0-9]*)\\)");
    final static Pattern DATE_UTC_PATTERN=Pattern.compile("new Date\\(Date.UTC\\(((0|[1-9]([0-9])*)(,(0|[1-9]([0-9])*))*)\\)\\)");
    final static Pattern COMMA_SPLIT_PATTERN=Pattern.compile(",");
    
    final static int[] CALENDAR_FIELDS={Calendar.YEAR,Calendar.MONTH,Calendar.DATE,
        Calendar.HOUR_OF_DAY,Calendar.MINUTE,Calendar.SECOND,Calendar.MILLISECOND};
    
    public static String jsonToJava(String str) throws ParseException {
        StringBuilder sb=new StringBuilder();
        int strLen=str.length()-1;
        for(int i=1;i<strLen;i++) {
            char ch=str.charAt(i);
            if(ch=='\\') {
                if(i<strLen) {
                    i++;
                    ch=str.charAt(i);
                    int found=-1;;
                    for(int k=0;k<ESC_CHARS.length&&found==-1;k++) {
                        if(ch==ESC_CHARS[k]) found=k;
                    }
                    if(found>-1) sb.append(ESC_MAP[found]);
                    else {
                        if(ch=='u') {
                            if((i+4)<strLen) {
                                String hexStr=str.substring(i+1,i+5);
                                ch=(char)Integer.parseInt(hexStr,16);
                                i+=4;
                            } else throw new ParseException("Premature end of JSON string: "+str);
                        }
                        sb.append(ch);
                    }
                } else throw new ParseException("Premature end of JSON string: "+str);
            } else sb.append(ch);
        }
        return sb.toString();
    }
    
    public static String jsonEscape(String str) {
        StringBuilder sb=new StringBuilder();
        sb.append("\"");
        for(int i=0;i<str.length();i++) {
            char ch=str.charAt(i);
            int found=-1;
            for(int k=0;k<ESC_MAP.length&&found==-1;k++) {
                if(ch==ESC_MAP[k]) found=k;
            }
            if(found>-1) sb.append("\\"+ESC_CHARS[found]);
            else if(ch<'\u0020') {
                String hexStr=Integer.toHexString(ch);
                if(hexStr.length()==1) hexStr="0"+hexStr;
                sb.append("\\u00"+hexStr);
            } else sb.append(ch);   
        }
        sb.append("\"");
        return sb.toString();
    }
    
    public static String javaToJson(Object obj) {
        if(obj==null) {
            return "null";
        } else if(obj instanceof String) {
            return jsonEscape((String)obj);
        } else if(obj instanceof Boolean) {
            return obj.toString();
        } else if(obj instanceof Number) {
            return obj.toString();
        } else if(obj instanceof JSONValue) {
            return ((JSONValue)obj).toJSONString();
        } else throw new IllegalArgumentException("Type not supported: "+obj.getClass().getName());
    }
    
    public static Number parseNumber(String numStr) {
        if(numStr.contains(".")||numStr.contains("e")||numStr.contains("E")) {
            return Double.valueOf(numStr);
        } else {
            Number num=null;
            try {
                num=Integer.valueOf(numStr);
            } catch(NumberFormatException x) {
                num=Long.valueOf(numStr);
            }
            return num;
        }
    }
    
    public static Calendar parseUTCDate(String dateStr) {
        Matcher mat=DATE_PATTERN.matcher(dateStr);
        if(mat.matches()) {
            long time=Long.parseLong(mat.group(1));
            Calendar cal=Calendar.getInstance();
            cal.setTimeInMillis(time);
            return cal;
        }
        return null;
    }
    
    public static Calendar parseUTCFuncDate(String dateStr) {
        Calendar cal=Calendar.getInstance();
        Matcher mat=DATE_UTC_PATTERN.matcher(dateStr);
        if(mat.matches()) {
            String[] vals=COMMA_SPLIT_PATTERN.split(mat.group(1));
            for(int i=0;i<vals.length;i++) {
                int val=Integer.parseInt(vals[i]);
                cal.set(CALENDAR_FIELDS[i],val);  
            }
        }
        return cal;
    }
    
    public static Calendar parseDate(String dateStr) {
        Calendar cal=parseUTCDate(dateStr);
        if(cal==null) cal=parseUTCFuncDate(dateStr);
        return cal;
    }
    
}
