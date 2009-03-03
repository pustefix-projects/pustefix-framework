/*
 * This file is part of PFIXCORE.
 *
 * PFIXCORE is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * PFIXCORE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with PFIXCORE; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package de.schlund.pfixcore.editor2.frontend.util;

import java.io.IOException;
import java.util.logging.LogRecord;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SessionCookieFilter implements Filter, java.util.logging.Filter {
    private final static String COOKIE_NAME = "STORED_SESSION_ID";
    public void destroy() {
        // Nothing to do here
    }

    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
            if (req instanceof HttpServletRequest && res instanceof HttpServletResponse) {
                HttpServletRequest httpReq = (HttpServletRequest) req;
                HttpServletResponse httpRes = (HttpServletResponse) res;
                
                String requestedId = httpReq.getRequestedSessionId();
                if (requestedId != null) {
                    if (httpReq.isRequestedSessionIdValid()) {
                        Cookie cookie = new Cookie(COOKIE_NAME, requestedId);
                        // String path = httpReq.getContextPath() + httpReq.getServletPath();
                        String path = (httpReq.getContextPath().length() > 0) ? httpReq.getContextPath() : "/";
                        if (!path.equals("/")) {
                            System.err.println(path);
                        }
                        cookie.setPath(path);
                        cookie.setMaxAge(-1);
                        httpRes.addCookie(cookie);
                    } else {
                        boolean foundCookie = false;
                        Cookie[] cookies = httpReq.getCookies();
                        if (cookies != null) {
                            for (int i = 0; i < cookies.length; i++) {
                                Cookie cookie = cookies[i];
                                if (cookie.getName().equals(COOKIE_NAME)) {
                                    cookie.setMaxAge(0);
                                    // String path = httpReq.getContextPath() + httpReq.getServletPath();
                                    String path = (httpReq.getContextPath().length() > 0) ? httpReq.getContextPath() : "/";
                                    cookie.setPath(path);
                                    cookie.setValue("ignore");
                                    httpRes.addCookie(cookie);
                                    foundCookie = true;
                                }
                            }
                        }
                        if (foundCookie) {
                            StringBuffer uri = new StringBuffer(httpReq.getRequestURI());
                            if (httpReq.getQueryString() != null) {
                                uri.append("?");
                                uri.append(httpReq.getQueryString());
                            }
                            httpRes.sendRedirect(uri.toString());
                            return;
                        }
                    }
                } else {
                    Cookie[] cookies = httpReq.getCookies();
                    String id = null;
                    if (cookies != null) {
                        for (int i = 0; i < cookies.length; i++) {
                            Cookie cookie = cookies[i];
                            if (cookie.getName().equals(COOKIE_NAME)) {
                                id = cookie.getValue();
                            }
                        }
                    }
                    if (id != null && id.trim().length() > 0 && !id.equals("ignore")) {
                        StringBuffer uri = new StringBuffer(httpReq.getRequestURI() + ";jsessionid=" + id);
                        if (httpReq.getQueryString() != null) {
                            uri.append("?");
                            uri.append(httpReq.getQueryString());
                        }
                        httpRes.sendRedirect(uri.toString());
                        return;
                    }
                }
            }
            chain.doFilter(req, res);
    }

    public void init(FilterConfig config) throws ServletException {
        // Nothing to do here
    }

    public boolean isLoggable(LogRecord record) {
        return false;
    }

}
