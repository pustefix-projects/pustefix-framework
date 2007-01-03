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

package de.schlund.pfixxml;

import de.schlund.pfixcore.exception.PustefixApplicationException;
import de.schlund.pfixcore.exception.PustefixCoreException;
import de.schlund.pfixcore.exception.PustefixRuntimeException;
import de.schlund.pfixcore.scriptedflow.ScriptedFlowConfig;
import de.schlund.pfixcore.scriptedflow.ScriptedFlowInfo;
import de.schlund.pfixcore.scriptedflow.compiler.CompilerException;
import de.schlund.pfixcore.scriptedflow.vm.Script;
import de.schlund.pfixcore.scriptedflow.vm.ScriptVM;
import de.schlund.pfixcore.scriptedflow.vm.VirtualHttpServletRequest;
import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixcore.workflow.ContextImpl;
import de.schlund.pfixcore.workflow.context.RequestContextImpl;
import de.schlund.pfixcore.workflow.context.ServerContextImpl;
import de.schlund.pfixxml.config.AbstractXMLServletConfig;
import de.schlund.pfixxml.config.ContextXMLServletConfig;
import de.schlund.pfixxml.config.PageRequestConfig;
import de.schlund.pfixxml.resources.FileResource;
import java.io.IOException;
import java.util.Properties;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

/**
 * @author jtl
 *
 */

public class ContextXMLServer extends AbstractXMLServer {
    private Logger LOG = Logger.getLogger(ContextXMLServer.class);

    public final static String CONTEXT_SUFFIX = "__CONTEXT__";

    private final static String ALREADY_SSL = "__CONTEXT_ALREADY_SSL__";

    private final static String PARAM_SCRIPTEDFLOW = "__scriptedflow";

    private final static String SCRIPTEDFLOW_SUFFIX = "__SCRIPTEDFLOW__";
    
    public final static String XSLPARAM_REQUESTCONTEXT = "__context__";

    private ContextXMLServletConfig config = null;

    private ServerContextImpl servercontext = null;

    protected ContextXMLServletConfig getContextXMLServletConfig() {
        return this.config;
    }

    protected AbstractXMLServletConfig getAbstractXMLServletConfig() {
        return this.config;
    }

    protected boolean needsSSL(PfixServletRequest preq) throws ServletException {
        if (super.needsSSL(preq)) {
            return true;
        } else {
            if (preq.getSession(false) != null && preq.isRequestedSessionIdValid()) {
                String      contextname = makeContextName();
                HttpSession session     = preq.getSession(false);
                String      already_ssl = (String) session.getAttribute(ALREADY_SSL);
                if (already_ssl != null && already_ssl.equals("true")) {
                    return true;
                } else {
                    String page = preq.getPageName();
                    if (page == null) {
                        ContextImpl context = (ContextImpl) session.getAttribute(contextname);
                        if (context != null) {
                            page = context.getLastPageName();
                        }
                    }
                    if (page != null) {
                        PageRequestConfig pageconfig = config.getContextConfig().getPageRequestConfig(page);
                        if (pageconfig != null) {
                            boolean retval = pageconfig.isSSL();
                            if (retval) {
                                session.setAttribute(ALREADY_SSL, "true");
                            }
                            return retval;
                        }
                    }
                }
            }
        }
        return false;
    }

    protected boolean needsSession() {
        return true;
    }

    protected boolean allowSessionCreate() {
        return true;
    }

    protected boolean tryReloadProperties(PfixServletRequest preq) throws ServletException {
        if (super.tryReloadProperties(preq)) {
            try {
                servercontext = new ServerContextImpl(getContextXMLServletConfig().getContextConfig(), makeContextName());
                this.getServletContext().setAttribute(makeContextName(), servercontext);
            } catch (Exception e) {
                String msg = "Error during reload of servlet configuration";
                LOG.error(msg, e);
                throw new ServletException(msg, e);
            }
            return true;
        } else {
            return false;
        }
    }

    public SPDocument getDom(PfixServletRequest preq) throws PustefixApplicationException, PustefixCoreException {
        Context context = getContext(preq);
        
        // Prepare context for current thread
        // Cleanup is performed in finally block
        ((ContextImpl) context).prepareForRequest();
        
        try {
            SPDocument spdoc;

            ScriptedFlowInfo info = getScriptedFlowInfo(preq);
            if (preq.getRequestParam(PARAM_SCRIPTEDFLOW) != null && preq.getRequestParam(PARAM_SCRIPTEDFLOW).getValue() != null) {
                String scriptedFlowName = preq.getRequestParam(PARAM_SCRIPTEDFLOW).getValue();
                
                // Do a virtual request without any request parameters
                // to get an initial SPDocument
                PfixServletRequest vpreq = new PfixServletRequest(VirtualHttpServletRequest.getVoidRequest(preq.getRequest()), getContextXMLServletConfig().getProperties());
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

            return spdoc;
        } finally {
            ((ContextImpl) context).cleanupAfterRequest();
        }
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
    
    private Context getContext(PfixServletRequest preq) throws PustefixApplicationException, PustefixCoreException {
        // Name of the attribute that is used to store the session context
        // within the session object.
        String contextname = makeContextName();

        HttpSession session = preq.getSession(false);
        if (session == null) {
            // The ServletManager class handles session creation
            throw new PustefixRuntimeException("No valid session found! Aborting...");
        }

        // FIXME: DCL is broken
        ContextImpl context = (ContextImpl) session.getAttribute(contextname);
        // Session does not have a context yet?
        if (context == null) {
            // Synchronize on session object to make sure only ONE
            // context per session is created
            synchronized (session) {
                context = (ContextImpl) session.getAttribute(contextname);
                if (context == null) {
                    context = new ContextImpl(servercontext, session);
                    session.setAttribute(contextname, context);
                } else {
                    // update, as it may have changed
                    context.setServerContext(servercontext);
                }
            }
        }
        return context;
    }

    private String makeContextName() {
        return servletname + CONTEXT_SUFFIX;
    }

    protected void reloadServletConfig(FileResource configFile, Properties globalProperties) throws ServletException {
        try {
            this.config = ContextXMLServletConfig.readFromFile(configFile, globalProperties);
        } catch (SAXException e) {
            throw new ServletException("Could not read servlet configuration from " + configFile.toURI(), e);
        } catch (IOException e) {
            throw new ServletException("Could not read servlet configuration from " + configFile.toURI(), e);
        }
    }

    protected void hookBeforeRender(PfixServletRequest preq, SPDocument spdoc, TreeMap paramhash, String stylesheet) {
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
    
    protected void hookAfterRender(PfixServletRequest preq, SPDocument spdoc, TreeMap paramhash, String stylesheet) {
        super.hookAfterRender(preq, spdoc, paramhash, stylesheet);
        RequestContextImpl rcontext = (RequestContextImpl) spdoc.getProperties().get(XSLPARAM_REQUESTCONTEXT);
        rcontext.getParentContext().setRequestContextForCurrentThread(null);
    }
    
}
