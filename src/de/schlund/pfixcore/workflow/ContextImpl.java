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
    
    private SessionContextImpl scontext;
    private ThreadLocal<RequestContextImpl> rcontext = new ThreadLocal<RequestContextImpl>();
    
    public ContextImpl(ServerContextImpl context, HttpSession session) throws Exception {
        this.scontext = new SessionContextImpl(context, this, session);
    }

    public RequestContextImpl getRequestContextForCurrentThread() {
        RequestContextImpl rcontext = this.rcontext.get();
        return rcontext;
    }

    private RequestContextImpl getRequestContextForCurrentThreadWithError() {
        RequestContextImpl rcontext = this.rcontext.get();
        if (rcontext == null) {
            throw new IllegalStateException("Request object is not available for current thread");
        }
        return rcontext;
    }
    
    public void addCookie(Cookie cookie) {
        RequestContextImpl rcontext = getRequestContextForCurrentThreadWithError();
        rcontext.addCookie(cookie);
    }

    public void addPageMessage(StatusCode scode) {
        RequestContextImpl rcontext = getRequestContextForCurrentThreadWithError();
        rcontext.addPageMessage(scode);
    }

    public void addPageMessage(StatusCode scode, String level) {
        RequestContextImpl rcontext = getRequestContextForCurrentThreadWithError();
        rcontext.addPageMessage(scode, level);
    }

    public void addPageMessage(StatusCode scode, String[] args) {
        RequestContextImpl rcontext = getRequestContextForCurrentThreadWithError();
        rcontext.addPageMessage(scode, args);
    }

    public void addPageMessage(StatusCode scode, String[] args, String level) {
        RequestContextImpl rcontext = getRequestContextForCurrentThreadWithError();
        rcontext.addPageMessage(scode, args, level);
    }

    public boolean finalPageIsRunning() {
        RequestContextImpl rcontext = getRequestContextForCurrentThreadWithError();
        return rcontext.finalPageIsRunning();
    }

    public boolean flowIsRunning() {
        RequestContextImpl rcontext = getRequestContextForCurrentThreadWithError();
        return rcontext.flowIsRunning();
    }

    public boolean flowStepsBeforeCurrentStepNeedData() throws Exception {
        RequestContextImpl rcontext = getRequestContextForCurrentThreadWithError();
        return rcontext.flowStepsBeforeCurrentStepNeedData();
    }

    public PageRequestConfig getConfigForCurrentPageRequest() {
        RequestContextImpl rcontext = getRequestContextForCurrentThreadWithError();
        return rcontext.getConfigForCurrentPageRequest();
    }

    public ContextConfig getContextConfig() {
        RequestContextImpl rcontext = getRequestContextForCurrentThreadWithError();
        return rcontext.getContextConfig();
    }

    public ContextResourceManager getContextResourceManager() {
        RequestContextImpl rcontext = getRequestContextForCurrentThreadWithError();
        return rcontext.getContextResourceManager();
    }

    public PageFlow getCurrentPageFlow() {
        RequestContextImpl rcontext = getRequestContextForCurrentThreadWithError();
        return rcontext.getCurrentPageFlow();
    }

    public PageRequest getCurrentPageRequest() {
        RequestContextImpl rcontext = getRequestContextForCurrentThreadWithError();
        return rcontext.getCurrentPageRequest();
    }

    public String getLanguage() {
        RequestContextImpl rcontext = getRequestContextForCurrentThreadWithError();
        return rcontext.getLanguage();
    }

    public Throwable getLastException() {
        RequestContextImpl rcontext = getRequestContextForCurrentThreadWithError();
        return rcontext.getLastException();
    }

    public String getName() {
        RequestContextImpl rcontext = getRequestContextForCurrentThreadWithError();
        return rcontext.getName();
    }

    public Properties getProperties() {
        RequestContextImpl rcontext = getRequestContextForCurrentThreadWithError();
        return rcontext.getProperties();
    }

    public Properties getPropertiesForContextResource(ContextResource res) {
        RequestContextImpl rcontext = getRequestContextForCurrentThreadWithError();
        return rcontext.getPropertiesForContextResource(res);
    }

    public Properties getPropertiesForCurrentPageRequest() {
        RequestContextImpl rcontext = getRequestContextForCurrentThreadWithError();
        return rcontext.getPropertiesForCurrentPageRequest();
    }

    public Cookie[] getRequestCookies() {
        RequestContextImpl rcontext = getRequestContextForCurrentThreadWithError();
        return rcontext.getRequestCookies();
    }

    public Variant getVariant() {
        RequestContextImpl rcontext = getRequestContextForCurrentThreadWithError();
        return rcontext.getVariant();
    }

    public String getVisitId() {
        RequestContextImpl rcontext = getRequestContextForCurrentThreadWithError();
        return rcontext.getVisitId();
    }

    public boolean isCurrentPageFlowRequestedByUser() {
        RequestContextImpl rcontext = getRequestContextForCurrentThreadWithError();
        return rcontext.isCurrentPageFlowRequestedByUser();
    }

    public boolean isCurrentPageRequestInCurrentFlow() {
        RequestContextImpl rcontext = getRequestContextForCurrentThreadWithError();
        return rcontext.isCurrentPageRequestInCurrentFlow();
    }

    public boolean isJumpToPageFlowSet() {
        RequestContextImpl rcontext = getRequestContextForCurrentThreadWithError();
        return rcontext.isJumpToPageFlowSet();
    }

    public boolean isJumpToPageSet() {
        RequestContextImpl rcontext = getRequestContextForCurrentThreadWithError();
        return rcontext.isJumpToPageSet();
    }

    public boolean isProhibitContinueSet() {
        RequestContextImpl rcontext = getRequestContextForCurrentThreadWithError();
        return rcontext.isProhibitContinueSet();
    }

    public boolean jumpToPageIsRunning() {
        RequestContextImpl rcontext = getRequestContextForCurrentThreadWithError();
        return rcontext.jumpToPageIsRunning();
    }

    public void prohibitContinue() {
        RequestContextImpl rcontext = getRequestContextForCurrentThreadWithError();
        rcontext.prohibitContinue();
    }

    public void setCurrentPageFlow(String pageflow) {
        RequestContextImpl rcontext = getRequestContextForCurrentThreadWithError();
        rcontext.setCurrentPageFlow(pageflow);
    }

    public void setJumpToPage(String pagename) {
        RequestContextImpl rcontext = getRequestContextForCurrentThreadWithError();
        rcontext.setJumpToPage(pagename);
    }

    public void setJumpToPageFlow(String pageflow) {
        RequestContextImpl rcontext = getRequestContextForCurrentThreadWithError();
        rcontext.setJumpToPageFlow(pageflow);
    }

    public void setLanguage(String lang) {
        RequestContextImpl rcontext = getRequestContextForCurrentThreadWithError();
        rcontext.setLanguage(lang);
    }

    public void setVariant(Variant variant) {
        RequestContextImpl rcontext = getRequestContextForCurrentThreadWithError();
        rcontext.setVariant(variant);
    }

    public void setVariantForThisRequestOnly(Variant variant) {
        RequestContextImpl rcontext = getRequestContextForCurrentThreadWithError();
        rcontext.setVariantForThisRequestOnly(variant);
    }

    public boolean stateMustSupplyFullDocument() {
        RequestContextImpl rcontext = getRequestContextForCurrentThreadWithError();
        return rcontext.stateMustSupplyFullDocument();
    }
    
    public boolean isPageAccessible(String pagename) throws Exception {
        RequestContextImpl rcontext = getRequestContextForCurrentThreadWithError();
        return rcontext.isPageAccessible(pagename);
    }

    public boolean isPageAlreadyVisited(String pagename) throws Exception {
        RequestContextImpl rcontext = getRequestContextForCurrentThreadWithError();
        return rcontext.isPageAlreadyVisited(pagename);
    }
    
    public String getLastPageName() {
        return scontext.getLastPageName();
    }
    
    public void prepareForRequest(ServerContextImpl context) throws Exception {
        this.rcontext.set(new RequestContextImpl(context, scontext, this));
    }
    
    public SPDocument handleRequest(PfixServletRequest preq) throws Exception {
        return this.getRequestContextForCurrentThreadWithError().handleRequest(preq);
    }
    
    public void cleanupAfterRequest() {
        this.rcontext.set(null);
    }
    
    // Used by TransformerCallback to set the right RequestContextImpl when
    // rendering a page
    public void setRequestContextForCurrentThread(RequestContextImpl rcontext) {
        this.rcontext.set(rcontext);
    }

}
