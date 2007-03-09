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
import java.util.Map;

import javax.servlet.ServletContext;

import de.schlund.pfixcore.workflow.context.ServerContextImpl;


/**
 * Stores context instances within a servlet context. The ContextXMLServlet uses
 * this class to store a context instance within the servlet context. All 
 * servlets use this class to get existing instances.
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class ServerContextStore {
    private final static String SERVLETCONTEXT_ATTRIBUTE = "__PFX_CONTEXTSTORE__";
    
    private Map<String, ServerContextImpl> servletMap = Collections.synchronizedMap(new HashMap<String, ServerContextImpl>()); 
    private Map<String, ServerContextImpl> nameMap = Collections.synchronizedMap(new HashMap<String, ServerContextImpl>());
    private Map<String, ServerContextImpl> pathMap = Collections.synchronizedMap(new HashMap<String, ServerContextImpl>());
    
    private ServerContextStore() {
        // Intentionally empty
    }
    
    /**
     * Provides store instance associated with a servlet context. If no store is
     * available for the servlet context a new one will be created.
     * 
     * @param servletContext servlet context
     * @return store instance stored in the specified servlet context
     */
    public static ServerContextStore getInstance(ServletContext servletContext) {
        ServerContextStore instance = (ServerContextStore) servletContext.getAttribute(SERVLETCONTEXT_ATTRIBUTE);
        if (instance == null) {
            synchronized (servletContext) {
                instance = (ServerContextStore) servletContext.getAttribute(SERVLETCONTEXT_ATTRIBUTE);
                if (instance == null) {
                    instance = new ServerContextStore();
                    servletContext.setAttribute(SERVLETCONTEXT_ATTRIBUTE, instance);
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
    public void storeContext(ContextXMLServlet servlet, PfixServletRequest preq, String name, ServerContextImpl context) {
        // We use the servlet name provided by the container. This might
        // cause problems for "unregistered" servlets, however a Pustefix
        // environment only uses servlets with well-defined names in the 
        // webapplication deployment descriptor.
        ServerContextImpl oldContext = this.servletMap.get(servlet.getServletName());
        this.servletMap.put(servlet.getServletName(), context);
        if (name != null) {
            synchronized (this.nameMap) {
                this.nameMap.put(name, context);
                // Delete entries for old names
                for (String key : this.nameMap.keySet()) {
                    if (this.nameMap.get(key) == oldContext) {
                        this.nameMap.remove(key);
                    }
                }
            }
        }
        synchronized (this.pathMap) {
            String path = preq.getRequest().getServletPath();
            this.pathMap.put(path, context);
            // Update entries for old paths
            for (String key : this.pathMap.keySet()) {
                if (this.pathMap.get(key) == oldContext) {
                    this.pathMap.put(key, context);
                }
            }
        }
    }
    
    /**
     * Returns context instance associated with a servlet
     * 
     * @param servlet servlet for which context instance is to be returned
     * @param preq current servlet request
     * @return context instance or <code>null</code> if no context instance is
     *         stored for the supplied parameters
     */
    public ServerContextImpl getContext(ContextXMLServlet servlet, PfixServletRequest preq) {
        ServerContextImpl context = servletMap.get(servlet.getServletName());
        if (context != null) {
            synchronized (this.pathMap) {
                this.pathMap.put(preq.getRequest().getServletPath(), context);
            }
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
    public ServerContextImpl getContext(String identifier) {
        if (identifier.startsWith("/")) {
            return this.pathMap.get(identifier);
        } else {
            ServerContextImpl instance = this.nameMap.get(identifier);
            if (instance == null) {
                instance = this.servletMap.get(identifier);
            }
            return instance;
        }
    }
}
