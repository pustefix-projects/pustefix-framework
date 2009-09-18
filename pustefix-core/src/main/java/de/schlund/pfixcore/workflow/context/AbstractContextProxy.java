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

import java.util.Properties;

import javax.servlet.http.Cookie;

import org.pustefixframework.config.contextxmlservice.ContextConfig;

import de.schlund.pfixcore.auth.Authentication;
import de.schlund.pfixcore.exception.PustefixApplicationException;
import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixcore.workflow.ContextResourceManager;
import de.schlund.pfixcore.workflow.PageRequest;
import de.schlund.pfixcore.workflow.PageRequestStatus;
import de.schlund.pfixcore.workflow.SessionStatusListener;
import de.schlund.pfixxml.PfixServletRequest;
import de.schlund.pfixxml.Variant;
import de.schlund.util.statuscodes.StatusCode;

/**
 * Abstract implementation for a context object that delegates
 * all actions to a target object.  
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
@SuppressWarnings("deprecation")
public abstract class AbstractContextProxy implements Context {

    /**
     * Returns the target object. Has to be implemented by child
     * classes.
     * 
     * @return the context instance all actions are performed on
     */
    protected abstract Context getContext();

    public void addCookie(Cookie cookie) {
        getContext().addCookie(cookie);
    }

    public void addPageMessage(StatusCode scode, String[] args, String level) {
        getContext().addPageMessage(scode, args, level);
    }

    public void addSessionStatusListener(SessionStatusListener l) {
        getContext().addSessionStatusListener(l);
    }

    public boolean checkIsAccessible(PageRequest page) throws PustefixApplicationException {
        return getContext().checkIsAccessible(page);
    }

    public boolean checkNeedsData(PageRequest page) throws PustefixApplicationException {
        return getContext().checkNeedsData(page);
    }

    public PageRequest createPageRequest(String name) {
        return getContext().createPageRequest(name);
    }

    public Authentication getAuthentication() {
        return getContext().getAuthentication();
    }

    public ContextConfig getContextConfig() {
        return getContext().getContextConfig();
    }

    public PageFlow getCurrentPageFlow() {
        return getContext().getCurrentPageFlow();
    }

    public PageRequest getCurrentPageRequest() {
        return getContext().getCurrentPageRequest();
    }

    public PageRequestStatus getCurrentStatus() {
        return getContext().getCurrentStatus();
    }

    public String getLanguage() {
        return getContext().getLanguage();
    }

    public Throwable getLastException() {
        return getContext().getLastException();
    }

    public Properties getProperties() {
        return getContext().getProperties();
    }

    public Properties getPropertiesForContextResource(Object res) {
        return getContext().getPropertiesForContextResource(res);
    }

    public Properties getPropertiesForCurrentPageRequest() {
        return getContext().getPropertiesForCurrentPageRequest();
    }

    public Cookie[] getRequestCookies() {
        return getContext().getRequestCookies();
    }

    public String getVisitId() {
        return getContext().getVisitId();
    }

    public boolean isJumpToPageFlowSet() {
        return getContext().isJumpToPageFlowSet();
    }

    public boolean isJumpToPageSet() {
        return getContext().isJumpToPageSet();
    }

    public boolean isProhibitContinueSet() {
        return getContext().isProhibitContinueSet();
    }

    public void markSessionForCleanup() {
        getContext().markSessionForCleanup();
    }

    public boolean precedingFlowNeedsData() throws PustefixApplicationException {
        return getContext().precedingFlowNeedsData();
    }

    public void prohibitContinue() {
        getContext().prohibitContinue();
    }

    public void removeSessionStatusListener(SessionStatusListener l) {
        getContext().removeSessionStatusListener(l);
    }

    public void setCurrentPageFlow(String pageflow) {
        getContext().setCurrentPageFlow(pageflow);
    }

    public void setJumpToPage(String pagename) {
        getContext().setJumpToPage(pagename);
    }

    public void setJumpToPageFlow(String pageflow) {
        getContext().setJumpToPageFlow(pageflow);
    }

    public void setLanguage(String lang) {
        getContext().setLanguage(lang);
    }

    public void setVariant(Variant variant) {
        getContext().setVariant(variant);
    }

    public void setVariantForThisRequestOnly(Variant variant) {
        getContext().setVariantForThisRequestOnly(variant);
    }

    public boolean stateMustSupplyFullDocument() {
        return getContext().stateMustSupplyFullDocument();
    }

    public boolean checkIsAccessible(String pagename) throws PustefixApplicationException {
        return getContext().checkIsAccessible(pagename);
    }

    public boolean checkNeedsData(String pagename) throws PustefixApplicationException {
        return getContext().checkNeedsData(pagename);
    }

    public ContextResourceManager getContextResourceManager() {
        return getContext().getContextResourceManager();
    }

    public PfixServletRequest getPfixServletRequest() {
        return getContext().getPfixServletRequest();
    }

    public Variant getVariant() {
        return getContext().getVariant();
    }

}
