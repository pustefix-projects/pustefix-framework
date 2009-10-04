package org.pustefixframework.resource.support;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DynamicResourceUtils {

    private static Pattern DYNAMIC_URI = Pattern.compile("^dynamic:(//[^/]+)?(.*)");
   
    public static String setBundleName(String uriStr, String bundleName) {
        Matcher matcher = DYNAMIC_URI.matcher(uriStr);
        if(matcher.matches()) {
            uriStr = "dynamic://" + bundleName + (matcher.group(2).startsWith("/")?"":"/") + matcher.group(2);
        }
        return uriStr;
    }
    
    public static String setBasePath(String uriStr, String basePath) {
        Matcher matcher = DYNAMIC_URI.matcher(uriStr);
        if(matcher.matches() && !matcher.group(2).startsWith(basePath)) {
            if(!basePath.startsWith("/")) basePath = "/" + basePath;
            if(basePath.endsWith("/")) basePath = basePath.substring(0, basePath.length()-1);
            uriStr = "dynamic:" + (matcher.group(1)!=null?matcher.group(1):"") + basePath + (matcher.group(2).startsWith("/")?"":"/") + matcher.group(2);
        }
        return uriStr;
    }
    
}
