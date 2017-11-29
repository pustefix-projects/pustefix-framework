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
package de.schlund.pfixcore.workflow.app;

import java.util.Map;

import org.pustefixframework.config.contextxmlservice.IWrapperConfig;
import org.pustefixframework.config.contextxmlservice.StateConfig;
import org.pustefixframework.generated.CoreStatusCodes;
import org.pustefixframework.util.LogUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;

import de.schlund.pfixcore.generator.IWrapper;
import de.schlund.pfixcore.scriptedflow.vm.VirtualHttpServletRequest;
import de.schlund.pfixcore.util.TokenManager;
import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixcore.workflow.IWrapperState;
import de.schlund.pfixcore.workflow.RequestTokenAwareState;
import de.schlund.pfixcore.workflow.StateImpl;
import de.schlund.pfixxml.PfixServletRequest;
import de.schlund.pfixxml.PropertyObjectManager;
import de.schlund.pfixxml.RequestParam;
import de.schlund.pfixxml.ResultDocument;
import de.schlund.pfixxml.Tenant;
import de.schlund.pfixxml.XMLException;
import de.schlund.util.statuscodes.StatusCode;

/**
 * State implementation used for pages which need to process input data.
 */
public class DefaultIWrapperState extends StateImpl implements IWrapperState, RequestTokenAwareState {

    private final static String IHDL_CONT_MANAGER = "de.schlund.pfixcore.workflow.app.IHandlerContainerManager";
    
    private final Logger CSRF_LOG = LoggerFactory.getLogger("LOGGER_CSRF");

    /**
     * @see de.schlund.pfixcore.workflow.State#isAccessible(Context,
     *      PfixServletRequest)
     */
    @Override
    public boolean isAccessible(Context context, PfixServletRequest preq) throws Exception {
        return getIHandlerContainer(context).isAccessible(context);
    }

    /**
     * @see de.schlund.pfixcore.workflow.State#needsData(Context,
     *      PfixServletRequest)
     */
    @Override
    public boolean needsData(Context context, PfixServletRequest preq) throws Exception {
        return getIHandlerContainer(context).needsData(context);
    }

    /**
     * @see de.schlund.pfixcore.workflow.State#getDocument(Context,
     *      PfixServletRequest)
     */
    @Override
    public ResultDocument getDocument(Context context, PfixServletRequest preq) throws Exception {
        ResultDocument  resDoc = new ResultDocument();
        IWrapperContainer wrapperContainer;
        ModelAndView modelAndView;
        if(getConfig() != null && getConfig().preMVC()) {
            wrapperContainer = handleWrappers(context, preq, resDoc);
            modelAndView = processMVC(context, preq);
            resDoc.setModelAndView(modelAndView);
        } else {
            modelAndView = processMVC(context, preq);
            resDoc.setModelAndView(modelAndView);
            wrapperContainer = handleWrappers(context, preq, resDoc);
        }
        render(context, wrapperContainer, resDoc, modelAndView);
        return resDoc;
    }

    /**
     * Process IWrappers/IHandlers.
     */
    protected IWrapperContainer handleWrappers(Context context, PfixServletRequest preq, ResultDocument resdoc) throws Exception {
        IWrapperContainer wrp_container =  getIHandlerContainer(context).createIWrapperContainerInstance(context, preq, resdoc);
        if (isSubmitTrigger(context, preq)) {
            if(handleCSRF(context, preq, wrp_container)) {
                return wrp_container;
            }
            boolean valid = true;
            RequestParam rp = preq.getRequestParam("__token");
            if (rp != null) {
                String token = rp.getValue();
                String[] tokenParts = token.split(":");
                if (tokenParts.length == 3) {
                    String tokenName = tokenParts[0];
                    String errorPage = tokenParts[1];
                    String tokenValue = tokenParts[2];
                    TokenManager tm = (TokenManager) context;
                    if (tm.isValidToken(tokenName, tokenValue)) {
                        tm.invalidateToken(tokenName);
                    } else {
                        context.addPageMessage(CoreStatusCodes.FORM_TOKEN_INVALID, null, null);
                        if (errorPage.equals("")) {
                            wrp_container.retrieveCurrentStatus(false);
                            context.prohibitContinue();
                        } else {
                            context.setJumpToPage(errorPage);
                        }
                        valid = false;
                    }
                } else {
                    throw new IllegalArgumentException("Invalid token format: " + token);
                }
            } else {
                StateConfig stateConf = getConfig();
                if (stateConf != null && stateConf.requiresToken() && !(preq.getRequest() instanceof VirtualHttpServletRequest)) {
                    context.addPageMessage(CoreStatusCodes.FORM_TOKEN_MISSING, null, null);
                    wrp_container.retrieveCurrentStatus(false);
                    context.prohibitContinue();
                    valid = false;
                }
            }
            if (valid) {
                wrp_container.handleSubmittedData();
                if (wrp_container.errorHappened()) {
                    handleWrapperErrors(wrp_container.getIWrappersWithError());
                    context.prohibitContinue();
                } else {
                    wrp_container.retrieveCurrentStatus(false);
                }
            }
        } else if (isDirectTrigger(context, preq) || isPageFlowRunning(context)) {
            wrp_container.retrieveCurrentStatus(true);
            context.prohibitContinue();
        } else {
            throw new XMLException("This should not happen: No submit trigger, no direct trigger, no final page and no workflow???");
        }
        return wrp_container;
    }

    /**
     * Render model to XML.
     */
    protected void render(Context context, IWrapperContainer wrp_container, ResultDocument resdoc, ModelAndView modelAndView) throws Exception {
        // We want to optimize away the case where the context tells us that we
        // don't need to supply a full document as the context will - because of
        // the current state of
        // the context itself - not use the returned document for displaying the
        // page or any
        // further processing anyway. The context is responsible to only return
        // false when it can be 100% sure that the
        // document is not needed. Most notably this is NOT the case whenever
        // the current flow step has
        // pageflow actions attached, because those can possibly call
        // prohibitContinue() which in turn would force
        // the context to display the current page.
        // See the implementation of Context.stateMustSupplyFullDocument() for
        // details.
        if (context.stateMustSupplyFullDocument()) {
            wrp_container.addStringValues();
            wrp_container.addErrorCodes();
            wrp_container.addIWrapperStatus();
            renderMVC(resdoc, modelAndView);
            renderContextResources(context, resdoc);
            addResponseHeadersAndType(context, resdoc);
        }
    }

    // Remember, a IHandlerContainer is a flyweight!!!
    protected IHandlerContainer getIHandlerContainer(Context context) throws Exception {
        // Use context config object as dummy configuration object to make sure
        // each context (server) has its own IHandlerContainerManager
        IHandlerContainerManager ihcm = (IHandlerContainerManager) PropertyObjectManager.getInstance().getConfigurableObject(context.getContextConfig(), IHDL_CONT_MANAGER);
        return ihcm.getIHandlerContainer(context, this.getConfig());
    }

    public Map<String, ? extends IWrapperConfig> getIWrapperConfigMap(Tenant tenant) {
        return getConfig().getIWrappers(tenant);
    }

    public boolean requiresToken() {
        return getConfig().requiresToken();
    }

    /**
     * Handles CSRF protection, i.e. checks HTTP method and CSRF token
     * and sets according status code as page message if CSRF is detected.
     *
     * @param context
     * @param preq
     * @param container
     * @return true if CSRF is detected, false otherwise
     * @throws Exception
     */
    protected boolean handleCSRF(Context context, PfixServletRequest preq, IWrapperContainer container) throws Exception {
        if(getConfig() != null && getConfig().isProtected() && !(preq.getRequest() instanceof VirtualHttpServletRequest)) {
            StatusCode sc = null;
            if(!preq.getRequest().getMethod().equals("POST")) {
                sc = CoreStatusCodes.INVALID_HTTP_REQUEST_METHOD;
            } else {
                RequestParam rp = preq.getRequestParam("__csrf");
                if(rp == null) {
                    sc = CoreStatusCodes.CSRF_TOKEN_MISSING;
                } else {
                    TokenManager tm = (TokenManager)context;
                    if(!tm.getCSRFToken().equals(rp.getValue())) {
                        sc = CoreStatusCodes.CSRF_TOKEN_INVALID;
                    }
                }
            }
            if(sc != null) {
                CSRF_LOG.warn(context.getVisitId() + "|" + sc.getStatusCodeId()
                        + "|" + preq.getRequest().getMethod()
                        + "|" + LogUtils.makeLogSafe(preq.getContextPath() + preq.getPathInfo())
                        + "|" + LogUtils.makeLogSafe(preq.getQueryString()));
                context.addPageMessage(sc, null, null);
                container.retrieveCurrentStatus(true);
                context.prohibitContinue();
                return true;
            }
        }
        return false;
    }

    /**
     * Handle IWrapper validation errors.
     * 
     * The default implementation does nothing, but can be overridden, e.g. for error logging.
     * 
     * @param wrappers all wrappers with validation errors
     */
    public void handleWrapperErrors(IWrapper[] wrappers) {
    }

}
