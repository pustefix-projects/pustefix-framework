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

package org.pustefixframework.http;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;

import de.schlund.pfixcore.workflow.ContextImpl;
import de.schlund.pfixxml.PfixServletRequest;

//FIXME Use Spring to manage context instances

/**
 * Stores context instances within a session. The ContextXMLServlet uses this
 * class to store a context instance within a session. All servlets use this 
 * class to get existing instances.  
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class SessionContextStore implements HttpSessionBindingListener {
    private final static String SESSION_ATTRIBUTE = "__PFX_CONTEXTSTORE__";

    private ContextImpl context;

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
    public void storeContext(String beanName, PfixServletRequest preq, String name, ContextImpl context) {
        this.context = context;
    }

    /**
     * Returns context instance associated with a servlet
     * 
     * @param servlet servlet for which context instance is to be returned
     * @param preq current servlet request
     * @return context instance or <code>null</code> if no context instance is
     *         stored for the supplied parameters
     */
    public ContextImpl getContext(String beanName, PfixServletRequest preq) {
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
        return context;
    }

    public void valueBound(HttpSessionBindingEvent ev) {
        if (context instanceof HttpSessionBindingListener) {
            HttpSessionBindingListener l = (HttpSessionBindingListener) context;
            l.valueBound(ev);
        }
    }

    public void valueUnbound(HttpSessionBindingEvent ev) {
        if (context instanceof HttpSessionBindingListener) {
            HttpSessionBindingListener l = (HttpSessionBindingListener) context;
            l.valueUnbound(ev);
        }
    }

}
