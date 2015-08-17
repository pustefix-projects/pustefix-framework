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

import java.util.Properties;

import javax.servlet.http.Cookie;

import org.pustefixframework.config.contextxmlservice.ContextConfig;

import de.schlund.pfixcore.auth.Authentication;
import de.schlund.pfixcore.exception.PustefixApplicationException;
import de.schlund.pfixcore.workflow.context.PageFlow;
import de.schlund.pfixxml.Tenant;
import de.schlund.pfixxml.Variant;
import de.schlund.util.statuscodes.StatusCode;

/**
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public interface Context extends PageFlowContext {
    
    Properties getProperties();
    Properties getPropertiesForCurrentPageRequest();
    Properties getPropertiesForContextResource(Object res);
    Authentication getAuthentication();
    ContextConfig getContextConfig();
    PageRequest createPageRequest(String name);
    PageRequest getCurrentPageRequest();
    PageFlow getCurrentPageFlow();
    PageRequestStatus getCurrentStatus();
    
    /**
     * Get the display name of the current page.
     * 
     * The display name of a page will differ from the internal name
     * if there's configured a general alias, or aliases depending on 
     * the currently set language, tenant or page alternative.
     * 
     * @return  current display page name
     */
    String getCurrentDisplayPageName();
    
    /**
     * Returns the currently set page alternative.
     * 
     * You have to be aware that the current value can be subject to change
     * depending on the moment within the request processing lifecycle when
     * you're calling this method, e.g. the value will be updated during a 
     * pageflow process or after a page jump.
     *
     * @return  current page alternative key
     */
    String getCurrentPageAlternative();
    
    /**
     * Set the current page alternative.
     * 
     * You have to be aware that setting the current value only applies
     * to the currently set page, i.e. if a pageflow process or page jump
     * happens afterwards, the value will be reset or changed.
     * 
     * @param  pageAlternativeKey  The current page alternative key
     */
    void setCurrentPageAlternative(String pageAlternativeKey);

    boolean checkIsAccessible(PageRequest page) throws PustefixApplicationException;
    boolean checkNeedsData(PageRequest page) throws PustefixApplicationException;

    void setCurrentPageFlow(String pageflow);

    void setJumpToPage(String pagename);
    boolean isJumpToPageSet();
    void setJumpToPageFlow(String pageflow);
    boolean isJumpToPageFlowSet();
    void prohibitContinue();
    boolean isProhibitContinueSet();

    boolean precedingFlowNeedsData() throws PustefixApplicationException;
    // boolean isCurrentPageRequestInCurrentFlow();
    boolean stateMustSupplyFullDocument();

    void setVariant(Variant variant);
    void setVariantForThisRequestOnly(Variant variant);
    
    void setLanguage(String lang);
    String getLanguage();
    
    /**
     * @deprecated  As of release 0.19.13, replaced by {@link #setCurrentPageAlternative(String)}
     */
    @Deprecated
    void setPageAlternative(String key);

    /**
     * @deprecated  As of release 0.19.13, replaced by {@link #getCurrentPageAlternative()}
     */
    @Deprecated
    String getPageAlternative();

    void addCookie(Cookie cookie);
    Cookie[] getRequestCookies();

    Throwable getLastException();
    String getVisitId();
    
    Tenant getTenant();

    void addPageMessage(StatusCode scode, String[] args, String level);

    void addSessionStatusListener(SessionStatusListener l);
    void removeSessionStatusListener(SessionStatusListener l);

    /**
     * Tells the servlet that the session for this context is not longer needed
     * and can be deleted. However, there is no guarantee <b>when</b> the
     * session will be deleted. Usually, there will be some delay, between the
     * call of this method and the actual invalidation taking place, so the
     * output page of the current request can still be rendered. <b>Do not use
     * this method if you are concerned about security!</b> As the session is
     * not invalidated immediately, session data is still available for some
     * time after calling this method. If you keep sensitive data in the session
     * (e.g. login data), you should reset the corresponding context resources
     * instead of using this method. This method is only provided for memory
     * reasons (so that memory allocated by this session can be freed, if it is
     * not needed any more).
     */
    void markSessionForCleanup();
}
