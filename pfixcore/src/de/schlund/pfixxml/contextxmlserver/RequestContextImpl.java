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

package de.schlund.pfixxml.contextxmlserver;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.schlund.pfixcore.generator.StatusCodeInfo;
import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixcore.workflow.ContextInterceptor;
import de.schlund.pfixcore.workflow.FlowStep;
import de.schlund.pfixcore.workflow.Navigation;
import de.schlund.pfixcore.workflow.NavigationFactory;
import de.schlund.pfixcore.workflow.PageFlow;
import de.schlund.pfixcore.workflow.PageRequest;
import de.schlund.pfixcore.workflow.PageRequestStatus;
import de.schlund.pfixcore.workflow.State;
import de.schlund.pfixcore.workflow.Navigation.NavigationElement;
import de.schlund.pfixcore.workflow.context.RequestContext;
import de.schlund.pfixxml.PfixServletRequest;
import de.schlund.pfixxml.RequestParam;
import de.schlund.pfixxml.ResultDocument;
import de.schlund.pfixxml.SPDocument;
import de.schlund.pfixxml.Variant;
import de.schlund.pfixxml.XMLException;
import de.schlund.pfixxml.config.PageRequestConfig;
import de.schlund.pfixxml.perflogging.PerfEvent;
import de.schlund.pfixxml.perflogging.PerfEventType;
import de.schlund.util.statuscodes.StatusCode;

/**
 * Implementation of the request part of the context used by ContextXMLServer,
 * DirectOutputServer and WebServiceServlet. This class should never be directly
 * used by application developers.
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class RequestContextImpl implements RequestContext, AccessibilityChecker {
    private final static String PARAM_JUMPPAGE = "__jumptopage";

    private final static String PARAM_JUMPPAGEFLOW = "__jumptopageflow";

    private final static String PARAM_FLOW = "__pageflow";

    private final static String PARAM_LASTFLOW = "__lf";

    private final static String PARAM_STARTWITHFLOW = "__startwithflow";

    private final static String PARAM_FORCESTOP = "__forcestop";

    private final static String PROP_NAVI_AUTOINV = "navigation.autoinvalidate";

    private final static Logger LOG = Logger.getLogger(RequestContextImpl.class);

    private PageFlowManager pageflowmanager;

    private VariantManager variantmanager;

    private PageMap pagemap;

    private Navigation navigation;

    private ServerContextImpl context;

    private SessionContextImpl scontext;

    private Context contextWrapper;

    private PageRequest currentpagerequest = null;

    private PageFlow currentpageflow = null;

    private String jumptopage = null;

    private String jumptopageflow = null;

    private PageRequest authpage = null;

    private PfixServletRequest currentpservreq = null;

    private Variant variant = null;

    private String language = null;

    private boolean prohibitcontinue = false;

    private boolean stopnextforcurrentrequest = false;

    private boolean on_jumptopage = false;

    private boolean pageflow_requested_by_user = false;

    private boolean startwithflow = false;

    private boolean autoinvalidate_navi = true;

    private boolean forceinvalidate_navi = false;

    private List<StatusCodeInfo> messages = new ArrayList<StatusCodeInfo>();

    private List<Cookie> cookielist = new ArrayList<Cookie>();

    public RequestContextImpl(ServerContextImpl context, SessionContextImpl scontext) throws Exception {
        pageflowmanager = context.getPageFlowManager();
        variantmanager = context.getVariantManager();
        pagemap = context.getPageMap();

        this.context = context;
        this.scontext = scontext;

        // The navigation is possibly shared across more than one
        // context, i.e. more than one properties object.  So we can't
        // let it be handled by the PropertyObjectManager.
        String navifile = context.getContextConfig().getNavigationFile();
        if (navifile != null) {
            navigation = NavigationFactory.getInstance().getNavigation(navifile);
        }

        // Look for last request in session
        // this is done to get a behaviour similar to the old one
        // when requests where only done synchronous
        String lastpage = scontext.getLastPageName();
        String lastpageflow = scontext.getLastPageFlowName();
        if (lastpage != null && lastpageflow != null) {
            PageFlow tempflow = pageflowmanager.getPageFlowByName(lastpageflow, getVariant());

            // Check if page is in flow - this is important as
            // we access the properties in an unsynchronized
            // way and so they may be inconsistent
            if (tempflow.containsPage(lastpage)) {
                currentpageflow = tempflow;
                currentpagerequest = createPageRequest(lastpage);
            }
        }
        if (currentpageflow == null) {
            currentpageflow = pageflowmanager.getPageFlowByName(context.getContextConfig().getDefaultFlow(), getVariant());
        }
        if (currentpagerequest == null) {
            currentpagerequest = createPageRequest(currentpageflow.getFirstStep().getPageName());
        }

        this.variant = scontext.getVariant();
        this.language = scontext.getLanguage();

        checkForAuthenticationMode();
        checkForNavigationReuse();
    }

    public Properties getPropertiesForCurrentPageRequest() {
        return context.getContextConfig().getPageRequestConfig(currentpagerequest.getName()).getProperties();
    }

    public PageRequest getPageRequest() {
        return currentpagerequest;
    }

    public PageFlow getPageFlow() {
        return currentpageflow;
    }

    public void setPageFlow(String pageflow) {
        PageFlow tmp = pageflowmanager.getPageFlowByName(pageflow, getVariant());
        if (tmp != null) {
            LOG.debug("===> Setting currentpageflow to user-requested flow " + pageflow);
            currentpageflow = tmp;
            pageflow_requested_by_user = true;
        } else {
            LOG.warn("*** Trying to set currentpageflow to " + pageflow + ", but it's not defined ***");
        }
    }

    public String getJumpToPage() {
        return jumptopage;
    }

    public void setJumpToPage(String pagename) {
        PageRequest page = createPageRequest(pagename);
        if (pagemap.getState(page) != null) {
            jumptopage = pagename;
        } else {
            LOG.warn("*** Trying to set jumppage " + pagename + ", but it's not defined ***");
            jumptopage = null;
        }
    }

    public String getJumpToPageFlow() {
        return jumptopageflow;
    }

    public void setJumpToPageFlow(String pageflow) {
        if (jumptopage != null) {
            PageFlow tmp = pageflowmanager.getPageFlowByName(pageflow, null);
            if (tmp != null) {
                jumptopageflow = pageflow;
            } else {
                LOG.warn("*** Trying to set jumptopageflow " + pageflow + ", but it's not defined ***");
                jumptopageflow = null;
            }
        } else {
            jumptopageflow = null;
        }
    }

    public void prohibitContinue() {
        prohibitcontinue = true;
    }

    public boolean isProhibitContinue() {
        return prohibitcontinue;
    }

    public boolean flowStepsBeforeCurrentStepNeedData() throws Exception {
        if (!currentpageflow.containsPage(currentpagerequest.getRootName())) {
            throw new RuntimeException("*** current pageflow " + currentpageflow.getName() + " does not contain current pagerequest " + currentpagerequest);
        }

        PageRequest current = currentpagerequest;
        FlowStep[] workflow = currentpageflow.getAllSteps();

        for (int i = 0; i < workflow.length; i++) {
            FlowStep step = workflow[i];
            String pagename = step.getPageName();
            PageRequest page = createPageRequest(pagename);
            if (pagename.equals(current.getRootName())) {
                return false;
            }
            if (checkIsAccessible(page, current.getStatus()) && checkNeedsData(page, current.getStatus())) {
                return true;
            }
        }
        return false;
    }

    public boolean finalPageIsRunning() {
        return (currentpagerequest.getStatus() == PageRequestStatus.FINAL);
    }

    public boolean jumpToPageIsRunning() {
        return on_jumptopage;
    }

    public boolean flowIsRunning() {
        if (currentpagerequest.getStatus() == PageRequestStatus.WORKFLOW) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isCurrentPageRequestInCurrentFlow() {
        return (currentpageflow != null && currentpageflow.containsPage(currentpagerequest.getRootName()));
    }

    public boolean isCurrentPageFlowRequestedByUser() {
        return pageflow_requested_by_user;
    }

    public void invalidateNavigation() {
        forceinvalidate_navi = true;
    }

    public void reuseNavigation() {
        autoinvalidate_navi = false;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String lang) {
        scontext.setLanguage(lang);
        language = lang;
    }

    public Variant getVariant() {
        return variant;
    }

    public void setVariant(Variant variant) {
        this.variant = variant;
        scontext.setVariant(variant);
    }

    public void setVariantForThisRequestOnly(Variant variant) {
        this.variant = variant;
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
        if (getJumpToPageFlow() != null) {
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

    private boolean currentFlowStepWantsPostProcess() {
        if (currentpageflow != null && currentpageflow.containsPage(currentpagerequest.getRootName())) {
            if (currentpageflow.getFlowStepForPage(currentpagerequest.getRootName()).hasOnContinueAction()) {
                return true;
            }
        }
        return false;
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

    public void addPageMessage(StatusCode scode, String[] args, String level) {
        if (scode == null)
            return;
        messages.add(new StatusCodeInfo(scode, args, level));
    }

    public SPDocument handleRequest(PfixServletRequest preq) throws Exception {
        try {
            SPDocument spdoc = handleRequestWorker(preq);

            // Make sure SSL pages are only returned using SSL.
            // This rule does not apply to pages with the nostore
            // flag, as we would not be able to return such a page
            // after the redirect
            if (getConfigForCurrentPageRequest() != null && spdoc != null && getConfigForCurrentPageRequest().isSSL() && !spdoc.getNostore() && !preq.getOriginalScheme().equals("https")) {
                spdoc.setSSLRedirect("https://" + preq.getServerName() + preq.getContextPath() + preq.getServletPath() + ";jsessionid=" + preq.getSession(false).getId() + "?__reuse=" + spdoc.getTimestamp());
            }

            return spdoc;
        } catch (Exception e) {
            throw e;
        }
    }

    private synchronized SPDocument handleRequestWorker(PfixServletRequest preq) throws Exception {
        currentpservreq = preq;
        prohibitcontinue = false;
        stopnextforcurrentrequest = false;
        jumptopage = null;
        jumptopageflow = null;
        on_jumptopage = false;
        pageflow_requested_by_user = false;
        startwithflow = false;

        RequestParam fstop = currentpservreq.getRequestParam(PARAM_FORCESTOP);
        if (fstop != null && fstop.getValue().equals("true")) {
            // We already decide here to stay on the page, what ever the state wants...
            prohibitContinue();
        }
        if (fstop != null && fstop.getValue().equals("step")) {
            // We want to behave the current pageflow as if it would have the stopnext attribute set to true
            stopnextforcurrentrequest = true;
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

        processIC(context.getStartInterceptors());

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
                    scontext.addVisitedPage(spdoc.getPagename());
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

        processIC(context.getEndInterceptors());
        
        // Save pagerequest and pageflow
        scontext.setLastPageName(getPageRequest().getRootName());
        scontext.setLastPageFlowName(getPageFlow().getRootName());

        return spdoc;
    }

    private void insertPageMessages(SPDocument spdoc) {
        if (spdoc == null)
            return;

        LOG.debug("Adding " + messages.size() + " PageMessages to result document");

        if (!messages.isEmpty()) {
            Document doc = spdoc.getDocument();
            Element formresult = doc.getDocumentElement();

            if (formresult != null) {
                Element messagesElem = doc.createElement("pagemessages");
                formresult.appendChild(messagesElem);

                Iterator<StatusCodeInfo> iter = messages.iterator();
                while (iter.hasNext()) {
                    StatusCodeInfo sci = iter.next();
                    Element msg = doc.createElement("message");
                    Element inc = ResultDocument.createIncludeFromStatusCode(doc, context.getContextConfig().getProperties(), sci.getStatusCode(), sci.getArgs());
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

    private void storeCookies(SPDocument spdoc) {
        for (Iterator<Cookie> i = cookielist.iterator(); i.hasNext();) {
            spdoc.addCookie(i.next());
        }
    }

    private void addPageFlowInfo(PageFlow flow, SPDocument spdoc) throws Exception {
        Document doc = spdoc.getDocument();
        Element root = doc.createElement("pageflow");
        doc.getDocumentElement().appendChild(root);
        root.setAttribute("name", flow.getRootName());
        FlowStep[] steps = flow.getAllSteps();
        for (int i = 0; i < steps.length; i++) {
            String step = steps[i].getPageName();
            Element stepelem = doc.createElement("step");
            root.appendChild(stepelem);
            stepelem.setAttribute("name", step);
        }
    }

    private void addNavigation(Navigation navi, SPDocument spdoc) throws Exception {
        Document doc = spdoc.getDocument();
        Element element = doc.createElement("navigation");
        doc.getDocumentElement().appendChild(element);

        if (autoinvalidate_navi || forceinvalidate_navi || scontext.navigationNeedsRefresh()) {
            LOG.debug("=> Add new navigation.");
            PerfEvent pe = new PerfEvent(PerfEventType.CONTEXT_CREATENAVICOMPLETE, spdoc.getPagename());
            pe.start();
            scontext.refreshNavigation(navi, this);
            recursePages(navi.getNavigationElements(), scontext.getNavigation(), element, doc);
            pe.save();
        } else {
            LOG.debug("=> Reuse old navigation.");
            PerfEvent pe = new PerfEvent(PerfEventType.CONTEXT_CREATENAVIREUSE, spdoc.getPagename());
            pe.start();
            recursePages(navi.getNavigationElements(), scontext.getNavigation(), element, doc);
            pe.save();
        }
    }

    private void recursePages(NavigationElement[] pages, Map<NavigationElement, Integer> navistate, Element parent, Document doc) throws Exception {
        for (int i = 0; i < pages.length; i++) {
            NavigationElement page = pages[i];
            String pagename = page.getName();
            Element pageelem = doc.createElement("page");

            parent.appendChild(pageelem);
            pageelem.setAttribute("name", pagename);
            pageelem.setAttribute("handler", page.getHandler());

            Integer page_vis = navistate.get(page);

            if (page_vis != null) {
                int visible = page_vis.intValue();
                pageelem.setAttribute("visible", "" + visible);
                if (visible == -1) {
                    pageelem.setAttribute("visited", "-1");
                } else {
                    if (scontext.isVisitedPage(pagename)) {
                        pageelem.setAttribute("visited", "1");
                    } else {
                        pageelem.setAttribute("visited", "0");
                    }
                }
            } else {
                // Okay, the page is not in the map
                // however there is a rare chance that the
                // navigation is outdated because the
                // configuration was reloaded in the meantime
                if (context.getContextConfig().getPageRequestConfig(pagename) != null) {
                    if (isPageAccessible(pagename)) { // this also updates navi_visible_map!
                        pageelem.setAttribute("visible", "1");
                    } else {
                        pageelem.setAttribute("visible", "0");
                    }
                    if (scontext.isVisitedPage(pagename)) {
                        pageelem.setAttribute("visited", "1");
                    } else {
                        pageelem.setAttribute("visited", "0");
                    }
                } else {
                    pageelem.setAttribute("visible", "-1");
                    pageelem.setAttribute("visited", "-1");
                }
            }

            if (page.hasChildren()) {
                recursePages(page.getChildren(), navistate, pageelem, doc);
            }
        }
    }

    private void processIC(ContextInterceptor[] icarr) {
        if (icarr != null) {
            for (int i = 0; i < icarr.length; i++) {
                icarr[i].process(getContextWrapper(), currentpservreq);
            }
        }
    }

    private PageRequest createPageRequest(String pagename) {
        Variant var = getVariant();
        if (var != null && var.getVariantFallbackArray() != null && variantmanager != null) {
            return new PageRequest(variantmanager.getVariantMatchingPageRequestName(pagename, var));
        } else {
            return new PageRequest(pagename);
        }
    }

    private PageRequestConfig getConfigForCurrentPageRequest() {
        return context.getContextConfig().getPageRequestConfig(currentpagerequest.getName());
    }

    private SPDocument documentFromFlow() throws Exception {
        SPDocument document = null;

        // First, check if the requested page is defined at all
        // We do this only if the current pagerequest is not the special STARTWITHFLOW_PAGE
        // because then we don't know yet which page to use.

        if (!startwithflow) {
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
            RequestParam sdreq = currentpservreq.getRequestParam(State.SENDAUTHDATA);
            boolean forceauth = (sdreq != null && sdreq.isTrue());

            document = checkAuthorization(forceauth);
            if (document != null) {
                return document;
            }

            // Now we need to make sure that the current page is accessible, and take the right measures if not.
            if (!checkIsAccessible(currentpagerequest, PageRequestStatus.DIRECT)) {
                LOG.warn("[" + currentpagerequest + "]: not accessible! Trying first page of default flow.");
                currentpageflow = pageflowmanager.getPageFlowByName(context.getContextConfig().getDefaultFlow(), getVariant());
                PageRequest defpage = createPageRequest(currentpageflow.getFirstStep().getPageName());
                currentpagerequest = defpage;
                if (!checkIsAccessible(defpage, PageRequestStatus.DIRECT)) {
                    throw new XMLException("Even first page [" + defpage + "] of default flow was not accessible! Bailing out.");
                }
            }

            resdoc = documentFromCurrentStep();
            if (currentpageflow != null && currentpageflow.containsPage(currentpagerequest.getRootName())) {
                FlowStep step = currentpageflow.getFlowStepForPage(currentpagerequest.getRootName());
                step.applyActionsOnContinue(getContextWrapper(), resdoc);
            }

            if (prohibitcontinue) {
                LOG.debug("* [" + currentpagerequest + "] returned document to show, skipping page flow.");
                document = resdoc.getSPDocument();
            } else if (jumptopage != null) {
                LOG.debug("* [" + currentpagerequest + "] signalled success, jumptopage is set as [" + jumptopage + "].");
                currentpagerequest = createPageRequest(jumptopage);
                if (jumptopageflow != null) {
                    setPageFlow(jumptopageflow);
                } else {
                    currentpageflow = pageflowmanager.pageFlowToPageRequest(currentpageflow, currentpagerequest, getVariant());
                }
                jumptopage = null; // we don't want to recurse infinitely
                jumptopageflow = null; // we don't want to recurse infinitely
                on_jumptopage = true; // we need this information to supress the interpretation of
                // the request as one that submits data. See StateImpl,
                // methods isSubmitTrigger & isDirectTrigger

                LOG.debug("******* JUMPING to [" + currentpagerequest + "] *******\n");
                document = documentFromFlow();
            } else if (currentpageflow != null) {
                if (pageflow_requested_by_user || currentpageflow.containsPage(currentpagerequest.getRootName())) {
                    LOG.debug("* [" + currentpagerequest + "] signalled success, starting page flow process");
                    document = runPageFlow(false);
                } else {
                    LOG.debug("* [" + currentpagerequest + "] signalled success, but is neither member of flow [" + currentpageflow + "] nor is this flow explicitely requested, skipping page flow.");
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
        ResultDocument resdoc = null;
        // We need to re-check the authorisation because the just handled submit could have changed the authorisation status.
        SPDocument document = checkAuthorization(false);
        if (document != null) {
            return document;
        }
        FlowStep[] workflow = currentpageflow.getAllSteps();
        PageRequest saved = currentpagerequest;
        boolean after_current = false;

        for (int i = 0; i < workflow.length; i++) {
            FlowStep step = workflow[i];
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
                        LOG.warn("SKIPPEDWOULDSTOP:" + currentpservreq.getServerName() + "|" + page.getName() + "|" + currentpageflow.getName());
                    }

                    after_current = true;
                }
            } else if (!checkIsAccessible(page, PageRequestStatus.WORKFLOW)) {
                LOG.debug("* Skipping step [" + page + "] in page flow (state is not accessible...)");
                // break;
            } else {
                LOG.debug("* Page flow is at step " + i + ": [" + page + "]");
                if (after_current && (step.wantsToStopHere() || stopnextforcurrentrequest)) {
                    if (stopnextforcurrentrequest)
                        LOG.debug("=> Request specifies to act like stophere='true'");
                    LOG.debug("=> [" + page + "]: Page flow wants to stop, getting document now.");
                    currentpagerequest = page;
                    currentpagerequest.setStatus(PageRequestStatus.WORKFLOW);
                    resdoc = documentFromCurrentStep();
                    document = resdoc.getSPDocument();
                    if (document == null) {
                        throw new XMLException("*** FATAL: [" + page + "] returns a 'null' SPDocument! ***");
                    }
                    LOG.debug("* [" + page + "] returned document => show it.");
                    break;
                } else if (checkNeedsData(page, PageRequestStatus.WORKFLOW)) {
                    LOG.debug("=> [" + page + "]: needsData() returned TRUE, leaving page flow and getting document now.");
                    currentpagerequest = page;
                    currentpagerequest.setStatus(PageRequestStatus.WORKFLOW);
                    resdoc = documentFromCurrentStep();
                    document = resdoc.getSPDocument();
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
                throw new XMLException("*** Reached end of page flow '" + currentpageflow.getName() + "' " + "with neither getting a non-null SPDocument or having a FINAL page defined ***");
            } else if (!checkIsAccessible(finalpage, PageRequestStatus.FINAL)) {
                throw new XMLException("*** Reached end of page flow '" + currentpageflow.getName() + "' " + "but FINAL page [" + finalpage + "] is inaccessible ***");
            } else {
                currentpagerequest = finalpage;
                currentpageflow = pageflowmanager.pageFlowToPageRequest(currentpageflow, finalpage, getVariant());
                finalpage.setStatus(PageRequestStatus.FINAL);
                resdoc = documentFromCurrentStep();
                document = resdoc.getSPDocument();
                if (document == null) {
                    throw new XMLException("*** FATAL: " + finalpage + " returns a 'null' SPDocument! ***");
                }
            }
        }
        return document;
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
            PageRequest saved = currentpagerequest;
            if (checkNeedsData(authpage, PageRequestStatus.AUTH) || forceauth) {
                LOG.debug("===> [" + authpage + "]: Need authorisation data");
                currentpagerequest = authpage;
                resdoc = documentFromCurrentStep();
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
                resdoc.getSPDocument().getDocument().getDocumentElement().setAttribute("authoriginalpage", saved.getRootName());
                resdoc.getSPDocument().setPagename(authpage.getName());
                return resdoc.getSPDocument();
            }
        }
        return null;
    }

    private ResultDocument documentFromCurrentStep() throws Exception {
        State state = pagemap.getState(currentpagerequest);
        if (state == null) {
            throw new XMLException("* Can't get a state in documentFromCurrentStep() for page " + currentpagerequest.getName());
        }

        LOG.debug("** [" + currentpagerequest + "]: associated state: " + state.getClass().getName());
        LOG.debug("=> [" + currentpagerequest + "]: Calling getDocument()");
        return state.getDocument(getContextWrapper(), currentpservreq);
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

            PageFlow flow = null;
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
                        LOG.debug("===> CAUTION: it doesn't contain page '" + currentpagerequest.getName() + "'! Make sure this is what you want...");
                    }
                } else {
                    LOG.error("\n\n!!!! CAUTION !!!! Flow '" + flowname + "' is not defined! I'll continue as if no flow was given\n\n");
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
            setJumpToPage(jump.getValue());
            // We only search for a special jumpflow when also a jumppage is set
            RequestParam jumpflow = currentpservreq.getRequestParam(PARAM_JUMPPAGEFLOW);
            if (jumpflow != null && !jumpflow.getValue().equals("")) {
                setJumpToPageFlow(jumpflow.getValue());
            }
        }
    }

    private boolean checkNeedsData(PageRequest page, PageRequestStatus status) throws Exception {
        PageRequest saved = currentpagerequest;
        currentpagerequest = page;
        State state = pagemap.getState(page);
        if (state == null) {
            throw new XMLException("*** Can't get a state to check needsData() for page " + page.getName() + " ***");
        }
        page.setStatus(status);

        PerfEvent pe = new PerfEvent(PerfEventType.PAGE_NEEDSDATA, page.getName());
        pe.start();
        boolean retval = state.needsData(getContextWrapper(), currentpservreq);
        pe.save();

        currentpagerequest = saved;
        return retval;
    }

    private boolean checkIsAccessible(PageRequest page, PageRequestStatus status) throws Exception {
        PageRequest saved = currentpagerequest;
        try {
            currentpagerequest = page;
            State state = pagemap.getState(page);
            if (state == null) {
                throw new XMLException("* Can't get a state to check isAccessible() for page " + page.getName());
            }
            page.setStatus(status);

            PerfEvent pe = new PerfEvent(PerfEventType.PAGE_ISACCESSIBLE, page.getName());
            pe.start();
            boolean retval = state.isAccessible(getContextWrapper(), currentpservreq);
            pe.save();

            return retval;
        } finally {
            currentpagerequest = saved;
        }
    }

    private void checkForAuthenticationMode() {
        String authpagename = context.getContextConfig().getAuthPage();
        if (authpagename != null) {
            authpage = createPageRequest(authpagename);
        } else {
            authpage = null;
        }
    }

    private void checkForNavigationReuse() {
        String navi_autoinv = context.getContextConfig().getProperties().getProperty(PROP_NAVI_AUTOINV);
        if (navi_autoinv != null && navi_autoinv.equals("false")) {
            autoinvalidate_navi = false;
            forceinvalidate_navi = false;
            LOG.info("CAUTION: Setting autoinvalidate of navigation to FALSE!");
            LOG.info("CAUTION: You need to call context.invalidateNavigation() to update the navigation.");
        } else if (navi_autoinv != null && navi_autoinv.equals("true")) {
            autoinvalidate_navi = true;
            forceinvalidate_navi = true;
        } else {
            autoinvalidate_navi = true;
            forceinvalidate_navi = false;
        }
    }

    // Must be public because it is declared in an interface,
    // however it should only be used within the same package
    public boolean isPageAccessible(String pagename) throws Exception {
        PageRequest page = createPageRequest(pagename);
        Variant currentvariant = getVariant();
        setVariant(scontext.getVariant());
        boolean retval = checkIsAccessible(page, PageRequestStatus.NAVIGATION);
        setVariant(currentvariant);
        return retval;
    }

    private Context getContextWrapper() {
        if (contextWrapper == null) {
            contextWrapper = new ContextWrapper(context, scontext, this);
        }
        return contextWrapper;

    }

    public Cookie[] getCookies() {
        return currentpservreq.getCookies();
    }

    public void addCookie(Cookie cookie) {
        cookielist.add(cookie);
    }

    protected Throwable getLastException() {
        return currentpservreq.getLastException();
    }

    public String toString() {
        StringBuffer contextbuf = new StringBuffer("\n");

        contextbuf.append("     pageflow:      " + currentpageflow + "\n");
        contextbuf.append("     PageRequest:   " + currentpagerequest + "\n");
        if (currentpagerequest != null) {
            contextbuf.append("       -> State: " + pagemap.getState(currentpagerequest) + "\n");
            contextbuf.append("       -> Status: " + currentpagerequest.getStatus() + "\n");
        }

        return contextbuf.toString();
    }
}
