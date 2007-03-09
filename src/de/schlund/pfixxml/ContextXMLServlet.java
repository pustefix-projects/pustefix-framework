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

import java.util.Properties;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

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
import de.schlund.pfixcore.workflow.ExtendedContext;
import de.schlund.pfixcore.workflow.context.RequestContextImpl;
import de.schlund.pfixcore.workflow.context.ServerContextImpl;
import de.schlund.pfixxml.config.AbstractXMLServletConfig;
import de.schlund.pfixxml.config.ConfigReader;
import de.schlund.pfixxml.config.ContextXMLServletConfig;
import de.schlund.pfixxml.resources.FileResource;

/**
 * @author jtl
 *
 */

public class ContextXMLServlet extends AbstractXMLServlet {
    private Logger LOG = Logger.getLogger(ContextXMLServlet.class);

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
            String pagename = preq.getPageName();
            if (pagename != null) {
                return config.getContextConfig().getPageRequestConfig(pagename).isSSL();
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
                servercontext = new ServerContextImpl(getContextXMLServletConfig().getContextConfig(), servletname);
                ServerContextStore.getInstance(this.getServletContext()).storeContext(this, preq, servletname, servercontext);
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
        ExtendedContext context = getContext(preq);
        
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
    
    private ExtendedContext getContext(PfixServletRequest preq) throws PustefixApplicationException, PustefixCoreException {
        HttpSession session = preq.getSession(false);
        if (session == null) {
            // The ServletManager class handles session creation
            throw new PustefixRuntimeException("No valid session found! Aborting...");
        }

        SessionContextStore store = SessionContextStore.getInstance(session);
        ContextImpl context = store.getContext(this, preq);
        // Session does not have a context yet?
        if (context == null) {
            // Synchronize on session object to make sure only ONE
            // context per session is created
            synchronized (session) {
                context = store.getContext(this, preq);
                if (context == null) {
                    context = new ContextImpl(servercontext, session);
                    store.storeContext(this, preq, this.servletname, context);
                }
            }
        }
        // Update reference to server context as it might have changed
        context.setServerContext(servercontext);
        
        return context;
    }

    protected void reloadServletConfig(FileResource configFile, Properties globalProperties) throws ServletException {
        try {
            this.config = ConfigReader.readContextXMLServletConfig(configFile, globalProperties);
        } catch (PustefixCoreException e) {
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
