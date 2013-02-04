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

package org.pustefixframework.http;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.pustefixframework.config.contextxmlservice.AbstractXMLServletConfig;
import org.pustefixframework.config.contextxmlservice.ContextXMLServletConfig;
import org.pustefixframework.config.contextxmlservice.PageRequestConfig;
import org.pustefixframework.util.LocaleUtils;

import de.schlund.pfixcore.exception.PustefixApplicationException;
import de.schlund.pfixcore.exception.PustefixCoreException;
import de.schlund.pfixcore.exception.PustefixRuntimeException;
import de.schlund.pfixcore.scriptedflow.ScriptedFlowConfig;
import de.schlund.pfixcore.scriptedflow.ScriptedFlowInfo;
import de.schlund.pfixcore.scriptedflow.compiler.CompilerException;
import de.schlund.pfixcore.scriptedflow.vm.Script;
import de.schlund.pfixcore.scriptedflow.vm.ScriptVM;
import de.schlund.pfixcore.scriptedflow.vm.VirtualHttpServletRequest;
import de.schlund.pfixcore.workflow.ContextImpl;
import de.schlund.pfixcore.workflow.ContextInterceptor;
import de.schlund.pfixcore.workflow.ExtendedContext;
import de.schlund.pfixcore.workflow.context.RequestContextImpl;
import de.schlund.pfixxml.PfixServletRequest;
import de.schlund.pfixxml.PfixServletRequestImpl;
import de.schlund.pfixxml.RenderOutputListener;
import de.schlund.pfixxml.RequestParam;
import de.schlund.pfixxml.SPDocument;
import de.schlund.pfixxml.Tenant;
import de.schlund.pfixxml.targets.PageInfo;

/**
 * @author jtl
 *
 */

public class PustefixContextXMLRequestHandler extends AbstractPustefixXMLRequestHandler {
   
    private final static Logger LOG       = Logger.getLogger(PustefixContextXMLRequestHandler.class);
    
    // private final static String ALREADY_SSL = "__CONTEXT_ALREADY_SSL__";

    private final static String PARAM_SCRIPTEDFLOW = "__scriptedflow";

    private final static String SCRIPTEDFLOW_SUFFIX = "__SCRIPTEDFLOW__";
    
    public final static String XSLPARAM_REQUESTCONTEXT = "__context__";
    
    private final static String PROP_RENDEROUTPUTLISTENER = "de.schlund.pfixxml.RenderOutputListener";

    private ContextXMLServletConfig config = null;
    
    private ContextImpl context = null;
    
    private RenderOutputListener renderOutputListener;
    
    protected ContextXMLServletConfig getContextXMLServletConfig() {
        return this.config;
    }

    public void setConfiguration(ContextXMLServletConfig config) {
        this.config = config;
    }
    
    @Override
    protected AbstractXMLServletConfig getAbstractXMLServletConfig() {
        return this.config;
    }
    
    @Override
    public boolean needsSSL(PfixServletRequest preq) throws ServletException {
        if (super.needsSSL(preq)) {
            return true;
        } else {
            String pagename = preq.getPageName();
            if (pagename != null) {
                PageRequestConfig pageConfig = config.getContextConfig().getPageRequestConfig(pagename);
                if (pageConfig != null) {
                    return pageConfig.isSSL();
                }
            }
        }
        return false;
    }

    @Override
    public boolean needsSession() {
        return true;
    }

    @Override
    public boolean allowSessionCreate() {
        return true;
    }
    
    @Override
    protected int validateRequest(HttpServletRequest req) {
        String path = req.getPathInfo();
        if(path != null && !path.equals("")) {
            // simple implementation which prevents that broken links
            // links with relative paths (e.g. img src="foo/bar.gif"),
            // which are interpreted relative to the servlet (as page
            // name) can create a lot of unwanted sessions or disturb
            // the client side session handling (problems in IE)
            if(path.startsWith("/")) path = path.substring(1);
            int ind = path.indexOf("/");
            if(ind > -1) {
                ind = path.indexOf("/", ind + 1);
                if(ind > -1) return HttpServletResponse.SC_NOT_FOUND;
            }
        }
        return 0;
    }
    
    public void init() throws ServletException {
        super.init();
        createOutputListener();
    }

    @Override
    public SPDocument getDom(PfixServletRequest preq) throws PustefixApplicationException, PustefixCoreException {
        
        ExtendedContext context = getContext(preq);
        try {
            // Prepare context for current thread
            // Cleanup is performed in finally block
            ((ContextImpl) context).prepareForRequest(preq.getRequest());
            
            SPDocument spdoc;

            ScriptedFlowInfo info = getScriptedFlowInfo(preq);
            if (preq.getRequestParam(PARAM_SCRIPTEDFLOW) != null && preq.getRequestParam(PARAM_SCRIPTEDFLOW).getValue() != null) {
                String scriptedFlowName = preq.getRequestParam(PARAM_SCRIPTEDFLOW).getValue();
                
                // Do a virtual request without any request parameters
                // to get an initial SPDocument
                PfixServletRequest vpreq = new PfixServletRequestImpl(VirtualHttpServletRequest.getVoidRequest(preq.getRequest()), getContextXMLServletConfig().getProperties(), this);
                spdoc = context.handleRequest(vpreq);

                // Reset current scripted flow state
                info.reset();

                // Lookup script name
                Script script;
                try {
                    script = getScriptedFlowByName(scriptedFlowName);
                } catch (CompilerException e) {
                    throw new PustefixCoreException("Could not compile scripted flow " + scriptedFlowName, e);
                }

                if (script != null) {
                    // Remember running script
                    info.isScriptRunning(true);

                    // Get parameters for scripted flow:
                    // They have the form __scriptedflow.<name>=<value>
                    String[] paramNames = preq.getRequestParamNames();
                    for (int i = 0; i < paramNames.length; i++) {
                        if (!paramNames[i].equals(PARAM_SCRIPTEDFLOW)) {
                            String paramName = paramNames[i];
                            String paramValue = preq.getRequestParam(paramName).getValue();
                            info.addParam(paramName, paramValue);
                        }
                    }

                    // Create VM and run script
                    ScriptVM vm = new ScriptVM();
                    vm.setPageAliasResolver(this);
                    vm.setScript(script);
                    try {
                        spdoc = vm.run(preq, spdoc, context, info.getParams());
                    } finally {
                        // Make sure this is done even if an error has occured
                        if (vm.isExitState()) {
                            info.reset();
                        } else {
                            info.setState(vm.saveVMState());
                        }
                    }
                }

            } else if (info.isScriptRunning()) {
                // First handle user request, then use result document
                // as base for further processing
                spdoc = context.handleRequest(preq);

                // Create VM and run script
                ScriptVM vm = new ScriptVM();
                vm.loadVMState(info.getState());
                try {
                    spdoc = vm.run(preq, spdoc, context, info.getParams());
                } finally {
                    if (vm.isExitState()) {
                        info.reset();
                    } else {
                        info.setState(vm.saveVMState());
                    }
                }
            } else {
                // No scripted flow request
                // handle as usual
                spdoc = context.handleRequest(preq);
            }
            
            if(spdoc != null) {
            	if(spdoc.getPageAlternative() != null) {
                    Set<String> pageAltKeys = siteMap.getPageAlternativeKeys(spdoc.getPagename());
                    if(pageAltKeys == null || !pageAltKeys.contains(spdoc.getPageAlternative())) {
                        spdoc.setPageAlternative(null);
                    }
                }
            }
            
            if(spdoc != null && !spdoc.isRedirect()) {
                String expectedPageName = null;
                boolean isAlias = false;
                String langPrefix = null;
                Tenant tenant = spdoc.getTenant();
                if((tenant != null && !spdoc.getLanguage().equals(tenant.getDefaultLanguage())) ||
                        tenant == null && projectInfo.getSupportedLanguages().size() > 1 && !spdoc.getLanguage().equals(projectInfo.getDefaultLanguage())) {
                    langPrefix = LocaleUtils.getLanguagePart(spdoc.getLanguage());
                }
                if(context.getContextConfig().getDefaultPage(context.getVariant()).equals(spdoc.getPagename())) {
                    expectedPageName = langPrefix;
                } else {
                    String alias = siteMap.getAlias(spdoc.getPagename(), spdoc.getLanguage(), spdoc.getPageAlternative());
                    if(langPrefix != null) {
                        alias = langPrefix + "/" + alias;
                    }
                    expectedPageName = alias;
                    isAlias = true;
                }
                String requestedPageName = preq.getRequestedPageName();
                if( ( expectedPageName != null  && !expectedPageName.equals(requestedPageName)) ||
                    ( expectedPageName == null && requestedPageName != null)) { 
                    // Make sure all requests that don't encode an explicite pagename
                    // (this normally is only the case for the first request)
                    // OR pages that have the "wrong" pagename in their request 
                    // (this applies to pages selected by stepping ahead in the page flow)
                    // are redirected to the page selected by the business logic below
                    String scheme = preq.getScheme();
                    String port = String.valueOf(preq.getServerPort());
                    String sessionIdPath = "";
                    HttpSession session = preq.getSession(false);
                    if(session.getAttribute(AbstractPustefixRequestHandler.SESSION_ATTR_COOKIE_SESSION) == null) {
                        sessionIdPath = ";jsessionid=" + session.getId();
                    }
                    String redirectURL = scheme + "://" + getServerName(preq.getRequest()) 
                        + ":" + port + preq.getContextPath() + preq.getServletPath() + "/" 
                        + (expectedPageName == null ? "" : expectedPageName)
                        + sessionIdPath + "?__reuse=" + spdoc.getTimestamp();
                    RequestParam rp = preq.getRequestParam("__frame");
                    if (rp != null && rp.getValue() != null && !rp.getValue().equals("")) {
                        redirectURL += "&__frame=" + rp.getValue();
                    }
                    rp = preq.getRequestParam("__lf");
                    if (rp != null) {
                    	redirectURL += "&__lf=" + rp.getValue();
                    }
                    spdoc.setRedirect(redirectURL, isAlias);
                }
            }
            return spdoc;
        } finally {
            ((ContextImpl) context).cleanupAfterRequest();
        }
    }
    
    @Override
    protected boolean isPageDefined(String name) {
    	return config.getContextConfig().getPageRequestConfig(name) != null;
    }

    private Script getScriptedFlowByName(String scriptedFlowName) throws CompilerException {
        ScriptedFlowConfig config = getContextXMLServletConfig().getScriptedFlowConfig();
        return config.getScript(scriptedFlowName);
    }

    private ScriptedFlowInfo getScriptedFlowInfo(PfixServletRequest preq) {
        // Context is already loaded at this time, so we cann assume
        // that there is a valid session
        String name = servletname + SCRIPTEDFLOW_SUFFIX;
        ScriptedFlowInfo info = (ScriptedFlowInfo) preq.getSession(false).getAttribute(name);
        if (info == null) {
            info = new ScriptedFlowInfo();
            preq.getSession(false).setAttribute(name, info);
        }
        return info;
    }
    
    private ExtendedContext getContext(PfixServletRequest preq) throws PustefixApplicationException, PustefixCoreException {
        HttpSession session = preq.getSession(false);
        if (session == null) {
            // The ServletManager class handles session creation
            throw new PustefixRuntimeException("No valid session found! Aborting...");
        }
        return context;
    }

    @Override
    protected void hookBeforeRender(PfixServletRequest preq, SPDocument spdoc, TreeMap<String, Object> paramhash, String stylesheet) {
        super.hookBeforeRender(preq, spdoc, paramhash, stylesheet);
        RequestContextImpl oldRequestContext = (RequestContextImpl) spdoc.getProperties().get(XSLPARAM_REQUESTCONTEXT);
        RequestContextImpl newRequestContext;
        try {
            newRequestContext = (RequestContextImpl) oldRequestContext.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Unexpected CloneException", e);
        }
        newRequestContext.setPfixServletRequest(preq);
        newRequestContext.getParentContext().setRequestContextForCurrentThread(newRequestContext);
    }
    
    @Override
    protected void hookAfterRender(PfixServletRequest preq, SPDocument spdoc, TreeMap<String, Object> paramhash, String stylesheet) {
        super.hookAfterRender(preq, spdoc, paramhash, stylesheet);
        RequestContextImpl rcontext = (RequestContextImpl) spdoc.getProperties().get(XSLPARAM_REQUESTCONTEXT);
        for (ContextInterceptor interceptor : getContextXMLServletConfig().getContextConfig().getPostRenderInterceptors()) {
            interceptor.process(rcontext.getParentContext(), preq);
        } 
        rcontext.getParentContext().setRequestContextForCurrentThread(null);
    }

    protected void hookAfterDelivery(PfixServletRequest preq, SPDocument spdoc, ByteArrayOutputStream output) {
        super.hookAfterDelivery(preq, spdoc, output);
        if(renderOutputListener != null) {
            RequestContextImpl reqContext = (RequestContextImpl) spdoc.getProperties().get(XSLPARAM_REQUESTCONTEXT);
            try {
                renderOutputListener.output(preq, reqContext.getParentContext(), output);
            } catch(Exception x) {
                //error in listener shouldn't interfere further processing, just log it out here
                LOG.error("Error in RenderOutputListener", x);
            }
        }
    }
    
    private void createOutputListener() {
        String renderOutputListenerClass = getAbstractXMLServletConfig().getProperties().getProperty(PROP_RENDEROUTPUTLISTENER);
        if(renderOutputListenerClass != null) {
            try {
                Class<? extends RenderOutputListener> clazz = Class.forName(renderOutputListenerClass).asSubclass(RenderOutputListener.class);
                renderOutputListener = clazz.newInstance();
            } catch (Exception x) {
                LOG.error("Can't instantiate RenderOutputListener", x);
            }
        }
    }
    
    public void setContext(ContextImpl context) {
        this.context = context;
    }
    
    @Override
    public String[] getRegisteredURIs() {

        SortedSet<String> uris = new TreeSet<String>();
        
        uris.add("/");
        
        //add path mapping for backwards compatibility
        String[] regUris = super.getRegisteredURIs();
        for(String regUri: regUris) uris.add(regUri);
        
        addPageURIs(uris);
        
        //add lang default page mappings
        if(!tenantInfo.getTenants().isEmpty()) {
            for(Tenant tenant: tenantInfo.getTenants()) {
                for(String supportedLanguage: tenant.getSupportedLanguages()) {
                    if(!supportedLanguage.equals(tenant.getDefaultLanguage())) {
                        uris.add("/" + LocaleUtils.getLanguagePart(supportedLanguage));
                    }
                }
            }
        } else if(projectInfo.getSupportedLanguages().size() > 1) {
            for(String supportedLanguage: projectInfo.getSupportedLanguages()) {
                if(!supportedLanguage.equals(projectInfo.getDefaultLanguage())) {
                    uris.add("/" + LocaleUtils.getLanguagePart(supportedLanguage));
                }
            }
        }
        
        String[] uriArr = uris.toArray(new String[uris.size()]);
        return uriArr;
    }
    

    
    @Override
    public String[] getRegisteredPages() {

        SortedSet<String> pages = new TreeSet<String>();
        
        pages.add("pfxsession");
        
        //add pages from configured pagerequests
        List<? extends PageRequestConfig> pageConfigs = config.getContextConfig().getPageRequestConfigs();
        for(PageRequestConfig pageConfig: pageConfigs) {
            pages.add(pageConfig.getPageName());
        }
        
        //add page mappings for standardpages
        Set<PageInfo> pageInfos = generator.getPageTargetTree().getPageInfos();
        for(PageInfo pageInfo: pageInfos) {
            pages.add(pageInfo.getName());
        }
        
        String[] pageArr = pages.toArray(new String[pages.size()]);
        return pageArr;
    }
    
}
