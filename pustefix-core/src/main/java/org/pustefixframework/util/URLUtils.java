package org.pustefixframework.util;

public class URLUtils {
    
    public static String getFirstPathComponent(String path) {
        if(path != null && path.length() > 0) {
            int ind = path.indexOf('/');
            if(ind < 0) {
                return path;
            } else if(ind == 0) {
                return getFirstPathComponent(path.substring(1));
            } else {
                return path.substring(0, ind);
            }
        } else {
            return null;
        }
    }
    
    public static String getParentPath(String path) {
        if(path!= null && path.length() > 0) {
            int ind = path.lastIndexOf('/');
            if(ind < 0) {
                return null;
            } else if(ind == 0) {
                if(path.length() == 1) {
                    return null;
                } else {
                    return "/";
                }
            } else {
                if(ind == path.length() -1) {
                    return getParentPath(path.substring(0, ind));
                } else {
                    return path.substring(0, ind);
                }
            }
        } else {
            return null;
        }
    }
    
    public static String removePathAttributes(String path) {
        int ind = path.indexOf(';');
        while(ind > -1) {
            String start = path.substring(0, ind);
            int to = path.indexOf('/', ind);
            path = (to > -1) ? start + path.substring(to) : start;
            ind = path.indexOf(';', ind);
        }
        return path;
    }

}
