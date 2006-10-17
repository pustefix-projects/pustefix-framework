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

import de.schlund.pfixcore.workflow.context.ServerContextImpl;
import de.schlund.pfixcore.workflow.context.SessionContextImpl;
import de.schlund.pfixxml.PfixServletRequest;
import de.schlund.pfixxml.SPDocument;
import de.schlund.pfixxml.Variant;
import de.schlund.pfixxml.config.ContextConfig;
import de.schlund.pfixxml.config.PageRequestConfig;
import de.schlund.util.statuscodes.StatusCode;

public class ContextImpl implements Context {
    
    private SessionContextImpl scontext;
    private ThreadLocal<RequestContextImpl> rcontext = new ThreadLocal<RequestContextImpl>();
    
    public ContextImpl(ServerContextImpl context, HttpSession session) throws Exception {
        this.scontext = new SessionContextImpl(context, session);
    }

    public RequestContextImpl getRequestContextForCurrentThread() {
        RequestContextImpl rcontext = this.rcontext.get();
        if (rcontext == null) {
            throw new IllegalStateException("Request object is not available for current thread");
        }
        return rcontext;
    }
    
    public void addCookie(Cookie cookie) {
        RequestContextImpl rcontext = getRequestContextForCurrentThread();
        rcontext.addCookie(cookie);
    }

    public void addPageMessage(StatusCode scode) {
        RequestContextImpl rcontext = getRequestContextForCurrentThread();
        rcontext.addPageMessage(scode);
    }

    public void addPageMessage(StatusCode scode, String level) {
        RequestContextImpl rcontext = getRequestContextForCurrentThread();
        rcontext.addPageMessage(scode, level);
    }

    public void addPageMessage(StatusCode scode, String[] args) {
        RequestContextImpl rcontext = getRequestContextForCurrentThread();
        rcontext.addPageMessage(scode, args);
    }

    public void addPageMessage(StatusCode scode, String[] args, String level) {
        RequestContextImpl rcontext = getRequestContextForCurrentThread();
        rcontext.addPageMessage(scode, args, level);
    }

    public boolean finalPageIsRunning() {
        RequestContextImpl rcontext = getRequestContextForCurrentThread();
        return rcontext.finalPageIsRunning();
    }

    public boolean flowIsRunning() {
        RequestContextImpl rcontext = getRequestContextForCurrentThread();
        return rcontext.flowIsRunning();
    }

    public boolean flowStepsBeforeCurrentStepNeedData() throws Exception {
        RequestContextImpl rcontext = getRequestContextForCurrentThread();
        return rcontext.flowStepsBeforeCurrentStepNeedData();
    }

    public PageRequestConfig getConfigForCurrentPageRequest() {
        RequestContextImpl rcontext = getRequestContextForCurrentThread();
        return rcontext.getConfigForCurrentPageRequest();
    }

    public ContextConfig getContextConfig() {
        RequestContextImpl rcontext = getRequestContextForCurrentThread();
        return rcontext.getContextConfig();
    }

    public ContextResourceManager getContextResourceManager() {
        RequestContextImpl rcontext = getRequestContextForCurrentThread();
        return rcontext.getContextResourceManager();
    }

    public PageFlow getCurrentPageFlow() {
        RequestContextImpl rcontext = getRequestContextForCurrentThread();
        return rcontext.getCurrentPageFlow();
    }

    public PageRequest getCurrentPageRequest() {
        RequestContextImpl rcontext = getRequestContextForCurrentThread();
        return rcontext.getCurrentPageRequest();
    }

    public String getLanguage() {
        RequestContextImpl rcontext = getRequestContextForCurrentThread();
        return rcontext.getLanguage();
    }

    public Throwable getLastException() {
        RequestContextImpl rcontext = getRequestContextForCurrentThread();
        return rcontext.getLastException();
    }

    public String getName() {
        RequestContextImpl rcontext = getRequestContextForCurrentThread();
        return rcontext.getName();
    }

    public Properties getProperties() {
        RequestContextImpl rcontext = getRequestContextForCurrentThread();
        return rcontext.getProperties();
    }

    public Properties getPropertiesForContextResource(ContextResource res) {
        RequestContextImpl rcontext = getRequestContextForCurrentThread();
        return rcontext.getPropertiesForContextResource(res);
    }

    public Properties getPropertiesForCurrentPageRequest() {
        RequestContextImpl rcontext = getRequestContextForCurrentThread();
        return rcontext.getPropertiesForCurrentPageRequest();
    }

    public Cookie[] getRequestCookies() {
        RequestContextImpl rcontext = getRequestContextForCurrentThread();
        return rcontext.getRequestCookies();
    }

    public Variant getVariant() {
        RequestContextImpl rcontext = getRequestContextForCurrentThread();
        return rcontext.getVariant();
    }

    public String getVisitId() {
        RequestContextImpl rcontext = getRequestContextForCurrentThread();
        return rcontext.getVisitId();
    }

    public boolean isCurrentPageFlowRequestedByUser() {
        RequestContextImpl rcontext = getRequestContextForCurrentThread();
        return rcontext.isCurrentPageFlowRequestedByUser();
    }

    public boolean isCurrentPageRequestInCurrentFlow() {
        RequestContextImpl rcontext = getRequestContextForCurrentThread();
        return rcontext.isCurrentPageRequestInCurrentFlow();
    }

    public boolean isJumpToPageFlowSet() {
        RequestContextImpl rcontext = getRequestContextForCurrentThread();
        return rcontext.isJumpToPageFlowSet();
    }

    public boolean isJumpToPageSet() {
        RequestContextImpl rcontext = getRequestContextForCurrentThread();
        return rcontext.isJumpToPageSet();
    }

    public boolean isProhibitContinueSet() {
        RequestContextImpl rcontext = getRequestContextForCurrentThread();
        return rcontext.isProhibitContinueSet();
    }

    public boolean jumpToPageIsRunning() {
        RequestContextImpl rcontext = getRequestContextForCurrentThread();
        return rcontext.jumpToPageIsRunning();
    }

    public void prohibitContinue() {
        RequestContextImpl rcontext = getRequestContextForCurrentThread();
        rcontext.prohibitContinue();
    }

    public void setCurrentPageFlow(String pageflow) {
        RequestContextImpl rcontext = getRequestContextForCurrentThread();
        rcontext.setCurrentPageFlow(pageflow);
    }

    public void setJumpToPage(String pagename) {
        RequestContextImpl rcontext = getRequestContextForCurrentThread();
        rcontext.setJumpToPage(pagename);
    }

    public void setJumpToPageFlow(String pageflow) {
        RequestContextImpl rcontext = getRequestContextForCurrentThread();
        rcontext.setJumpToPageFlow(pageflow);
    }

    public void setLanguage(String lang) {
        RequestContextImpl rcontext = getRequestContextForCurrentThread();
        rcontext.setLanguage(lang);
    }

    public void setVariant(Variant variant) {
        RequestContextImpl rcontext = getRequestContextForCurrentThread();
        rcontext.setVariant(variant);
    }

    public void setVariantForThisRequestOnly(Variant variant) {
        RequestContextImpl rcontext = getRequestContextForCurrentThread();
        rcontext.setVariantForThisRequestOnly(variant);
    }

    public boolean stateMustSupplyFullDocument() {
        RequestContextImpl rcontext = getRequestContextForCurrentThread();
        return rcontext.stateMustSupplyFullDocument();
    }
    
    public String getLastPageName() {
        return scontext.getLastPageName();
    }
    
    public void prepareForRequest(ServerContextImpl context) throws Exception {
        this.rcontext.set(new RequestContextImpl(context, scontext));
    }
    
    public SPDocument handleRequest(PfixServletRequest preq) throws Exception {
        return this.getRequestContextForCurrentThread().handleRequest(preq);
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
