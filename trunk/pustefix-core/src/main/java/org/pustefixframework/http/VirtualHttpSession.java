package org.pustefixframework.http;

import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;
import javax.servlet.http.HttpSessionContext;

/**
 * Implementation of HttpSession interface for simulating sessions
 * on the server-side without being in a real request thread.
 */
public class VirtualHttpSession implements HttpSession {

    private static AtomicInteger generatedId = new AtomicInteger();
    
    private ServletContext servletContext;
    private String id;
    private Map<String, Object> attributes = new LinkedHashMap<String, Object>();
    private long creationTime = System.currentTimeMillis();
    private long lastAccessedTime = creationTime;
    private int interval = 60 * 60;
    private boolean isNew = true;
    private boolean invalid;
    
    public VirtualHttpSession() {
        this(null);
    }
    
    public VirtualHttpSession(ServletContext servletContext) {
        this(servletContext, String.valueOf(generatedId.incrementAndGet()));
    }
    
    public VirtualHttpSession(ServletContext servletContext, String id) {
        this.servletContext = servletContext;
        this.id = id;
    }
    
    @Override
    public Object getAttribute(String name) {
        return attributes.get(name);
    }
    
    @Override
    public Object getValue(String name) {
        return getAttribute(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return Collections.enumeration(attributes.keySet());
    }
    
    @Override
    public String[] getValueNames() {
        return attributes.keySet().toArray(new String[attributes.size()]);
    }
    
    @Override
    public void removeAttribute(String name) {
        Object value = attributes.remove(name);
        if(value != null && value instanceof HttpSessionBindingListener) {
            HttpSessionBindingEvent event = new HttpSessionBindingEvent(this, name, value);
            ((HttpSessionBindingListener)value).valueUnbound(event);
        }
    }

    @Override
    public void removeValue(String name) {
        removeAttribute(name);
    }

    @Override
    public void setAttribute(String name, Object value) {
        if(value == null) {
            removeAttribute(name);
        } else {
            attributes.put(name, value);
            if(value instanceof HttpSessionBindingListener) {
                HttpSessionBindingEvent event = new HttpSessionBindingEvent(this, name, value);
                ((HttpSessionBindingListener)value).valueBound(event);
            }
        }
    }
    
    @Override
    public void putValue(String name, Object value) {
        setAttribute(name, value);
    }    

    @Override
    public long getCreationTime() {
        return creationTime;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public long getLastAccessedTime() {
        return lastAccessedTime;
    }

    @Override
    public int getMaxInactiveInterval() {
        return interval;
    }
    
    @Override
    public void setMaxInactiveInterval(int interval) {
        this.interval = interval;
    }

    @Override
    public ServletContext getServletContext() {
        return servletContext;
    }

    @Override
    public HttpSessionContext getSessionContext() {
        throw new UnsupportedOperationException("getSessionContext");
    }

    @Override
    public void invalidate() {
        invalid = true;
        Enumeration<String> e = getAttributeNames();
        while(e.hasMoreElements()) {
            String name = e.nextElement();
            removeAttribute(name);
        }
    }
    
    public boolean isInvalid() {
        return invalid;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }
    
    public void setNew(boolean isNew) {
        this.isNew = isNew;
    }

    public void touch() {
        lastAccessedTime = System.currentTimeMillis();
        isNew = false;
    }

}
