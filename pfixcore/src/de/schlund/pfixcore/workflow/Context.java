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

import de.schlund.pfixcore.auth.Authentication;
import de.schlund.pfixcore.exception.PustefixApplicationException;
import de.schlund.pfixcore.workflow.context.PageFlow;
import de.schlund.pfixxml.Variant;
import de.schlund.pfixxml.config.ContextConfig;
import de.schlund.pfixxml.config.PageRequestConfig;
import de.schlund.util.statuscodes.StatusCode;

/**
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public interface Context {
    ContextResourceManager getContextResourceManager();
    Properties             getProperties();
    Properties             getPropertiesForCurrentPageRequest();
    PageRequestConfig      getConfigForCurrentPageRequest();
    PageRequest            getCurrentPageRequest();
    PageFlow               getCurrentPageFlow();
    void                   setCurrentPageFlow(String pageflow);
    void                   setJumpToPage(String pagename);
    void                   setJumpToPageFlow(String pageflow);
    void                   prohibitContinue();
    void                   forceStopAtNextStep(boolean forcestop);
    Cookie[]               getRequestCookies();
    void                   setLanguage(String lang);
    String                 getLanguage();
    void                   addCookie(Cookie cookie);
    Variant                getVariant();
    void                   setVariant(Variant variant);
    void                   setVariantForThisRequestOnly(Variant variant);
    String                 getVisitId();
    void                   addSessionStatusListener(SessionStatusListener l);
    void                   removeSessionStatusListener(SessionStatusListener l);
    boolean                precedingFlowNeedsData() throws PustefixApplicationException;
    boolean                finalPageIsRunning();
    boolean                jumpToPageIsRunning();
    boolean                flowIsRunning();
    boolean                isCurrentPageRequestInCurrentFlow();
    boolean                isCurrentPageFlowRequestedByUser();
    boolean                isJumpToPageSet();
    boolean                isJumpToPageFlowSet();
    boolean                isProhibitContinueSet();
    boolean                stateMustSupplyFullDocument();
    String                 getName();
    Throwable              getLastException();
    void                   addPageMessage(StatusCode scode);
    void                   addPageMessage(StatusCode scode, String level);
    void                   addPageMessage(StatusCode scode, String[] args);
    void                   addPageMessage(StatusCode scode, String[] args, String level);
    Properties             getPropertiesForContextResource(Object res);
    ContextConfig          getContextConfig();
    Authentication         getAuthentication();
    
    /**
     * Tells the servlet that the session for this context is not longer needed 
     * and can be deleted. However, there is no guarantee <b>when</b> the session
     * will be deleted. Usually, there will be some delay, between the call of
     * this method and the actual invalidation taking place, so the output page
     * of the current request can still be rendered.
     * <b>Do not use this method if you are concerned about security!</b> As the
     * session is not invalidated immediately, session data is still available for
     * some time after calling this method. If you keep sensitive data in the
     * session (e.g. login data), you should reset the corresponding context
     * resources instead of using this method. This method is only provided for
     * memory reasons (so that memory allocated by this session can be freed, if
     * it is not needed any more). 
     */
    void                   markSessionForCleanup();
}
