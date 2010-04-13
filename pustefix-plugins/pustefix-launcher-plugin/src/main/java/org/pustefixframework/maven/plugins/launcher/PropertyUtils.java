package org.pustefixframework.maven.plugins.launcher;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class PropertyUtils {

    public static Map<String, List<String>> getPropertiesByPrefix(Properties properties, String prefix) {
        Map<String, List<String>> filteredProps = new HashMap<String, List<String>>();
        Enumeration<?> propertyNames = properties.propertyNames();
        while(propertyNames.hasMoreElements()) {
            String propertyName = (String)propertyNames.nextElement();
            if(propertyName.startsWith(prefix)) {
                String key = propertyName.substring(prefix.length());
                String value = properties.getProperty(propertyName);
                List<String> values = splitListValue(value);
                filteredProps.put(key, values);
            }
        }
        return filteredProps;
    }
 
    public static List<String> getProperty(Properties properties, String key) {
        String value = properties.getProperty(key);
        if(value != null) {
            return splitListValue(value);
        } else {
            return new ArrayList<String>();
        }
    }
    
    public static List<String> splitListValue(String value) {
        List<String> res = new ArrayList<String>();
        String[] values = value.split("(\\s+)|(\\s*,\\s*)");
        for(String s: values) {
            s = s.trim();
            if(!s.equals("")) {
                res.add(s);
            }
        }
        return res;
    }
    
}
