package de.schlund.pfixxml.util;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

/**
 * @author mleidig@schlund.de
 */
public class CookieUtils {
    
    public static Cookie[] getCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if(cookies == null) {
            String header = request.getHeader("Cookie");
            if(header != null) {
                cookies=getCookies(header);
            }
        }
        return cookies;
    }
        
    public static Cookie[] getCookies(String cookieHeader) {
        Cookie[] cookies = null;
        if(cookieHeader != null && cookieHeader.length()>0) {
            String header = cookieHeader;
            List<Cookie> list = new ArrayList<Cookie>();
            while (header.length() > 0) {
                int pos = header.indexOf(';');
                if (pos < 0) pos = header.length();
                if (pos == 0) break;
                String token = header.substring(0, pos);
                if (pos < header.length()) header = header.substring(pos + 1);
                else header = "";
                int sign = token.indexOf('=');
                if (sign > 0) {
                    String name = token.substring(0, sign).trim();
                    String value = token.substring(sign+1).trim();
                    try {
                        Cookie cookie = new Cookie(name, value);
                        list.add(cookie);
                    } catch(IllegalArgumentException x) {
                        //Ignore reserved names like "Expires", "Path", "$Path", "$Version"
                        //TODO: support cookie attributes
                    }    
                }
            }
            cookies = new Cookie[list.size()];
            list.toArray(cookies);
        }
        return cookies;
    }
    
}
