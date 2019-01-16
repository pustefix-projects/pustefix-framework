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
package org.pustefixframework.web;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

import javax.servlet.ServletContext;
import javax.servlet.SessionTrackingMode;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.pustefixframework.util.NetUtils;
import org.pustefixframework.util.net.IPRangeMatcher;

/**
 * Miscellaneous utility methods for Servlet API usage.
 */
public class ServletUtils {

    public final static String SESSION_ATTR_SESSION_MUTEX = "__PFX_SESSION_MUTEX__";
    public final static String SESSION_ATTR_LOCK = "__PFX_SESSION_LOCK__";
    public final static String SESSION_ATTR_VISIT_ID = "__VISIT_ID__";

    public final static String DEFAULT_SESSION_COOKIE_NAME = "JSESSIONID";
    public final static String SESSION_PATH_PARAM_NAME = "jsessionid";

    private final static IPRangeMatcher PRIVATE_IP_RANGE = new IPRangeMatcher("10.0.0.0/8", "169.254.0.0/16",
            "172.16.0.0/12", "192.168.0.0/16", "fc00::/7");

    /**
     * Returns SessionMutex object bound to the session, e.g. for
     * synchronizing access to session attributes.
     * @param session - current HttpSession
     * @return SessionMutex object bound to current session
     */
    public static Object getSessionMutex(HttpSession session) {
        return session.getAttribute(SESSION_ATTR_SESSION_MUTEX);
    }

    /**
     * Returns ReadWriteLock object bound to the session, e.g. for
     * synchronizing session invalidation.
     * @param session - current HttpSession
     * @return ReadWriteLock object bound to current session
     */
    public static ReadWriteLock getSessionLock(HttpSession session) {
        return (ReadWriteLock)session.getAttribute(SESSION_ATTR_LOCK);
    }

    /**
     * Invalidates the current HttpSession using a ReadWriteLock,
     * thus allowing parallel requests using this session to
     * finish before.
     * @param session - current HttpSession
     */
    public static void invalidate(HttpSession session) {
        ReadWriteLock lock = getSessionLock(session);
        if(lock != null) {
            Lock writeLock = lock.writeLock();
            writeLock.lock();
            try {
                session.invalidate();
            } finally {
                writeLock.unlock();
            }
        } else {
            session.invalidate();
        }
    }

    /**
     * Reconstructs the current request URL for redirecting, optionally
     * setting a new scheme (automatically changing the port number).
     * @param req - current HttpServletRequest
     * @param newScheme - new scheme
     * @return the current request URL with changed scheme/port
     */
    public static String getRedirectURL(HttpServletRequest req, String newScheme) {
        int port = req.getServerPort();
        if("https".equals(newScheme)) {
            port = getHTTPSPort(req);
        }
        return getRequestURL(req, false, newScheme, port);
    }

    /**
     * Reconstructs the current request URL for redirecting, optionally
     * stripping an existing session parameter.
     * @param req - current HttpServletRequest
     * @param stripSession - sets if session parameter should be stripped
     * @return the current request URL
     */
    public static String getRedirectURL(HttpServletRequest req, boolean stripSession) {
        return getRequestURL(req, stripSession, null, -1);
    }

    private static String getRequestURL(HttpServletRequest req, boolean stripSession, String newScheme, int newPort) {
        StringBuilder sb = new StringBuilder();
        String scheme;
        if(newScheme == null) {
            scheme = req.getScheme();
        } else {
            scheme = newScheme;
        }
        int port;
        if(newPort <= 0) {
            port = req.getServerPort();
        } else {
            port = newPort;
        }
        sb.append(scheme).append("://").append(req.getServerName());
        if((scheme.equals("http") && (port != 80)) || (scheme.equals("https") && (port != 443))) {
            sb.append(':').append(port);
        }
        String requestURI = req.getRequestURI();
        if(stripSession) {
            int ind = requestURI.indexOf(";jsessionid");
            if(ind > -1) {
                requestURI = requestURI.substring(0, ind);
            }
        }
        sb.append(requestURI);
        if(req.getQueryString() != null) {
            sb.append("?").append(req.getQueryString());
        }
        return sb.toString();
    }

    /**
     * Performs redirect by setting according HTTP status and headers
     * at HttpServletResponse.
     * @param req - current HttpServletRequest
     * @param res - current HttpServletResponse
     * @param status - response status code
     */
    public static void redirect(HttpServletRequest req, HttpServletResponse res, int status) {
        String redirectURL = res.encodeRedirectURL(getRequestURL(req, true, null, 0));
        redirect(res, status, redirectURL);
    }

    /**
     * Performs redirect to HTTPS by setting according HTTP status and headers
     * at HttpServletResponse.
     * @param req - current HttpServletRequest
     * @param res - current HttpServletResponse
     * @param status - response status code
     */
    public static void redirectToSSL(HttpServletRequest req, HttpServletResponse res, int status) {
        int port = ServletUtils.getHTTPSPort(req);
        String redirectURL;
        if(!req.isSecure() && req.isRequestedSessionIdFromURL() && req.isRequestedSessionIdValid()) {
            redirectURL = getRequestURL(req, false, "https", port);
        } else {
            redirectURL = res.encodeRedirectURL(getRequestURL(req, true, "https", port));
        }
        redirect(res, status, redirectURL);
    }

    /**
     * Performs redirect by setting according HTTP status and headers
     * at HttpServletResponse.
     * @param res - current HttpServletResponse
     * @param status - response status code
     * @param location - redirect location
     */
    public static void redirect(HttpServletResponse res, int status, String location) {
        res.setHeader("Expires", "Mon, 26 Jul 1997 05:00:00 GMT");
        res.setHeader("Pragma", "no-cache");
        res.setHeader("Cache-Control", "no-cache, no-store, private, must-revalidate");
        res.setStatus(status);
        res.setHeader("Location", location);
    }

    /**
     * Returns the HTTPS port configured as ServletContext init parameter
     * or, if not available, tries to resolve it from the HTTP port.
     * @param req - current HttpServletRequest
     * @return the HTTPS port
     */
    public static int getHTTPSPort(HttpServletRequest req) {

        int httpsPort = 443;
        String param = req.getServletContext().getInitParameter("pustefix.https.port");
        if(param != null) {
            httpsPort = Integer.parseInt(param);
        } else {
            //try derived standard port
            int base = req.getServerPort() - 80;
            if(base % 100 == 0) {
                httpsPort = base + 443;
            }
        }
        return httpsPort;
    }

    /**
     * Returns request cookie with the specified name.
     * @param req - current HttpServletRequest
     * @param cookieName - cookie name
     * @return matching cookie or null otherwise
     */
    public static Cookie getCookie(HttpServletRequest req, String cookieName) {
        Cookie[] cookies = req.getCookies();
        if(cookies != null) {
            for(Cookie cookie : cookies) {
                if(cookieName.equals(cookie.getName())) {
                    return cookie;
                }
            }
        }
        return null;
    }

    /**
     * Check if request sent a cookie with the specified name and value.
     * @param request - current HttpServletRequest
     * @param name - cookie name
     * @param value - cookie value
     * @return returns true if matching cookie was found, false otherwise
     */
    public static boolean hasCookie(HttpServletRequest request, String name, String value) {
        Cookie cookie = getCookie(request, name);
        if(cookie != null) {
            return value.equals(cookie.getValue());
        }
        return false;
    }

    /**
     * Returns the session cookie from the request.
     * @param req - current HttpServletRequest
     * @return returns the session cookie or null if not available.
     */
    public static Cookie getSessionCookie(HttpServletRequest req) {
        String cookieName = getSessionCookieName(req.getServletContext());
        return getCookie(req, cookieName);
    }

    /**
     * Return the session cookie name, i.e. the configured value
     * or JSESSIONID as default.
     * @param context - current ServletContext
     * @return the session cookie name
     */
    public static String getSessionCookieName(ServletContext context) {
        String cookieName = context.getSessionCookieConfig().getName();
        if(cookieName == null) {
            cookieName = DEFAULT_SESSION_COOKIE_NAME;
        }
        return cookieName;
    }

    /**
     * Returns session cookie path, i.e. the configured value or
     * context path as default.
     * @param context - current ServletContext
     * @return the session cookie path
     */
    public static String getSessionCookiePath(ServletContext context) {
        String cookiePath = context.getSessionCookieConfig().getPath();
        if(cookiePath == null) {
            cookiePath = context.getContextPath();
        }
        if(!cookiePath.endsWith("/")) {
            cookiePath += "/";
        }
        return cookiePath;
    }

    /**
     * Deletes session cookie by adding it to response with zero expiration and empty value.
     * @param req - current HttpServletRequest
     * @param res - current HttpServletResponse
     */
    public static void resetSessionCookie(HttpServletRequest req, HttpServletResponse res) {
        String cookieName = getSessionCookieName(req.getServletContext());
        String cookiePath = getSessionCookiePath(req.getServletContext());
        Cookie cookie = new Cookie(cookieName, "");
        cookie.setMaxAge(0);
        cookie.setPath(cookiePath);
        res.addCookie(cookie);
    }

    /**
     * Adds cookie to response using default maxage and context path.
     * @param req - current HttpServletRequest
     * @param res - current HttpServletResponse
     * @param cookieName - the cookie name
     * @param cookieValue - the cookie value
     */
    public static void addCookie(HttpServletRequest req, HttpServletResponse res, String cookieName, String cookieValue) {
        String cookiePath = req.getServletContext().getContextPath();
        if(!cookiePath.endsWith("/")) {
            cookiePath += "/";
        }
        Cookie cookie = new Cookie(cookieName, cookieValue);
        cookie.setPath(cookiePath);
        res.addCookie(cookie);
    }

    /**
     * Returns the client IP. Incorporates the X-Forwarded-For
     * header and returns the first IP which is not part of
     * the known private IP ranges, thus skipping internal IPs,
     * like proxies, etc.
     * @param req - current HttpServletRequest
     * @return the real client IP
     */
    public static String getRemoteAddr(HttpServletRequest req) {
        String remoteIp = req.getRemoteAddr();
        String forward = req.getHeader("X-Forwarded-For");
        if (forward != null && !forward.equals("")) {
            if(PRIVATE_IP_RANGE.matches(remoteIp)) {
                String[] ips = forward.split(",");
                for(int i=ips.length - 1; i >= 0; i--) {
                    String ip = ips[i].trim();
                    if(ip.length() > 0) {
                        if(NetUtils.checkIP(ip) && !PRIVATE_IP_RANGE.matches(ip)) {
                            remoteIp = ip;
                            break;
                        }
                    }
                }
            }
        }
        return remoteIp;
    }

    /**
     * Returns session id URL path parameter, if session doesn't come from
     * cookie and URL session tracking mode is enabled, returns empty string
     * otherwise.
     * @param req - current HttpServletRequest
     * @return session id path parameter or empty string if session comes from cookie
     */
    public static String getSessionIdPath(HttpServletRequest req) {
        String sessionIdPath = "";
        HttpSession session = req.getSession(false);
        if (session != null && !req.isRequestedSessionIdFromCookie() &&
                req.getServletContext().getEffectiveSessionTrackingModes().contains(SessionTrackingMode.URL)) {
            sessionIdPath = ";" + SESSION_PATH_PARAM_NAME + "=" + session.getId();
        }
        return sessionIdPath;
    }

    /**
     * Returns the request URI, stripping the jsessionid parameter if exisiting.
     * @param req - the request
     * @return request URI without session parameter
     */
    public static String getUnencodedRequestURI(HttpServletRequest req) {
        String uri = req.getRequestURI();
        int ind = uri.indexOf(";jsessionid");
        if(ind > -1) {
            uri = uri.substring(0, ind);
        }
        return uri;
    }

}
