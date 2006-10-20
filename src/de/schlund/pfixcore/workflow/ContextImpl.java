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

import java.util.Properties;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;

import de.schlund.pfixcore.workflow.context.AccessibilityChecker;
import de.schlund.pfixcore.workflow.context.ServerContextImpl;
import de.schlund.pfixcore.workflow.context.SessionContextImpl;
import de.schlund.pfixxml.PfixServletRequest;
import de.schlund.pfixxml.SPDocument;
import de.schlund.pfixxml.Variant;
import de.schlund.pfixxml.config.ContextConfig;
import de.schlund.pfixxml.config.PageRequestConfig;
import de.schlund.util.statuscodes.StatusCode;

public class ContextImpl implements Context, AccessibilityChecker {
    
    private SessionContextImpl sessioncontext;
    private ThreadLocal<RequestContextImpl> requestcontextstore = new ThreadLocal<RequestContextImpl>();
    
    public ContextImpl(ServerContextImpl servercontext, HttpSession session) throws Exception {
        sessioncontext = new SessionContextImpl(servercontext, this, session);
    }

    public RequestContextImpl getRequestContextForCurrentThread() {
        return requestcontextstore.get();
    }

    private RequestContextImpl getRequestContextForCurrentThreadWithError() {
        RequestContextImpl requestcontext = requestcontextstore.get();
        if (requestcontext == null) {
            throw new IllegalStateException("Request object is not available for current thread");
        }
        return requestcontext;
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

    public boolean flowStepsBeforeCurrentStepNeedData() throws Exception {
        return getRequestContextForCurrentThreadWithError().flowStepsBeforeCurrentStepNeedData();
    }

    public PageRequestConfig getConfigForCurrentPageRequest() {
        return getRequestContextForCurrentThreadWithError().getConfigForCurrentPageRequest();
    }

    public ContextConfig getContextConfig() {
        return getRequestContextForCurrentThreadWithError().getContextConfig();
    }

    public ContextResourceManager getContextResourceManager() {
        return getRequestContextForCurrentThreadWithError().getContextResourceManager();
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

    public Throwable getLastException() {
        return getRequestContextForCurrentThreadWithError().getLastException();
    }

    public String getName() {
        return getRequestContextForCurrentThreadWithError().getName();
    }

    public Properties getProperties() {
        return getRequestContextForCurrentThreadWithError().getProperties();
    }

    public Properties getPropertiesForContextResource(ContextResource res) {
        return getRequestContextForCurrentThreadWithError().getPropertiesForContextResource(res);
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

    public String getVisitId() {
        return getRequestContextForCurrentThreadWithError().getVisitId();
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
    }

    public void setVariant(Variant variant) {
        getRequestContextForCurrentThreadWithError().setVariant(variant);
    }

    public void setVariantForThisRequestOnly(Variant variant) {
        getRequestContextForCurrentThreadWithError().setVariantForThisRequestOnly(variant);
    }

    public boolean stateMustSupplyFullDocument() {
        return getRequestContextForCurrentThreadWithError().stateMustSupplyFullDocument();
    }
    
    public boolean isPageAccessible(String pagename) throws Exception {
        return getRequestContextForCurrentThreadWithError().isPageAccessible(pagename);
    }

    public boolean isPageAlreadyVisited(String pagename) throws Exception {
        return getRequestContextForCurrentThreadWithError().isPageAlreadyVisited(pagename);
    }
    
    public String getLastPageName() {
        return sessioncontext.getLastPageName();
    }
    
    // ----------------

    public void prepareForRequest(ServerContextImpl servercontext) throws Exception {
        requestcontextstore.set(new RequestContextImpl(servercontext, sessioncontext, this));
    }
    
    public SPDocument handleRequest(PfixServletRequest preq) throws Exception {
        return getRequestContextForCurrentThreadWithError().handleRequest(preq);
    }
    
    public void cleanupAfterRequest() {
        requestcontextstore.set(null);
    }
    
    // Used by TransformerCallback to set the right RequestContextImpl when
    // rendering a page
    public void setRequestContextForCurrentThread(RequestContextImpl requestcontext) {
        requestcontextstore.set(requestcontext);
    }

}
