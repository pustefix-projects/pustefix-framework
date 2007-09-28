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

package de.schlund.pfixxml;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;

import de.schlund.pfixcore.workflow.ContextImpl;

/**
 * Stores context instances within a session. The ContextXMLServlet uses this
 * class to store a context instance within a session. All servlets use this 
 * class to get existing instances.  
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class SessionContextStore implements HttpSessionBindingListener {
    private final static String SESSION_ATTRIBUTE = "__PFX_CONTEXTSTORE__";
    
    private Map<String, ContextImpl> servletMap = Collections.synchronizedMap(new HashMap<String, ContextImpl>()); 
    private Map<String, ContextImpl> nameMap = Collections.synchronizedMap(new HashMap<String, ContextImpl>());
    private Map<String, ContextImpl> pathMap = Collections.synchronizedMap(new HashMap<String, ContextImpl>());
    
    private SessionContextStore() {
        // Intentionally empty
    }
    
    /**
     * Provides store instance associated with a session. If no store is
     * available for the session a new one will be created.
     * 
     * @param session HTTP session
     * @return store instance stored in the specified session
     */
    public static SessionContextStore getInstance(HttpSession session) {
        SessionContextStore instance = (SessionContextStore) session.getAttribute(SESSION_ATTRIBUTE);
        if (instance == null) {
            synchronized (session) {
                instance = (SessionContextStore) session.getAttribute(SESSION_ATTRIBUTE);
                if (instance == null) {
                    instance = new SessionContextStore();
                    session.setAttribute(SESSION_ATTRIBUTE, instance);
                }
            }
        }
        return instance;
    }
    
    /**
     * Stores a context instance.
     * 
     * @param servlet servlet the context instance is associated to
     * @param preq current servlet request
     * @param name name of the servlet (if configured)
     * @param context the context instance to store
     */
    public void storeContext(ContextXMLServlet servlet, PfixServletRequest preq, String name, ContextImpl context) {
        // We use the servlet name provided by the container. This might
        // cause problems for "unregistered" servlets, however a Pustefix
        // environment only uses servlets with well-defined names in the 
        // webapplication deployment descriptor.
        this.servletMap.put(servlet.getServletName(), context);
        if (name != null) {
            this.nameMap.put(name, context);
        }
        this.pathMap.put(preq.getRequest().getServletPath(), context);
    }
    
    /**
     * Returns context instance associated with a servlet
     * 
     * @param servlet servlet for which context instance is to be returned
     * @param preq current servlet request
     * @return context instance or <code>null</code> if no context instance is
     *         stored for the supplied parameters
     */
    public ContextImpl getContext(ContextXMLServlet servlet, PfixServletRequest preq) {
        ContextImpl context = servletMap.get(servlet.getServletName());
        if (context != null) {
            this.pathMap.put(preq.getRequest().getServletPath(), context);
        }
        return context;
    }
    
    /**
     * Returns the context instance for the servlet identified by the supplied
     * identifier. The identifier can be the path (starting with a '/') of
     * the ContextXMLServlet, the name manually specified in its configuration
     * or the name provided by the webapplication deployment descriptor. The
     * method will try to guess the type of identifier.  
     * 
     * @param identifier string identifying the origin servlet
     * @return context instance or <code>null</code> if no matching instance ca
     *         be found
     */
    public ContextImpl getContext(String identifier) {
        if (identifier.startsWith("/")) {
            return this.pathMap.get(identifier);
        } else {
            ContextImpl instance = this.nameMap.get(identifier);
            if (instance == null) {
                instance = this.servletMap.get(identifier);
            }
            return instance;
        }
    }
    
    public Set<String> getServletNames() {
        return Collections.unmodifiableSet(servletMap.keySet());
    }

    public void valueBound(HttpSessionBindingEvent ev) {
        HashSet<ContextImpl> contexts = getAllContexts();
        for (ContextImpl context : contexts) {
            if (context instanceof HttpSessionBindingListener) {
                HttpSessionBindingListener l = (HttpSessionBindingListener) context;
                l.valueBound(ev);
            }
        }
    }

    public void valueUnbound(HttpSessionBindingEvent ev) {
        HashSet<ContextImpl> contexts = getAllContexts();
        for (ContextImpl context : contexts) {
            if (context instanceof HttpSessionBindingListener) {
                HttpSessionBindingListener l = (HttpSessionBindingListener) context;
                l.valueUnbound(ev);
            }
        }
    }
    
    private HashSet<ContextImpl> getAllContexts() {
        HashSet<ContextImpl> contexts = new HashSet<ContextImpl>();
        contexts.addAll(servletMap.values());
        contexts.addAll(nameMap.values());
        contexts.addAll(pathMap.values());
        return contexts;
    }
    
}
