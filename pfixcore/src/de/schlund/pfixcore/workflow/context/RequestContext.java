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

package de.schlund.pfixcore.workflow.context;

import java.util.Properties;

import javax.servlet.http.Cookie;

import de.schlund.pfixcore.workflow.PageFlow;
import de.schlund.pfixcore.workflow.PageRequest;
import de.schlund.pfixxml.Variant;
import de.schlund.util.statuscodes.StatusCode;

/**
 * Stores data associated to a request and the state of the request handling. 
 * As a request is only handled by one thread at the same time, 
 * implementations do not have to thread-safe. However components that are
 * shared between requests have to be.
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public interface RequestContext {
    /**
     * Returns properties being set fo the page that was requested.
     * If the requested page changes during request processsing (due to
     * pageflow handling) the return value of this method will change
     * accordingly.
     * 
     * @return properties for the current page request
     */
    Properties getPropertiesForCurrentPageRequest();
    
    /**
     * Returns the page that was requested. May change during request
     * processing due to pageflow handling.
     * 
     * @return current page request
     */
    PageRequest getPageRequest();
    
    /**
     * Returns the pageflow that is active at the moment. This pageflow
     * may not contain the current page. Use {@link #isCurrentPageRequestInCurrentFlow()}
     * to check whether the page is within the flow.
     * 
     * @return the active pageflow
     */
    PageFlow getPageFlow();
    
    /**
     * Sets the active pageflow
     * 
     * @param pageflow pageflow to use
     */
    void setPageFlow(String pageflow);
    
    /**
     * Returns the page processing will jump to after having processed the
     * current page request. This page might have been set by the
     * {@link #setJumpToPage(String)} method or by a parameter in the
     * request URL.
     * 
     * @return Page processing will jump to after having processed the
     *         current page
     */
    String getJumpToPage();
    
    /**
     * Sets the page to jump to after the processing of the current page
     * request has been completed. This means that instead of returning the
     * current page to the user or continuing with the active pageflow, the
     * specified page will be requested and returned to the user.
     * 
     * @param pagename name of the page to jump to. Must not containt variant,
     *        as the right variant is being selected automatically.
     */
    void setJumpToPage(String pagename);
    
    /**
     * Returns the pageflow to use when jumping to another page. Will only
     * have any effect if jumpToPage is set, too. If the page specified in
     * jumpToPage is contained in more than one flow, the right pageflow
     * will be selected using this name. Can be set programmatically as well
     * as by a parameter in the request URL.
     * 
     * @return pageflow to use when jumping to another page
     */
    String getJumpToPageFlow();
    
    /**
     * Sets the pageflow to use when jumping to another page.
     * 
     * @param pageflow name of the pageflow to use for the jumpTo page
     * @see #getJumpToPageFlow() for details about how this property is used
     */
    void setJumpToPageFlow(String pageflow);
    
    /**
     * Will force the pageflow to stay on the current page. Useful
     * for example when an error has occured while handling user input.
     */
    void prohibitContinue();
    
    /**
     * Returns whether {@link #prohibitContinue()} has been called earlier
     * and thus the pageflow will stay on the current page. 
     * 
     * @return <code>true</code> if {@link #prohibitContinue()} has been called
     */
    boolean isProhibitContinue();
    
    /**
     * Checks whether any page that is in the current flow before the
     * current step needs data. This is done by iterating through the flow
     * and checking for each step whether the corresponding state (if it is
     * active) has needsData() set to true. This iteration is performed right
     * before the current step, so needsData() for the current page and any
     * following pages is not checked.
     *  
     * @return <code>true</code> if the (active) state of at least one page 
     * that precedes the current step in the current flow returns <code>true</code>
     * for <code>needsData()</code>. returns <code>false</code> if none of 
     * the (active) preceding steps indicates to require input.
     */
    boolean flowStepsBeforeCurrentStepNeedData() throws Exception;
    
    /**
     * Returns <code>true</code> if the currently served page is the last
     * page in the active pageflow.
     * 
     * @return <code>true</code> if current page is last page in flow,
     *         <code>false</code> otherwise
     */
    boolean finalPageIsRunning();
    
    /**
     * Returns <code>true</code> if the current page has been triggered
     * by the jumpToPage parameter.
     * 
     * @return <code>true</code> if just processing a jumpTo page,
     *         <code>false</code> otherwise
     */
    boolean jumpToPageIsRunning();
    
    /**
     * Checks whether the current page was triggered by a flow or requested
     * directly.
     * 
     * @return <code>true</code> if current page was triggered by a flow,
     * <code>false</code> if it was requested directly
     */
    boolean flowIsRunning();
    
    /**
     * Returns <code>true</code> if the current page is contained within
     * the active pageflow.
     * 
     * @return <code>true</code> if current page is within the pageflow, 
     *         <code>false</code> otherwise.
     */
    boolean isCurrentPageRequestInCurrentFlow();
    
    /**
     * Returns <code>true</code> if the pageflow has been set by a 
     * special parameter in the request URL
     * 
     * @return <code>true</code> if pageflow has just been set by the
     *         frontend, <code>false</code> otherwise
     */
    boolean isCurrentPageFlowRequestedByUser();
    
    /**
     * Tells the system not to use the navigation tree again.
     * This method should be called when the return value of
     * {@link de.schlund.pfixcore.workflow.State#isAccessible(Context, PfixServletRequest)}
     * of some pages might have changed. 
     */
    void invalidateNavigation();
    
    /**
     * Tells the system that accessibility state of all pages cannot have
     * changed and so the navigation tree might be savely reused.
     */
    void reuseNavigation();
    
    /**
     * Returns the language that is set for this request. On creation
     * of the request, the language is retrieved from the session.
     * Changes of the language in the session will not affect an active
     * request.
     * 
     * @return the current language
     */
    String getLanguage();
    
    /**
     * Sets the language to use for the current request. This will also
     * set the language in the session, however this setting might be
     * overwritten by other requests for the same session.
     * 
     * @param lang code identifying the language that should be used
     */
    void setLanguage(String lang);
    
    /**
     * Returns the variant being used for this request. A change of the 
     * variant in the session, that is triggered by another request, will not
     * affect this setting.
     * 
     * @return current variant or <code>null</code> if no variant is set
     */
    Variant getVariant();
    
    /**
     * Sets the variant to use. Will change the variant for this request as well
     * as for the whole session.
     * 
     * @param variant variant to use for further processing
     */
    void setVariant(Variant variant);
    
    /**
     * Sets the variant to use for this request. Will not affect the variant
     * that is stored in the session.
     * 
     * @param variant variant to use for this request 
     */
    void setVariantForThisRequestOnly(Variant variant);
    
    /**
     * Indicates whether the state has to supply a complete document.
     * Will return <code>false</code> if the system already knows, that the
     * current page will not be rendered, thus no processing time will have
     * to be spent on building an unused document.
     * 
     * @return <code>true</code> if a complete document is needed for rendering,
     *         <code>false</code> if document is being to be ommitted and thus 
     *         does not have to be complete
     */
    boolean stateMustSupplyFullDocument();
    
    /**
     * Adds a page message that will be included in the output document.
     * Like form errors page messages are set using status codes, however
     * they will usually not affect the pageflow but instead be displayed
     * on whichever page is returned to the user (in fact the page has
     * to display the message itself, however it is always included in the
     * DOM tree).
     * 
     * @param scode status code identifying the page message to display
     */
    void addPageMessage(StatusCode scode);
    
    /**
     * Adds a page message with a certain level. Levels can be used
     * to distinguish between different cases (e.g. severity).
     * 
     * @param scode status code of the page message
     * @param level code identifying the level to use
     * @see #addPageMessage(StatusCode)
     */
    void addPageMessage(StatusCode scode, String level);
    
    /**
     * Adds a page message using the supplied arguments. Arguments can
     * be used by the include part defined for a status message to 
     * include runtime data within the displayed text. 
     * 
     * @param scode status code of the page message
     * @param args array containing arguments in right order
     * @see #addPageMessage(StatusCode)
     */
    void addPageMessage(StatusCode scode, String[] args);
    
    /**
     * Adds a page message with a level and arguments.
     * 
     * @param scode status code of the page message
     * @param args array containing arguments in right order
     * @param level code identifying the level to use
     * @see #addPageMessage(StatusCode, String[])
     * @see #addPageMessage(StatusCode, String)
     */
    void addPageMessage(StatusCode scode, String[] args, String level);
    
    /**
     * Returns an array containing all cookies that were sent with the current
     * request.
     * 
     * @return Array containing objects each representing a cookie that was sent
     *         by the browser
     */
    Cookie[] getCookies();
    
    /**
     * Sends the supplied cookie with the response to the browser.
     * 
     * @param cookie object containing the cookie data that will be sent to the
     *        browser
     */
    void addCookie(Cookie cookie);
}
