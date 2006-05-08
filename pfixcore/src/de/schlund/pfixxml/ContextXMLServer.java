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

import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;
import java.util.WeakHashMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Category;
import org.xml.sax.SAXException;

import de.schlund.pfixcore.scriptedflow.ScriptedFlowConfig;
import de.schlund.pfixcore.scriptedflow.ScriptedFlowInfo;
import de.schlund.pfixcore.scriptedflow.vm.Script;
import de.schlund.pfixcore.scriptedflow.vm.ScriptVM;
import de.schlund.pfixcore.scriptedflow.vm.VirtualHttpServletRequest;
import de.schlund.pfixxml.config.AbstractXMLServletConfig;
import de.schlund.pfixxml.config.ContextXMLServletConfig;
import de.schlund.pfixxml.resources.FileResource;

/**
 * @author jtl
 *
 */

public class ContextXMLServer extends AbstractXMLServer {
    private Category CAT = Category.getInstance(ContextXMLServer.class
            .getName());

    public final static String CONTEXT_SUFFIX = "__CONTEXT__";

    private final static String CONTEXT_CLASS = "context.class";

    private final static String ALREADY_SSL = "__CONTEXT_ALREADY_SSL__";

    private final static String PARAM_SCRIPTEDFLOW = "__scriptedflow";

    private final static String SCRIPTEDFLOW_SUFFIX = "__SCRIPTEDFLOW__";

    private ContextXMLServletConfig config = null;

    private WeakHashMap contextMap = new WeakHashMap();

    private String contextclassnname;

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        contextclassnname = this.getContextXMLServletConfig()
                .getContextConfig().getContextClass().getName();
        if (contextclassnname == null) {
            throw (new ServletException(
                    "Need name for context class from context.class property"));
        }
    }

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
            if (preq.getSession(false) != null
                    && preq.isRequestedSessionIdValid()) {
                String contextname = makeContextName();
                HttpSession session = preq.getSession(false);
                String already_ssl = (String) session.getAttribute(ALREADY_SSL);
                if (already_ssl != null && already_ssl.equals("true")) {
                    return true;
                } else {
                    try {
                        AppContext context = (AppContext) session
                                .getAttribute(contextname);
                        if (context != null) {
                            boolean retval = context.currentPageNeedsSSL(preq);
                            if (retval == true) {
                                session.setAttribute(ALREADY_SSL, "true");
                            }
                            return retval;
                        }
                    } catch (Exception exp) {
                        throw new ServletException(exp);
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

    protected boolean tryReloadProperties(PfixServletRequest preq)
            throws ServletException {
        if (super.tryReloadProperties(preq)) {
            //Reset PropertyObjects
            PropertyObjectManager.getInstance().resetPropertyObjects(
                    this.getContextXMLServletConfig().getProperties());
            //Reset Contexts
            synchronized (contextMap) {
                //Set name of Context class, compare with old name
                String oldClassName = contextclassnname;
                contextclassnname = getContextXMLServletConfig()
                        .getContextConfig().getContextClass().getName();
                if (contextclassnname.equals(oldClassName)) {
                    //Iterate over Contexts and reset them
                    Iterator it = contextMap.keySet().iterator();
                    while (it.hasNext()) {
                        try {
                            AppContext appCon = (AppContext) it.next();
                            appCon.reset();
                        } catch (Exception e) {
                            throw new ServletException(
                                    "Error while resetting context.");
                        }
                    }
                } else {
                    //Remove deprecated Contexts from contextMap
                    contextMap.clear();
                }
            }
            return true;
        } else {
            return false;
        }
    }

    public SPDocument getDom(PfixServletRequest preq) throws Exception {
        AppContext context = getContext(preq);
        SPDocument spdoc;

        ScriptedFlowInfo info = getScriptedFlowInfo(preq);
        if (preq.getRequestParam(PARAM_SCRIPTEDFLOW) != null
                && preq.getRequestParam(PARAM_SCRIPTEDFLOW).getValue() != null) {
            String scriptedFlowName = preq.getRequestParam(PARAM_SCRIPTEDFLOW)
                    .getValue();

            // Do a virtual request without any request parameters
            // to get an initial SPDocument
            PfixServletRequest vpreq = new PfixServletRequest(
                    VirtualHttpServletRequest.getVoidRequest(preq.getRequest()),
                    getContextXMLServletConfig().getProperties());
            spdoc = context.handleRequest(vpreq);

            // Reset current scripted flow state
            info.reset();

            // Lookup script name
            Script script = getScriptedFlowByName(scriptedFlowName);

            if (script != null) {
                // Remember running script
                info.isScriptRunning(true);

                // Get parameters for scripted flow:
                // They have the form __scriptedflow.<name>=<value>
                String[] paramNames = preq.getRequestParamNames();
                for (int i = 0; i < paramNames.length; i++) {
                    if (!paramNames[i].equals(PARAM_SCRIPTEDFLOW)) {
                        String paramName = paramNames[i];
                        String paramValue = preq.getRequestParam(paramName)
                                .getValue();
                        info.addParam(paramName, paramValue);
                    }
                }

                // Create VM and run script
                ScriptVM vm = new ScriptVM();
                vm.setScript(script);
                spdoc = vm.run(preq, spdoc, context, info.getParams());
                if (vm.isExitState()) {
                    info.reset();
                } else {
                    info.setState(vm.saveVMState());
                }
            }
        } else if (info.isScriptRunning()) {
            // First handle user request, then use result document
            // as base for further processing
            spdoc = context.handleRequest(preq);

            // Create VM and run script
            ScriptVM vm = new ScriptVM();
            vm.loadVMState(info.getState());
            spdoc = vm.run(preq, spdoc, context, info.getParams());
            if (vm.isExitState()) {
                info.reset();
            } else {
                info.setState(vm.saveVMState());
            }
        } else {
            // No scripted flow request
            // handle as usual
            spdoc = context.handleRequest(preq);
        }

        return spdoc;
    }

    private Script getScriptedFlowByName(String scriptedFlowName)
            throws Exception {
        ScriptedFlowConfig config = getContextXMLServletConfig()
                .getScriptedFlowConfig();
        return config.getScript(scriptedFlowName);
    }

    private ScriptedFlowInfo getScriptedFlowInfo(PfixServletRequest preq) {
        // Context is already loaded at this time, so we cann assume
        // that there is a valid session
        String name = servletname + SCRIPTEDFLOW_SUFFIX;
        ScriptedFlowInfo info = (ScriptedFlowInfo) preq.getSession(false)
                .getAttribute(name);
        if (info == null) {
            info = new ScriptedFlowInfo();
            preq.getSession(false).setAttribute(name, info);
        }
        return info;
    }

    private AppContext getContext(PfixServletRequest preq) throws Exception {
        String contextname = makeContextName();
        HttpSession session = preq.getSession(false);
        if (session == null) {
            throw new XMLException("No valid session found! Aborting...");
        }
        AppContext context = (AppContext) session.getAttribute(contextname);
        // Create new context and add it to contextMap, if context is null or contextClass has changed
        if ((context == null)
                || (!contextclassnname.equals(context.getClass().getName()))) {
            context = createContext();
            session.setAttribute(contextname, context);
            synchronized (contextMap) {
                contextMap.put(context, null);
            }
        }
        return context;
    }

    private String makeContextName() {
        return servletname + CONTEXT_SUFFIX;
    }

    private AppContext createContext() throws Exception {
        AppContext context = (AppContext) Class.forName(contextclassnname)
                .newInstance();
        context.init(this.getContextXMLServletConfig().getContextConfig(),
                makeContextName());
        return context;
    }

    protected void reloadServletConfig(FileResource configFile,
            Properties globalProperties) throws ServletException {
        try {
            this.config = ContextXMLServletConfig.readFromFile(configFile,
                    globalProperties);
        } catch (SAXException e) {
            throw new ServletException(
                    "Could not read servlet configuration from "
                            + configFile.toString(), e);
        } catch (IOException e) {
            throw new ServletException(
                    "Could not read servlet configuration from "
                            + configFile.toString(), e);
        }
    }
}
