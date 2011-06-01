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
package de.schlund.pfixcore.workflow.context;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;

import org.apache.log4j.Logger;
import org.pustefixframework.http.AbstractPustefixRequestHandler;
import org.pustefixframework.http.AbstractPustefixXMLRequestHandler;

import de.schlund.pfixcore.auth.Authentication;
import de.schlund.pfixcore.auth.AuthenticationImpl;
import de.schlund.pfixcore.auth.Role;
import de.schlund.pfixcore.exception.PustefixApplicationException;
import de.schlund.pfixcore.exception.PustefixCoreException;
import de.schlund.pfixcore.util.TokenUtils;
import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixcore.workflow.ContextResourceManager;
import de.schlund.pfixcore.workflow.ContextResourceManagerImpl;
import de.schlund.pfixcore.workflow.SessionStatusEvent;
import de.schlund.pfixcore.workflow.SessionStatusListener;
import de.schlund.pfixxml.Variant;

/**
 * Implementation of the session part of the context used by
 * ContextXMLServlet, DirectOutputServlet and WebServiceServlet. This class
 * should never be directly used by application developers.
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class SessionContextImpl {

    private final static Logger LOG = Logger.getLogger(SessionContextImpl.class);
    
    private HttpSession                session;
    private Variant                    variant      = null;
    private Map<String, String>        pageSelectors = new HashMap<String, String>();
    private String                     visitId      = null;
    private ContextResourceManager     crm;
    private SessionEndNotificator      sessionEndNotificator;
    private Authentication             authentication;
    private Set<String>                visitedPages = Collections.synchronizedSet(new HashSet<String>());
    private Map<String, String>        tokens;

    private class SessionEndNotificator implements HttpSessionBindingListener {
        private LinkedHashSet<SessionStatusListener> sessionListeners = new LinkedHashSet<SessionStatusListener>();

        public void valueBound(HttpSessionBindingEvent ev) {
            // Ignore this event
        }

        public void valueUnbound(HttpSessionBindingEvent ev) {
            // Send event to registered listeners
            try {
                SessionStatusListener[] currentListeners;
                synchronized (this) {
                    currentListeners = new SessionStatusListener[sessionListeners.size()];
                    sessionListeners.toArray(currentListeners);
                }
                for (SessionStatusListener l : currentListeners) {
                    l.sessionStatusChanged(new SessionStatusEvent(SessionStatusEvent.Type.SESSION_DESTROYED));
                }
            } catch(Throwable t) {
                //if we're not catching all exceptions here, valueUnbound for the SessionAdmin
                //won't be called and the session is never removed
                LOG.error("Error calling SessionStatusListener at end of session", t);
            }
        }
    }

    public void init(Context context) throws PustefixApplicationException, PustefixCoreException {
        
        synchronized(this) {
            if(authentication == null) {
       
                this.authentication = new AuthenticationImpl(context.getContextConfig().getRoleProvider());
                List<Role> roles = context.getContextConfig().getRoleProvider().getRoles();
                if (roles != null) {
                    for (Role role : roles) {
                        if (role.isInitial())
                            authentication.addRole(role.getName());
                    }
                }
                
                ((ContextResourceManagerImpl)crm).init(context, context.getContextConfig());
            }
        }
    }
    
    private void initSession(HttpSession session) {
        synchronized (this) {
            this.sessionEndNotificator = (SessionEndNotificator) this.session.getAttribute("de.schlund.pfixcore.workflow.ContextImpl.SessionContextImpl.dummylistenerobject");
            if (this.sessionEndNotificator == null) {
                this.sessionEndNotificator = new SessionEndNotificator();
                this.session.setAttribute("de.schlund.pfixcore.workflow.ContextImpl.SessionContextImpl.dummylistenerobject", this.sessionEndNotificator);
            }
        }
    }
    
    public HttpSession getSession() {
        return session;
    }
    
    public void setSession(HttpSession session) {
        this.session = session;
        if(session != null) initSession(session);
    }

    public ContextResourceManager getContextResourceManager() {
        return crm;
    }

    public void setContextResourceManager(ContextResourceManager crm) {
        this.crm = crm;
    }
    
    public void setLanguage(String langcode) {
        session.setAttribute(AbstractPustefixXMLRequestHandler.SESS_LANG, langcode);
    }

    public String getLanguage() {
        try {
            return (String) session.getAttribute(AbstractPustefixXMLRequestHandler.SESS_LANG);
        } catch (IllegalStateException e) {
            // May be thrown if session has been invalidated
            return null;
        }
    }

    public Variant getVariant() {
        return variant;
    }

    public void setVariant(Variant variant) {
        this.variant = variant;
    }
    
    public Map<String, String> getPageSelectors() {
        return pageSelectors;
    }
    
    public void setPageSelector(String name, String value) {
        pageSelectors.put(name, value);
    }

    public String getVisitId() {
        if (visitId == null) {
            visitId = (String) session.getAttribute(AbstractPustefixRequestHandler.VISIT_ID);
            if (visitId == null) {
                throw new RuntimeException("visit_id not set, but asked for!!!!");
            }
        }
        return visitId;
    }

    public void addVisitedPage(String pagename) {
        visitedPages.add(pagename);
    }

    public boolean isVisitedPage(String pagename) {
        return visitedPages.contains(pagename);
    }

    public void invalidateToken(String tokenName) {
        synchronized (this) {
            if (tokens != null)
                tokens.remove(tokenName);
        }
    }

    public boolean isValidToken(String tokenName, String token) {
        synchronized (this) {
            if (tokens == null)
                return false;
            String storedToken = tokens.get(tokenName);
            return storedToken != null && storedToken.equals(token);
        }
    }

    public String getToken(String tokenName) {
        synchronized (this) {
            if (tokens == null)
                tokens = new LinkedHashMap<String, String>();
            String token = TokenUtils.createRandomToken();
            if (tokens.size() > 25) {
                Iterator<String> it = tokens.keySet().iterator();
                it.next();
                it.remove();
            }
            tokens.put(tokenName, token);
            return token;
        }
    }

    public Authentication getAuthentication() {
        return authentication;
    }

    @Override
    public String toString() {
        StringBuffer contextbuf = new StringBuffer("\n");

        contextbuf.append("     >>>> Resources <<<<\n");
        for (Iterator<Object> i = crm.getResourceIterator(); i.hasNext();) {
            Object res = i.next();
            contextbuf.append("         " + res.getClass().getName() + ": ");
            contextbuf.append(res.toString() + "\n");
        }

        return contextbuf.toString();
    }

    public void markSessionForCleanup() {
        this.session.setAttribute(AbstractPustefixXMLRequestHandler.SESS_CLEANUP_FLAG_STAGE1, true);
    }

    public void addSessionStatusListener(SessionStatusListener l) {
        synchronized (this.sessionEndNotificator) {
            if (!sessionEndNotificator.sessionListeners.contains(l)) {
                sessionEndNotificator.sessionListeners.add(l);
            }
        }
    }

    public void removeSessionStatusListener(SessionStatusListener l) {
        synchronized (this.sessionEndNotificator) {
            sessionEndNotificator.sessionListeners.remove(l);
        }
    }
    
}
