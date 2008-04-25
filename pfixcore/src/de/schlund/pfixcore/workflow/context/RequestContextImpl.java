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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.servlet.http.Cookie;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.schlund.pfixcore.auth.AuthConstraint;
import de.schlund.pfixcore.auth.AuthConstraintViolation;
import de.schlund.pfixcore.auth.AuthorizationException;
import de.schlund.pfixcore.auth.AuthorizationInterceptor;
import de.schlund.pfixcore.auth.Role;
import de.schlund.pfixcore.exception.PustefixApplicationException;
import de.schlund.pfixcore.exception.PustefixCoreException;
import de.schlund.pfixcore.generator.StatusCodeInfo;
import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixcore.workflow.ContextImpl;
import de.schlund.pfixcore.workflow.ContextInterceptor;
import de.schlund.pfixcore.workflow.PageMap;
import de.schlund.pfixcore.workflow.PageRequest;
import de.schlund.pfixcore.workflow.PageRequestStatus;
import de.schlund.pfixcore.workflow.State;
import de.schlund.pfixcore.workflow.StateFactory;
import de.schlund.pfixcore.workflow.VariantManager;
import de.schlund.pfixxml.ContextXMLServlet;
import de.schlund.pfixxml.PfixServletRequest;
import de.schlund.pfixxml.RequestParam;
import de.schlund.pfixxml.ResultDocument;
import de.schlund.pfixxml.SPDocument;
import de.schlund.pfixxml.ServletManager;
import de.schlund.pfixxml.Variant;
import de.schlund.pfixxml.config.PageRequestConfig;
import de.schlund.pfixxml.config.ProcessActionConfig;
import de.schlund.pfixxml.perflogging.PerfEvent;
import de.schlund.pfixxml.perflogging.PerfEventType;
import de.schlund.util.statuscodes.StatusCode;

/**
 * Implementation of the request part of the context used by ContextXMLServlet,
 * DirectOutputServlet and WebServiceServlet. This class should never be
 * directly used by application developers.
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class RequestContextImpl implements Cloneable, AuthorizationInterceptor {

    private final static Logger LOG = Logger.getLogger(ContextImpl.class);
    public final static String PARAM_ACTION = "__action";
    private final static String PARAM_JUMPPAGE = "__jumptopage";
    private final static String PARAM_JUMPPAGEFLOW = "__jumptopageflow";
    private final static String PARAM_FLOW = "__pageflow";
    private final static String PARAM_LASTFLOW = "__lf";
    private final static String PARAM_STARTWITHFLOW = "__startwithflow";
    private final static String PARAM_FORCESTOP = "__forcestop";
    private final static String PARAM_ROLEAUTH = "__roleauth";

    private ContextImpl parentcontext;
    private ServerContextImpl servercontext;
    private PageFlowManager pageflowmanager;
    private VariantManager variantmanager;
    private PageMap pagemap;

    private Variant variant = null;
    private String language = null;

    private PageRequest currentpagerequest = null;
    private PageFlow currentpageflow = null;
    private PfixServletRequest currentpservreq = null;

    private String jumptopage = null;
    private String jumptopageflow = null;

    private boolean roleAuth;
    private String roleAuthTarget;
    private Set<String> roleAuthDeps;

    private PageRequest authpage = null;
    private boolean prohibitcontinue = false;
    private boolean stopnextforcurrentrequest = false;
    // private boolean on_jumptopage = false;

    private Set<StatusCodeInfo> messages = new HashSet<StatusCodeInfo>();
    private List<Cookie> cookielist = new ArrayList<Cookie>();

    public RequestContextImpl(ServerContextImpl servercontext, ContextImpl context) {
        this.parentcontext = context;
        this.servercontext = servercontext;
        this.pageflowmanager = servercontext.getPageFlowManager();
        this.variantmanager = servercontext.getVariantManager();
        this.pagemap = servercontext.getPageMap();

        this.variant = parentcontext.getSessionVariant();
        this.language = parentcontext.getSessionLanguage();

        checkForAuthenticationMode();
    }

    public ServerContextImpl getServerContext() {
        return servercontext;
    }

    public Properties getPropertiesForCurrentPageRequest() {
        if (currentpagerequest != null) {
            PageRequestConfig conf = servercontext.getContextConfig().getPageRequestConfig(currentpagerequest.getName());
            if (conf != null) {
                return conf.getProperties();
            }
        }
        return null;
    }

    public PageRequest getCurrentPageRequest() {
        return currentpagerequest;
    }

    public PageFlow getCurrentPageFlow() {
        if (currentpservreq == null) {
            throw new IllegalStateException("PageFlow is only available witihin request handling");
        }
        return currentpageflow;
    }

    public void setCurrentPageFlow(String pageflow) {
        if (currentpservreq == null) {
            throw new IllegalStateException("PageFlow is only available witihin request handling");
        }

        PageFlow tmp = pageflowmanager.getPageFlowByName(pageflow, getVariant());
        if (tmp != null) {
            LOG.debug("===> Setting currentpageflow to user-requested flow " + pageflow);
            currentpageflow = tmp;
        } else {
            LOG.warn("*** Trying to set currentpageflow to " + pageflow + ", but it's not defined ***");
        }
    }

    public String getJumpToPage() {
        if (currentpservreq == null) {
            throw new IllegalStateException("JumpToPage is only available witihin request handling");
        }
        return jumptopage;
    }

    public void setJumpToPage(String pagename) {
        if (currentpservreq == null) {
            throw new IllegalStateException("JumpToPage is only available witihin request handling");
        }
        jumptopage = pagename;
    }

    public boolean isJumpToPageSet() {
        return getJumpToPage() != null;
    }

    public String getJumpToPageFlow() {
        if (currentpservreq == null) {
            throw new IllegalStateException("JumpToPageFlow is only available witihin request handling");
        }
        return jumptopageflow;
    }

    public void setJumpToPageFlow(String pageflow) {
        if (currentpservreq == null) {
            throw new IllegalStateException("JumpToPageFlow is only available witihin request handling");
        }

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

    public boolean isJumpToPageFlowSet() {
        return getJumpToPageFlow() != null;
    }

    public void prohibitContinue() {
        if (currentpservreq == null) {
            throw new IllegalStateException("PageFlow handling is only available witihin request handling");
        }
        prohibitcontinue = true;
    }

    public boolean isProhibitContinue() {
        if (currentpservreq == null) {
            throw new IllegalStateException("PageFlow handling is only available witihin request handling");
        }
        return prohibitcontinue;
    }

    public boolean isProhibitContinueSet() {
        return isProhibitContinue();
    }

    public boolean precedingFlowNeedsData() throws PustefixApplicationException {
        if (currentpservreq == null) {
            throw new IllegalStateException("PageFlow information is only availabe during request handling");
        }

        if (!currentpageflow.containsPage(currentpagerequest.getRootName(), this.parentcontext)) {
            throw new RuntimeException("*** current pageflow " + currentpageflow.getName() + " does not contain current pagerequest "
                    + currentpagerequest);
        }

        return currentpageflow.precedingFlowNeedsData(this.parentcontext);
    }

//    public boolean jumpToPageIsRunning() {
//        if (currentpservreq == null) {
//            throw new IllegalStateException("PageFlow information is only availabe during request handling");
//        }
//        return on_jumptopage;
//    }

    public boolean flowIsRunning() {
        if (currentpservreq == null) {
            throw new IllegalStateException("PageFlow information is only availabe during request handling");
        }

        if (currentpagerequest.getStatus() == PageRequestStatus.WORKFLOW) {
            return true;
        } else {
            return false;
        }
    }

//    public boolean isCurrentPageRequestInCurrentFlow() {
//        if (currentpservreq == null) {
//            throw new IllegalStateException("PageFlow information is only availabe during request handling");
//        }
//        return (currentpageflow != null && currentpageflow.containsPage(currentpagerequest.getRootName(), this.parentcontext));
//    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String lang) {
        language = lang;
    }

    public Variant getVariant() {
        return variant;
    }

    public void setVariantForThisRequestOnly(Variant variant) {
        if (currentpservreq == null) {
            throw new IllegalStateException("This feature is only available during request handling");
        }
        this.variant = variant;
    }

    public boolean stateMustSupplyFullDocument() {
        if (currentpservreq == null) {
            throw new IllegalStateException("PageFlow information is only availabe during request handling");
        }

        if (prohibitcontinue) {
            // We will use the returned document no matter what else happens.
            return true;
        }
        if (currentpageflow != null && currentpageflow.hasHookAfterRequest(parentcontext)) {
            // We need the full doc for the post processing no matter what else
            // happens.
            return true;
        }
        if (getJumpToPageFlow() != null) {
            // We will jump to some page and not use the returned document for
            // creating the UI.
            return false;
        }
        if (currentpageflow != null) {
            // The next page to display is determined from the pageflow
            return false;
        }

        // better create the document one time too much...
        return true;
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
        if (currentpservreq == null) {
            throw new IllegalStateException("PageMessages are only availabe during request handling");
        }
        if (scode == null) return;
        messages.add(new StatusCodeInfo(scode, args, level));
    }

    public SPDocument handleRequest(PfixServletRequest preq) throws PustefixApplicationException, PustefixCoreException {
        SPDocument spdoc;

        spdoc = handleRequestWorker(preq);

        boolean forceSSL = false;
        if (getConfigForCurrentPageRequest() != null && getConfigForCurrentPageRequest().isSSL() && !preq.getOriginalScheme().equals("https")) {
            forceSSL = true;
        }

        if (spdoc != null && forceSSL) {
            // Make sure connection is switched to SSL if current page is marked
            // as "secure"
            String scheme = "https";
            String port = getServerContext().getProperties().getProperty(ServletManager.PROP_SSL_REDIRECT_PORT + String.valueOf(preq.getOriginalServerPort()));
            if (port == null) {
                port = "443";
            }

            String redirectURL = scheme + "://" + ServletManager.getServerName(preq.getRequest()) + ":" + port + preq.getContextPath()
                    + preq.getServletPath() + "/" + spdoc.getPagename() + ";jsessionid=" + preq.getSession(false).getId() + "?__reuse="
                    + spdoc.getTimestamp();

            RequestParam rp = preq.getRequestParam("__frame");
            if (rp != null) {
                redirectURL += "&__frame=" + rp.getValue();
            }
            spdoc.setRedirect(redirectURL);

        }

        // Reset stored variant so the session variant is being used
        // to check the visibility of other pages when rendering the output
        this.variant = parentcontext.getSessionVariant();

        return spdoc;
    }

    private SPDocument handleRequestWorker(PfixServletRequest preq) throws PustefixApplicationException, PustefixCoreException {
        currentpservreq = preq;
        prohibitcontinue = false;
        stopnextforcurrentrequest = false;
        jumptopage = null;
        jumptopageflow = null;
        // on_jumptopage = false;
        roleAuthDeps = null;


        RequestParam swflow = currentpservreq.getRequestParam(PARAM_STARTWITHFLOW);
        boolean startwithflow = false;
        if (swflow != null && swflow.getValue().equals("true")) {
            startwithflow = true;
        }

        processIC(servercontext.getStartInterceptors());

        String tmppagename = currentpservreq.getPageName();
        if (tmppagename != null) {
            currentpagerequest = createPageRequest(tmppagename);
        } else {
            currentpagerequest = createPageRequest(parentcontext.getContextConfig().getDefaultPage());
        }

        RequestParam reqParam = currentpservreq.getRequestParam(PARAM_ROLEAUTH);
        roleAuth = (reqParam != null && reqParam.isTrue());
        if (roleAuth) {
            roleAuthTarget = currentpagerequest.getName();
            PageRequestConfig targetPageConf = servercontext.getContextConfig().getPageRequestConfig(roleAuthTarget);
            if (targetPageConf != null) {
                AuthConstraint authConst = targetPageConf.getAuthConstraint();
                if (authConst == null) authConst = getParentContext().getContextConfig().getDefaultAuthConstraint();
                if (authConst != null) {
                    String authPageName = authConst.getAuthPage();
                    if (authPageName != null) {
                        currentpagerequest = createPageRequest(authPageName);
                        if(!roleAuthTarget.equals(authPageName)) setJumpToPage(roleAuthTarget);
                    } else throw new RuntimeException("No authpage defined for authconstraint " + "of page: " + roleAuthTarget);
                } else throw new RuntimeException("No authconstraint defined for page: " + roleAuthTarget);
            } else throw new RuntimeException("Target page not configured: " + roleAuthTarget);
        }

        // action lookup
        ProcessActionConfig action = null;
        RequestParam actionname = preq.getRequestParam(RequestContextImpl.PARAM_ACTION);
        if (actionname != null && !actionname.getValue().equals("")) {
            LOG.debug("======> Found __action parameter " + actionname);
            Map<String, ? extends ProcessActionConfig> actionmap = getConfigForCurrentPageRequest().getProcessActions();
            if (actionmap != null) {
                action = actionmap.get(actionname.getValue());
                if (action != null) {
                    LOG.debug("        ...and found matching ProcessAction: " + action);
                }
            }
            if (action == null) {
                throw new PustefixApplicationException("Page " + currentpagerequest.getName() + " has been called with unknown action " + actionname);
            }
        }

        // jumptopage/jumptopageflow handling
        RequestParam jump = currentpservreq.getRequestParam(PARAM_JUMPPAGE);
        if (jump != null && !jump.getValue().equals("")) {
            setJumpToPage(jump.getValue());
            // We only search for a special jumpflow when also a jumppage is set
            RequestParam jumpflow = currentpservreq.getRequestParam(PARAM_JUMPPAGEFLOW);
            if (jumpflow != null && !jumpflow.getValue().equals("")) {
                setJumpToPageFlow(jumpflow.getValue());
            }
        } else if (action != null) {
            String ac_jumptopage = action.getJumpToPage();
            String ac_jumptopageflow = action.getJumpToPageFlow();
            if (ac_jumptopage != null) {
                setJumpToPage(ac_jumptopage);
                if (getJumpToPage() != null && ac_jumptopageflow != null) {
                    setJumpToPageFlow(ac_jumptopageflow);
                }
            }
        }

        // forcestop handling
        RequestParam fstop = currentpservreq.getRequestParam(PARAM_FORCESTOP);
        if (fstop != null) {
            if (fstop.getValue().equals("true")) {
                // We already decide here to stay on the page, what ever the
                // state wants...
                prohibitContinue();
            }
            if (fstop.getValue().equals("step")) {
                // We want the current pageflow to behave as if it would have
                // the stopnext attribute set to true
                stopnextforcurrentrequest = true;
            }
        } else if (action != null) {
            String ac_forcestop = action.getForceStop();
            if (ac_forcestop != null) {
                if (ac_forcestop.equals("true")) {
                    prohibitcontinue = true;
                } else if (ac_forcestop.equals("step")) {
                    stopnextforcurrentrequest = true;
                }
            }
        }

        PageFlow lastflow = null;
        if (currentpservreq.getRequestParam(PARAM_LASTFLOW) != null) {
            lastflow = pageflowmanager.getPageFlowByName(currentpservreq.getRequestParam(PARAM_LASTFLOW).getValue(), getVariant());
        }

        // TODO: remove authpage
        if (!currentpagerequest.equals(authpage)) {
            RequestParam pageflow = currentpservreq.getRequestParam(PARAM_FLOW);
            if (pageflow != null && !pageflow.getValue().equals("") && pageflowmanager.getPageFlowByName(pageflow.getValue(), getVariant()) != null) {
                currentpageflow = pageflowmanager.getPageFlowByName(pageflow.getValue(), getVariant());
                LOG.debug("===> Got pageflow from request parameter as [" + currentpageflow.getName() + "]");
            } else if (action != null && action.getPageflow() != null
                    && pageflowmanager.getPageFlowByName(action.getPageflow(), getVariant()) != null) {
                currentpageflow = pageflowmanager.getPageFlowByName(action.getPageflow(), getVariant());
                LOG.debug("===> Got pageflow from action [" + action.getName() + "] as [" + currentpageflow.getName() + "]");
            } else {
                LOG.debug("===> Searching matching pageflow to page [" + currentpagerequest.getName() + "]...");
                if (lastflow != null) {
                    LOG.debug("     ...prefering flow [" + lastflow.getName() + "]...");
                }
                currentpageflow = pageflowmanager.pageFlowToPageRequest(lastflow, currentpagerequest, getVariant(), parentcontext);
                if (currentpageflow != null) {
                    LOG.debug("     ...got pageflow [" + currentpageflow.getName() + "] as matching flow.");
                } else {
                    LOG.debug("     ...got no matching pageflow for page [" + currentpagerequest.getName() + "]");
                }
            }
        }
        
        SPDocument spdoc = documentFromFlow(startwithflow);

        processIC(servercontext.getEndInterceptors());

        if (spdoc != null) {
            if (spdoc.getPagename() == null) {
                spdoc.setPagename(currentpagerequest.getRootName());
            }

            if (currentpageflow != null) {
                // TODO: rename "pageflow" to "lastflow" and search for consequences...
                spdoc.setProperty("pageflow", currentpageflow.getRootName());
                addPageFlowInfo(spdoc);
            } else if (lastflow != null) {
                spdoc.setProperty("pageflow", lastflow.getRootName());
            }

            Variant var = getVariant();
            if (var != null) {
                spdoc.setVariant(var);
                spdoc.getDocument().getDocumentElement().setAttribute("requested-variant", var.getVariantId());
                if (currentpagerequest != null) spdoc.getDocument().getDocumentElement().setAttribute("used-pr", currentpagerequest.getName());
                if (currentpageflow != null) spdoc.getDocument().getDocumentElement().setAttribute("used-pf", currentpageflow.getName());
            }

            if (spdoc.getResponseError() == 0 && parentcontext.getContextConfig().getPageRequestConfig(spdoc.getPagename()) != null) {
                parentcontext.addVisitedPage(spdoc.getPagename());
            }

            LOG.debug("\n");
            insertPageMessages(spdoc);
            storeCookies(spdoc);
            spdoc.setProperty(ContextXMLServlet.XSLPARAM_REQUESTCONTEXT, this);
        }

        return spdoc;
    }

    private void insertPageMessages(SPDocument spdoc) {
        if (spdoc == null) return;

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
                    Element inc = ResultDocument.createIncludeFromStatusCode(doc, servercontext.getContextConfig().getProperties(), sci
                            .getStatusCode(), sci.getArgs());
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

    private void addPageFlowInfo(SPDocument spdoc) {
        if (currentpageflow != null) {
            Document doc = spdoc.getDocument();
            Element root = doc.createElement("pageflow");
            doc.getDocumentElement().appendChild(root);
            root.setAttribute("name", currentpageflow.getRootName());
            currentpageflow.addPageFlowInfo(root, this.parentcontext);
        }
    }

    private void processIC(ContextInterceptor[] icarr) {
        if (icarr != null) {
            for (int i = 0; i < icarr.length; i++) {
                icarr[i].process(parentcontext, currentpservreq);
            }
        }
    }

    public PageRequest createPageRequest(String pagename) {
        Variant var = getVariant();
        if (var != null && var.getVariantFallbackArray() != null && variantmanager != null) {
            return new PageRequest(variantmanager.getVariantMatchingPageRequestName(pagename, var));
        } else {
            return new PageRequest(pagename);
        }
    }

    public PageRequestConfig getConfigForCurrentPageRequest() {
        if (currentpagerequest == null) {
            throw new IllegalStateException("PageRequest is only available witihin request handling");
        }
        return servercontext.getContextConfig().getPageRequestConfig(currentpagerequest.getName());
    }

    private SPDocument documentFromFlow(boolean startwithflow) throws PustefixApplicationException, PustefixCoreException {
        SPDocument document = null;

        // First, check if the requested page is defined at all
        // We do this only if the current pagerequest is not the special
        // STARTWITHFLOW_PAGE
        // because then we don't know yet which page to use.

        if (!startwithflow) {
            ResultDocument resdoc;

            State state = pagemap.getState(currentpagerequest);
            if (state == null) {
                LOG.warn("*** Can't get a handling state for page " + currentpagerequest);
                LOG.warn("    ...will continue and use the default state '" + parentcontext.getContextConfig().getDefaultState().getName() + "'");
            }

            // Now, check for possibly needed authorization
            RequestParam sdreq = currentpservreq.getRequestParam(State.SENDAUTHDATA);
            boolean forceauth = (sdreq != null && sdreq.isTrue());

            document = checkAuthorization(forceauth);
            if (document != null) {
                return document;
            }

            // Now we need to make sure that the current page is accessible, and
            // take the right measures if not.
            if (!checkIsAccessible(currentpagerequest, PageRequestStatus.SELECT)) {
                LOG.warn("[" + currentpagerequest + "]: not accessible! Trying default page.");
                String defpage = parentcontext.getContextConfig().getDefaultPage();
                currentpagerequest = createPageRequest(defpage);
                currentpageflow = pageflowmanager.pageFlowToPageRequest(currentpageflow, currentpagerequest, variant, parentcontext);
                if (!checkIsAccessible(currentpagerequest, PageRequestStatus.SELECT)) {
                    throw new PustefixCoreException("Even default page [" + defpage + "] was not accessible! Bailing out.");
                }
            }

            resdoc = documentFromCurrentStep();
            if (currentpageflow != null) {
                currentpageflow.hookAfterRequest(resdoc, parentcontext);
                currentpageflow = pageflowmanager.getPageFlowByName(currentpageflow.getRootName(), getVariant());
            }

            if (prohibitcontinue) {
                LOG.debug("* [" + currentpagerequest + "] returned document to show, skipping page flow.");
                document = resdoc.getSPDocument();
            } else if (jumptopage != null) {
                LOG.debug("* [" + currentpagerequest + "] signalled success, jumptopage is set as [" + jumptopage + "].");
                currentpagerequest = createPageRequest(jumptopage);
                currentpagerequest.setStatus(PageRequestStatus.JUMP);
                if (jumptopageflow != null) {
                    setCurrentPageFlow(jumptopageflow);
                } else {
                    currentpageflow = pageflowmanager.pageFlowToPageRequest(currentpageflow, currentpagerequest, getVariant(), parentcontext);
                }
                jumptopage = null; // we don't want to recurse infinitely
                jumptopageflow = null; // we don't want to recurse infinitely
                
                // on_jumptopage = true; // we need this information to supress
                // the interpretation of
                // the request as one that submits data. See StateImpl,
                // methods isSubmitTrigger & isDirectTrigger

                LOG.debug("******* JUMPING to [" + currentpagerequest + "] *******\n");
                document = documentFromFlow(false);
            } else if (currentpageflow != null) {
                LOG.debug("* [" + currentpagerequest + "] signalled success, starting page flow process");
                document = runPageFlow(false);
            } else {
                // throw new XMLException("*** ERROR! *** [" +
                // currentpagerequest + "] signalled success, but current page
                // flow == null!");
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

    private SPDocument runPageFlow(boolean startwithflow) throws PustefixApplicationException, PustefixCoreException {
        ResultDocument resdoc = null;
        // We need to re-check the authorization because the just handled submit
        // could have changed the authorisation status.
        SPDocument document = checkAuthorization(false);
        if (document != null) {
            return document;
        }

        String nextPage = currentpageflow.findNextPage(this.parentcontext, startwithflow);
        assert(nextPage != null);
        
        currentpagerequest = createPageRequest(nextPage);
        currentpagerequest.setStatus(PageRequestStatus.WORKFLOW);
        resdoc = documentFromCurrentStep();
        document = resdoc.getSPDocument();
        if (document == null) {
            throw new PustefixCoreException("*** FATAL: [" + currentpagerequest + "] returns a 'null' SPDocument! ***");
        }
        LOG.debug("* [" + currentpagerequest + "] returned document => show it.");
        return document;
    }

    public SPDocument checkAuthorization(boolean forceauth) throws PustefixApplicationException, PustefixCoreException {
        return checkAuthorization(forceauth, true);
    }

    public SPDocument checkAuthorization(boolean forceauth, boolean needDocument) throws PustefixApplicationException, PustefixCoreException {
        // The needDocument flag is a hack that should only be used internally:
        // It is needed, when this method is called only to see, whether
        // the context is authenticated, but the creation of SPDocument
        // for the login page would lead to an exception being thrown.
        if (authpage != null) {
            ResultDocument resdoc = null;
            LOG.debug("===> [" + authpage + "]: Checking authorisation");
            if (!checkIsAccessible(authpage, PageRequestStatus.AUTH)) {
                throw new PustefixCoreException("*** Authorisation page [" + authpage + "] is not accessible! ***");
            }
            if (forceauth) {
                LOG.debug("* [" + currentpagerequest + "] forceauth is TRUE ***");
            }
            PageRequest saved = currentpagerequest;
            if (checkNeedsData(authpage, PageRequestStatus.AUTH) || forceauth) {
                if (!needDocument) {
                    // Return an empty SPDocument that is just used to
                    // signal the context is not authenticated yet.
                    return new SPDocument();
                }
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
                    throw new PustefixCoreException("*** FATAL: " + authpage + " returns a 'null' SPDocument! ***");
                }
                resdoc.getSPDocument().getDocument().getDocumentElement().setAttribute("authoriginalpage", saved.getRootName());
                resdoc.getSPDocument().setPagename(authpage.getName());
                return resdoc.getSPDocument();
            }
        }
        return null;
    }

    private ResultDocument documentFromCurrentStep() throws PustefixApplicationException, PustefixCoreException {

        ResultDocument document = null;
        document = checkPageAuthorization();
        if (document != null) return document;

        State state = getStateForPageRequest(currentpagerequest);

        LOG.debug("** [" + currentpagerequest + "]: associated state: " + state.getClass().getName());
        LOG.debug("=> [" + currentpagerequest + "]: Calling getDocument()");

        try {
            ResultDocument resdoc = state.getDocument(parentcontext, currentpservreq);
            // TODO: find better place and only insert if authorization failed
            if (resdoc != null && roleAuth) {
                addAuthenticationData(resdoc);
            }
            return resdoc;
        } catch (Exception e) {
            throw new PustefixApplicationException("Exception while running getDocument() for page " + currentpagerequest.getName(), e);
        }
    }

    public void checkAuthorization(Context context) {
        String pageName = currentpagerequest.getRootName();
        PageRequestConfig pageConfig = this.getConfigForCurrentPageRequest();
        AuthConstraint authConstraint = pageConfig.getAuthConstraint();
        if (authConstraint == null) authConstraint = parentcontext.getContextConfig().getDefaultAuthConstraint();
        if (authConstraint != null) {
            if (!authConstraint.isAuthorized(parentcontext)) {
                if (LOG.isDebugEnabled()) LOG.debug("Not authorized to access page '" + pageName + "'");
                throw new AuthConstraintViolation("Not authorized to access page '" + pageName + "'.", "pageaccess", pageName, authConstraint);
            }
        }
    }

    private ResultDocument checkPageAuthorization() throws PustefixApplicationException, PustefixCoreException {
        if (!parentcontext.getContextConfig().hasRoles()) return null;
        try {
            checkAuthorization(parentcontext);
        } catch (AuthorizationException authEx) {
            String targetPage = authEx.getTarget();
            if (roleAuthDeps != null && roleAuthDeps.contains(targetPage)) {
                StringBuilder sb = new StringBuilder();
                for (String s : roleAuthDeps)
                    sb.append(s + " -> ");
                sb.append(targetPage);
                throw new PustefixCoreException("Authorization page has circular dependencies: " + sb.toString());
            }
            if (roleAuthDeps == null) roleAuthDeps = new LinkedHashSet<String>();
            roleAuthDeps.add(authEx.getTarget());
            PageRequest localAuthPage = null;
            PageRequestConfig pageConfig = this.getConfigForCurrentPageRequest();
            AuthConstraint authConstraint = pageConfig.getAuthConstraint();
            if (authConstraint == null) authConstraint = parentcontext.getContextConfig().getDefaultAuthConstraint();
            if (authConstraint != null) {
                String authPageName = authConstraint.getAuthPage();
                if (authPageName != null) localAuthPage = createPageRequest(authPageName);
            }
            if (localAuthPage == null) throw authEx;
            if (localAuthPage.equals(authpage))
                throw new PustefixCoreException("Authconstraint authpage isn't " + "allowed to be equal to context authpage: " + authpage);
            PageRequest saved = currentpagerequest;
            if (LOG.isDebugEnabled()) LOG.debug("===> [" + localAuthPage + "]: Checking authorisation");
            if (!checkIsAccessible(localAuthPage, PageRequestStatus.AUTH)) {
                throw new PustefixCoreException("*** Authorisation page [" + localAuthPage + "] is not accessible! ***");
            }
            if (LOG.isDebugEnabled()) LOG.debug("===> [" + localAuthPage + "]: Need authorisation data");
            currentpagerequest = localAuthPage;
            ResultDocument resdoc = documentFromCurrentStep();
            currentpagerequest = saved;
            prohibitcontinue = true;
            if (resdoc != null && prohibitcontinue) {
                if (resdoc.getSPDocument() == null) {
                    throw new PustefixCoreException("*** FATAL: " + localAuthPage + " returns a 'null' SPDocument! ***");
                }
                resdoc.getSPDocument().getDocument().getDocumentElement().setAttribute("authoriginalpage", saved.getRootName());
                resdoc.getSPDocument().setPagename(localAuthPage.getName());
                Element authElem = addAuthenticationData(resdoc);
                if (authElem != null) {
                    authElem.setAttribute("targetpage", authEx.getTarget());
                    Element misElem = resdoc.createSubNode(authElem, "authorizationfailure");
                    if (authEx.getType() != null) misElem.setAttribute("type", authEx.getType());
                    if (authEx.getAuthorization() != null) misElem.setAttribute("authorization", authEx.getAuthorization());
                    if (authEx.getTarget() != null) misElem.setAttribute("target", authEx.getTarget());
                    if (authEx instanceof AuthConstraintViolation) {
                        AuthConstraintViolation authVio = (AuthConstraintViolation) authEx;
                        AuthConstraint authCon = authVio.getViolatedConstraint();
                        if (authCon != null) {
                            Element constraintElem = authCon.toXML(misElem.getOwnerDocument());
                            misElem.appendChild(constraintElem);
                        }
                    }
                }
                return resdoc;
            }
        }
        return null;
    }

    private Element addAuthenticationData(ResultDocument resDoc) {
        if (parentcontext.getAuthentication() != null) {
            Element root = resDoc.getRootElement();
            Element authElem = resDoc.createNode("authentication");
            if (root.getFirstChild() != null) root.insertBefore(authElem, root.getFirstChild());
            else root.appendChild(authElem);
            if (roleAuthTarget != null) authElem.setAttribute("targetpage", roleAuthTarget);
            Role[] roles = parentcontext.getAuthentication().getRoles();
            Element rolesElem = resDoc.createSubNode(authElem, "roles");
            if (roles != null) for (Role role : roles) {
                Element roleElem = resDoc.createSubNode(rolesElem, "role");
                roleElem.setAttribute("name", role.getName());
            }
            return authElem;
        }
        return null;
    }

    public void forceStopAtNextStep(boolean forcestop) {
        this.stopnextforcurrentrequest = forcestop;
    }

    public boolean checkNeedsData(PageRequest page, PageRequestStatus status) throws PustefixApplicationException {
        PageRequest saved = currentpagerequest;
        currentpagerequest = page;
        State state = getStateForPageRequest(page);
        page.setStatus(status);

        PerfEvent pe = new PerfEvent(PerfEventType.PAGE_NEEDSDATA, page.getName());
        pe.start();
        boolean retval;
        try {
            retval = state.needsData(parentcontext, currentpservreq);
        } catch (Exception e) {
            throw new PustefixApplicationException("Exception while running needsData() for page " + page.getName(), e);
        }
        pe.save();

        currentpagerequest = saved;
        return retval;
    }

    public boolean checkIsAccessible(PageRequest page, PageRequestStatus status) throws PustefixApplicationException {
        PageRequest saved = currentpagerequest;
        try {
            currentpagerequest = page;
            State state = getStateForPageRequest(page);
            page.setStatus(status);

            PerfEvent pe = new PerfEvent(PerfEventType.PAGE_ISACCESSIBLE, page.getName());
            pe.start();
            boolean retval;
            try {
                retval = state.isAccessible(parentcontext, currentpservreq);
            } catch (Exception e) {
                throw new PustefixApplicationException("Got exception from state for page " + page.getName() + " while calling isAccessible()", e);
            }
            pe.save();

            return retval;
        } finally {
            currentpagerequest = saved;
        }
    }

    private void checkForAuthenticationMode() {
        String authpagename = servercontext.getContextConfig().getAuthPage();
        if (authpagename != null) {
            authpage = createPageRequest(authpagename);
        } else {
            authpage = null;
        }
    }

    public boolean isPageAccessible(String pagename) throws Exception {
        PageRequest page = createPageRequest(pagename);
        Variant currentvariant = getVariant();
        setVariantForThisRequestOnly(parentcontext.getSessionVariant());
        boolean retval = checkIsAccessible(page, PageRequestStatus.SELECT);
        setVariantForThisRequestOnly(currentvariant);
        return retval;
    }

    public Cookie[] getRequestCookies() {
        if (currentpservreq == null) {
            throw new IllegalStateException("Cookies are only available witihin request handling");
        }
        return currentpservreq.getCookies();
    }

    public void addCookie(Cookie cookie) {
        if (currentpservreq == null) {
            throw new IllegalStateException("Cookies are only available witihin request handling");
        }
        cookielist.add(cookie);
    }

    public Throwable getLastException() {
        if (currentpservreq == null) {
            throw new IllegalStateException("This method is only available during request processing");
        }
        return currentpservreq.getLastException();
    }

    public String toString() {
        StringBuffer contextbuf = new StringBuffer("\n");

        contextbuf.append("     pageflow:      " + currentpageflow + "\n");
        contextbuf.append("     PageRequest:   " + currentpagerequest + "\n");
        if (currentpagerequest != null) {
            contextbuf.append("       -> State: " + getStateForPageRequest(currentpagerequest) + "\n");
            contextbuf.append("       -> Status: " + currentpagerequest.getStatus() + "\n");
        }

        return contextbuf.toString();
    }

    public Object clone() throws CloneNotSupportedException {
        RequestContextImpl copy = (RequestContextImpl) super.clone();
        if (currentpagerequest != null) {
            copy.currentpagerequest = new PageRequest(currentpagerequest.getName());
            copy.currentpagerequest.setStatus(currentpagerequest.getStatus());
        }
        if (authpage != null) {
            copy.authpage = new PageRequest(authpage.getName());
            copy.authpage.setStatus(authpage.getStatus());
        }
        copy.cookielist = new ArrayList<Cookie>(cookielist);
        copy.messages = new HashSet<StatusCodeInfo>(messages);
        return copy;
    }

    public void setPfixServletRequest(PfixServletRequest pservreq) {
        // Usually the PfixServletRequest is supplied when
        // calling handleSubmittedData(), however there might
        // be situations when though there is no request to
        // handle a PfixServletRequest is needed.
        this.currentpservreq = pservreq;
    }

    public ContextImpl getParentContext() {
        return this.parentcontext;
    }

    public PfixServletRequest getPfixServletRequest() {
        return currentpservreq;
    }

    public boolean isForceStopAtNextStep() {
        return stopnextforcurrentrequest;
    }
    
    
    private State getStateForPageRequest(PageRequest page) {
        State state = pagemap.getState(page);
        if (state == null) {
            Class<? extends State> clazz = parentcontext.getContextConfig().getDefaultState();
            state = StateFactory.getInstance().getState(clazz.getName());
        }
        return state;
    }

}
