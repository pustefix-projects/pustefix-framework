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

package de.schlund.pfixcore.workflow;

import java.util.List;
import java.util.Properties;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;

import org.pustefixframework.config.contextxmlservice.ContextConfig;
import org.pustefixframework.config.contextxmlservice.PageRequestConfig;
import org.pustefixframework.config.project.ProjectInfo;
import org.pustefixframework.container.spring.beans.TenantScope;
import org.pustefixframework.http.AbstractPustefixRequestHandler;

import de.schlund.pfixcore.auth.Authentication;
import de.schlund.pfixcore.exception.PustefixApplicationException;
import de.schlund.pfixcore.exception.PustefixCoreException;
import de.schlund.pfixcore.exception.PustefixRuntimeException;
import de.schlund.pfixcore.util.TokenManager;
import de.schlund.pfixcore.workflow.context.AccessibilityChecker;
import de.schlund.pfixcore.workflow.context.PageFlow;
import de.schlund.pfixcore.workflow.context.RequestContextImpl;
import de.schlund.pfixcore.workflow.context.ServerContextImpl;
import de.schlund.pfixcore.workflow.context.SessionContextImpl;
import de.schlund.pfixxml.PfixServletRequest;
import de.schlund.pfixxml.SPDocument;
import de.schlund.pfixxml.Tenant;
import de.schlund.pfixxml.Variant;
import de.schlund.util.statuscodes.StatusCode;

public class ContextImpl implements AccessibilityChecker, ExtendedContext, TokenManager, HttpSessionBindingListener {
    
    private SessionContextImpl              sessioncontext;
    private ServerContextImpl               servercontext;
    private ThreadLocal<RequestContextImpl> requestcontextstore = new ThreadLocal<RequestContextImpl>();
    
    public ContextImpl() {
        this.sessioncontext = new SessionContextImpl();
    }
    
    public void init() throws PustefixCoreException, PustefixApplicationException{
        sessioncontext.init(this);
    }
    
    public void setContextResourceManager(ContextResourceManager crm) {
        sessioncontext.setContextResourceManager(crm);
    }
    
    public void addCookie(Cookie cookie) {
        getRequestContextForCurrentThreadWithError().addCookie(cookie);
    }
    
    public List<Cookie> getCookies() {
    	return getRequestContextForCurrentThreadWithError().getCookies();
    }
    
    public Cookie[] getRequestCookies() {
        return getRequestContextForCurrentThreadWithError().getRequestCookies();
    }

    public void addPageMessage(StatusCode scode, String[] args, String level) {
        getRequestContextForCurrentThreadWithError().addPageMessage(scode, args, level);
    }

    // public boolean isPageFlowRunning() {
    // return getRequestContextForCurrentThreadWithError().isPageFlowRunning();
    // }

    public boolean precedingFlowNeedsData() throws PustefixApplicationException {
        return getRequestContextForCurrentThreadWithError().precedingFlowNeedsData();
    }

    public PageRequestConfig getConfigForCurrentPageRequest() {
        return getRequestContextForCurrentThreadWithError().getConfigForCurrentPageRequest();
    }

    public ContextConfig getContextConfig() {
        return getServerContext().getContextConfig();
    }

    public ProjectInfo getProjectInfo() {
        return getServerContext().getProjectInfo();
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

    public String getCurrentDisplayPageName() {
        return getRequestContextForCurrentThreadWithError().getCurrentDisplayPageName();
    }
    
    public PageRequestStatus getCurrentStatus() {
        return getRequestContextForCurrentThreadWithError().getCurrentStatus();
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
        return null;
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

    public Variant getVariant() {
        return getRequestContextForCurrentThreadWithError().getVariant();
    }

    public Variant getSessionVariant() {
        return sessioncontext.getVariant();
    }

    public String getVisitId() {
        return sessioncontext.getVisitId();
    }

    // public boolean isCurrentPageRequestInCurrentFlow() {
    // return
    // getRequestContextForCurrentThreadWithError().isCurrentPageRequestInCurrentFlow();
    // }

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
        String matchingLang = null;
        if(getTenant() == null) {
            matchingLang = lang;
        } else {
            List<String> supportedLangs = getTenant().getSupportedLanguages();
            if(supportedLangs.contains(lang)) {
                matchingLang = lang;
            } else if(!(lang.contains("_") || lang.contains("-"))) {
                lang = lang + "_";
                for(String supportedLang: supportedLangs) {
                    if(supportedLang.startsWith(lang)) {
                        matchingLang = supportedLang;
                    }
                }
            }
        }
        if(matchingLang != null) {
            getRequestContextForCurrentThreadWithError().setLanguage(matchingLang);
            sessioncontext.setLanguage(matchingLang);
        }
    }

    @Deprecated
    public void setPageAlternative(String key) {
        setCurrentPageAlternative(key);
    }
    
    public void setCurrentPageAlternative(String key) {
        getRequestContextForCurrentThreadWithError().setCurrentPageAlternative(key);
    }
    
    @Deprecated
    public String getPageAlternative() {
        return getCurrentPageAlternative();
    }
    
    public String getCurrentPageAlternative() {
        return getRequestContextForCurrentThreadWithError().getCurrentPageAlternative();
    }
    
    public void setVariant(Variant variant) {
        getRequestContextForCurrentThreadWithError().setVariantForThisRequestOnly(variant);
        sessioncontext.setVariant(variant);
    }

    public void setVariantForThisRequestOnly(Variant variant) {
        getRequestContextForCurrentThreadWithError().setVariantForThisRequestOnly(variant);
    }
    
    public void setTenant(Tenant tenant) {
        sessioncontext.setTenant(tenant);
    }
    
    public Tenant getTenant() {
        return sessioncontext.getTenant();
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

    public void prepareForRequest(HttpServletRequest req) {
        // This allows to use OLDER servercontexts during requests
        requestcontextstore.set(new RequestContextImpl(servercontext, this));
        Tenant matchingTenant = (Tenant)req.getAttribute(TenantScope.REQUEST_ATTRIBUTE_TENANT);
        if(matchingTenant != null) {
            Tenant currentTenant = getTenant();
            if(currentTenant == null) {
                setTenant(matchingTenant);
                setLanguage(matchingTenant.getDefaultLanguage());
            } else {
                if(!currentTenant.equals(matchingTenant)) {
                    //TODO: handle this case
                    throw new PustefixRuntimeException("Illegal tenant switch");
                }
            }
        }
        String matchingLanguage = (String)req.getAttribute(AbstractPustefixRequestHandler.REQUEST_ATTR_LANGUAGE);
        if(matchingLanguage != null) {
            setLanguage(matchingLanguage);
        }
        String pageAltKey = (String)req.getAttribute(AbstractPustefixRequestHandler.REQUEST_ATTR_PAGE_ALTERNATIVE);
        if(pageAltKey != null) {
            setPageAlternative(pageAltKey);
        }
        String langParam = req.getParameter("__language");
        if(langParam != null && langParam.length() > 0) {
            setLanguage(langParam);
        }
    }
    
    public void setPfixServletRequest(PfixServletRequest pfixReq) {
        getRequestContextForCurrentThreadWithError().setPfixServletRequest(pfixReq);
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

    public boolean isValidToken(String tokenName, String token) {
        return sessioncontext.isValidToken(tokenName, token);
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
        this.sessioncontext.setSession(ev.getSession());
    }

    public void valueUnbound(HttpSessionBindingEvent ev) {
        if (ev.getSession() == this.sessioncontext.getSession()) {
            this.sessioncontext.setSession(null);
        }
    }

    public boolean checkIsAccessible(String pagename) throws PustefixApplicationException {
        PageRequest page = createPageRequest(pagename);
        return checkIsAccessible(page);
    }

    public boolean checkIsAccessible(PageRequest page) throws PustefixApplicationException {
        return getRequestContextForCurrentThreadWithError().checkIsAccessible(page);
    }

    public boolean checkNeedsData(String pagename) throws PustefixApplicationException {
        PageRequest page = createPageRequest(pagename);
        return checkNeedsData(page);
    }

    public boolean checkNeedsData(PageRequest page) throws PustefixApplicationException {
        return getRequestContextForCurrentThreadWithError().checkNeedsData(page);
    }

    public PageRequest createPageRequest(String name) {
        return getRequestContextForCurrentThreadWithError().createPageRequest(name);
    }

    public PfixServletRequest getPfixServletRequest() {
        return getRequestContextForCurrentThreadWithError().getPfixServletRequest();
    }
    
    public PageMap getPageMap() {
        return getServerContext().getPageMap();
    }
    
    public boolean needsLastFlowParameter(String pageName, String lastFlowName) {
        return getRequestContextForCurrentThreadWithError().needsLastFlowParameter(pageName, lastFlowName);
    }
    
    public boolean needsPageFlowParameter(String pageName, String flowName) {
    	return getRequestContextForCurrentThreadWithError().needsPageFlowParameter(pageName, flowName);
    }
    
    public String getLastFlow() {
        return sessioncontext.getLastFlow();
    }
    
    public void setLastFlow(String lastFlow) {
        sessioncontext.setLastFlow(lastFlow);
    }
}
