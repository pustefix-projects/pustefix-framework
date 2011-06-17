package org.pustefixframework.util;

import java.util.Locale;

public class LocaleUtils {

    public static Locale getLocale(String localeString) {
        String[] parts = localeString.split("[-_]");
        if(parts.length == 1) {
            return new Locale(parts[0].toLowerCase());
        } else if(parts.length == 2) {
            return new Locale(parts[0].toLowerCase(), parts[1].toUpperCase());
        } else if(parts.length == 3) {
            return new Locale(parts[0].toLowerCase(), parts[1].toUpperCase(), parts[2]);
        } else {
            throw new IllegalArgumentException("Illegal locale string format: " + localeString);
        }
    }
    
    public static void main(String[] args) {
        System.out.println(getLocale("en-us"));
        System.out.println(getLocale("en_US"));
    }
    
}
