package org.pustefixframework.http;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

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

/**
 * Implementation of HttpServletRequest interface for simulating requests
 * on the server-side without being in a real request thread.
 */
public class VirtualHttpServletRequest implements HttpServletRequest {

    private ServletContext servletContext;
    private String requestURI;
    private Map<String, Object> attributes = new LinkedHashMap<String, Object>();
    private String characterEncoding;
    private String method;
    private String pathInfo;
    private byte[] content;
    private String contentType;
    private String localAddr;
    private String localName;
    private int localPort;
    private List<Locale> locales = new ArrayList<Locale>();
    private Map<String, String[]> parameters = new LinkedHashMap<String, String[]>();
    private String protocol = "http";
    private String remoteAddr = "127.0.0.1";
    private String remoteHost = "localhost";
    private int remotePort = 80;
    private String scheme = "http";
    private String serverName = "localhost";
    private int serverPort = 80;
    private boolean secure;
    private String authType;
    private String contextPath = "";
    private Cookie[] cookies;
    private Map<String, String[]> headers = new LinkedHashMap<String, String[]>();
    private String queryString;
    private String remoteUser;
    private String requestedSessionId;
    private String servletPath = "";
    private HttpSession session;
    private Principal userPrincipal;
    private boolean requestedSessionIdFromCookie = true;
    private boolean requestedSessionIdFromURL;
    private boolean requestedSessionIdValid = true;
    private Set<String> userRoles = new HashSet<String>();
    
    public VirtualHttpServletRequest() {
        this(null);
    }
    
    public VirtualHttpServletRequest(ServletContext servletContext) {
        this(servletContext, "GET", "/");
    }
    
    public VirtualHttpServletRequest(ServletContext servletContext, String method, String requestURI) {
        this.servletContext = servletContext;
        this.method = method;
        this.requestURI = requestURI;
        locales.add(Locale.ENGLISH);
    }
    
    @Override
    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return Collections.enumeration(attributes.keySet());
    }

    @Override
    public void removeAttribute(String name) {
        attributes.remove(name);
    }

    @Override
    public void setAttribute(String name, Object obj) {
        attributes.put(name, obj);
    }
    
    @Override
    public String getCharacterEncoding() {
        return characterEncoding;
    }

    @Override
    public void setCharacterEncoding(String characterEncoding) throws UnsupportedEncodingException {
        this.characterEncoding = characterEncoding;
    }
    
    @Override
    public int getContentLength() {
        return (content == null ? -1 : content.length);
    }

    @Override
    public String getContentType() {
        return contentType;
    }
    
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        if(content == null) {
            return null;
        } else {
            return new ServletInputStream() {
                
                ByteArrayInputStream in = new ByteArrayInputStream(content);
                    
                @Override
                public int read() throws IOException {
                    return in.read();
                }
            };
        }
    }

    @Override
    public String getLocalAddr() {
        return localAddr;
    }
    
    public void setLocalAddr(String localAddr) {
        this.localAddr = localAddr;
    }

    @Override
    public String getLocalName() {
        return localName;
    }
    
    public void setLocalName(String localName) {
        this.localName = localName;
    }

    @Override
    public int getLocalPort() {
        return localPort;
    }
    
    public void setLocalPort(int localPort) {
        this.localPort = localPort;
    }

    @Override
    public Locale getLocale() {
        return locales.get(0);
    }

    @Override
    public Enumeration<Locale> getLocales() {
        return Collections.enumeration(locales);
    }

    public void setLocales(List<Locale> locales) {
        this.locales = locales;
    }
    
    @Override
    public String getParameter(String name) {
        String[] values = parameters.get(name);
        if(values == null) {
            return null;
        } else {
            return values[0];
        }
    }

    public void addParameter(String name, String value) {
        String[] values = parameters.get(name);
        if(values == null) {
            values = new String[] {value};
        } else {
            values = Arrays.copyOf(values, values.length + 1);
            values[values.length - 1] = value;
        }
        parameters.put(name, values);
    }
    
    @Override
    public Map<String, String[]> getParameterMap() {
        return Collections.unmodifiableMap(parameters);
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return Collections.enumeration(parameters.keySet());
    }

    @Override
    public String[] getParameterValues(String name) {
        return parameters.get(name);
    }

    @Override
    public String getProtocol() {
        return protocol;
    }
    
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    @Override
    public BufferedReader getReader() throws IOException {
        if(content == null) {
            return null;
        } else {
            InputStream in = new ByteArrayInputStream(content);
            Reader reader;
            if(characterEncoding == null) {
                reader = new InputStreamReader(in);
            } else {
                reader = new InputStreamReader(in, characterEncoding);
            }
            return new BufferedReader(reader);
        }
    }

    @Override
    public String getRealPath(String path) {
        return servletContext.getRealPath(path);
    }

    @Override
    public String getRemoteAddr() {
        return remoteAddr;
    }
    
    public void setRemoteAddr(String remoteAddr) {
        this.remoteAddr = remoteAddr;
    }

    @Override
    public String getRemoteHost() {
        return remoteHost;
    }
    
    public void setRemoteHost(String remoteHost) {
        this.remoteHost = remoteHost;
    }

    @Override
    public int getRemotePort() {
        return remotePort;
    }
    
    public void setRemotePort(int remotePort) {
        this.remotePort = remotePort;
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String path) {
        throw new UnsupportedOperationException("getRequestDispatcher");
    }

    @Override
    public String getScheme() {
        return scheme;
    }
    
    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    @Override
    public String getServerName() {
        return serverName;
    }
    
    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    @Override
    public int getServerPort() {
        return serverPort;
    }
    
    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    @Override
    public boolean isSecure() {
        return secure;
    }
    
    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    @Override
    public String getAuthType() {
        return authType;
    }
    
    public void setAuthType(String authType) {
        this.authType = authType;
    }

    @Override
    public String getContextPath() {
        return contextPath;
    }
    
    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    @Override
    public Cookie[] getCookies() {
        return cookies;
    }
    
    public void setCookies(Cookie[] cookies) {
        this.cookies = cookies;
    }

    @Override
    public long getDateHeader(String name) {
        String value = getHeader(name);
        if(value == null) {
            return -1;
        } else {
            return Long.parseLong(value);
        }
    }

    public void addDateHeader(String name, long value) {
        addHeader(name, String.valueOf(value));
    }
    
    @Override
    public String getHeader(String name) {
        String[] values = headers.get(name);
        if(values == null) {
            return null;
        } else {
            return values[0];
        }
    }
    
    public void addHeader(String name, String value) {
        String[] values = headers.get(name);
        if(values == null) {
            values = new String[] {value};
        } else {
            values = Arrays.copyOf(values, values.length + 1);
            values[values.length - 1] = value;
        }
        headers.put(name, values);
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        return Collections.enumeration(headers.keySet());
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        List<String> values = new ArrayList<String>();
        String[] vals= headers.get(name);
        if(vals != null) {
            for(String val: vals) {
                values.add(val);
            }
        }
        return Collections.enumeration(values);
    }

    @Override
    public int getIntHeader(String name) {
        String value = getHeader(name);
        if(value == null) {
            return -1;
        } else {
            return Integer.parseInt(value);
        }
    }
    
    public void addIntHeader(String name, int value) {
        addHeader(name, String.valueOf(value));
    }

    @Override
    public String getMethod() {
        return method;
    }
    
    public void setMethod(String method) {
        this.method = method;
    }

    @Override
    public String getPathInfo() {
        return pathInfo;
    }
    
    public void setPathInfo(String pathInfo) {
        this.pathInfo = pathInfo;
    }

    @Override
    public String getPathTranslated() {
        if(getPathInfo() == null) {
            return null;
        } else {
            return getRealPath(getPathInfo());
        }
    }

    @Override
    public String getQueryString() {
        return queryString;
    }
    
    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    @Override
    public String getRemoteUser() {
        return remoteUser;
    }
    
    public void setRemoteUser(String remoteUser) {
        this.remoteUser = remoteUser;
    }

    @Override
    public String getRequestURI() {
        return requestURI;
    }
    
    public void setRequestURI(String requestURI) {
        this.requestURI = requestURI;
    }

    @Override
    public StringBuffer getRequestURL() {
        StringBuffer sb = new StringBuffer();
        sb.append(scheme).append("://").append(serverName);
        sb.append(":").append(serverPort).append(requestURI);
        return sb;
    }

    @Override
    public String getRequestedSessionId() {
        return requestedSessionId;
    }
    
    public void setRequestedSessionId(String requestedSessionId) {
        this.requestedSessionId = requestedSessionId;
    }

    @Override
    public String getServletPath() {
        return servletPath;
    }
    
    public void setServletPath(String servletPath) {
        this.servletPath = servletPath;
    }

    @Override
    public HttpSession getSession() {
        return session;
    }
    
    public void setSession(HttpSession session) {
        this.session = session;
        if(session instanceof VirtualHttpSession) {
            ((VirtualHttpSession)session).touch();
        }
    }

    @Override
    public HttpSession getSession(boolean create) {
        if(session == null && create) {
            session = new VirtualHttpSession(servletContext);
        }
        return session;
    }

    @Override
    public Principal getUserPrincipal() {
        return userPrincipal;
    }
    
    public void setUserPrincipal(Principal userPrincipal) {
        this.userPrincipal = userPrincipal;
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        return requestedSessionIdFromCookie;
    }
    
    public void setRequestedSessionIdFromCookie(boolean requestedSessionIdFromCookie) {
        this.requestedSessionIdFromCookie = requestedSessionIdFromCookie;
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        return requestedSessionIdFromURL;
    }

    public void setRequestedSessionIdFromURL(boolean requestedSessionIdFromURL) {
        this.requestedSessionIdFromURL = requestedSessionIdFromURL;
    }
    
    @Override
    public boolean isRequestedSessionIdFromUrl() {
        return isRequestedSessionIdFromURL();
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        return requestedSessionIdValid;
    }
    
    public void setRequestedSessionIdValid(boolean requestedSessionIdValid) {
        this.requestedSessionIdValid = requestedSessionIdValid;
    }

    @Override
    public boolean isUserInRole(String role) {
        return userRoles.contains(role);
    }
    
    public void addUserRole(String role) {
        userRoles.add(role);
    }

    @Override
    public ServletContext getServletContext() {
        return servletContext;
    }

    @Override
    public AsyncContext startAsync() throws IllegalStateException {
        throw new IllegalStateException("asynchronous operations not supported");
    }

    @Override
    public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException {
        throw new IllegalStateException("asynchronous operations not supported");
    }

    @Override
    public boolean isAsyncStarted() {
        return false;
    }

    @Override
    public boolean isAsyncSupported() {
        return false;
    }

    @Override
    public AsyncContext getAsyncContext() {
        return null;
    }

    @Override
    public DispatcherType getDispatcherType() {
        return DispatcherType.REQUEST;
    }

    @Override
    public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
        return false;
    }

    @Override
    public void login(String username, String password) throws ServletException {
    }

    @Override
    public void logout() throws ServletException {
    }

    @Override
    public Collection<Part> getParts() throws IOException, ServletException {
        return Collections.emptyList();
    }

    @Override
    public Part getPart(String name) throws IOException, ServletException {
        return null;
    }

}