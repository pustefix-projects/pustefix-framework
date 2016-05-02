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

package de.schlund.pfixcore.scriptedflow.vm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

public class VirtualHttpServletRequest implements HttpServletRequest {
    private HttpServletRequest orig;

    private String pagename = null;

    private Map<String, String[]> params;

    private String queryString;

    // Constructor is intenionally in default scope
    // as it should only be used within this package
    VirtualHttpServletRequest(HttpServletRequest originalRequest,
            String pagename, Map<String, String[]> params) {
        this.orig = originalRequest;
        this.pagename = pagename;
        this.params = params;

        StringBuffer qs = new StringBuffer();
        for (String key : params.keySet()) {
            String[] values = params.get(key);
            if (values != null) {
                for (int i = 0; i < values.length; i++) {
                    qs.append('&');
                    qs.append(key);
                    if (values[i] != null && values[i].length() > 0) {
                        qs.append('=');
                        qs.append(values[i]);
                    }
                }
            }
        }
        if (qs.length() > 0) {
            // Remove leading '&'
            qs.deleteCharAt(0);
        }
        queryString = qs.toString();
    }

    public String getAuthType() {
        return orig.getAuthType();
    }

    public Cookie[] getCookies() {
        return orig.getCookies();
    }

    public long getDateHeader(String arg0) {
        return orig.getDateHeader(arg0);
    }

    public String getHeader(String arg0) {
        return orig.getHeader(arg0);
    }

    @SuppressWarnings("rawtypes")
    public Enumeration getHeaders(String arg0) {
        return orig.getHeaders(arg0);
    }

    @SuppressWarnings("rawtypes")
    public Enumeration getHeaderNames() {
        return orig.getHeaderNames();
    }

    public int getIntHeader(String arg0) {
        return orig.getIntHeader(arg0);
    }

    public String getMethod() {
        // A virtual request is always a GET request
        return "GET";
    }

    public String getPathInfo() {
        if (pagename == null) {
            return null;
        } else {
            return "/" + pagename;
        }
    }

    public String getPathTranslated() {
        if (getPathInfo() != null) {
            return getRealPath(getPathInfo());
        } else {
            return null;
        }
    }

    public String getContextPath() {
        return orig.getContextPath();
    }

    public String getQueryString() {
        return queryString;
    }

    public String getRemoteUser() {
        return orig.getRemoteUser();
    }

    public boolean isUserInRole(String arg0) {
        return orig.isUserInRole(arg0);
    }

    public Principal getUserPrincipal() {
        return orig.getUserPrincipal();
    }

    public String getRequestedSessionId() {
        return orig.getRequestedSessionId();
    }

    public String getRequestURI() {
        // Construct the request URI using servlet path
        // and path info
        String contextpath = getContextPath();
        String servletpath = getServletPath();
        String pathinfo = getPathInfo();
        if (pathinfo == null && servletpath == null) {
            if (contextpath.length() == 0) {
                return "/";
            } else {
                return contextpath;
            }
        } else if (servletpath == null) {
            return contextpath + pathinfo;
        } else if (pathinfo == null) {
            return contextpath + servletpath;
        } else {
            return contextpath + servletpath + pathinfo;
        }
    }

    public StringBuffer getRequestURL() {
        // Construct the URL
        StringBuffer buffer = new StringBuffer();
        String scheme = getScheme();
        buffer.append(scheme);
        buffer.append("://");
        buffer.append(getLocalName());
        int port = getLocalPort();
        if (scheme.equals("http") && port != 80) {
            buffer.append(":" + String.valueOf(port));
        } else if (scheme.equals("https") && port != 443) {
            buffer.append(":" + String.valueOf(port));
        }
        buffer.append(getRequestURI());
        return buffer;
    }

    public String getServletPath() {
        return orig.getServletPath();
    }

    public HttpSession getSession(boolean arg0) {
        return orig.getSession(arg0);
    }

    public HttpSession getSession() {
        return orig.getSession();
    }

    public boolean isRequestedSessionIdValid() {
        return orig.isRequestedSessionIdValid();
    }

    public boolean isRequestedSessionIdFromCookie() {
        return orig.isRequestedSessionIdFromCookie();
    }

    public boolean isRequestedSessionIdFromURL() {
        return orig.isRequestedSessionIdFromURL();
    }

    @SuppressWarnings("deprecation")
    public boolean isRequestedSessionIdFromUrl() {
        return orig.isRequestedSessionIdFromUrl();
    }

    public Object getAttribute(String arg0) {
        return orig.getAttribute(arg0);
    }

    @SuppressWarnings("rawtypes")
    public Enumeration getAttributeNames() {
        return orig.getAttributeNames();
    }

    public String getCharacterEncoding() {
        return orig.getCharacterEncoding();
    }

    public void setCharacterEncoding(String arg0)
            throws UnsupportedEncodingException {
        throw new RuntimeException("Cannot set encoding on a virtual request!");
    }

    public int getContentLength() {
        // Virtual requests have no request body
        return -1;
    }

    public String getContentType() {
        // Virtual requests have no request body
        return null;
    }

    public ServletInputStream getInputStream() throws IOException {
        // Return empty stream
        return new ServletInputStream() {
            @Override
            public int read() throws IOException {
                return -1;
            }
        };
    }

    public String getParameter(String arg0) {
        String[] values = params.get(arg0);
        if (values == null || values.length < 1) {
            return null;
        } else {
            return values[0];
        }
    }

    @SuppressWarnings("rawtypes")
    public Enumeration getParameterNames() {
        return Collections.enumeration(params.keySet());
    }

    public String[] getParameterValues(String arg0) {
        return params.get(arg0);
    }

    @SuppressWarnings("rawtypes")
    public Map getParameterMap() {
        return Collections.unmodifiableMap(params);
    }

    public String getProtocol() {
        return orig.getProtocol();
    }

    public String getScheme() {
        return orig.getScheme();
    }

    public String getServerName() {
        return orig.getServerName();
    }

    public int getServerPort() {
        return orig.getServerPort();
    }

    public BufferedReader getReader() throws IOException {
        // Return empty reader
        return new BufferedReader(new StringReader(""));
    }

    public String getRemoteAddr() {
        return orig.getRemoteAddr();
    }

    public String getRemoteHost() {
        return orig.getRemoteHost();
    }

    public void setAttribute(String arg0, Object arg1) {
        orig.setAttribute(arg0, arg1);
    }

    public void removeAttribute(String arg0) {
        orig.removeAttribute(arg0);
    }

    public Locale getLocale() {
        return orig.getLocale();
    }

    @SuppressWarnings("rawtypes")
    public Enumeration getLocales() {
        return orig.getLocales();
    }

    public boolean isSecure() {
        return orig.isSecure();
    }

    public RequestDispatcher getRequestDispatcher(String arg0) {
        return orig.getRequestDispatcher(arg0);
    }

    @SuppressWarnings("deprecation")
    public String getRealPath(String arg0) {
        return orig.getRealPath(arg0);
    }

    public int getRemotePort() {
        return orig.getRemotePort();
    }

    public String getLocalName() {
        return orig.getLocalName();
    }

    public String getLocalAddr() {
        return orig.getLocalAddr();
    }

    public int getLocalPort() {
        return orig.getLocalPort();
    }

    /**
     * Create a new HttpServletRequest based on the given request, but without
     * any request parameters or path info.
     * 
     * @param orig Original request to use as a base for the new reqeust
     * @return new request which simply queries the servlet without giving
     *         any parameters or path info
     */
    public static HttpServletRequest getVoidRequest(HttpServletRequest orig) {
        return new VirtualHttpServletRequest(orig, null,
                new HashMap<String, String[]>());
    }

    //new methods introduced with Servlet API 3, which aren't used/implemented here
    
    public ServletContext getServletContext() {
        throw new UnsupportedOperationException();
    }

    public AsyncContext startAsync() throws IllegalStateException {
        throw new UnsupportedOperationException();
    }

    public AsyncContext startAsync(ServletRequest servletRequest,
            ServletResponse servletResponse) throws IllegalStateException {
        throw new UnsupportedOperationException();
    }

    public boolean isAsyncStarted() {
        throw new UnsupportedOperationException();
    }

    public boolean isAsyncSupported() {
        throw new UnsupportedOperationException();
    }

    public AsyncContext getAsyncContext() {
        throw new UnsupportedOperationException();
    }

    public DispatcherType getDispatcherType() {
        throw new UnsupportedOperationException();
    }

    public boolean authenticate(HttpServletResponse response)
            throws IOException, ServletException {
        throw new UnsupportedOperationException();
    }

    public void login(String username, String password) throws ServletException {
        throw new UnsupportedOperationException();
    }

    public void logout() throws ServletException {
        throw new UnsupportedOperationException();
    }

    public Collection<Part> getParts() throws IOException, ServletException {
        throw new UnsupportedOperationException();
    }

    public Part getPart(String name) throws IOException, ServletException {
        throw new UnsupportedOperationException();
    }

}
