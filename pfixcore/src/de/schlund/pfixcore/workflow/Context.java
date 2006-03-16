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
 *
 */

package de.schlund.pfixcore.workflow;

import de.schlund.pfixcore.generator.StatusCodeInfo;
import de.schlund.pfixcore.util.PropertiesUtils;
import de.schlund.pfixcore.workflow.Navigation.NavigationElement;
import de.schlund.pfixxml.*;
import de.schlund.pfixxml.perflogging.PerfEvent;
import de.schlund.pfixxml.perflogging.PerfEventType;
import de.schlund.util.statuscodes.StatusCode;
import java.util.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Category;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class is the corner piece of our workflow concept.
 * Here we decide which {@link de.schlund.pfixcore.workflow.State State} handles the
 * request. This decision is based on the notion of workflows (aka
 * {@link de.schlund.pfixcore.workflow.PageFlow PageFlow}s).
 *
 * @author jtl
 *
 */
public class Context implements AppContext {
    private final static Category LOG                 = Category.getInstance(Context.class.getName());
    private final static String   NOSTORE             = "nostore";
    private final static String   DEFPROP             = "context.defaultpageflow";
    private final static String   STARTIC             = "context.startinterceptor";
    private final static String   ENDIC               = "context.endinterceptor";
    private final static String   NAVPROP             = "xmlserver.depend.xml";
    private final static String   PROP_NAVI_AUTOINV   = "navigation.autoinvalidate";
    private final static String   PROP_NEEDS_SSL      = "needsSSL";
    private final static String   WATCHMODE           = "context.adminmode.watch";
    private final static String   ADMINPAGE           = "context.adminmode.page";
    private final static String   ADMINMODE           = "context.adminmode";
    private final static String   AUTH_PROP           = "authcontext.authpage";
    private final static String   JUMPPAGE            = "__jumptopage";
    private final static String   JUMPPAGEFLOW        = "__jumptopageflow";
    private final static String   PARAM_FLOW          = "__pageflow";
    private final static String   PARAM_LASTFLOW      = "__lf";
    private final static String   PARAM_STARTWITHFLOW = "__startwithflow";
    private final static String   PARAM_FORCESTOP     = "__forcestop";
    private final static String   DEF_MESSAGE_LEVEL   = "info";

    // from constructor
    private String     name;
    private Properties properties;

    // shared between all instances that have the same properties
    private PageFlowManager       pageflowmanager;
    private PageRequestProperties preqprops;
    private PageMap               pagemap;

    // new instance for every Context
    private ContextResourceManager rmanager;
    private Navigation             navigation    = null;
    private PageRequest            authpage      = null;
    private HashSet                visited_pages = null;
    private Variant                variant       = null;
    private ContextInterceptor[]   startIC       = null;
    private ContextInterceptor[]   endIC         = null;

    private Variant variantToRestoreOnNextRequest = null;
    private boolean restoreVariantOnNextRequest   = false;
    
    // values read from properties
    private boolean     autoinvalidate_navi = true;
    private boolean     in_adminmode        = false;
    private PageRequest admin_pagereq;

    // the request state
    private PfixServletRequest currentpservreq;
    private PageRequest        currentpagerequest;
    private PageFlow           currentpageflow;
    private PageRequest        jumptopagerequest;
    private PageFlow           jumptopageflow;
    private boolean            on_jumptopage;
    private boolean            pageflow_requested_by_user;
    private boolean            startwithflow;
    private ArrayList          cookielist;
    private boolean            prohibitcontinue;
    private boolean            stopnextforcurrentrequest;
    private boolean            needs_update;
    
    private ArrayList messages           = new ArrayList();
    private HashMap   navigation_visible = null;
    private String    visit_id           = null;

    /**
     * <code>init</code> sets up the Context for operation.
     *
     * @param properties a <code>Properties</code> value
     * @exception Exception if an error occurs
     */
    public void init(Properties properties, String name) throws Exception {
        this.properties = properties;
        this.name       = name;
        rmanager        = new ContextResourceManager();
        visited_pages   = new HashSet();
        rmanager.init(this);
        reset();
    }

    public void reset() {
        needs_update = true;
        invalidateNavigation();
    }

    /**
     * <code>handleRequest</code> is the entry point where the Context is called
     * from outside to handle the supplied request.
     *
     * @param req a <code>HttpServletRequest</code> value
     * @return a <code>SPDocument</code> value
     * @exception Exception if an error occurs
     */
    public synchronized SPDocument handleRequest(PfixServletRequest preq) throws Exception {
        currentpservreq            = preq;
        prohibitcontinue           = false;
        stopnextforcurrentrequest  = false;
        jumptopagerequest          = null;
        jumptopageflow             = null;
        on_jumptopage              = false;
        pageflow_requested_by_user = false;
        startwithflow              = false;
        cookielist                 = new ArrayList();

        if (restoreVariantOnNextRequest) {
            variant                     = variantToRestoreOnNextRequest;
            restoreVariantOnNextRequest = false;
        }
        
        if (needs_update) {
            do_update();
        }

        if (visit_id == null)
            visit_id = (String) currentpservreq.getSession(false).getAttribute(ServletManager.VISIT_ID);

        RequestParam fstop = currentpservreq.getRequestParam(PARAM_FORCESTOP);
        if (fstop != null && fstop.getValue().equals("true")) {
            // We already decide here to stay on the page, what ever the state wants...
            prohibitContinue();
        }
        if (fstop != null && fstop.getValue().equals("step")) {
            // We want to behave the current pageflow as if it would have the stopnext attribute set to true
            forceStopAtNextStep(true);
        }

        RequestParam swflow = currentpservreq.getRequestParam(PARAM_STARTWITHFLOW);
        if (swflow != null && swflow.getValue().equals("true")) {
            startwithflow = true;
        }

        // This helps to reset the state between different request from different windows
        // representing different locations in the same application.
        // The page will be set a bit below in trySettingPageRequestAndFlow, where the "real" pageflow to use is also deduced.
        // At least, the currentpageflow is updated to be the currently valid variant.
        RequestParam lastflow = currentpservreq.getRequestParam(PARAM_LASTFLOW);
        
        processIC(startIC);
        
        if (lastflow != null && !lastflow.getValue().equals("")) {
            PageFlow tmp = pageflowmanager.getPageFlowByName(lastflow.getValue(), variant);
            if (tmp != null) {
                LOG.debug("* Got last pageflow state from request as [" + tmp.getName() + "]");
                currentpageflow = tmp;
            }
        } else if (currentpageflow != null) {
            currentpageflow = pageflowmanager.getPageFlowByName(currentpageflow.getRootName(), variant);
        }
        // Update currentpagerequest to currently valid variant
        if (currentpagerequest != null) {
            currentpagerequest = PageRequest.createPageRequest(currentpagerequest.getRootName(), variant, preqprops);
        }
        
        SPDocument  spdoc;
        PageRequest prevpage = currentpagerequest;
        PageFlow    prevflow = currentpageflow;

        if (in_adminmode) {
            ResultDocument resdoc;
            if (checkIsAccessible(admin_pagereq, PageRequestStatus.UNDEF)) {
                currentpagerequest = admin_pagereq;
                resdoc             = documentFromCurrentStep();
                currentpagerequest = prevpage;
            } else {
                throw new XMLException("*** admin mode requested but admin page " + admin_pagereq + " is inaccessible ***");
            }
            spdoc = resdoc.getSPDocument();
            spdoc.setPagename(admin_pagereq.getName());
            insertPageMessages(spdoc);
            storeCookies(spdoc);
            processIC(endIC);
            return spdoc;
        }

        trySettingPageRequestAndFlow();
        spdoc = documentFromFlow();

        if (spdoc != null && spdoc.getPagename() == null) {
            spdoc.setPagename(currentpagerequest.getRootName());
        }

        if (spdoc != null && currentpageflow != null) {
            spdoc.setProperty("pageflow", currentpageflow.getRootName());
            addPageFlowInfo(currentpageflow, spdoc);
        }

        if (spdoc != null && variant != null) {
            spdoc.setVariant(variant);
            spdoc.getDocument().getDocumentElement().setAttribute("requested-variant", variant.getVariantId());
            if (currentpagerequest != null) 
                spdoc.getDocument().getDocumentElement().setAttribute("used-pr", currentpagerequest.getName());
            if (currentpageflow != null)
                spdoc.getDocument().getDocumentElement().setAttribute("used-pf", currentpageflow.getName());
        }
        
        if (spdoc.getResponseError() != 0) {
            currentpagerequest = prevpage;
            currentpageflow    = prevflow;
            insertPageMessages(spdoc);
            storeCookies(spdoc);
            processIC(endIC);
            return spdoc;
        }

        if (navigation != null && spdoc != null) {
            visited_pages.add(spdoc.getPagename());
            addNavigation(navigation, spdoc);
        }

        if (pageIsSidestepPage(currentpagerequest)) {
            LOG.info("* [" + currentpagerequest + "] is sidestep: Restoring to [" +
                     prevpage + "] in flow [" + prevflow.getName() + "]");
            currentpagerequest = prevpage;
            currentpageflow    = prevflow;
            spdoc.setNostore(true);
        }

        LOG.debug("\n");
        insertPageMessages(spdoc);
        storeCookies(spdoc);
        processIC(endIC);
        return spdoc;
    }

    private void createInterceptors(Properties props) throws Exception {
        TreeMap sic = PropertiesUtils.selectPropertiesSorted(props, STARTIC);
        TreeMap eic = PropertiesUtils.selectPropertiesSorted(props, ENDIC);
        
        if (sic != null && sic.size() > 0) {
            ArrayList list = new ArrayList();
            for (Iterator i = sic.keySet().iterator(); i.hasNext();) {
                list.add(ContextInterceptorFactory.getInstance().getInterceptor((String) sic.get((String) i.next())));
            }
            startIC = (ContextInterceptor[]) list.toArray(new ContextInterceptor[]{});
        } else {
            startIC = null;
        }
        
        if (eic != null && eic.size() > 0) {
            ArrayList list = new ArrayList();
            for (Iterator i = eic.keySet().iterator(); i.hasNext();) {
                list.add(ContextInterceptorFactory.getInstance().getInterceptor((String) eic.get((String) i.next())));
            }
            endIC = (ContextInterceptor[]) list.toArray(new ContextInterceptor[]{});
        } else {
            endIC = null;
        }
    }
    
    private void processIC(ContextInterceptor[] icarr) {
        if (icarr != null) {
            for (int i = 0; i < icarr.length; i++) {
                icarr[i].process(this, currentpservreq);
            }
        }
    }
    
    /**
     * <code>getContextResourceManager</code> returns the ContextResourceManager defined in init(Properties properties).
     *
     * @return a <code>ContextResourceManager</code> value
     */
    public ContextResourceManager getContextResourceManager() {
        return rmanager;
    }

    /**
     * <code>getProperties</code> returns the Properties supplied in init(Properties properties).
     *
     * @return a <code>Properties</code> value
     */
    public Properties getProperties() {
        return properties;
    }

    /**
     * Describe <code>getPropertiesForCurrentPageRequest</code> method here.
     * This returnes the all Properties which match the current PageRequest <b>without</b> the common prefix.
     * E.g. the page is named "foo", then all properties starting with "pagerequest.foo.*" will be returned after
     * stripping the "pagerequest.foo." prefix.
     *
     * @return a <code>Properties</code> value
     */
    public Properties getPropertiesForCurrentPageRequest() {
        return preqprops.getPropertiesForPageRequest(currentpagerequest);
    }

    /**
     * <code>getCurrentPageRequest</code> gets the currently active PageRequest.
     *
     * @return a <code>PageRequest</code> value
     */
    public PageRequest getCurrentPageRequest() {
        return currentpagerequest;
    }

    /**
     * <code>getCurrentPageFlow</code> gets the currently active PageRequest.
     *
     * @return a <code>PageFlow</code> value
     */
    public PageFlow getCurrentPageFlow() {
        return currentpageflow;
    }

    public void setPageFlow(String flowname) {
        PageFlow tmp = pageflowmanager.getPageFlowByName(flowname, variant);
        if (tmp != null) {
            LOG.debug("===> Setting currentpageflow to user-requested flow " + flowname);
            currentpageflow            = tmp;
            pageflow_requested_by_user = true;
        } else {
            LOG.warn("*** Trying to set currentpageflow to " + flowname + ", but it's not defined ***");
        }
    }
    
    public void setJumpToPageRequest(String pagename) {
        PageRequest page = PageRequest.createPageRequest(pagename, variant, preqprops);
        if (pagemap.getState(page) != null) {
            jumptopagerequest = page;
        } else {
            LOG.warn("*** Trying to set jumppage " + pagename + ", but it's not defined ***");
            jumptopagerequest = null;
        }
    }

    public void setJumpToPageFlow(String flowname) {
        if (jumptopagerequest != null) {
            PageFlow tmp = pageflowmanager.getPageFlowByName(flowname, variant);
            if (tmp != null) {
                jumptopageflow = tmp;
            } else {
                jumptopageflow = pageflowmanager.pageFlowToPageRequest(currentpageflow, jumptopagerequest, variant);
            }
        } else {
            jumptopageflow = null;
        }
    }

    public void forceStopAtNextStep(boolean force) {
        stopnextforcurrentrequest = force;
    }

    public void prohibitContinue() {
        prohibitcontinue = true;
    }

    public void invalidateNavigation() {
        navigation_visible = new HashMap();
    }


    public Cookie[] getRequestCookies() {
        return currentpservreq.getCookies();
    }

    public void setLanguage(String lang) {
        currentpservreq.getSession(false).setAttribute(AbstractXMLServer.SESS_LANG, lang);        
    }

    public void addCookie(Cookie cookie) {
        cookielist.add(cookie);
    }

    public Variant getVariant() {
        return variant;
    }

    public void setVariant(Variant var) {
        if (restoreVariantOnNextRequest) {
            variantToRestoreOnNextRequest = var;
        } else {
            variant = var;
        }
    }

    public void setVariantForThisRequestOnly(Variant var) {
        // Note: it only makes sense to call this method once during a request, if you insist on
        // calling it more than once, the variant that is scheduled to be restored the next time
        // will always be the first variant to be stored.
        if (!restoreVariantOnNextRequest) {
            variantToRestoreOnNextRequest = variant;
            restoreVariantOnNextRequest = true;
        }
        variant = var;
    }

    /**
     * <code>getCurrentVisitId</code> returns the visit_id.
     *
     * @return <code>String</code> visit_id
     */
    public String getVisitId() throws RuntimeException {
        if ( visit_id != null) {
            return visit_id;
        } else {
            throw new RuntimeException("visit_id not set, but asked for!!!!");
        }
    }

    public boolean flowBeforeNeedsData() throws Exception {
        if (!currentpageflow.containsPage(currentpagerequest.getRootName())) {
            throw new RuntimeException("*** current pageflow " + currentpageflow.getName() +
                                       " does not contain current pagerequest " + currentpagerequest);
        }

        PageRequest current  = currentpagerequest;
        FlowStep[]  workflow = currentpageflow.getAllSteps();

        for (int i = 0; i < workflow.length; i++) {
            FlowStep    step     = workflow[i];
            String      pagename = step.getPageName();
            PageRequest page     = PageRequest.createPageRequest(pagename, variant, preqprops);
            if (pagename.equals(current.getRootName())) {
                return false;
            }
            if (checkIsAccessible(page, current.getStatus()) && checkNeedsData(page, current.getStatus())) {
                return true;
            }
        }
        return false;
    }

    /**
     * <code>finalPageIsRunning</code> can be called from inside a {@link de.schlund.pfixcore.workflow.State State}
     * It returnes true if the Context is currently running a FINAL page of a defined workflow.
     *
     * @return a <code>boolean</code> value
     */
    public boolean finalPageIsRunning() {
        return (currentpagerequest.getStatus() == PageRequestStatus.FINAL);
    }

    /**
     * <code>jumpToPageIsRunning</code> can be called from inside a {@link de.schlund.pfixcore.workflow.State State}
     * It returnes true if the pagerequest has already been jumped to internally after a successful submit.
     *
     * @return a <code>boolean</code> value
     */
    public boolean jumpToPageIsRunning() {
        return on_jumptopage;
    }

    /**
     * <code>flowIsRunning</code> can be called from inside a {@link de.schlund.pfixcore.workflow.State State}
     * It returned true if the Context is currently running one of the defined pageflows.
     *
     * @return a <code>boolean</code> value
     */
    public boolean flowIsRunning() {
        if (currentpagerequest.getStatus() == PageRequestStatus.WORKFLOW) {
            return true;
        } else {
            return false;
        }
    }

    public boolean currentFlowStepWantsPostProcess() {
        if (currentpageflow != null && currentpageflow.containsPage(currentpagerequest.getRootName())) {
            if (currentpageflow.getFlowStepForPage(currentpagerequest.getRootName()).hasOnContinueAction()) {
                return true;
            }
        }
        return false;
    }

    public boolean currentPageNeedsSSL(PfixServletRequest preq) throws Exception {
        PageRequest page = PageRequest.createPageRequest(preq, variant, preqprops);
        if (page == null && currentpagerequest != null) {
            page = currentpagerequest;
        }
        if (page != null) {
            Properties props = preqprops.getPropertiesForPageRequest(page);
            if (props != null) {
                String needssl = props.getProperty(PROP_NEEDS_SSL);
                if (needssl != null && needssl.equals("true")) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isCurrentPageRequestInCurrentFlow() {
        return isPageRequestInFlow(currentpagerequest, currentpageflow);
    }

    public boolean isCurrentPageFlowRequestedByUser() {
        return pageflow_requested_by_user;
    }

    public boolean isJumptToPageSet() {
        return jumptopagerequest != null;
    }
    
    public boolean isJumptToPageFlowSet() {
        return jumptopageflow != null;
    }
    
    public boolean isProhibitContinueSet() {
        return prohibitcontinue;
    }
    
    public boolean isForceStopAtNextStepSet() {
        return stopnextforcurrentrequest;
    }


    public synchronized SPDocument checkAuthorization(boolean forceauth) throws Exception {
        if (authpage != null) {
            ResultDocument resdoc = null;
            LOG.debug("===> [" + authpage + "]: Checking authorisation");
            if (!checkIsAccessible(authpage, PageRequestStatus.AUTH)) {
                throw new XMLException("*** Authorisation page [" + authpage + "] is not accessible! ***");
            }
            if (forceauth) {
                LOG.debug("* [" + currentpagerequest + "] forceauth is TRUE ***");
            }
            if (checkNeedsData(authpage, PageRequestStatus.AUTH) || forceauth) {
                LOG.debug("===> [" + authpage + "]: Need authorisation data");
                PageRequest saved  = currentpagerequest;
                currentpagerequest = authpage;
                resdoc             = documentFromCurrentStep();
                currentpagerequest = saved;
                if (!prohibitcontinue) {
                    LOG.debug("===> [" + authpage + "]: Authorisation granted");
                } else {
                    LOG.debug("===> [" + authpage + "]: Authorisation failed");
                }
            } else {
                LOG.debug("===> [" + authpage + "]: Already authorised");
            }
            if (resdoc != null && prohibitcontinue) {
                // getting a document here means we need to show the authpage
                if (resdoc.getSPDocument() == null) {
                    throw new XMLException("*** FATAL: " + authpage + " returns a 'null' SPDocument! ***");
                }
                resdoc.getSPDocument().setPagename(authpage.getName());
                return resdoc.getSPDocument();
            }
        }
        return null;
    }

    private void do_update() throws Exception {
    	// get PropertyObjects from PropertyObjectManager
    	PropertyObjectManager pom = PropertyObjectManager.getInstance();

        pageflowmanager = (PageFlowManager) pom.getPropertyObject(properties,"de.schlund.pfixcore.workflow.PageFlowManager");
        preqprops       = (PageRequestProperties) pom.getPropertyObject(properties,"de.schlund.pfixcore.workflow.PageRequestProperties");
        pagemap         = (PageMap) pom.getPropertyObject(properties,"de.schlund.pfixcore.workflow.PageMap");

        // The navigation is possibly shared across more than one
        // context, i.e. more than one properties object.  So we can't
        // let it be handled by the PropertyObjectManager.
        if (properties.getProperty(NAVPROP) != null) {
            navigation = NavigationFactory.getInstance().getNavigation(properties.getProperty(NAVPROP));
        }

        currentpageflow    = pageflowmanager.getPageFlowByName(properties.getProperty(DEFPROP), variant);
        currentpagerequest = PageRequest.createPageRequest(currentpageflow.getFirstStep().getPageName(), variant, preqprops);

        createInterceptors(properties);
        
        checkForAuthenticationMode();
        checkForAdminMode();
        checkForNavigationReuse();

        needs_update = false;
    }

    private boolean isPageRequestInFlow(PageRequest page, PageFlow pageflow) {
        return (pageflow != null && pageflow.containsPage(page.getRootName()));
    }

    private boolean pageIsSidestepPage(PageRequest page) {
        Properties props  = preqprops.getPropertiesForPageRequest(page);
        if (props != null) {
            String nostore = props.getProperty(NOSTORE);
            if (nostore != null && nostore.toLowerCase().equals("true")) {
                // LOG.debug("*** Found sidestep page: " + page);
                return true;
            }
        } else {
            LOG.error("*** Got NULL properties for page " + page);
        }
        return false;
    }

    private void checkForAuthenticationMode() {
        String authpagename = properties.getProperty(AUTH_PROP);
        if (authpagename != null) {
            authpage = PageRequest.createPageRequest(authpagename, variant, preqprops);
        } else {
            authpage = null;
        }
    }

    private void checkForNavigationReuse() {
        String navi_autoinv = properties.getProperty(PROP_NAVI_AUTOINV);
        if (navi_autoinv != null && navi_autoinv.equals("false")) {
            autoinvalidate_navi = false;
            LOG.info("CAUTION: Setting autoinvalidate of navigation to FALSE!");
            LOG.info("CAUTION: You need to call context.invalidateNavigation() to update the navigation.");
        } else {
            autoinvalidate_navi = true;
        }
    }

    private void checkForAdminMode() {
        admin_pagereq = null;
        in_adminmode  = false;

        String watchprop = properties.getProperty(WATCHMODE);
        if (watchprop != null && !watchprop.equals("")) {
            String adminprop = properties.getProperty(ADMINMODE + "." + watchprop + ".status");
            String adminpage = properties.getProperty(ADMINPAGE);
            if (adminpage != null && !adminpage.equals("") && adminprop != null && adminprop.equals("on")) {
                LOG.debug("*** setting Adminmode for : " + watchprop + " ***");
                admin_pagereq = PageRequest.createPageRequest(adminpage, variant, preqprops);
                in_adminmode  = true;
            }
        }
    }

    private boolean checkNeedsData(PageRequest page, PageRequestStatus status) throws Exception {
        PageRequest saved  = currentpagerequest;
        currentpagerequest = page;
        State       state  = pagemap.getState(page);
        if (state == null) {
            throw new XMLException ("*** Can't get a state to check needsData() for page " + page.getName() + " ***");
        }
        page.setStatus(status);
      
        
        PerfEvent pe = new PerfEvent(PerfEventType.PAGE_NEEDSDATA, page.getName());
        pe.start();
        boolean retval     = state.needsData(this, currentpservreq);
        pe.save();
       
        currentpagerequest = saved;
        return retval;
    }

    private boolean checkIsAccessible(PageRequest page, PageRequestStatus status) throws Exception {
        PageRequest saved = currentpagerequest;
        currentpagerequest = page;
        State state = pagemap.getState(page);
        if (state == null) {
            throw new XMLException ("* Can't get a state to check isAccessible() for page " + page.getName());
        }
        page.setStatus(status);
        
        PerfEvent pe = new PerfEvent(PerfEventType.PAGE_ISACCESSIBLE, page.getName());
        pe.start();
        boolean retval = state.isAccessible(this, currentpservreq);
        pe.save();
        
        currentpagerequest = saved;
        return retval;
    }

    private SPDocument documentFromFlow() throws Exception {
        SPDocument     document = null;

        // First, check if the requested page is defined at all
        // We do this only if the current pagerequest is not the special STARTWITHFLOW_PAGE
        // because then we don't know yet which page to use.

        if (!startwithflow) {

            PageRequest[]  workflow;
            ResultDocument resdoc;

            State state = pagemap.getState(currentpagerequest);
            if (state == null) {
                LOG.warn("* Can't get a handling state for page " + currentpagerequest);
                resdoc = new ResultDocument();
                document = resdoc.getSPDocument();
                document.setResponseError(HttpServletResponse.SC_NOT_FOUND);
                return document;
            }

            // Now, check for possibly needed authorization
            RequestParam sdreq     = currentpservreq.getRequestParam(State.SENDAUTHDATA);
            boolean      forceauth = (sdreq != null && sdreq.isTrue());

            document = checkAuthorization(forceauth);
            if (document != null) {
                return document;
            }

            // Now we need to make sure that the current page is accessible, and take the right measures if not.
            if (!checkIsAccessible(currentpagerequest, PageRequestStatus.DIRECT)) {
                LOG.warn("[" + currentpagerequest + "]: not accessible! Trying first page of default flow.");
                currentpageflow     = pageflowmanager.getPageFlowByName(properties.getProperty(DEFPROP), variant);
                PageRequest defpage = PageRequest.createPageRequest(currentpageflow.getFirstStep().getPageName(), variant, preqprops);
                currentpagerequest  = defpage;
                if (!checkIsAccessible(defpage, PageRequestStatus.DIRECT)) {
                    throw new XMLException("Even first page [" + defpage + "] of default flow was not accessible! Bailing out.");
                }
            }

            resdoc = documentFromCurrentStep();
            if ( // !prohibitcontinue &&
                currentpageflow != null && currentpageflow.containsPage(currentpagerequest.getRootName())) {
                FlowStep step = currentpageflow.getFlowStepForPage(currentpagerequest.getRootName());
                step.applyActionsOnContinue(this, resdoc);
            }

            if (prohibitcontinue) {
                LOG.debug("* [" + currentpagerequest + "] returned document to show, skipping page flow.");
                document = resdoc.getSPDocument();
            } else if (jumptopagerequest != null) {
                LOG.debug("* [" + currentpagerequest + "] signalled success, jumptopage is set as [" + jumptopagerequest + "].");
                currentpagerequest = jumptopagerequest;
                if (jumptopageflow != null) {
                    currentpageflow = jumptopageflow;
                }
                jumptopagerequest = null; // we don't want to recurse infinitely
                jumptopageflow    = null; // we don't want to recurse infinitely
                on_jumptopage     = true; // we need this information to supress the interpretation of
                                          // the request as one that submits data. See StateImpl,
                                          // methods isSubmitTrigger & isDirectTrigger

                LOG.debug("******* JUMPING to [" + currentpagerequest + "] *******\n");
                document = documentFromFlow();
            } else if (currentpageflow != null) {
                LOG.debug("* [" + currentpagerequest + "] signalled success, starting page flow process");
                document = runPageFlow(false);
            } else {
                throw new XMLException("*** ERROR! *** [" + currentpagerequest + "] signalled success, but current page flow == null!");
            }
        } else {
            LOG.debug("* Page is determined from flow [" + currentpageflow + "], starting page flow process");
            LOG.debug("* Current page: [" + currentpagerequest + "]");
            document = runPageFlow(true);
        }
        return document;
    }


    private SPDocument runPageFlow(boolean startwithflow) throws Exception {
        ResultDocument resdoc   = null;
        // We need to re-check the authorisation because the just handled submit could have changed the authorisation status.
        SPDocument     document = checkAuthorization(false);
        if (document != null) {
            return document;
        }
        FlowStep[]  workflow      = currentpageflow.getAllSteps();
        PageRequest saved         = currentpagerequest;
        boolean     after_current = false;

        for (int i = 0; i < workflow.length; i++) {
            FlowStep    step = workflow[i];
            PageRequest page = PageRequest.createPageRequest(step.getPageName(), variant, preqprops);
            if (page.equals(saved)) {
                if (startwithflow && checkIsAccessible(page, PageRequestStatus.WORKFLOW)) {
                    LOG.debug("=> [" + page + "]: STARTWITHFLOW: Pageflow must stop here at last.");
                    currentpagerequest = page;
                    currentpagerequest.setStatus(PageRequestStatus.WORKFLOW);
                    document = documentFromCurrentStep().getSPDocument();
                    if (document == null) {
                        throw new XMLException("*** FATAL: [" + page + "] returns a 'null' SPDocument! ***");
                    }
                    LOG.debug("* [" + page + "] returned document => show it.");
                    break;
                } else {
                    LOG.debug("* Skipping step [" + page + "] in page flow (been there already...)");
                    after_current = true;
                }
            } else if (!checkIsAccessible(page, PageRequestStatus.WORKFLOW)) {
                LOG.debug("* Skipping step [" + page + "] in page flow (state is not accessible...)");
                // break;
            } else {
                LOG.debug("* Page flow is at step " + i + ": [" + page + "]");
                boolean needsdata;
                if (after_current && (step.wantsToStopHere() || isForceStopAtNextStepSet())) {
                    if (isForceStopAtNextStepSet()) LOG.debug("=> Request specifies to act like stophere='true'");
                    LOG.debug("=> [" + page + "]: Page flow wants to stop, getting document now.");
                    currentpagerequest = page;
                    currentpagerequest.setStatus(PageRequestStatus.WORKFLOW);
                    resdoc             = documentFromCurrentStep();
                    document           = resdoc.getSPDocument();
                    if (document == null) {
                        throw new XMLException("*** FATAL: [" + page + "] returns a 'null' SPDocument! ***");
                    }
                    LOG.debug("* [" + page + "] returned document => show it.");
                    break;
                } else if (checkNeedsData(page, PageRequestStatus.WORKFLOW)) {
                    LOG.debug("=> [" + page + "]: needsData() returned TRUE, leaving page flow and getting document now.");
                    currentpagerequest = page;
                    currentpagerequest.setStatus(PageRequestStatus.WORKFLOW);
                    resdoc             = documentFromCurrentStep();
                    document           = resdoc.getSPDocument();
                    if (document == null) {
                        throw new XMLException("*** FATAL: [" + page + "] returns a 'null' SPDocument! ***");
                    }
                    LOG.debug("* [" + page + "] returned document => show it.");
                    break;
                } else {
                    LOG.debug("=> [" + page + "]: Page flow doesn't want to stop and needsData() returned FALSE");
                    LOG.debug("=> [" + page + "]: going to next step in page flow.");
                }
            }
        }
        if (document == null) {
            PageRequest finalpage = null;
            if (startwithflow) {
                finalpage = saved;
                LOG.debug("=> STARTWITHFLOW is active, using original target page [" + saved + "] as final page");
            } else if (currentpageflow.getFinalPage() != null) {
                finalpage = PageRequest.createPageRequest(currentpageflow.getFinalPage(), variant, preqprops);
                LOG.debug("=> Pageflow [" + currentpageflow + "] defines page [" + finalpage + "] as final page");
            }
            if (finalpage == null) {
                throw new XMLException("*** Reached end of page flow '" + currentpageflow.getName() + "' " +
                                       "with neither getting a non-null SPDocument or having a FINAL page defined ***");
            } else if (!checkIsAccessible(finalpage, PageRequestStatus.FINAL)) {
                throw new XMLException("*** Reached end of page flow '" + currentpageflow.getName() + "' " +
                                       "but FINAL page [" + finalpage + "] is inaccessible ***");
            } else {
                currentpagerequest = finalpage;
                currentpageflow    = pageflowmanager.pageFlowToPageRequest(currentpageflow, finalpage, variant);
                finalpage.setStatus(PageRequestStatus.FINAL);
                resdoc             = documentFromCurrentStep();
                document           = resdoc.getSPDocument();
                if (document == null) {
                    throw new XMLException("*** FATAL: " + finalpage + " returns a 'null' SPDocument! ***");
                }
            }
        }
        return document;
    }

    /**
     * <code>documentFromCurrentStep</code> handles how to get a SPDocument from the state
     * that is associated (via the properties) to the current PageRequest.
     *
     * @param req a <code>HttpServletRequest</code> value
     * @param skip_on_inaccessible a <code>boolean</code> value
     * @return a <code>SPDocument</code> value
     * @exception Exception if an error occurs
     */
    private ResultDocument documentFromCurrentStep() throws Exception {
        State state = pagemap.getState(currentpagerequest);
        if (state == null) {
            throw new XMLException ("* Can't get a state in documentFromCurrentStep() for page " +
                                    currentpagerequest.getName());
        }

        LOG.debug("** [" + currentpagerequest + "]: associated state: " + state.getClass().getName());
        LOG.debug("=> [" + currentpagerequest + "]: Calling getDocument()");
        return state.getDocument(this, currentpservreq);
    }

    private void trySettingPageRequestAndFlow() {
        PageRequest tmp = PageRequest.createPageRequest(currentpservreq, variant, preqprops);
        if (tmp != null && (authpage == null || !tmp.equals(authpage))) {
            currentpagerequest = tmp;
            currentpagerequest.setStatus(PageRequestStatus.DIRECT);

            PageFlow     flow     = null;
            RequestParam flowname = currentpservreq.getRequestParam(PARAM_FLOW);
            if (flowname != null && !flowname.getValue().equals("")) {
                LOG.debug("===> User requesting to switch to flow '" + flowname.getValue() + "'");
                flow = pageflowmanager.getPageFlowByName(flowname.getValue(), variant);
                if (flow != null) {
                    LOG.debug("===> Flow '" + flowname.getValue() + "' exists...");
                    pageflow_requested_by_user = true;
                    if (flow.containsPage(currentpagerequest.getRootName())) {
                    LOG.debug("===> and it contains page '" + currentpagerequest.getName() + "'");
                    } else {
                        LOG.debug("===> CAUTION: it doesn't contain page '" +
                                  currentpagerequest.getName() + "'! Make sure this is what you want...");
                    }
                } else {
                    LOG.error("\n\n!!!! CAUTION !!!! Flow '" + flowname +
                              "' is not defined! I'll continue as if no flow was given\n\n");
                    flow = pageflowmanager.pageFlowToPageRequest(currentpageflow, currentpagerequest, variant);
                    pageflow_requested_by_user = false;
                }
            } else {
                flow = pageflowmanager.pageFlowToPageRequest(currentpageflow, currentpagerequest, variant);
                pageflow_requested_by_user = false;
            }
            currentpageflow = flow;
            LOG.debug("* Setting currentpagerequest to [" + currentpagerequest.getName() + "]");
            LOG.debug("* Setting currentpageflow to [" + currentpageflow.getName() + "]");
        } else {
            if (currentpagerequest != null) {
                currentpagerequest = PageRequest.createPageRequest(currentpagerequest.getRootName(), variant, preqprops);
                currentpagerequest.setStatus(PageRequestStatus.DIRECT);
                LOG.debug("* Reusing page [" + currentpagerequest + "]");
                LOG.debug("* Reusing flow [" + currentpageflow.getName() + "]");
            } else {
                throw new RuntimeException("Don't have a current page to use as output target");
            }
        }
        RequestParam jump = currentpservreq.getRequestParam(JUMPPAGE);
        if (jump != null && !jump.getValue().equals("")) {
            setJumpToPageRequest(jump.getValue());
            // We only search for a special jumpflow when also a jumppage is set
            RequestParam jumpflow = currentpservreq.getRequestParam(JUMPPAGEFLOW);
            if (jumpflow != null && !jumpflow.getValue().equals("")) {
                setJumpToPageFlow(jumpflow.getValue());
            }
        }
    }

    private void addPageFlowInfo(PageFlow flow, SPDocument spdoc) throws Exception {
        Document   doc   = spdoc.getDocument();
        Element    root  = doc.createElement("pageflow");
        doc.getDocumentElement().appendChild(root);
        root.setAttribute("name", flow.getRootName());
        FlowStep[] steps = flow.getAllSteps();
        for (int i = 0; i < steps.length; i++) {
            String  step     = steps[i].getPageName();
            Element stepelem = doc.createElement("step");
            root.appendChild(stepelem);
            stepelem.setAttribute("name", step);
        }
    }
    
    private void storeCookies(SPDocument spdoc) {
        for (Iterator i = cookielist.iterator(); i.hasNext();) {
            spdoc.addCookie((Cookie) i.next());
        }
    }

    private void addNavigation(Navigation navi, SPDocument spdoc) throws Exception {
        long     start   = System.currentTimeMillis();
        Document doc     = spdoc.getDocument();
        Element  element = doc.createElement("navigation");
        doc.getDocumentElement().appendChild(element);

        StringBuffer debug_buffer = new StringBuffer();
        StringBuffer warn_buffer  = new StringBuffer();

        if (autoinvalidate_navi) {
            LOG.debug("=> Add new navigation.");
           
            PerfEvent pe = new PerfEvent(PerfEventType.CONTEXT_CREATENAVICOMPLETE, spdoc.getPagename());
            pe.start();
            recursePages(navi.getNavigationElements(), element, doc, null, warn_buffer, debug_buffer);
            pe.save();
        } else {
            if (navigation_visible != null) {
                LOG.debug("=> Reuse old navigation.");
            } else {
                LOG.debug("=> Add new navigation (has been invalidated).");
            }
           
            PerfEvent pe = new PerfEvent(PerfEventType.CONTEXT_CREATENAVIREUSE, spdoc.getPagename());
            pe.start();
            recursePages(navi.getNavigationElements(), element, doc, navigation_visible, warn_buffer, debug_buffer);
            pe.save();
        }
    }

    private void recursePages(NavigationElement[] pages, Element parent,  Document doc,
                              HashMap vis_map, StringBuffer warn_buffer, StringBuffer debug_buffer) throws Exception {
        for (int i = 0; i < pages.length; i++) {
            NavigationElement page     = pages[i];
            String            pagename = page.getName();
            PageRequest       pagereq  = PageRequest.createPageRequest(pagename, variant, preqprops);
            Element           pageelem = doc.createElement("page");

            parent.appendChild(pageelem);
            pageelem.setAttribute("name", pagename);
            pageelem.setAttribute("handler", page.getHandler());

            Integer page_vis = null;
            if (vis_map != null) {
                page_vis = (Integer) vis_map.get(page);
            }

            if (page_vis != null) {
                int visible = page_vis.intValue();
                pageelem.setAttribute("visible", "" + visible);
                if (visible == -1) {
                    pageelem.setAttribute("visited", "-1");
                } else {
                    if (visited_pages.contains(pagename)) {
                        pageelem.setAttribute("visited", "1");
                    } else {
                        pageelem.setAttribute("visited", "0");
                    }
                }
            } else {
                if (preqprops.pageRequestIsDefined(pagereq)) {
                    if (checkIsAccessible(pagereq,PageRequestStatus.NAVIGATION)) {
                        pageelem.setAttribute("visible", "1");
                        if (vis_map != null) {
                            vis_map.put(page, new Integer(1));
                        }
                    } else {
                        pageelem.setAttribute("visible", "0");
                        if (vis_map != null) {
                            vis_map.put(page, new Integer(0));
                        }
                    }
                    if (visited_pages.contains(pagename)) {
                        pageelem.setAttribute("visited", "1");
                    } else {
                        pageelem.setAttribute("visited", "0");
                    }
                } else {
                    pageelem.setAttribute("visible", "-1");
                    pageelem.setAttribute("visited", "-1");
                    if (vis_map != null) {
                        vis_map.put(page, new Integer(-1));
                    }
                }
            }

            if (page.hasChildren()) {
                recursePages(page.getChildren(), pageelem, doc, vis_map, warn_buffer, debug_buffer);
            }
        }
    }

    private void insertPageMessages(SPDocument spdoc) {
        if (spdoc == null)
            return;
        
        LOG.debug("Adding " + messages.size() + " PageMessages to result document");
        
        if (!messages.isEmpty()) {
            Document doc        = spdoc.getDocument();
            Element  formresult = doc.getDocumentElement();

            if (formresult != null) {
                Element messagesElem = doc.createElement("pagemessages");
                formresult.appendChild(messagesElem);

                Iterator iter = messages.iterator();
                while (iter.hasNext()) {
                    StatusCodeInfo sci = (StatusCodeInfo) iter.next();
                    Element        msg = doc.createElement("message");
                    Element        inc = ResultDocument.createIncludeFromStatusCode(doc, properties, sci.getStatusCode(), sci.getArgs());
                    msg.appendChild(inc);
                    if (sci.getLevel() != null) {
                        msg.setAttribute("level", sci.getLevel());
                    }
                    messagesElem.appendChild(msg);
                    
                    LOG.debug("Added PageMessage for level " + sci.getLevel() + " with args " + sci.getArgs());
                }
            }
            messages.clear();
        }
    }


    /**
     * <code>toString</code> tries to give a detailed printed representation of the Context.
     * WARNING: this may be very long!
     *
     * @return a <code>String</code> value
     */
    public String toString() {
        StringBuffer contextbuf = new StringBuffer("\n");

        contextbuf.append("     pageflow:      " + currentpageflow  + "\n");
        contextbuf.append("     PageRequest:   " + currentpagerequest + "\n");
        if (currentpagerequest != null) {
            contextbuf.append("       -> State: " + pagemap.getState(currentpagerequest) + "\n");
            contextbuf.append("       -> Status: " + currentpagerequest.getStatus() + "\n");
        }
        contextbuf.append("     >>>> ContextResourcen <<<<\n");
        for (Iterator i = rmanager.getResourceIterator(); i.hasNext(); ) {
            ContextResource res = (ContextResource) i.next();
            contextbuf.append("         " + res.getClass().getName() + ": ");
            contextbuf.append(res.toString() + "\n");
        }

        return contextbuf.toString();
    }

    public String getName() {
        return name;
    }

  

    /**
     * <b>NOTE: </b> This should be used only inside the {@link #handleRequest()}-method
     * as it accesses a non-thread-safe field of this class.
     *
     * @return the last exception-object that was stored in the request object.
     * See {@link PfixServletRequest#getLastException() PfixServletRequest} for details.
     */
    public Throwable getLastException() {
        return currentpservreq.getLastException();
    }

    public void addPageMessage(StatusCode scode) {
        addPageMessage(scode, null, null);
    }

    public void addPageMessage(StatusCode scode, String level) {
        addPageMessage(scode, null, level);
    }

    public void addPageMessage(StatusCode scode, String[] args) {
        addPageMessage(scode, args, null);
    }

    /**
     * <b>NOTE: </b> This should be used only inside the {@link #handleRequest()}-method
     * as it accesses a non-thread-safe field of this class.
     * <br />
     * Adds the <code>StatusCode</code>, along with the provided arguments,
     * to the list of <code>StatusCodes</code>, that get
     * inserted into the requests result-tree.
     *
     * @param scode an instance of <code>StatusCode</code>, that should be added
     * to the collection of message codes, for this request.
     * @param args arguments to the provided <code>StatusCode</code>.
     * @param level the value, that's used to this message's level. If this value
     * is <code>null</code> or an empty String, the value of
     * {@link #DEF_MESSAGE_LEVEL DEF_MESSAGE_LEVEL} is used
     */
    public void addPageMessage(StatusCode scode, String[] args, String level) {
        if (scode == null)
            return;
        messages.add(new StatusCodeInfo(scode, args, level));
    }

    
}
