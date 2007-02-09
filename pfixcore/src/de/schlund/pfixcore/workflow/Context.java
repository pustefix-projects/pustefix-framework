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



import de.schlund.pfixcore.exception.PustefixApplicationException;
import de.schlund.pfixcore.exception.PustefixCoreException;
import de.schlund.pfixcore.workflow.context.PageFlow;
import de.schlund.pfixxml.PfixServletRequest;
import de.schlund.pfixxml.SPDocument;
import de.schlund.pfixxml.Variant;
import de.schlund.pfixxml.config.ContextConfig;
import de.schlund.pfixxml.config.PageRequestConfig;
import de.schlund.util.statuscodes.StatusCode;
import java.util.Properties;
import javax.servlet.http.Cookie;

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
    Properties             getPropertiesForContextResource(ContextResource res);
    ContextConfig          getContextConfig();
    SPDocument             handleRequest(PfixServletRequest preq) throws PustefixApplicationException, PustefixCoreException;
}
