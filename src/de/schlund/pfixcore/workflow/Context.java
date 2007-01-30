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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.schlund.pfixcore.generator.StatusCodeInfo;
import de.schlund.pfixcore.workflow.Navigation.NavigationElement;
import de.schlund.pfixxml.AbstractXMLServer;
import de.schlund.pfixxml.AppContext;
import de.schlund.pfixxml.PfixServletRequest;
import de.schlund.pfixxml.PropertyObjectManager;
import de.schlund.pfixxml.RequestParam;
import de.schlund.pfixxml.ResultDocument;
import de.schlund.pfixxml.SPDocument;
import de.schlund.pfixxml.ServletManager;
import de.schlund.pfixxml.Variant;
import de.schlund.pfixxml.XMLException;
import de.schlund.pfixxml.config.ContextConfig;
import de.schlund.pfixxml.config.PageRequestConfig;
import de.schlund.pfixxml.perflogging.PerfEvent;
import de.schlund.pfixxml.perflogging.PerfEventType;
import de.schlund.util.statuscodes.StatusCode;

/**
 * This class is the corner piece of our workflow concept.
 * Here we decide which {@link de.schlund.pfixcore.workflow.State State} handles the
 * request. This decision is based on the notion of
 * {@link de.schlund.pfixcore.workflow.PageFlow PageFlow}s.
 *
 * @author jtl
 *
 */
public class Context implements AppContext {
    private final static Logger   LOG                 = Logger.getLogger(Context.class);
    private final static String   PROP_NAVI_AUTOINV   = "navigation.autoinvalidate";
    private final static String   PARAM_JUMPPAGE      = "__jumptopage";
    private final static String   PARAM_JUMPPAGEFLOW  = "__jumptopageflow";
    private final static String   PARAM_FLOW          = "__pageflow";
    private final static String   PARAM_LASTFLOW      = "__lf";
    private final static String   PARAM_STARTWITHFLOW = "__startwithflow";
    private final static String   PARAM_FORCESTOP     = "__forcestop";
    private final static String   DEF_MESSAGE_LEVEL   = "info";

    // from constructor
    private String name;

    // shared between all instances that have the same properties
    private PageFlowManager pageflowmanager;
    private VariantManager  variantmanager;
    private PageMap         pagemap;

    // new instance for every Context
    private ContextResourceManager          rmanager;
    private Navigation                      navigation       = null;
    private PageRequest                     authpage         = null;
    private Set<String>                     visited_pages    = null;
    private Map<NavigationElement, Integer> navi_visible_map = null;
    private Variant                         sess_variant     = null;
    private ContextInterceptor[]            startIC          = null;
    private ContextInterceptor[]            endIC            = null;
    private String                          visit_id         = null;

    private Boolean saved_autoinvalidate = null;
    
    // values read from properties
    private boolean autoinvalidate_navi = true;

    // the request state
    private Variant            req_variant; // will be initialized to the value of sess_variant on every request
    private PfixServletRequest currentpservreq;
    private PageRequest        currentpagerequest;
    private PageFlow           currentpageflow;
    private String             jumptopagename;
    private String             jumptopageflowname;
    private boolean            on_jumptopage;
    private boolean            pageflow_requested_by_user;
    private boolean            startwithflow;
    private boolean            prohibitcontinue;
    private boolean            stopnextforcurrentrequest;
    private ArrayList          cookielist  = new ArrayList();
    private ArrayList          messages    = new ArrayList();

    // session state
    private PageRequest lastPageRequest;
    private PageFlow lastPageFlow;
    private boolean            needs_update;
    private ContextConfig      config;

    /**
     * <code>init</code> sets up the Context for operation.
     *
     * @param properties a <code>Properties</code> value
     * @exception Exception if an error occurs
     */
    public void init(ContextConfig config, String name) throws Exception {
        this.config   = config;
        this.name     = name;
        rmanager      = new ContextResourceManager();
        visited_pages = new HashSet<String>();
        rmanager.init(this, config);
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
        currentpagerequest=lastPageRequest;
        currentpageflow=lastPageFlow;
        try {
            SPDocument spdoc = handleRequestWorker(preq);
            
            // Make sure SSL pages are only returned using SSL.
            // This rule does not apply to pages with the nostore
            // flag, as we would not be able to return such a page
            // after the redirect
            if (this.getConfigForCurrentPageRequest() != null && this.getConfigForCurrentPageRequest().isSSL() &&
                spdoc != null && !spdoc.getNostore() && !preq.getOriginalScheme().equals("https") && preq.getSession(false) != null) {
                spdoc.setSSLRedirect("https://" + ServletManager.getServerName(preq.getRequest()) + preq.getContextPath() + preq.getServletPath()
                                     + ";jsessionid=" + preq.getSession(false).getId() + "?__reuse=" + spdoc.getTimestamp());
            }
            
            return spdoc;
        } catch (Exception e) {
            throw e;
        } finally {
            if (saved_autoinvalidate != null) {
                autoinvalidate_navi  = saved_autoinvalidate;
                saved_autoinvalidate = null;
            }
            if(currentpagerequest!=null && getConfigForCurrentPageRequest().isStoreXML()) {
                lastPageRequest=currentpagerequest;
                lastPageFlow=currentpageflow;
            }
        }
    }

    private synchronized SPDocument handleRequestWorker(PfixServletRequest preq) throws Exception {
        currentpservreq            = preq;
        prohibitcontinue           = false;
        stopnextforcurrentrequest  = false;
        jumptopagename      = null;
        jumptopageflowname         = null;
        on_jumptopage              = false;
        pageflow_requested_by_user = false;
        startwithflow              = false;
        req_variant                = sess_variant;
        messages.clear();
        cookielist.clear();
        
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
        // representing different locations in the same application.  The page will be set a bit
        // below in trySettingPageRequestAndFlow, where the "real" pageflow to use is also deduced.
        // At least, the currentpageflow is updated to be the currently valid variant.
        RequestParam lastflow = currentpservreq.getRequestParam(PARAM_LASTFLOW);
        
        processIC(startIC);
        
        if (lastflow != null && !lastflow.getValue().equals("")) {
            PageFlow tmp = pageflowmanager.getPageFlowByName(lastflow.getValue(), getVariant());
            if (tmp != null) {
                LOG.debug("* Got last pageflow state from request as [" + tmp.getName() + "]");
                currentpageflow = tmp;
            }
        } else if (currentpageflow != null) {
//             HttpServletRequest req = currentpservreq.getRequest();
//             LOG.warn("LASTFLOWNOTSET:" + currentpservreq.getServerName() + "|"
//                      + currentpservreq.getRequest().getHeader("Referer") + "|"
//                      + currentpservreq.getRequestURI() + "|" + currentpageflow.getName());
            currentpageflow = pageflowmanager.getPageFlowByName(currentpageflow.getRootName(), getVariant());
        }
        // Update currentpagerequest to currently valid variant
        if (currentpagerequest != null) {
            currentpagerequest = createPageRequest(currentpagerequest.getRootName());
        }
        
        trySettingPageRequestAndFlow();

        SPDocument spdoc = documentFromFlow();

        processIC(endIC);
        
        if (spdoc != null) {
            if (spdoc.getPagename() == null) {
                spdoc.setPagename(currentpagerequest.getRootName());
            }
            
            if (currentpageflow != null) {
                spdoc.setProperty("pageflow", currentpageflow.getRootName());
                addPageFlowInfo(currentpageflow, spdoc);
            }

            Variant var = getVariant();
            if (var != null) {
                spdoc.setVariant(var);
                spdoc.getDocument().getDocumentElement().setAttribute("requested-variant", var.getVariantId());
                if (currentpagerequest != null) 
                    spdoc.getDocument().getDocumentElement().setAttribute("used-pr", currentpagerequest.getName());
                if (currentpageflow != null)
                    spdoc.getDocument().getDocumentElement().setAttribute("used-pf", currentpageflow.getName());
            }
        
            if (spdoc.getResponseError() == 0) {
                if (navigation != null && spdoc != null) {
                    visited_pages.add(spdoc.getPagename());
                    addNavigation(navigation, spdoc);
                }

                if (!getConfigForCurrentPageRequest().isStoreXML()) {
                    spdoc.setNostore(true);
                }
            }

            LOG.debug("\n");
            insertPageMessages(spdoc);
            storeCookies(spdoc);
        }

        return spdoc;
    }

    private void createInterceptors(Properties props) throws Exception {
        ArrayList<ContextInterceptor> list = new ArrayList<ContextInterceptor>();
        for (Iterator<Class> i = config.getStartInterceptors().iterator(); i.hasNext();) {
            String classname = i.next().getName();
            list.add(ContextInterceptorFactory.getInstance().getInterceptor(classname));
        }
        startIC = (ContextInterceptor[]) list.toArray(new ContextInterceptor[] {});

        list.clear();
        for (Iterator<Class> i = config.getEndInterceptors().iterator(); i.hasNext();) {
            String classname = i.next().getName();
            list.add(ContextInterceptorFactory.getInstance().getInterceptor(classname));
        }
        endIC = (ContextInterceptor[]) list.toArray(new ContextInterceptor[] {});
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
        return config.getProperties();
    }

    public Properties getPropertiesForCurrentPageRequest() {
        return config.getPageRequestConfig(currentpagerequest.getName()).getProperties();
    }
    
    public PageRequestConfig getConfigForCurrentPageRequest() {
        return config.getPageRequestConfig(currentpagerequest.getName());
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
        PageFlow tmp = pageflowmanager.getPageFlowByName(flowname, getVariant());
        if (tmp != null) {
            LOG.debug("===> Setting currentpageflow to user-requested flow " + flowname);
            currentpageflow            = tmp;
            pageflow_requested_by_user = true;
        } else {
            LOG.warn("*** Trying to set currentpageflow to " + flowname + ", but it's not defined ***");
        }
    }
    
    public void setJumpToPageRequest(String pagename) {
        // Just check if the pagename will map to ANY pagerequest in any variant...
        PageRequest page = createPageRequest(pagename);
        if (pagemap.getState(page) != null) {
            jumptopagename = pagename;
        } else {
            LOG.warn("*** Trying to set jumppage " + pagename + ", but it's not defined ***");
            jumptopagename = null;
        }
    }

    public void setJumpToPageFlow(String flowname) {
        if (jumptopagename != null) {
            PageFlow tmp = pageflowmanager.getPageFlowByName(flowname, null);
            if (tmp != null) {
                jumptopageflowname = flowname;
            } else {
                LOG.warn("*** Trying to set jumptopageflow " + flowname + ", but it's not defined ***");
                jumptopageflowname = null;
            }
        } else {
            jumptopageflowname = null;
        }
    }

    public void forceStopAtNextStep(boolean force) {
        stopnextforcurrentrequest = force;
    }

    public void prohibitContinue() {
        prohibitcontinue = true;
    }

    public void invalidateNavigation() {
        navi_visible_map = null;
    }


    public Cookie[] getRequestCookies() {
        return currentpservreq.getCookies();
    }

    public void setLanguage(String lang) {
        currentpservreq.getSession(false).setAttribute(AbstractXMLServer.SESS_LANG, lang);        
    }

    public String getLanguage() {
        return (String) currentpservreq.getSession(false).getAttribute(AbstractXMLServer.SESS_LANG);
    }

    public void addCookie(Cookie cookie) {
        cookielist.add(cookie);
    }

    public Variant getVariant() {
        return req_variant;
    }

    public void setVariant(Variant var) {
        if (sess_variant == req_variant) { // req_variant has not already been changed independently
            req_variant = var;
        }
        sess_variant = var;
    }

    public void setVariantForThisRequestOnly(Variant var) {
        req_variant = var;
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
            PageRequest page     = createPageRequest(pagename);
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
        PageRequest page = null;
        String pagename = preq.getPageName();
        if (pagename != null) {
            page = createPageRequest(pagename);
        }
        if (page == null && currentpagerequest != null) {
            page = currentpagerequest;
        }
        if (page != null) {
            PageRequestConfig prconfig = config.getPageRequestConfig(page.getName());
            if (prconfig != null) {
                if (prconfig.isSSL()) {
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
        return jumptopagename != null;
    }
    
    public boolean isJumptToPageFlowSet() {
        return jumptopageflowname != null;
    }
    
    public boolean isProhibitContinueSet() {
        return prohibitcontinue;
    }
    
    private boolean isForceStopAtNextStepSet() {
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

        pageflowmanager = (PageFlowManager) pom.getConfigurableObject(config, de.schlund.pfixcore.workflow.PageFlowManager.class);
        variantmanager  = (VariantManager) pom.getConfigurableObject(config, VariantManager.class);
        pagemap         = (PageMap) pom.getConfigurableObject(config, de.schlund.pfixcore.workflow.PageMap.class);

        // The navigation is possibly shared across more than one
        // context, i.e. more than one properties object.  So we can't
        // let it be handled by the PropertyObjectManager.
        if (this.config.getNavigationFile() != null) {
            navigation = NavigationFactory.getInstance().getNavigation(this.config.getNavigationFile());
        }

        currentpageflow    = pageflowmanager.getPageFlowByName(this.config.getDefaultFlow(), getVariant());
        currentpagerequest = createPageRequest(currentpageflow.getFirstStep().getPageName());

        // Use properties until interceptors are in XSD
        createInterceptors(config.getProperties());
        
        checkForAuthenticationMode();
        checkForNavigationReuse();

        needs_update = false;
    }

    private boolean isPageRequestInFlow(PageRequest page, PageFlow pageflow) {
        return (pageflow != null && pageflow.containsPage(page.getRootName()));
    }

    private void checkForAuthenticationMode() {
        String authpagename = this.config.getAuthPage();
        if (authpagename != null) {
            authpage = createPageRequest(authpagename);
        } else {
            authpage = null;
        }
    }

    private void checkForNavigationReuse() {
        String navi_autoinv = config.getProperties().getProperty(PROP_NAVI_AUTOINV);
        if (navi_autoinv != null && navi_autoinv.equals("false")) {
            autoinvalidate_navi = false;
            LOG.info("CAUTION: Setting autoinvalidate of navigation to FALSE!");
            LOG.info("CAUTION: You need to call context.invalidateNavigation() to update the navigation.");
        } else {
            autoinvalidate_navi = true;
        }
    }

    public void setAutoinvalidateNavigationForThisRequestOnly(boolean invalidate) {
        if (saved_autoinvalidate == null) {
            saved_autoinvalidate = autoinvalidate_navi;
        }
        autoinvalidate_navi = invalidate;
    }
    
    public boolean stateMustSupplyFullDocument() {
        if (prohibitcontinue) {
            // We will use the returned document no matter what else happens.
            return true;
        }
        if (currentFlowStepWantsPostProcess()) {
            // We need the full doc for the post processing no matter what else happens.
            return true;
        }
        if (isJumptToPageFlowSet()) {
            // We will jump to some page and not use the returned document for creating the UI.
            return false;
        }
        if (isCurrentPageRequestInCurrentFlow() || isCurrentPageFlowRequestedByUser()) {
            // The next page to display is determined from the pageflow
            return false;
        }
        
        // better create the document one time too much...
        return true;
    }

    private boolean checkNeedsData(PageRequest page, PageRequestStatus status) throws Exception {
        PageRequest saved  = currentpagerequest;
        currentpagerequest = page;
        State       state  = pagemap.getState(page);
        if (state == null) {
            throw new XMLException ("*** Can't get a state to check needsData() for page " + page.getName() + " ***");
        }
        page.setStatus(status);
      
        
        PerfEvent pe = new PerfEvent(PerfEventType.PAGE_NEEDS_DATA.name(), page.getName());
        pe.start();
        boolean retval = state.needsData(this, currentpservreq);
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
        
        PerfEvent pe = new PerfEvent(PerfEventType.PAGE_IS_ACCESSIBLE.name(), page.getName());
        pe.start();
        boolean retval = state.isAccessible(this, currentpservreq);
        pe.save();

        if (navigation != null &&  navi_visible_map != null) {
            NavigationElement navi_elem = navigation.getNavigationElementForPageRequest(page);
            if (navi_elem != null) {
                if (retval) {
                    navi_visible_map.put(navi_elem, 1);
                } else {
                    navi_visible_map.put(navi_elem, 0);
                }
            }
        }

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
                currentpageflow     = pageflowmanager.getPageFlowByName(config.getDefaultFlow(), getVariant());
                PageRequest defpage = createPageRequest(currentpageflow.getFirstStep().getPageName());
                currentpagerequest  = defpage;
                if (!checkIsAccessible(defpage, PageRequestStatus.DIRECT)) {
                    throw new XMLException("Even first page [" + defpage + "] of default flow was not accessible! Bailing out.");
                }
            }

            resdoc = documentFromCurrentStep();
            if (currentpageflow != null && currentpageflow.containsPage(currentpagerequest.getRootName())) {
                FlowStep step = currentpageflow.getFlowStepForPage(currentpagerequest.getRootName());
                step.applyActionsOnContinue(this, resdoc);
            }

            if (currentpageflow != null) {
                currentpageflow = pageflowmanager.getPageFlowByName(currentpageflow.getRootName(), getVariant());
            }

            if (prohibitcontinue) {
                LOG.debug("* [" + currentpagerequest + "] returned document to show, skipping page flow.");
                document = resdoc.getSPDocument();
            } else if (jumptopagename != null) {
                LOG.debug("* [" + currentpagerequest + "] signalled success, jumptopage is set as [" + jumptopagename + "].");
                currentpagerequest = createPageRequest(jumptopagename);
                if (jumptopageflowname != null) {
                    setPageFlow(jumptopageflowname);
                } else {
                    currentpageflow = pageflowmanager.pageFlowToPageRequest(currentpageflow, currentpagerequest, getVariant());
                }

                jumptopagename     = null; // we don't want to recurse infinitely
                jumptopageflowname = null; // we don't want to recurse infinitely
                on_jumptopage      = true; // we need this information to supress the interpretation of
                                           // the request as one that submits data. See StateImpl,
                                           // methods isSubmitTrigger & isDirectTrigger

                LOG.debug("******* JUMPING to [" + currentpagerequest + "] *******\n");
                document = documentFromFlow();
            } else if (currentpageflow != null) {
                if (pageflow_requested_by_user || currentpageflow.containsPage(currentpagerequest.getRootName())) {
                    LOG.debug("* [" + currentpagerequest + "] signalled success, starting page flow process");
                    document = runPageFlow(false);
                } else {
                    LOG.debug("* [" + currentpagerequest + "] signalled success, but is neither member of flow [" +
                              currentpageflow + "] nor is this flow explicitely requested, skipping page flow.");
                    document = resdoc.getSPDocument();
                }
            } else {
                // throw new XMLException("*** ERROR! *** [" + currentpagerequest + "] signalled success, but current page flow == null!");
                LOG.debug("* [" + currentpagerequest + "] signalled success, but page flow == null, skipping page flow.");
                document = resdoc.getSPDocument();
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
            PageRequest page = createPageRequest(step.getPageName());
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
                    if (checkIsAccessible(page, PageRequestStatus.WORKFLOW) && checkNeedsData(page, PageRequestStatus.WORKFLOW)) {
                        LOG.warn("SKIPPEDWOULDSTOP:" + currentpservreq.getServerName() + "|"
                                 + page.getName() + "|" + currentpageflow.getName());
                    }

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
                finalpage = createPageRequest(currentpageflow.getFinalPage());
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
                currentpageflow    = pageflowmanager.pageFlowToPageRequest(currentpageflow, finalpage, getVariant());
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
        PageRequest tmp = null;
        String tmppagename = currentpservreq.getPageName();
        if (tmppagename != null) {
            tmp = createPageRequest(tmppagename);
        }
        if (tmp != null && (authpage == null || !tmp.equals(authpage))) {
            currentpagerequest = tmp;
            currentpagerequest.setStatus(PageRequestStatus.DIRECT);

            PageFlow     flow     = null;
            RequestParam flowname = currentpservreq.getRequestParam(PARAM_FLOW);
            if (flowname != null && !flowname.getValue().equals("")) {
                LOG.debug("===> User requesting to switch to flow '" + flowname.getValue() + "'");
                flow = pageflowmanager.getPageFlowByName(flowname.getValue(), getVariant());
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
                    flow = pageflowmanager.pageFlowToPageRequest(currentpageflow, currentpagerequest, getVariant());
                    pageflow_requested_by_user = false;
                }
            } else {
                flow = pageflowmanager.pageFlowToPageRequest(currentpageflow, currentpagerequest, getVariant());
                pageflow_requested_by_user = false;
            }
            currentpageflow = flow;
            LOG.debug("* Setting currentpagerequest to [" + currentpagerequest.getName() + "]");
            LOG.debug("* Setting currentpageflow to [" + currentpageflow.getName() + "]");
        } else {
            if (currentpagerequest != null) {
                currentpagerequest = createPageRequest(currentpagerequest.getRootName());
                currentpagerequest.setStatus(PageRequestStatus.DIRECT);
                LOG.debug("* Reusing page [" + currentpagerequest + "]");
                LOG.debug("* Reusing flow [" + currentpageflow.getName() + "]");
            } else {
                throw new RuntimeException("Don't have a current page to use as output target");
            }
        }
        RequestParam jump = currentpservreq.getRequestParam(PARAM_JUMPPAGE);
        if (jump != null && !jump.getValue().equals("")) {
            setJumpToPageRequest(jump.getValue());
            // We only search for a special jumpflow when also a jumppage is set
            RequestParam jumpflow = currentpservreq.getRequestParam(PARAM_JUMPPAGEFLOW);
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

        if (autoinvalidate_navi || navi_visible_map == null) {
            LOG.debug("=> Add new navigation.");
            navi_visible_map = new HashMap<NavigationElement, Integer>();
            PerfEvent pe = new PerfEvent(PerfEventType.CONTEXT_CREATE_NAVI_COMPLETE.name(), spdoc.getPagename());
            pe.start();
            recursePages(navi.getNavigationElements(), element, doc, true);
            pe.save();
        } else {
            LOG.debug("=> Reuse old navigation.");
            PerfEvent pe = new PerfEvent(PerfEventType.CONTEXT_CREATE_NAVI_REUSE.name(), spdoc.getPagename());
            pe.start();
            recursePages(navi.getNavigationElements(), element, doc, false);
            pe.save();
        }
    }

    private void recursePages(NavigationElement[] pages, Element parent,  Document doc, boolean create_new) throws Exception {
        for (int i = 0; i < pages.length; i++) {
            NavigationElement page     = pages[i];
            String            pagename = page.getName();
            PageRequest       pagereq  = createPageRequest(pagename);
            Element           pageelem = doc.createElement("page");

            parent.appendChild(pageelem);
            pageelem.setAttribute("name", pagename);
            pageelem.setAttribute("handler", page.getHandler());

            Integer page_vis = null;
            if (!create_new) {
                page_vis = (Integer) navi_visible_map.get(page);
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
                if (config.getPageRequestConfig(pagereq.getName()) != null) {
                    if (checkIsAccessible(pagereq,PageRequestStatus.NAVIGATION)) { // this also updates navi_visible_map!
                        pageelem.setAttribute("visible", "1");
                    } else {
                        pageelem.setAttribute("visible", "0");
                    }
                    if (visited_pages.contains(pagename)) {
                        pageelem.setAttribute("visited", "1");
                    } else {
                        pageelem.setAttribute("visited", "0");
                    }
                } else {
                    pageelem.setAttribute("visible", "-1");
                    pageelem.setAttribute("visited", "-1");
                    navi_visible_map.put(page, -1);
                }
            }

            if (page.hasChildren()) {
                recursePages(page.getChildren(), pageelem, doc, create_new);
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
                    Element        inc = ResultDocument.createIncludeFromStatusCode(doc, config.getProperties(), sci.getStatusCode(), sci.getArgs());
                    msg.appendChild(inc);
                    if (sci.getLevel() != null) {
                        msg.setAttribute("level", sci.getLevel());
                    }
                    messagesElem.appendChild(msg);
                    
                    LOG.debug("Added PageMessage for level " + sci.getLevel() + " with args " + sci.getArgs());
                }
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

    public Properties getPropertiesForContextResource(ContextResource res) {
        return this.config.getContextResourceConfig(res.getClass()).getProperties();
    }
    
    public ContextConfig getContextConfig() {
        return config;
    }
    
    private PageRequest createPageRequest(String pagename) {
        Variant var = getVariant();
        if (var != null && var.getVariantFallbackArray() != null && variantmanager != null) {
            return new PageRequest(variantmanager.getVariantMatchingPageRequestName(pagename, var));
        } else {
            return new PageRequest(pagename);
        }
    }

    public synchronized void updateConfig(ContextConfig newConfig) {
        if (newConfig != config) {
            config = newConfig;
            reset();
        }
    }
}
