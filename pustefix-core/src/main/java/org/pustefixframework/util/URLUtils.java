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
    

    
    public static void main(String[] args) {
        System.out.println(getFirstPathComponent(null));
        System.out.println(getFirstPathComponent(""));
        System.out.println(getFirstPathComponent("/ "));
        System.out.println(getFirstPathComponent("//"));
        System.out.println(getFirstPathComponent("/foo"));
        System.out.println(getFirstPathComponent("/foo/bar"));
        System.out.println(getFirstPathComponent("/foo/bar/baz"));
        System.out.println(getFirstPathComponent("foo"));
        System.out.println(getFirstPathComponent("foo/bar"));
        System.out.println(getFirstPathComponent("foo/bar/baz"));
    }

}
