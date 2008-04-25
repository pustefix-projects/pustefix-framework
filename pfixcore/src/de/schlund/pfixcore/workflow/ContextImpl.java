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

package de.schlund.pfixcore.workflow;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;

import de.schlund.pfixcore.auth.Authentication;
import de.schlund.pfixcore.auth.AuthenticationImpl;
import de.schlund.pfixcore.auth.Role;
import de.schlund.pfixcore.exception.PustefixApplicationException;
import de.schlund.pfixcore.exception.PustefixCoreException;
import de.schlund.pfixcore.util.TokenManager;
import de.schlund.pfixcore.util.TokenUtils;
import de.schlund.pfixcore.workflow.context.AccessibilityChecker;
import de.schlund.pfixcore.workflow.context.PageFlow;
import de.schlund.pfixcore.workflow.context.RequestContextImpl;
import de.schlund.pfixcore.workflow.context.ServerContextImpl;
import de.schlund.pfixxml.AbstractXMLServlet;
import de.schlund.pfixxml.PfixServletRequest;
import de.schlund.pfixxml.SPDocument;
import de.schlund.pfixxml.ServletManager;
import de.schlund.pfixxml.Variant;
import de.schlund.pfixxml.config.ContextConfig;
import de.schlund.pfixxml.config.PageRequestConfig;
import de.schlund.util.statuscodes.StatusCode;

public class ContextImpl implements Context, AccessibilityChecker, ExtendedContext, PageFlowContext, TokenManager, HttpSessionBindingListener {

    /**
     * Implementation of the session part of the context used by
     * ContextXMLServlet, DirectOutputServlet and WebServiceServlet. This class
     * should never be directly used by application developers.
     * 
     * @author Sebastian Marsching <sebastian.marsching@1und1.de>
     */
    private class SessionContextImpl {
        private HttpSession            session;
        private Variant                variant          = null;
        private String                 visitId          = null;
        private ContextResourceManagerImpl crm;
        private SessionEndNotificator  sessionEndNotificator;
        private Authentication authentication;

        // private Map<NavigationElement, Integer> navigationMap = new
        // HashMap<NavigationElement, Integer>();
        private Set<String>            visitedPages     = Collections.synchronizedSet(new HashSet<String>());
        
        private Map<String,String>          tokens;

        
        private class SessionEndNotificator implements HttpSessionBindingListener {
            private LinkedHashSet<SessionStatusListener> sessionListeners = new LinkedHashSet<SessionStatusListener>();
            
            public void valueBound(HttpSessionBindingEvent ev) {
                // Ignore this event
            }

            public void valueUnbound(HttpSessionBindingEvent ev) {
                // Send event to registered listeners
                synchronized (this) {
                    for (SessionStatusListener l : sessionListeners) {
                        l.sessionStatusChanged(new SessionStatusEvent(SessionStatusEvent.Type.SESSION_DESTROYED));
                    }
                }
            }
        }
        
        public SessionContextImpl(HttpSession session) {
            this.session = session;
            this.crm = new ContextResourceManagerImpl();
            synchronized(this.getClass()) {
                this.sessionEndNotificator = (SessionEndNotificator) this.session.getAttribute("de.schlund.pfixcore.workflow.ContextImpl.SessionContextImpl.dummylistenerobject"); 
                if (this.sessionEndNotificator == null) {
                    this.sessionEndNotificator = new SessionEndNotificator();
                    this.session.setAttribute("de.schlund.pfixcore.workflow.ContextImpl.SessionContextImpl.dummylistenerobject", this.sessionEndNotificator);
                }
            }
        }

        private void init(Context context) throws PustefixApplicationException, PustefixCoreException {
            this.authentication = new AuthenticationImpl(servercontext);
            if(getContextConfig().hasRoles()) {
            	for(Role role:getContextConfig().getInitialRoles()) this.authentication.addRole(role.getName());
            }
            crm.init(context, context.getContextConfig());
        }

        public ContextResourceManager getContextResourceManager() {
            return crm;
        }

        public void setLanguage(String langcode) {
            session.setAttribute(AbstractXMLServlet.SESS_LANG, langcode);
        }

        public String getLanguage() {
            try {
                return (String) session.getAttribute(AbstractXMLServlet.SESS_LANG);
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

        public String getVisitId() {
            if (visitId == null) {
                visitId = (String) session.getAttribute(ServletManager.VISIT_ID);
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
            synchronized(this) {
                if(tokens!=null) tokens.remove(tokenName);
            }
        }
        
        public boolean isValidToken(String tokenName,String token) {
            synchronized(this) {
                if(tokens==null) return false;
                String storedToken=tokens.get(tokenName);
                return storedToken!=null && storedToken.equals(token);
            }
        }
        
        public String getToken(String tokenName) {
            synchronized(this) {
                if(tokens==null) tokens=new LinkedHashMap<String,String>();
                String token=TokenUtils.createRandomToken();
                if(tokens.size()>25) {
                    Iterator<String> it=tokens.keySet().iterator();
                    it.next();
                    it.remove();
                }
                tokens.put(tokenName,token);
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
            this.session.setAttribute(AbstractXMLServlet.SESS_CLEANUP_FLAG_STAGE1, true);
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
    
    private SessionContextImpl sessioncontext;
    private ServerContextImpl servercontext;
    private ThreadLocal<RequestContextImpl> requestcontextstore = new ThreadLocal<RequestContextImpl>();
    
    public ContextImpl(ServerContextImpl servercontext, HttpSession session) throws PustefixApplicationException, PustefixCoreException {
        this.servercontext = servercontext;
        this.sessioncontext = new SessionContextImpl(session);
        sessioncontext.init(this);
    }

    public void addCookie(Cookie cookie) {
        getRequestContextForCurrentThreadWithError().addCookie(cookie);
    }

    public void addPageMessage(StatusCode scode) {
        getRequestContextForCurrentThreadWithError().addPageMessage(scode);
    }

    public void addPageMessage(StatusCode scode, String level) {
        getRequestContextForCurrentThreadWithError().addPageMessage(scode, level);
    }

    public void addPageMessage(StatusCode scode, String[] args) {
        getRequestContextForCurrentThreadWithError().addPageMessage(scode, args);
    }

    public void addPageMessage(StatusCode scode, String[] args, String level) {
        getRequestContextForCurrentThreadWithError().addPageMessage(scode, args, level);
    }

    public boolean flowIsRunning() {
        return getRequestContextForCurrentThreadWithError().flowIsRunning();
    }

    public boolean precedingFlowNeedsData() throws PustefixApplicationException {
        return getRequestContextForCurrentThreadWithError().precedingFlowNeedsData();
    }

    public PageRequestConfig getConfigForCurrentPageRequest() {
        return getRequestContextForCurrentThreadWithError().getConfigForCurrentPageRequest();
    }

    public ContextConfig getContextConfig() {
        return getServerContext().getContextConfig();
    }

    public ContextResourceManager getContextResourceManager() {
        return sessioncontext.getContextResourceManager();
    }

    public PageFlow getCurrentPageFlow() {
        return getRequestContextForCurrentThreadWithError().getCurrentPageFlow();
    }

    public PageRequest getCurrentPageRequest() {
        return getRequestContextForCurrentThreadWithError().getCurrentPageRequest();
    }

    public String getLanguage() {
        return getRequestContextForCurrentThreadWithError().getLanguage();
    }
    
    public String getSessionLanguage() {
        return sessioncontext.getLanguage();
    }

    public Throwable getLastException() {
        return getRequestContextForCurrentThreadWithError().getLastException();
    }

    public String getName() {
        return getServerContext().getName();
    }

    public Properties getProperties() {
        return getServerContext().getProperties();
    }

    public Properties getPropertiesForContextResource(Object res) {
        return getServerContext().getPropertiesForContextResource(res);
    }

    public Properties getPropertiesForCurrentPageRequest() {
        return getRequestContextForCurrentThreadWithError().getPropertiesForCurrentPageRequest();
    }

    public Cookie[] getRequestCookies() {
        return getRequestContextForCurrentThreadWithError().getRequestCookies();
    }

    public Variant getVariant() {
        return getRequestContextForCurrentThreadWithError().getVariant();
    }
    
    public Variant getSessionVariant() {
        return sessioncontext.getVariant();
    }

    public String getVisitId() {
        return sessioncontext.getVisitId();
    }

//    public boolean isCurrentPageRequestInCurrentFlow() {
//        return getRequestContextForCurrentThreadWithError().isCurrentPageRequestInCurrentFlow();
//    }

    public boolean isJumpToPageFlowSet() {
        return getRequestContextForCurrentThreadWithError().isJumpToPageFlowSet();
    }

    public boolean isJumpToPageSet() {
        return getRequestContextForCurrentThreadWithError().isJumpToPageSet();
    }

    public boolean isProhibitContinueSet() {
        return getRequestContextForCurrentThreadWithError().isProhibitContinueSet();
    }

    public void prohibitContinue() {
        getRequestContextForCurrentThreadWithError().prohibitContinue();
    }

    public void setCurrentPageFlow(String pageflow) {
        getRequestContextForCurrentThreadWithError().setCurrentPageFlow(pageflow);
    }

    public void setJumpToPage(String pagename) {
        getRequestContextForCurrentThreadWithError().setJumpToPage(pagename);
    }

    public void setJumpToPageFlow(String pageflow) {
        getRequestContextForCurrentThreadWithError().setJumpToPageFlow(pageflow);
    }
    
    public void setLanguage(String lang) {
        getRequestContextForCurrentThreadWithError().setLanguage(lang);
        sessioncontext.setLanguage(lang);
    }

    public void setVariant(Variant variant) {
        getRequestContextForCurrentThreadWithError().setVariantForThisRequestOnly(variant);
        sessioncontext.setVariant(variant);
    }

    public void setVariantForThisRequestOnly(Variant variant) {
        getRequestContextForCurrentThreadWithError().setVariantForThisRequestOnly(variant);
    }

    public boolean stateMustSupplyFullDocument() {
        return getRequestContextForCurrentThreadWithError().stateMustSupplyFullDocument();
    }
    
    public boolean isPageAccessible(String pagename) throws Exception {
        RequestContextImpl requestcontext = getRequestContextForCurrentThreadWithError();
        if (getContextConfig().isSynchronized()) {
            synchronized (this) {
                return requestcontext.isPageAccessible(pagename);
            }
        } else {
            return requestcontext.isPageAccessible(pagename);
        }
    }

    public boolean isPageAlreadyVisited(String pagename) throws Exception {
        return sessioncontext.isVisitedPage(pagename);
    }
    
    public void addVisitedPage(String pagename) {
        sessioncontext.addVisitedPage(pagename);
    }
    
    // ----------------

    public void setServerContext(ServerContextImpl servercontext) {
        // Update current configuration
        this.servercontext = servercontext;
    }
    
    public boolean isAuthorized() throws Exception {
        return (this.getRequestContextForCurrentThreadWithError().checkAuthorization(false, false) == null);
    }
    
    public void prepareForRequest() {
        // This allows to use OLDER servercontexts during requests
        requestcontextstore.set(new RequestContextImpl(servercontext, this));
    }
    
    public SPDocument handleRequest(PfixServletRequest preq) throws PustefixApplicationException, PustefixCoreException {
        if (getContextConfig().isSynchronized()) {
            synchronized (this) {
                return getRequestContextForCurrentThreadWithError().handleRequest(preq);
            }
        } else {
            return getRequestContextForCurrentThreadWithError().handleRequest(preq);
        }
    }
    
    public void cleanupAfterRequest() {
        requestcontextstore.set(null);
    }
    
    // Used by TransformerCallback to set the right RequestContextImpl when
    // rendering a page
    public void setRequestContextForCurrentThread(RequestContextImpl requestcontext) {
        requestcontextstore.set(requestcontext);
    }
    
    public void invalidateToken(String token) {
        sessioncontext.invalidateToken(token);
    }
    
    public String getToken(String tokenName) {
        return sessioncontext.getToken(tokenName);
    }
    
    public boolean isValidToken(String tokenName,String token) {
        return sessioncontext.isValidToken(tokenName,token);
    }
    
    public Authentication getAuthentication() {
        return sessioncontext.getAuthentication();
    }
    
    @Override
    public String toString() {
        RequestContextImpl requestcontext = getRequestContextForCurrentThread();
        if (requestcontext != null) {
            return requestcontext.toString() + sessioncontext.toString();
        } else {
            return sessioncontext.toString();
        }
    }

    // --------------
    
    private RequestContextImpl getRequestContextForCurrentThread() {
        return requestcontextstore.get();
    }

    private RequestContextImpl getRequestContextForCurrentThreadWithError() {
        RequestContextImpl requestcontext = requestcontextstore.get();
        if (requestcontext == null) {
            throw new IllegalStateException("Request object is not available for current thread");
        }
        return requestcontext;
    }
    
    private ServerContextImpl getServerContext() {
        RequestContextImpl requestcontext = getRequestContextForCurrentThread();
        if (requestcontext != null) {
            return requestcontext.getServerContext();
        } else {
            return servercontext;
        }
    }
    
    public void markSessionForCleanup() {
        this.sessioncontext.markSessionForCleanup();
    }

    public void addSessionStatusListener(SessionStatusListener l) {
        this.sessioncontext.addSessionStatusListener(l);
    }

    public void removeSessionStatusListener(SessionStatusListener l) {
        this.sessioncontext.removeSessionStatusListener(l);
    }

    // Notification on session binding / unbinding
    
    public void valueBound(HttpSessionBindingEvent ev) {
        this.sessioncontext.session = ev.getSession();
    }

    public void valueUnbound(HttpSessionBindingEvent ev) {
        if (ev.getSession() == this.sessioncontext.session) {
            this.sessioncontext.session = null;
        }
    }

    public boolean checkIsAccessible(PageRequest page, PageRequestStatus status) throws PustefixApplicationException {
        return getRequestContextForCurrentThreadWithError().checkIsAccessible(page, status);
    }

    public boolean checkNeedsData(PageRequest page, PageRequestStatus status) throws PustefixApplicationException {
        return getRequestContextForCurrentThreadWithError().checkNeedsData(page, status);
    }

    public PageRequest createPageRequest(String name) {
        return getRequestContextForCurrentThreadWithError().createPageRequest(name);
    }

    public PfixServletRequest getPfixServletRequest() {
        return getRequestContextForCurrentThreadWithError().getPfixServletRequest();
    }

}
