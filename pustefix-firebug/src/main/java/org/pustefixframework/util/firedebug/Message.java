package org.pustefixframework.util.firedebug;

import java.text.DecimalFormat;
import java.util.HashMap;


/**
 * Message
 * 
 * Each message encapsulates a single FireDebug log message.
 * 
 * @author Holger Rüprich
 */

public class Message {
    
    public final static String LOG = "LOG";
    public final static String INFO = "INFO";
    public final static String WARN = "WARN";
    public final static String ERROR = "ERROR";
    public final static String DUMP = "DUMP";
    public final static String TRACE = "TRACE";
    public final static String EXCEPTION = "EXCEPTION";
    public final static String TABLE = "TABLE"; 
    private final static int HEADER_LENGTH_LIMIT = 5000;
    private final static DecimalFormat DF = new DecimalFormat("00000000000"); 

    private int counter;
    private String jsonValue;
    private String label;
    private String type;
    private int header_length;
    
    private static int message_counter = 2;
    
    public Message(Object value, String type, JSONSerializer serializer) {
        this.jsonValue = serializer.javaToJson(value);
        this.type = type;
        this.counter = message_counter;  
        this.header_length = (int) Math.ceil((float)jsonValue.length() / HEADER_LENGTH_LIMIT);
        message_counter += header_length;
    }
    
    public Message(Object value, String label, String type, JSONSerializer serializer) {
        this(value, type, serializer);
        this.label = label;
    }

    public void addToResponseHeaders(HashMap<String, String> headers) {
        for (int i = 0; i < header_length; i++) {
            headers.put("X-FirePHP-Data-" + getHeaderKey(i), getHeaderValue(i * HEADER_LENGTH_LIMIT));
        }
    }
    
    private String getHeaderKey(int start) {
        if (type.equals(DUMP)) {
            return "2" + DF.format(counter + start);
        } else {
            return "3" + DF.format(counter + start);
        }
    }
    
    private String getHeaderValue(int start) {
        String result = "";
        
        if (start == 0) {
            result += getMessagePrefix(); 
        }
        
        result += getMessageValuePart(start);
        
        if ((start == 0 && header_length == 1) || start / HEADER_LENGTH_LIMIT + 1 == header_length) {
            result += getMessageSuffix(); 
        }
        
        return result;
    }

    private String getMessageValuePart(int start) {
        int end = start + HEADER_LENGTH_LIMIT;
        if (end > jsonValue.length()) {
            end = jsonValue.length();
        }
        return jsonValue.substring(start, end);
    }
    
    private String getMessagePrefix() {
        if (type.equals(DUMP)) {
            return "\"" + label + "\":";
        } else {
            if (label == null) {
                return "[\"" + type + "\",";            
            } else {
                return "[\"" + type + "\",[\"" + label + "\",";
            }
        }  
    }
    
    private String getMessageSuffix() {
        if (type.equals(DUMP)) {
            return ",";
        } else {
            if (label == null) {
                return "],";            
            } else {
                return "]],";
            }
        }
    }

}