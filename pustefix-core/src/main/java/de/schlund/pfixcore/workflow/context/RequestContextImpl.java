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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.pustefixframework.config.contextxmlservice.PageRequestConfig;
import org.pustefixframework.config.contextxmlservice.PreserveParams;
import org.pustefixframework.config.contextxmlservice.ProcessActionPageRequestConfig;
import org.pustefixframework.http.AbstractPustefixRequestHandler;
import org.pustefixframework.http.AbstractPustefixXMLRequestHandler;
import org.pustefixframework.http.PustefixContextXMLRequestHandler;
import org.pustefixframework.util.LogUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.schlund.pfixcore.auth.AuthConstraint;
import de.schlund.pfixcore.auth.AuthConstraintViolation;
import de.schlund.pfixcore.auth.AuthorizationException;
import de.schlund.pfixcore.auth.AuthorizationInterceptor;
import de.schlund.pfixcore.auth.Role;
import de.schlund.pfixcore.exception.PustefixApplicationException;
import de.schlund.pfixcore.exception.PustefixCoreException;
import de.schlund.pfixcore.exception.PustefixRuntimeException;
import de.schlund.pfixcore.generator.StatusCodeInfo;
import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixcore.workflow.ContextImpl;
import de.schlund.pfixcore.workflow.ContextInterceptor;
import de.schlund.pfixcore.workflow.PageMap;
import de.schlund.pfixcore.workflow.PageRequest;
import de.schlund.pfixcore.workflow.PageRequestStatus;
import de.schlund.pfixcore.workflow.State;
import de.schlund.pfixcore.workflow.VariantManager;
import de.schlund.pfixcore.workflow.SiteMap.PageLookupResult;
import de.schlund.pfixxml.PfixServletRequest;
import de.schlund.pfixxml.RequestParam;
import de.schlund.pfixxml.ResultDocument;
import de.schlund.pfixxml.SPDocument;
import de.schlund.pfixxml.Variant;
import de.schlund.pfixxml.config.EnvironmentProperties;
import de.schlund.util.statuscodes.StatusCode;

/**
 * Implementation of the request part of the context used by ContextXMLServlet,
 * DirectOutputServlet and WebServiceServlet. This class should never be
 * directly used by application developers.
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class RequestContextImpl implements Cloneable, AuthorizationInterceptor {

    private final static Logger LOG                 = Logger.getLogger(ContextImpl.class);
    public final static String  PARAM_ACTION        = "__action";
    private final static String PARAM_JUMPPAGE      = "__jumptopage";
    private final static String PARAM_JUMPPAGEFLOW  = "__jumptopageflow";
    private final static String PARAM_FLOW          = "__pageflow";
    private final static String PARAM_LASTFLOW      = "__lf";
    private final static String PARAM_STARTWITHFLOW = "__startwithflow";
    private final static String PARAM_FORCESTOP     = "__forcestop";
    private final static String PARAM_ROLEAUTH      = "__sendingauthdata";

    private ContextImpl         parentcontext;
    private ServerContextImpl   servercontext;
    private PageFlowManager     pageflowmanager;
    private VariantManager      variantmanager;
    private PageMap             pagemap;

    private Variant             variant             = null;
    private String              language            = null;
    private String              pageAlternativeKey;

    private PageRequest         currentpagerequest  = null;
    private PageFlow            currentpageflow     = null;
    private PfixServletRequest  currentpservreq     = null;
    private PageRequestStatus   currentstatus       = PageRequestStatus.UNDEF;

    private String              jumptopage          = null;
    private String              jumptopageflow      = null;
    private boolean             prohibitcontinue    = false;

    private boolean             roleAuth;
    private String              roleAuthTarget;
    private Set<String>         roleAuthDeps;

    private Set<StatusCodeInfo> messages            = new HashSet<StatusCodeInfo>();
    private List<Cookie>        cookielist          = new ArrayList<Cookie>();

    public RequestContextImpl(ServerContextImpl servercontext, ContextImpl context) {
        this.parentcontext = context;
        this.servercontext = servercontext;
        this.pageflowmanager = servercontext.getPageFlowManager();
        this.variantmanager = servercontext.getVariantManager();
        this.pagemap = servercontext.getPageMap();

        this.variant = parentcontext.getSessionVariant();
        this.language = parentcontext.getSessionLanguage();

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
        // Return empty Properties object instead of null because we need
        // this to handle not explicitly declared pages
        return new Properties();
    }

    public PageRequest getCurrentPageRequest() {
        return currentpagerequest;
    }

    public PageRequestStatus getCurrentStatus() {
        return currentstatus;
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

    public void setJumpToPage(String pagename) {
        if (currentpservreq == null) {
            throw new IllegalStateException("JumpToPage is only available witihin request handling");
        }
        jumptopage = pagename;
    }

    public boolean isJumpToPageSet() {
        return (jumptopage != null);
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
        return (jumptopageflow != null);
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

        if (currentpageflow == null) {
            throw new RuntimeException("*** no current pageflow is set");
        }

        if (!currentpageflow.containsPage(currentpagerequest.getRootName())) {
            throw new RuntimeException("*** current pageflow " + currentpageflow.getName() + " does not contain current pagerequest "
                    + currentpagerequest);
        }

        return currentpageflow.precedingFlowNeedsData(this.parentcontext, currentpagerequest.getRootName());
    }

    //    public boolean isCurrentPageRequestInCurrentFlow() {
    //        if (currentpservreq == null) {
    //            throw new IllegalStateException("PageFlow information is only availabe during request handling");
    //        }
    //        return (currentpageflow != null && currentpageflow.containsPage(currentpagerequest.getRootName()));
    //    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String lang) {
        language = lang;
    }
    
    public void setPageAlternative(String key) {
        pageAlternativeKey = key;
    }
    
    public String getPageAlternative() {
        return pageAlternativeKey;
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
        if (currentpageflow != null && currentpageflow.hasHookAfterRequest(currentpagerequest.getRootName())) {
            // We need the full doc for the post processing no matter what else
            // happens.
            return true;
        }
        if (jumptopageflow != null) {
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

    public void addPageMessage(StatusCode scode, String[] args, String level) {
        if (currentpservreq == null) {
            throw new IllegalStateException("PageMessages are only availabe during request handling");
        }
        if (scode == null)
            return;
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
            // Make sure connection is switched to SSL if current page is marked as "secure"
            int port = AbstractPustefixRequestHandler.getSSLRedirectPort(preq.getOriginalServerPort(), getServerContext().getProperties());
            String sessionIdPath = "";
            HttpSession session = preq.getSession(false);
            if(session.getAttribute(AbstractPustefixRequestHandler.SESSION_ATTR_COOKIE_SESSION) == null) {
                sessionIdPath = ";jsessionid=" + session.getId();
            }
            String redirectURL = "https://" + AbstractPustefixRequestHandler.getServerName(preq.getRequest()) 
                    + ( port == 443 ? "" : ":" + port ) + preq.getContextPath()
                    + preq.getServletPath() + "/" + spdoc.getPagename() + sessionIdPath + "?__reuse="
                    + spdoc.getTimestamp();
            PreserveParams preserveParams = parentcontext.getContextConfig().getPreserveParams();
            for(String paramName: preq.getRequestParamNames()) {
                if(preserveParams.containsParam(paramName)) {
                    RequestParam rp = preq.getRequestParam(paramName);
                    if (rp != null && rp.getValue() != null && !rp.getValue().equals("")) {
                        redirectURL += "&" + paramName + "=" + rp.getValue();
                    }
                }
            }
            if(preq.getRequestParam(PARAM_LASTFLOW) == null && currentpageflow != null &&
                    needsLastFlow(spdoc.getPagename(), currentpageflow.getRootName())) {
                redirectURL += "&__lf=" + currentpageflow.getRootName();
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
        jumptopage = null;
        jumptopageflow = null;
        roleAuthDeps = null;
        currentstatus = PageRequestStatus.SELECT;

        processIC(parentcontext.getContextConfig().getStartInterceptors());
        
        RequestParam swflow = currentpservreq.getRequestParam(PARAM_STARTWITHFLOW);
        boolean startwithflow = false;
        if (swflow != null && swflow.getValue().equals("true")) {
            startwithflow = true;
        }

        String tmppagename = currentpservreq.getPageName();
        if (tmppagename != null) {
            currentpagerequest = createPageRequest(tmppagename);
        } else {
            String defaultPage = parentcontext.getContextConfig().getDefaultPage(variant);
            if(defaultPage != null) {
                currentpagerequest = createPageRequest(defaultPage);
            } else throw new RuntimeException("No defaultpage found!");
        }
        RequestParam reqParam = currentpservreq.getRequestParam(PARAM_ROLEAUTH);
        roleAuth = (reqParam != null && reqParam.isTrue());
        if (roleAuth) {
            roleAuthTarget = currentpagerequest.getName();
            AuthConstraint authConst = null;
            PageRequestConfig targetPageConf = servercontext.getContextConfig().getPageRequestConfig(roleAuthTarget);
            if (targetPageConf != null) authConst = targetPageConf.getAuthConstraint();
            if (authConst == null)
                authConst = getParentContext().getContextConfig().getDefaultAuthConstraint();
            if (authConst != null) {
                String authPageName = authConst.getAuthPage(parentcontext);
                if (authPageName != null) {
                    currentpagerequest = createPageRequest(authPageName);
                    if (!roleAuthTarget.equals(authPageName)) {
                        if(authConst.getAuthJump()) {
                            setJumpToPage(roleAuthTarget);
                        }
                        parentcontext.getAuthentication().setAuthTarget(roleAuthTarget);
                    }
                } else
                    throw new RuntimeException("No authpage defined for authconstraint " + "of page: " + roleAuthTarget);
            } else
                throw new RuntimeException("No authconstraint defined for page: " + roleAuthTarget);
        }

        // action lookup
        ProcessActionPageRequestConfig action = null;
        RequestParam actionname = preq.getRequestParam(RequestContextImpl.PARAM_ACTION);
        if (actionname != null && !actionname.getValue().equals("")) {
            LOG.debug("======> Found __action parameter " + actionname);
            PageRequestConfig pageConf = getConfigForCurrentPageRequest();
            if(pageConf != null) {
                Map<String, ? extends ProcessActionPageRequestConfig> actionmap = pageConf.getProcessActions();
                if (actionmap != null) {
                    action = actionmap.get(actionname.getValue());
                    if (action != null) {
                        LOG.debug("        ...and found matching ProcessAction: " + action);
                    }
                }
                if (action == null) {
                    LOG.warn("Page '" + currentpagerequest.getName() + "' has been called with unknown action.");
                }
            } else {
                LOG.warn("Page " + currentpagerequest.getName() + " has been called with action, but isn't configured.");
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
                if (ac_jumptopageflow != null) {
                    setJumpToPageFlow(ac_jumptopageflow);
                }
            }
        }

        // forcestop handling
        boolean stopnextforcurrentrequest = false;
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

        PageFlow lastFlow = null;
        PageFlow lastFlowFromSession = null;
        if(parentcontext.getLastFlow() != null) {
            lastFlowFromSession = pageflowmanager.getPageFlowByName(parentcontext.getLastFlow(), getVariant());
        }
        if(!parentcontext.getContextConfig().getPageFlowPassThrough()) {
            lastFlow = lastFlowFromSession;
        } else {
            if (currentpservreq.getRequestParam(PARAM_LASTFLOW) != null) {
                lastFlow = pageflowmanager.getPageFlowByName(currentpservreq.getRequestParam(PARAM_LASTFLOW).getValue(), getVariant());
            }
        }
        
        RequestParam pageflow = currentpservreq.getRequestParam(PARAM_FLOW);
        if (pageflow != null && !pageflow.getValue().equals("") && pageflowmanager.getPageFlowByName(pageflow.getValue(), getVariant()) != null) {
            currentpageflow = pageflowmanager.getPageFlowByName(pageflow.getValue(), getVariant());
            LOG.debug("===> Got pageflow from request parameter as [" + currentpageflow.getName() + "]");
        } else if (action != null && action.getPageflow() != null && pageflowmanager.getPageFlowByName(action.getPageflow(), getVariant()) != null) {
            currentpageflow = pageflowmanager.getPageFlowByName(action.getPageflow(), getVariant());
            LOG.debug("===> Got pageflow from action [" + action.getName() + "] as [" + currentpageflow.getName() + "]");
        } else {
            LOG.debug("===> Searching matching pageflow to page [" + currentpagerequest.getName() + "]...");
            if (lastFlow != null) {
                LOG.debug("     ...prefering flow [" + lastFlow.getName() + "]...");
            }
            currentpageflow = pageflowmanager.pageFlowToPageRequest(lastFlow, currentpagerequest, getVariant());
            if (currentpageflow != null) {
                LOG.debug("     ...got pageflow [" + currentpageflow.getName() + "] as matching flow.");
            } else {
                LOG.debug("     ...got no matching pageflow for page [" + currentpagerequest.getName() + "]");
            }
            if(parentcontext.getContextConfig().getPageFlowPassThrough()) {
                PageFlow flowBySession = pageflowmanager.pageFlowToPageRequest(lastFlowFromSession, currentpagerequest, getVariant());
                if( !( ( currentpageflow == null && flowBySession == null ) || 
                       ( currentpageflow != null && flowBySession != null && currentpageflow.getRootName().equals(flowBySession.getRootName())) ) ) {
               
                    LOG.warn("PAGEFLOW_PASSTHROUGH_DIFF|" + currentpagerequest.getName() + "|"
                       + ( currentpageflow == null ? "-" : currentpageflow.getName() ) + "|" 
                       + ( flowBySession == null ? "-" : flowBySession.getName() ) + "|"
                       + LogUtils.makeLogSafe(preq.getRequest().getHeader("Referer")) );
                }
            }
        }
        SPDocument spdoc = null;
        if(currentpservreq.getRequestParam(AbstractPustefixXMLRequestHandler.PARAM_RENDER_HREF) != null) {
            if(checkPageAuthorization() == null && checkIsAccessible(currentpagerequest)) {
                spdoc = documentFromCurrentStep().getSPDocument();
            } else {
                spdoc = new SPDocument();
                spdoc.setResponseError(403);
                spdoc.setResponseErrorText("Page is not accessible");
                LOG.warn("Can't get result document for render part because page '" + 
                            currentpagerequest.getName() + "' isn't accessible.");
            }
        } else if(currentpservreq.getRequestParam(AbstractPustefixXMLRequestHandler.PARAM_STATIC_DOM) != null
                && !"prod".equals(EnvironmentProperties.getProperties().getProperty("mode"))) {
            try {
                spdoc = parentcontext.getContextConfig().getDefaultState().getDocument(parentcontext, currentpservreq).getSPDocument();
            } catch(Exception x) {
                throw new PustefixCoreException("Error getting SPDocument from default State", x);
            }
        } else {
            ResultDocument res = checkPageAuthorization();
            if(res != null) {
                if(roleAuth) {
                    addAuthenticationData(res);
                }
                spdoc = res.getSPDocument();
            } else {
                spdoc = documentFromFlow(startwithflow, stopnextforcurrentrequest);
            }
        }
        
        processIC(parentcontext.getContextConfig().getEndInterceptors());
        
        if (spdoc != null) {
            if (spdoc.getPagename() == null) {
                spdoc.setPagename(currentpagerequest.getRootName());
            }

            if (currentpageflow != null) {
                spdoc.setProperty(PARAM_LASTFLOW, currentpageflow.getRootName());
                spdoc.setProperty("pageflow", currentpageflow.getRootName());
                addPageFlowInfo(spdoc);
                parentcontext.setLastFlow(currentpageflow.getRootName());
            } else if (lastFlow != null) {
                spdoc.setProperty(PARAM_LASTFLOW, lastFlow.getRootName());
            }

            Variant var = getVariant();
            if (var != null) {
                spdoc.setVariant(var);
                if(spdoc.getDocument() != null) {
                    spdoc.getDocument().getDocumentElement().setAttribute("requested-variant", var.getVariantId());
                    if (currentpagerequest != null)
                        spdoc.getDocument().getDocumentElement().setAttribute("used-pr", currentpagerequest.getName());
                    if (currentpageflow != null)
                        spdoc.getDocument().getDocumentElement().setAttribute("used-pf", currentpageflow.getName());
                }
            }
            if(parentcontext.getTenant() != null) {
                spdoc.setTenant(parentcontext.getTenant());
                parentcontext.getTenant().toXML(spdoc.getDocument().getDocumentElement());
            }
            if(language != null) {
                spdoc.setLanguage(language);
            }
            if(pageAlternativeKey != null) {
                spdoc.setPageAlternative(getPageAlternative());
            }
            
            if (spdoc.getResponseError() == 0 && parentcontext.getContextConfig().getPageRequestConfig(spdoc.getPagename()) != null) {
                parentcontext.addVisitedPage(spdoc.getPagename());
            }

            LOG.debug("\n");
            insertPageMessages(spdoc);
            storeCookies(spdoc);
            spdoc.setProperty(PustefixContextXMLRequestHandler.XSLPARAM_REQUESTCONTEXT, this);
        }

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
            if(doc != null) {
                Element root = doc.createElement("pageflow");
                doc.getDocumentElement().appendChild(root);
                root.setAttribute("name", currentpageflow.getRootName());
                currentpageflow.addPageFlowInfo(currentpagerequest.getRootName(), root);
            }
        }
    }

    private void processIC(Collection<? extends ContextInterceptor> interceptors) {
        if (interceptors != null) {
            for (ContextInterceptor interceptor : interceptors) {
                interceptor.process(parentcontext, currentpservreq);
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
    
    public State getStateForCurrentPageRequest() throws PustefixApplicationException {
        if (currentpagerequest == null) {
            throw new IllegalStateException("PageRequest is only available witihin request handling");
        }
        return getStateForPageRequest(currentpagerequest);
    }
    
    private SPDocument documentFromFlow(boolean startwithflow, boolean stopnextforcurrentrequest) throws PustefixApplicationException, PustefixCoreException {
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
                LOG.warn("    ...will continue and use the default state '" + parentcontext.getContextConfig().getDefaultStateType().getName() + "'");
            }

            // Now we need to make sure that the current page is accessible, and
            // take the right measures if not.
            if (!checkIsAccessible(currentpagerequest)) {
                LOG.warn("[" + currentpagerequest + "]: Page is not accessible...");
                boolean foundNext = false;
                if (currentpageflow != null) {
                    LOG.warn("[" + currentpagerequest + "]: ...but trying to find an accessible page from the current page flow [" 
                             + currentpageflow.getName() + "]");
                    PageRequestStatus saved = currentstatus;
                    currentstatus = PageRequestStatus.WORKFLOW;
                    String nextPage = currentpageflow.findNextPage(this.parentcontext, currentpagerequest.getRootName(), false, stopnextforcurrentrequest);
                    if(nextPage != null) {
                        currentpagerequest = createPageRequest(nextPage);
                        foundNext = true;
                    }
                    currentstatus = saved;
                }
                if(!foundNext) {
                    String defpage = parentcontext.getContextConfig().getDefaultPage(variant);
                    LOG.warn("[" + currentpagerequest + "]: ...but trying to use the default page " + defpage); 
                    currentpagerequest = createPageRequest(defpage);
                    if (!checkIsAccessible(currentpagerequest)) {
                        LOG.warn("[" + currentpagerequest + "]: Page is not accessible...");
                        currentpageflow = pageflowmanager.pageFlowToPageRequest(currentpageflow, currentpagerequest, variant);
                        if(currentpageflow != null) {
                            LOG.warn("[" + currentpagerequest + "]: ...but trying to find an accessible page from the page flow of the default page [" 
                                    + currentpageflow.getName() + "]");
                            PageRequestStatus saved = currentstatus;
                            currentstatus = PageRequestStatus.WORKFLOW;
                            String nextPage = currentpageflow.findNextPage(this.parentcontext, currentpagerequest.getRootName(), false, stopnextforcurrentrequest);
                            if(nextPage != null) {
                                currentpagerequest = createPageRequest(nextPage);
                                foundNext = true;
                            }
                            currentstatus = saved;
                        }
                        if(!foundNext) {
                            throw new PustefixCoreException("Even default page [" + defpage + "] or other page of its flow  was not accessible! Bailing out.");
                        }
                    }
                }
            }

            resdoc = documentFromCurrentStep();
            if (currentpageflow != null) {
                currentpageflow.hookAfterRequest(parentcontext, resdoc);
                currentpageflow = pageflowmanager.getPageFlowByName(currentpageflow.getRootName(), getVariant());
            }

            if (prohibitcontinue) {
                LOG.debug("* [" + currentpagerequest + "] returned document to show, skipping page flow.");
                document = resdoc.getSPDocument();
            } else if (jumptopage != null) {
                document = doJump(stopnextforcurrentrequest);
            } else if (currentpageflow != null) {
                LOG.debug("* [" + currentpagerequest + "] signalled success, starting page flow process");
                document = runPageFlow(false, stopnextforcurrentrequest);
            } else {
                LOG.debug("* [" + currentpagerequest + "] signalled success, but page flow == null, skipping page flow.");
                document = resdoc.getSPDocument();
            }
        } else {
            if (currentpageflow == null) {
                throw new PustefixRuntimeException("Called with startwithflow == true, but currentpageflow == null");
            }
            LOG.debug("* Page is determined from flow [" + currentpageflow + "], starting page flow process");
            LOG.debug("* Current page: [" + currentpagerequest + "]");
            document = runPageFlow(true, stopnextforcurrentrequest);
        }

        if(!prohibitcontinue && jumptopage != null) {
            document = doJump(stopnextforcurrentrequest);
        }
        
        return document;
    }

    private SPDocument doJump(boolean stopnextforcurrentrequest) throws PustefixApplicationException, PustefixCoreException {
        LOG.debug("* [" + currentpagerequest + "] signalled success, jumptopage is set as [" + jumptopage + "].");
        PageLookupResult result = servercontext.getSiteMap().getPageName(jumptopage, getLanguage());
        jumptopage = result.getPageName();
        if(result.getPageAlternativeKey() != null) {
            pageAlternativeKey = result.getPageAlternativeKey();
        }
        currentpagerequest = createPageRequest(jumptopage);
        currentstatus = PageRequestStatus.JUMP;
        if (jumptopageflow != null) {
            setCurrentPageFlow(jumptopageflow);
        } else {
            currentpageflow = pageflowmanager.pageFlowToPageRequest(currentpageflow, currentpagerequest, getVariant());
        }
        jumptopage = null; // we don't want to recurse infinitely
        jumptopageflow = null; // we don't want to recurse infinitely

        LOG.debug("******* JUMPING to [" + currentpagerequest + "] *******\n");
        return documentFromFlow(false, stopnextforcurrentrequest);
    }
    
    private SPDocument runPageFlow(boolean stopatcurrentpage, boolean stopatnextaftercurrentpage) throws PustefixApplicationException, PustefixCoreException {
        ResultDocument resdoc = null;
        SPDocument document = null;
        currentstatus = PageRequestStatus.WORKFLOW;

        String nextPage = currentpageflow.findNextPage(this.parentcontext, currentpagerequest.getRootName(), stopatcurrentpage, stopatnextaftercurrentpage);
        if(nextPage == null) throw new PustefixApplicationException("Can't get an accessible page from pageflow '" + currentpageflow.getName() + "'.");
        currentpagerequest = createPageRequest(nextPage);

        resdoc = documentFromCurrentStep();
        document = resdoc.getSPDocument();
        if (document == null) {
            throw new PustefixCoreException("*** FATAL: [" + currentpagerequest + "] returns a 'null' SPDocument! ***");
        }
        LOG.debug("* [" + currentpagerequest + "] returned document => show it.");
        return document;
    }

    private ResultDocument documentFromCurrentStep() throws PustefixApplicationException, PustefixCoreException {

        ResultDocument document = null;
        document = checkPageAuthorization();
        if (document != null)
            return document;

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
        AuthConstraint authConstraint = null;
        PageRequestConfig pageConfig = this.getConfigForCurrentPageRequest();
        if(pageConfig != null) authConstraint = pageConfig.getAuthConstraint();
        if (authConstraint == null)
            authConstraint = parentcontext.getContextConfig().getDefaultAuthConstraint();
        if (authConstraint != null) {
            if (!authConstraint.isAuthorized(parentcontext)) {
                if (LOG.isDebugEnabled())
                    LOG.debug("Not authorized to access page '" + pageName + "'");
                throw new AuthConstraintViolation("Not authorized to access page '" + pageName + "'.", "pageaccess", pageName, authConstraint);
            }
        }
    }

    /**
     * Returns if accessing the current page is already permitted or 
     * it it will be possible by authenticating using an according authpage 
     */
    private boolean isAuthorizationPossible() {
        PageRequestConfig pageConfig = getConfigForCurrentPageRequest();
        if(pageConfig != null) {
            AuthConstraint authConstraint = pageConfig.getAuthConstraint();
            if (authConstraint == null) authConstraint = parentcontext.getContextConfig().getDefaultAuthConstraint();
            if (authConstraint != null && !authConstraint.isAuthorized(parentcontext) && authConstraint.getAuthPage(parentcontext)==null) return false;
        }
        return true;
    }
    
    private ResultDocument checkPageAuthorization() throws PustefixApplicationException, PustefixCoreException {
        if (parentcontext.getContextConfig().getRoleProvider().getRoles().size() == 0)
            return null;
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
            if (roleAuthDeps == null)
                roleAuthDeps = new LinkedHashSet<String>();
            roleAuthDeps.add(authEx.getTarget());
            PageRequest localAuthPage = null;
            PageRequestConfig pageConfig = this.getConfigForCurrentPageRequest();
            AuthConstraint authConstraint = null;
            if(pageConfig != null) authConstraint = pageConfig.getAuthConstraint();
            if (authConstraint == null)
                authConstraint = parentcontext.getContextConfig().getDefaultAuthConstraint();
            if (authConstraint != null) {
                String authPageName = authConstraint.getAuthPage(parentcontext);
                if (authPageName != null)
                    localAuthPage = createPageRequest(authPageName);
            }
            if (localAuthPage == null)
                throw authEx;
            PageRequest saved = currentpagerequest;
            if (LOG.isDebugEnabled())
                LOG.debug("===> [" + localAuthPage + "]: Checking authorisation");
            if (!checkIsAccessible(localAuthPage)) {
                throw new PustefixCoreException("*** Authorisation page [" + localAuthPage + "] is not accessible! ***");
            }
            if (LOG.isDebugEnabled())
                LOG.debug("===> [" + localAuthPage + "]: Need authorisation data");
            currentpagerequest = localAuthPage;
            ResultDocument resdoc = documentFromCurrentStep();
            currentpagerequest = saved;
            prohibitcontinue = true;
            if (resdoc != null && prohibitcontinue) {
                if (resdoc.getSPDocument() == null) {
                    throw new PustefixCoreException("*** FATAL: " + localAuthPage + " returns a 'null' SPDocument! ***");
                }
                resdoc.getSPDocument().setPagename(localAuthPage.getName());
                Element authElem = addAuthenticationData(resdoc);
                if (authElem != null) {
                    authElem.setAttribute("targetpage", authEx.getTarget());
                    Element misElem = resdoc.createSubNode(authElem, "authorizationfailure");
                    if (authEx.getType() != null)
                        misElem.setAttribute("type", authEx.getType());
                    if (authEx.getAuthorization() != null)
                        misElem.setAttribute("authorization", authEx.getAuthorization());
                    if (authEx.getTarget() != null)
                        misElem.setAttribute("target", authEx.getTarget());
                    if (authEx instanceof AuthConstraintViolation) {
                        AuthConstraintViolation authVio = (AuthConstraintViolation) authEx;
                        AuthConstraint authCon = authVio.getViolatedConstraint();
                        if (authCon != null) {
                            Element constraintElem = parentcontext.getContextConfig().getAuthConstraintAsXML(misElem.getOwnerDocument(), authCon);
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
            if (root.getFirstChild() != null)
                root.insertBefore(authElem, root.getFirstChild());
            else
                root.appendChild(authElem);
            if (roleAuthTarget != null)
                authElem.setAttribute("targetpage", roleAuthTarget);
            Role[] roles = parentcontext.getAuthentication().getRoles();
            Element rolesElem = resDoc.createSubNode(authElem, "roles");
            if (roles != null)
                for (Role role : roles) {
                    Element roleElem = resDoc.createSubNode(rolesElem, "role");
                    roleElem.setAttribute("name", role.getName());
                }
            return authElem;
        }
        return null;
    }

    public boolean checkNeedsData(PageRequest page) throws PustefixApplicationException {
        PageRequest saved = currentpagerequest;
        currentpagerequest = page;
        State state = getStateForPageRequest(page);

        boolean retval;
        try {
            retval = state.needsData(parentcontext, currentpservreq);
        } catch (Exception e) {
            throw new PustefixApplicationException("Exception while running needsData() for page " + page.getName(), e);
        }

        currentpagerequest = saved;
        return retval;
    }

    public boolean checkIsAccessible(PageRequest page) throws PustefixApplicationException {
        PageRequest saved = currentpagerequest;
        try {
            currentpagerequest = page;
            
            if(!isAuthorizationPossible()) return false;

            State state = getStateForPageRequest(page);

            boolean retval;
            try {
                retval = state.isAccessible(parentcontext, currentpservreq);
            } catch (Exception e) {
                throw new PustefixApplicationException("Got exception from state for page " + page.getName() + " while calling isAccessible()", e);
            }

            return retval;
        } finally {
            currentpagerequest = saved;
        }
    }

    public boolean isPageAccessible(String pagename) throws Exception {
        PageRequest page = createPageRequest(pagename);
        Variant currentvariant = getVariant();
        setVariantForThisRequestOnly(parentcontext.getSessionVariant());
        boolean retval = checkIsAccessible(page);
        setVariantForThisRequestOnly(currentvariant);
        return retval;
    }

    public void addCookie(Cookie cookie) {
        cookielist.add(cookie);
    }
    
    public List<Cookie> getCookies() {
        return cookielist;
    }

    public Cookie[] getRequestCookies() {
        if (currentpservreq == null) {
            throw new IllegalStateException("Cookies are only available witihin request handling");
        }
        return currentpservreq.getCookies();
    }
    
    public Throwable getLastException() {
        if (currentpservreq == null) {
            throw new IllegalStateException("This method is only available during request processing");
        }
        return currentpservreq.getLastException();
    }

    @Override
    public String toString() {
        StringBuffer contextbuf = new StringBuffer("\n");

        contextbuf.append("     pageflow:      " + currentpageflow + "\n");
        contextbuf.append("     PageRequest:   " + currentpagerequest + "\n");
        if (currentpagerequest != null) {
            try {
                contextbuf.append("       -> State: " + getStateForPageRequest(currentpagerequest) + "\n");
            } catch (PustefixApplicationException e) {
                // Ignore
            }
            contextbuf.append("       -> Status: " + getCurrentStatus() + "\n");
        }

        return contextbuf.toString();
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        RequestContextImpl copy = (RequestContextImpl) super.clone();
        if (currentpagerequest != null) {
            copy.currentpagerequest = new PageRequest(currentpagerequest.getName());
        }
        copy.cookielist = new ArrayList<Cookie>(cookielist);
        copy.messages = new HashSet<StatusCodeInfo>(messages);
        copy.currentstatus = PageRequestStatus.UNDEF;
        return copy;
    }

    public void setPfixServletRequest(PfixServletRequest pservreq) {
        // Usually the PfixServletRequest is supplied when
        // calling handleSubmittedData(), however there might
        // be situations when (although there is no request to
        // handle) a PfixServletRequest is needed.
        this.currentpservreq = pservreq;
    }

    public ContextImpl getParentContext() {
        return this.parentcontext;
    }

    public PfixServletRequest getPfixServletRequest() {
        return currentpservreq;
    }

    private State getStateForPageRequest(PageRequest page) throws PustefixApplicationException {
        State state = pagemap.getState(page);
        if (state == null) {
            state = parentcontext.getContextConfig().getDefaultState();
        }
        return state;
    }
    
    public boolean needsLastFlow(String pageName, String lastFlowName) {
        if(!parentcontext.getContextConfig().getPageFlowPassThrough()) {
            return false;
        }
        if(lastFlowName != null && !lastFlowName.equals("")) {
            PageFlow lastFlow = pageflowmanager.getPageFlowByName(lastFlowName, variant);
            if(lastFlow != null) {
                PageRequest page = createPageRequest(pageName);
                return pageflowmanager.needsLastFlow(lastFlow, page);
            }
        }
        return false;
    }

}
