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

import de.schlund.pfixcore.workflow.Navigation.NavigationElement;
import de.schlund.pfixxml.*;
import java.util.*;
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
 *
 * @author jtl
 * @version 2.0
 *
 *
 */
public class Context implements AppContext {
    private final static Category LOG                 = Category.getInstance(Context.class.getName());
    private final static String   NOSTORE             = "nostore";
    private final static String   DEFPROP             = "context.defaultpageflow";
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
    private final static String   PARAM_STARTWITHFLOW = "__startwithflow";

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

    // values read from properties
    private boolean     autoinvalidate_navi = true;
    private boolean     in_adminmode        = false;
    private PageRequest admin_pagereq;

    // the request state
    private PfixServletRequest currentpreq;
    private PageRequest        currentpagerequest;
    private PageFlow           currentpageflow;
    private PageRequest        jumptopagerequest;
    private PageFlow           jumptopageflow;
    private boolean            on_jumptopage;
    private boolean            pageflow_requested_by_user;
    private boolean            startwithflow;
    
    private HashMap navigation_visible = null;
    private String  visit_id           = null;
    private boolean needs_update;


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
        currentpreq                = preq;
        jumptopagerequest          = null;
        jumptopageflow             = null;
        on_jumptopage              = false;
        pageflow_requested_by_user = false;
        startwithflow              = false;
        
        if (needs_update) {
            do_update();
        }

        RequestParam swflow = currentpreq.getRequestParam(PARAM_STARTWITHFLOW);
        if (swflow != null && swflow.getValue().equals("true")) {
            startwithflow = true;
        }

        SPDocument  spdoc;
        PageRequest prevpage = currentpagerequest;
        PageFlow    prevflow = currentpageflow;

        if (visit_id == null)
            visit_id = (String) currentpreq.getSession(false).getAttribute(ServletManager.VISIT_ID);

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
            return spdoc;
        }

        trySettingPageRequestAndFlow();
        spdoc = documentFromFlow();

        if (spdoc != null && spdoc.getPagename() == null) {
            spdoc.setPagename(currentpagerequest.getName());
        }

        if (spdoc != null && currentpageflow != null) {
            spdoc.setProperty("pageflow", currentpageflow.getName());
        }

        if (spdoc.getResponseError() != 0) {
            currentpagerequest = prevpage;
            currentpageflow    = prevflow;
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
        return spdoc;
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

    public void setJumpToPageRequest(String pagename) {
        PageRequest page = new PageRequest(pagename);
        if (pagemap.getState(page) != null) {
            jumptopagerequest = page;
        } else {
            LOG.warn("*** Trying to set jumppage " + pagename + ", but it's not defined ***");
            jumptopagerequest = null;
        }
    }

    public PageRequest getJumpToPageRequest() {
        return jumptopagerequest;
    }

    public void setJumpToPageFlow(String flowname) {
        if (jumptopagerequest != null) {
            PageFlow tmp = pageflowmanager.getPageFlowByName(flowname);
            if (tmp != null) {
                jumptopageflow = tmp;
            } else {
                jumptopageflow = pageflowmanager.pageFlowToPageRequest(currentpageflow, jumptopagerequest);
            }
        } else {
            jumptopageflow = null;
        }
    }

    public PageFlow getJumpToPageFlow() {
        return jumptopageflow;
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

    public void invalidateNavigation() {
        navigation_visible = new HashMap();
    }

    /**
     * <code>getCurrentSessionId</code> returns the visit_id.
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
        if (!currentpageflow.containsPageRequest(currentpagerequest)) {
            throw new RuntimeException("*** current pageflow " + currentpageflow.getName() +
                                       " does not contain current pagerequest " + currentpagerequest);
        }
        PageRequest current  = currentpagerequest;
        FlowStep[]  workflow = currentpageflow.getAllSteps();

        for (int i = 0; i < workflow.length; i++) {
            FlowStep    step = workflow[i];
            PageRequest page = step.getPageRequest();
            if (page.equals(current)) {
                return false;
            }
            if (checkIsAccessible(current, current.getStatus()) && checkNeedsData(page, current.getStatus())) {
                return true;
            }
        }
        return false;
    }

    /**
     * <code>finalPageIsRunning</code> can be called from inside a {@link de.schlund.pfixcore.workflow.State State}
     * It returned true if the Context is currently running a FINAL page of a defined workflow.
     *
     * @return a <code>boolean</code> value
     */
    public boolean finalPageIsRunning() {
        if (currentpagerequest.getStatus() == PageRequestStatus.FINAL) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isCurrentPageRequestInCurrentFlow() {
        if (currentpageflow != null && currentpageflow.containsPageRequest(currentpagerequest)) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isCurrentPageFlowRequestedByUser() {
        return pageflow_requested_by_user;
    }

    public boolean currentFlowStepWantsPostProcess() {
        if (currentpageflow != null && currentpageflow.containsPageRequest(currentpagerequest)) {
            if (currentpageflow.getFlowStepForPage(currentpagerequest).hasOnContinueAction()) {
                return true;
            }
        }
        return false;
    }

    public boolean currentPageNeedsSSL(PfixServletRequest preq) throws Exception {
        PageRequest page = new PageRequest(preq);
        if (page.isEmpty() && currentpagerequest != null) {
            page = currentpagerequest;
        }
        if (!page.isEmpty()) {
            Properties props = preqprops.getPropertiesForPageRequest(page);
            if (props != null) {
                String     needssl = props.getProperty(PROP_NEEDS_SSL);
                if (needssl != null && needssl.equals("true")) {
                    return true;
                }
            }
        }
        return false;
    }

    public synchronized SPDocument checkAuthorization() throws Exception {
        if (authpage != null) {
            ResultDocument resdoc = null;
            LOG.debug("===> [" + authpage + "]: Checking authorisation");
            if (!checkIsAccessible(authpage, PageRequestStatus.AUTH)) {
                throw new XMLException("*** Authorisation page [" + authpage + "] is not accessible! ***");
            }
            if (checkNeedsData(authpage, PageRequestStatus.AUTH)) {
                LOG.debug("===> [" + authpage + "]: Need authorisation data");
                PageRequest saved  = currentpagerequest;
                currentpagerequest = authpage;
                resdoc             = documentFromCurrentStep();
                currentpagerequest = saved;
                if (resdoc.wantsContinue()) {
                    LOG.debug("===> [" + authpage + "]: Authorisation granted");
                } else {
                    LOG.debug("===> [" + authpage + "]: Authorisation failed");
                }
            } else {
                LOG.debug("===> [" + authpage + "]: Already authorised");
            }
            if (resdoc != null && !resdoc.wantsContinue()) {
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

        currentpageflow    = pageflowmanager.getPageFlowByName(properties.getProperty(DEFPROP));
        currentpagerequest = currentpageflow.getFirstStep().getPageRequest();

        checkForAuthenticationMode();
        checkForAdminMode();
        checkForNavigationReuse();

        needs_update = false;
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
            authpage = new PageRequest(authpagename);
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
                admin_pagereq = new PageRequest(adminpage);
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
        currentpreq.startLogEntry();
        boolean retval     = state.needsData(this, currentpreq);
        currentpreq.endLogEntry("NEEDS_DATA (" + page + ")", 10);
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
        currentpreq.startLogEntry();
        boolean retval = state.isAccessible(this, currentpreq);
        currentpreq.endLogEntry("IS_ACCESSIBLE (" + page + ")", 10);
        currentpagerequest = saved;
        return retval;
    }

    private SPDocument documentFromFlow() throws Exception {
        SPDocument     document = null;
        PageRequest[]  workflow;
        ResultDocument resdoc;

        // First, check if the requested page is defined at all
        // We do this only if the current pagerequest is not the special STARTWITHFLOW_PAGE
        // because then we don't know yet which page to use.

        if (!startwithflow) {
            State state = pagemap.getState(currentpagerequest);
            if (state == null) {
                LOG.warn("* Can't get a handling state for page " + currentpagerequest);
                resdoc = new ResultDocument();
                document = resdoc.getSPDocument();
                document.setResponseError(HttpServletResponse.SC_NOT_FOUND);
                return document;
            }

            // Now, check for possibly needed authorization
            document = checkAuthorization();
            if (document != null) {
                return document;
            }

            // Now we need to make sure that the current page is accessible, and take the right measures if not.
            if (!checkIsAccessible(currentpagerequest, PageRequestStatus.DIRECT)) {
                LOG.warn("[" + currentpagerequest + "]: not accessible! Trying first page of default flow.");
                currentpageflow     = pageflowmanager.getPageFlowByName(properties.getProperty(DEFPROP));
                PageRequest defpage = currentpageflow.getFirstStep().getPageRequest();
                currentpagerequest  = defpage;
                if (!checkIsAccessible(defpage, PageRequestStatus.DIRECT)) {
                    throw new XMLException("Even first page [" + defpage + "] of default flow was not accessible! Bailing out.");
                }
            }

            resdoc = documentFromCurrentStep();
            if (resdoc.wantsContinue() &&
                currentpageflow != null && currentpageflow.containsPageRequest(currentpagerequest)) {
                FlowStep step = currentpageflow.getFlowStepForPage(currentpagerequest);
                step.applyActionsOnContinue(this, resdoc);
            }

            if (!resdoc.wantsContinue()) {
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
                document = runPageFlow();
            } else {
                throw new XMLException("*** ERROR! *** [" + currentpagerequest + "] signalled success, but current page flow == null!");
            }
        } else {
            LOG.debug("* Page is determined from flow [" + currentpageflow + "], starting page flow process");
            document = runPageFlow();
        }
        return document;
    }


    private SPDocument runPageFlow() throws Exception {
        ResultDocument resdoc   = null;
        // We need to re-check the authorisation because the just handled submit could have changed the authorisation status.
        SPDocument     document = checkAuthorization();
        if (document != null) {
            return document;
        }
        FlowStep[]  workflow      = currentpageflow.getAllSteps();
        PageRequest saved         = currentpagerequest;
        boolean     after_current = false;

        for (int i = 0; i < workflow.length; i++) {
            FlowStep    step = workflow[i];
            PageRequest page = step.getPageRequest();
            if (page.equals(saved)) {
                LOG.debug("* Skipping step [" + page + "] in page flow (been there already...)");
                after_current = true;
            } else if (!checkIsAccessible(page, PageRequestStatus.WORKFLOW)) {
                LOG.debug("* Skipping step [" + page + "] in page flow (state is not accessible...)");
                // break;
            } else {
                LOG.debug("* Page flow is at step " + i + ": [" + page + "]");
                boolean needsdata;
                if (after_current && step.wantsToStopHere()) {
                    LOG.debug("=> [" + page + "]: Page flow wants to stop, getting document now.");
                    currentpagerequest = page;
                    page.setStatus(PageRequestStatus.WORKFLOW);
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
                    page.setStatus(PageRequestStatus.WORKFLOW);
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
            PageRequest finalpage = currentpageflow.getFinalPage();
            if (finalpage == null) {
                throw new XMLException("*** Reached end of page flow '" + currentpageflow.getName() + "' " +
                                       "with neither getting a non-null SPDocument or having a FINAL page defined ***");
            } else if (!checkIsAccessible(finalpage, PageRequestStatus.FINAL)) {
                throw new XMLException("*** Reached end of page flow '" + currentpageflow.getName() + "' " +
                                       "but FINAL page [" + finalpage + "] is inaccessible ***");
            } else {
                currentpagerequest = finalpage;
                currentpageflow    = pageflowmanager.pageFlowToPageRequest(currentpageflow, finalpage);
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
        return state.getDocument(this, currentpreq);
    }

    private void trySettingPageRequestAndFlow() {
        PageRequest page = new PageRequest(currentpreq);
        if (!page.isEmpty() && (authpage == null || !page.equals(authpage))) {
            page.setStatus(PageRequestStatus.DIRECT);
            currentpagerequest    = page;
            PageFlow     flow     = null; 
            RequestParam flowname = currentpreq.getRequestParam(PARAM_FLOW);
            if (flowname != null && !flowname.getValue().equals("")) {
                LOG.debug("===> User requesting to switch to flow '" + flowname.getValue() + "'");
                flow = pageflowmanager.getPageFlowByName(flowname.getValue());
                if (flow != null) {
                    LOG.debug("===> Flow '" + flowname.getValue() + "' exists...");
                    pageflow_requested_by_user = true;
                    if (flow.containsPageRequest(page)) {
                    LOG.debug("===> and it contains page '" + page.getName() + "'");
                    } else {
                        LOG.debug("===> CAUTION: it doesn't contain page '" +
                                  page.getName() + "'! Make sure this is what you want...");
                    }
                } else {
                    flow = pageflowmanager.pageFlowToPageRequest(currentpageflow, page);
                    pageflow_requested_by_user = false;
                }
            } else {
                flow = pageflowmanager.pageFlowToPageRequest(currentpageflow, page);
                pageflow_requested_by_user = false;
            }          
            currentpageflow = flow;
            LOG.debug("* Setting currentpagerequest to [" + page + "]");
            LOG.debug("* Setting currentpageflow to [" + currentpageflow.getName() + "]");
        } else {
            page = currentpagerequest;
            if (page != null) {
                page.setStatus(PageRequestStatus.DIRECT);
                LOG.debug("* Reusing page [" + page + "]");
                LOG.debug("* Reusing flow [" + currentpageflow.getName() + "]");
            } else {
                throw new RuntimeException("Don't have a current page to use as output target");
            }
        }
        RequestParam jump = currentpreq.getRequestParam(JUMPPAGE);
        if (jump != null && !jump.getValue().equals("")) {
            setJumpToPageRequest(jump.getValue());
            // We only search for a special jumpflow when also a jumppage is set
            RequestParam jumpflow = currentpreq.getRequestParam(JUMPPAGEFLOW);
            if (jumpflow != null && !jumpflow.getValue().equals("")) {
                setJumpToPageFlow(jumpflow.getValue());
            }
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
            currentpreq.startLogEntry();
            recursePages(navi.getNavigationElements(), element, doc, null, warn_buffer, debug_buffer);
            currentpreq.endLogEntry("CREATE_NAVI_COMPLETE", 25);
        } else {
            if (navigation_visible != null) {
                LOG.debug("=> Reuse old navigation.");
            } else {
                LOG.debug("=> Add new navigation (has been invalidated).");
            }
            currentpreq.startLogEntry();
            recursePages(navi.getNavigationElements(), element, doc, navigation_visible, warn_buffer, debug_buffer);
            currentpreq.endLogEntry("CREATE_NAVI_REUSE", 2);
        }
    }

    private void recursePages(NavigationElement[] pages, Element parent,  Document doc,
                              HashMap vis_map, StringBuffer warn_buffer, StringBuffer debug_buffer) throws Exception {
        for (int i = 0; i < pages.length; i++) {
            NavigationElement page     = pages[i];
            String            pagename     = page.getName();
            PageRequest       pagereq  = new PageRequest(pagename);
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

    public void startLogEntry() {
        currentpreq.startLogEntry();
    }

    public void endLogEntry(String info, long min) {
        currentpreq.endLogEntry(info, min);
    }

    /**
	 * Returns the last exception-object that was stored in the request object.
     * See {@link PfixServletRequest#getLastException() PfixServletRequest} for details.
	 */
	public Throwable getLastException()
	{
		return currentpreq.getLastException();
	}

}
