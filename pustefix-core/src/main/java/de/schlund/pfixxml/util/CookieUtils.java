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
 */
package de.schlund.pfixxml.util;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

/**
 * @author mleidig@schlund.de
 */
public class CookieUtils {
    
    private static Logger LOG = Logger.getLogger(CookieUtils.class);
    
    public static Cookie[] getCookies(HttpServletRequest request) {
        //Workaround for cookie loss problem: 
        //Despite receiving a non-empty request cookie header from the browser Tomcat
        //sometimes inexplicably returns null calling HttpServletRequest.getCookies().
        //In this case we directly parse the cookie header by calling the utility
        //method CookieUtils.getCookies().
        Cookie[] cookies = request.getCookies();
        if(cookies != null && cookies.length == 0) {
            cookies = null;
        }
        if(cookies == null) {
            String header = request.getHeader("Cookie");
            if(header != null) {
                cookies=getCookies(header);
                if(cookies != null) {
                    String userAgent = request.getHeader("User-Agent");
                    if (userAgent == null) userAgent = "-";
                    String cookieHeader = request.getHeader("Cookie");
                    LOG.warn("COOKIE_LOSS_WORKAROUND|" + userAgent + "|" + cookieHeader);
                }
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
