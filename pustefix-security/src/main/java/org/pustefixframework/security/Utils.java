package org.pustefixframework.security;

public class Utils {
    
    public static String removeLineBreaks(String value) {
        value = value.replace('\n', ' ');
        value = value.replace('\r', ' ');
        return value;
    }

}
