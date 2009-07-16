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

package org.pustefixframework.container.spring.beans.internal;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;


/**
 * Proxy class for servlet context. This implementation delegates all method
 * calls to another instance of {@link ServletContext} that can be changed
 * at runtime.    
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class ServletContextProxy implements ServletContext {
    
    private ServletContext servletContext;
    
    /**
     * Changes the object method calls are delegated to.
     * 
     * @param servletContext target object for method calls
     */
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }
    
    private void checkState() throws IllegalStateException {
        if (this.servletContext == null) {
            throw new IllegalStateException("Method called on " + this.getClass().getName() + " but target servlet context is not set.");
        }
    }
    
    public Object getAttribute(String name) {
        checkState();
        return servletContext.getAttribute(name);
    }
    
    @SuppressWarnings("unchecked")
    public Enumeration getAttributeNames() {
        checkState();
        return servletContext.getAttributeNames();
    }
    
    public ServletContext getContext(String uripath) {
        checkState();
        return servletContext.getContext(uripath);
    }
    
    public String getInitParameter(String name) {
        checkState();
        return servletContext.getInitParameter(name);
    }
    
    @SuppressWarnings("unchecked")
    public Enumeration getInitParameterNames() {
        checkState();
        return servletContext.getInitParameterNames();
    }
    
    public int getMajorVersion() {
        checkState();
        return servletContext.getMajorVersion();
    }
    
    public String getMimeType(String file) {
        checkState();
        return servletContext.getMimeType(file);
    }
    
    public int getMinorVersion() {
        checkState();
        return servletContext.getMinorVersion();
    }
    
    public RequestDispatcher getNamedDispatcher(String name) {
        checkState();
        return servletContext.getNamedDispatcher(name);
    }
    
    public String getRealPath(String path) {
        checkState();
        return servletContext.getRealPath(path);
    }
    
    public RequestDispatcher getRequestDispatcher(String path) {
        checkState();
        return servletContext.getRequestDispatcher(path);
    }
    
    public URL getResource(String path) throws MalformedURLException {
        checkState();
        return servletContext.getResource(path);
    }
    
    public InputStream getResourceAsStream(String path) {
        checkState();
        return servletContext.getResourceAsStream(path);
    }
    
    @SuppressWarnings("unchecked")
    public Set getResourcePaths(String path) {
        checkState();
        return servletContext.getResourcePaths(path);
    }
    
    public String getServerInfo() {
        checkState();
        return servletContext.getServerInfo();
    }
    
    @Deprecated
    public Servlet getServlet(String name) throws ServletException {
        checkState();
        return servletContext.getServlet(name);
    }
    
    public String getServletContextName() {
        checkState();
        return servletContext.getServletContextName();
    }
    
    @SuppressWarnings("unchecked")
    @Deprecated
    public Enumeration getServletNames() {
        checkState();
        return servletContext.getServletNames();
    }
    
    @SuppressWarnings("unchecked")
    @Deprecated
    public Enumeration getServlets() {
        checkState();
        return servletContext.getServlets();
    }
    
    public void log(String msg) {
        checkState();
        servletContext.log(msg);
    }
    
    @Deprecated
    public void log(Exception exception, String msg) {
        checkState();
        servletContext.log(exception, msg);
    }
    
    public void log(String msg, Throwable throwable) {
        checkState();
        servletContext.log(msg, throwable);
    }
    
    public void removeAttribute(String name) {
        checkState();
        servletContext.removeAttribute(name);
    }
    
    public void setAttribute(String name, Object object) {
        checkState();
        servletContext.setAttribute(name, object);
    }
    
}
