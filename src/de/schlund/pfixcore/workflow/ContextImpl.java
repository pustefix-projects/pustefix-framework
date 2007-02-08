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
import java.util.Properties;
import java.util.Set;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;

import de.schlund.pfixcore.exception.PustefixApplicationException;
import de.schlund.pfixcore.exception.PustefixCoreException;
import de.schlund.pfixcore.workflow.context.AccessibilityChecker;
import de.schlund.pfixcore.workflow.context.PageFlow;
import de.schlund.pfixcore.workflow.context.RequestContextImpl;
import de.schlund.pfixcore.workflow.context.ServerContextImpl;
import de.schlund.pfixxml.AbstractXMLServer;
import de.schlund.pfixxml.PfixServletRequest;
import de.schlund.pfixxml.SPDocument;
import de.schlund.pfixxml.ServletManager;
import de.schlund.pfixxml.Variant;
import de.schlund.pfixxml.config.ContextConfig;
import de.schlund.pfixxml.config.PageRequestConfig;
import de.schlund.util.statuscodes.StatusCode;

public class ContextImpl implements Context, AccessibilityChecker {

    /**
     * Implementation of the session part of the context used by
     * ContextXMLServer, DirectOutputServer and WebServiceServlet. This class
     * should never be directly used by application developers.
     * 
     * @author Sebastian Marsching <sebastian.marsching@1und1.de>
     */
    private class SessionContextImpl {
        private HttpSession            session;
        private String                 lastPageName     = null;
        private String                 lastPageFlowName = null;
        private Variant                variant          = null;
        private String                 visitId          = null;
        private ContextResourceManager crm;

        // private Map<NavigationElement, Integer> navigationMap = new
        // HashMap<NavigationElement, Integer>();
        private Set<String>            visitedPages     = Collections.synchronizedSet(new HashSet<String>());

        public SessionContextImpl(HttpSession session) {
            this.session = session;
            this.crm = new ContextResourceManager();
        }

        private void init(Context context) throws PustefixApplicationException, PustefixCoreException {
            crm.init(context, context.getContextConfig());
        }

        public ContextResourceManager getContextResourceManager() {
            return crm;
        }

        public void setLanguage(String langcode) {
            session.setAttribute(AbstractXMLServer.SESS_LANG, langcode);
        }

        public String getLanguage() {
            try {
                return (String) session.getAttribute(AbstractXMLServer.SESS_LANG);
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

        public String getLastPageFlowName() {
            return lastPageFlowName;
        }

        public void setLastPageFlowName(String lastPageFlowName) {
            this.lastPageFlowName = lastPageFlowName;
        }

        public String getLastPageName() {
            return lastPageName;
        }

        public void setLastPageName(String lastPageName) {
            this.lastPageName = lastPageName;
        }

        public void addVisitedPage(String pagename) {
            visitedPages.add(pagename);
        }

        public boolean isVisitedPage(String pagename) {
            return visitedPages.contains(pagename);
        }

        public String toString() {
            StringBuffer contextbuf = new StringBuffer("\n");

            contextbuf.append("     >>>> ContextResourcen <<<<\n");
            for (Iterator i = crm.getResourceIterator(); i.hasNext();) {
                ContextResource res = (ContextResource) i.next();
                contextbuf.append("         " + res.getClass().getName() + ": ");
                contextbuf.append(res.toString() + "\n");
            }

            return contextbuf.toString();
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

    public boolean finalPageIsRunning() {
        return getRequestContextForCurrentThreadWithError().finalPageIsRunning();
    }

    public boolean flowIsRunning() {
        return getRequestContextForCurrentThreadWithError().flowIsRunning();
    }

    public boolean flowStepsBeforeCurrentStepNeedData() throws PustefixApplicationException {
        return getRequestContextForCurrentThreadWithError().flowStepsBeforeCurrentStepNeedData();
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

    public Properties getPropertiesForContextResource(ContextResource res) {
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

    public void resetUICache() {
        getRequestContextForCurrentThreadWithError().resetUICache();
    }

    public boolean isCurrentPageFlowRequestedByUser() {
        return getRequestContextForCurrentThreadWithError().isCurrentPageFlowRequestedByUser();
    }

    public boolean isCurrentPageRequestInCurrentFlow() {
        return getRequestContextForCurrentThreadWithError().isCurrentPageRequestInCurrentFlow();
    }

    public boolean isJumpToPageFlowSet() {
        return getRequestContextForCurrentThreadWithError().isJumpToPageFlowSet();
    }

    public boolean isJumpToPageSet() {
        return getRequestContextForCurrentThreadWithError().isJumpToPageSet();
    }

    public boolean isProhibitContinueSet() {
        return getRequestContextForCurrentThreadWithError().isProhibitContinueSet();
    }

    public boolean jumpToPageIsRunning() {
        return getRequestContextForCurrentThreadWithError().jumpToPageIsRunning();
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
    
    public void forceStopAtNextStep(boolean forcestop) {
        getRequestContextForCurrentThreadWithError().forceStopAtNextStep(forcestop);
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
    
    public String getLastPageName() {
        return sessioncontext.getLastPageName();
    }
    
    public String getLastPageFlowName() {
        return sessioncontext.getLastPageFlowName();
    }
    
    public void setLastPageName(String pagename) {
        sessioncontext.setLastPageName(pagename);
    }
    
    public void setLastPageFlowName(String pagename) {
        sessioncontext.setLastPageFlowName(pagename);
    }
    
    // ----------------

    public void setServerContext(ServerContextImpl servercontext) {
        // Update current configuration
        this.servercontext = servercontext;
    }
    
    public boolean isAuthorized() throws Exception {
        return (this.getRequestContextForCurrentThreadWithError().checkAuthorization(false) == null);
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
    

}
