package org.pustefixframework.util;

import java.util.regex.Pattern;

public class NetUtils {

    private static String IPV4_REGEXP = "(([0-9])|([1-9][0-9])|(1[0-9][0-9])|(2[0-4][0-9])|(25[0-5]))";
    private static Pattern IPV4_PATTERN = Pattern.compile(IPV4_REGEXP + "\\." + IPV4_REGEXP + "\\." + IPV4_REGEXP + "\\." + IPV4_REGEXP);
    
    private static String IPV6_REGEXP = "[0-9a-fA-F]{1,4}";
    private static Pattern IPV6_PATTERN = Pattern.compile(IPV6_REGEXP);
    
    public static boolean checkIP(String ip) {
        boolean ok = checkIPv4(ip);
        if(!ok) {
            ok = checkIPv6(ip);
        }
        return ok;
    }
    
    public static boolean checkIPv4(String ip) {
        return IPV4_PATTERN.matcher(ip).matches();    
    }
    
    public static boolean checkIPv6(String ip) {
        boolean conColon = false;
        int groupNo = 0;
        int start = -1;
        int end = -1;
        char last = 0;
        for(int i=0; i<ip.length(); i++) {
            if(ip.charAt(i) == ':') {
                if(start > -1) {
                    if(i == ip.length()-1) {
                        return false;
                    }
                    groupNo++;
                    if(groupNo>8) {
                        return false;
                    }
                    boolean ok = IPV6_PATTERN.matcher(ip.substring(start, end+1)).matches();
                    if(!ok) {
                        return false;
                    }
                    start = -1;
                    end = -1;
                } else if(last == ':') {
                    if(conColon) {
                        return false;
                    } else {
                        conColon = true;
                        groupNo++;
                    }
                }
            } else {
                if(i == 1 && last == ':') {
                    return false;
                }
                if(start == -1) {
                    start = i;
                }
                end = i;
            }
            last = ip.charAt(i);
        }
        if(start > -1) {
            groupNo++;
            if(groupNo>8) {
                return false;
            }
            boolean ok = IPV6_PATTERN.matcher(ip.substring(start, end+1)).matches();
            if(!ok) {
                return false;
            }
        }
        if(groupNo < 8 && !conColon) {
            return false;
        }
        return true;
    }
    
   
    
}
