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

import de.schlund.pfixcore.util.TokenManager;
import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixcore.workflow.IWrapperState;
import de.schlund.pfixcore.workflow.RequestTokenAwareState;
import de.schlund.pfixcore.workflow.StateImpl;
import de.schlund.pfixxml.PfixServletRequest;
import de.schlund.pfixxml.RequestParam;
import de.schlund.pfixxml.ResultDocument;
import de.schlund.pfixxml.XMLException;

/**
 * DefaultIWrapperState.java
 * 
 * 
 * Created: Wed Oct 10 10:31:49 2001
 * 
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 */

public class DefaultIWrapperState extends StateImpl implements IWrapperState, RequestTokenAwareState {

    private IHandlerContainer handlerContainer;

    /**
     * @see de.schlund.pfixcore.workflow.State#isAccessible(Context,
     *      PfixServletRequest)
     */
    @Override
    public boolean isAccessible(Context context, PfixServletRequest preq) throws Exception {
        return getIHandlerContainer().isAccessible(context);
    }

    /**
     * @see de.schlund.pfixcore.workflow.State#needsData(Context,
     *      PfixServletRequest)
     */
    @Override
    public boolean needsData(Context context, PfixServletRequest preq) throws Exception {
        CAT.debug(">>> [" + context.getCurrentPageRequest().getName() + "] Checking needsData()...");

        boolean retval = getIHandlerContainer().needsData(context);
        if (retval) {
            CAT.debug("    TRUE! now going to retrieve the current status.");
        } else {
            CAT.debug("    FALSE! continue with pageflow check.");
        }
        return retval;
    }

    /**
     * @see de.schlund.pfixcore.workflow.State#getDocument(Context,
     *      PfixServletRequest)
     */
    @Override
    public ResultDocument getDocument(Context context, PfixServletRequest preq) throws Exception {
        CAT.debug("[[[[[ " + context.getCurrentPageRequest().getName() + " ]]]]]");
        
        ResultDocument  resdoc = new ResultDocument();
      
        IWrapperContainer wrp_container =  getIHandlerContainer().createIWrapperContainerInstance(context, preq, resdoc);

        if (isSubmitTrigger(context, preq)) {
            CAT.debug(">>> In SubmitHandling...");

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
                if (stateConf != null && stateConf.requiresToken()) {
                    context.addPageMessage(CoreStatusCodes.FORM_TOKEN_MISSING, null, null);
                    wrp_container.retrieveCurrentStatus(false);
                    context.prohibitContinue();
                    valid = false;
                }
            }

            if (valid) {
                wrp_container.handleSubmittedData();

                if (wrp_container.errorHappened()) {
                    CAT.debug("    => Can't continue, as errors happened during load/work.");
                    context.prohibitContinue();
                } else {
                    CAT.debug("    => No error happened during work... end of submit reached successfully.");
                    CAT.debug("    => retrieving current status.");
                    wrp_container.retrieveCurrentStatus(false);

                }
            }
        } else if (isDirectTrigger(context, preq) || isPageFlowRunning(context)) {
            CAT.debug(">>> Retrieving current status...");

            wrp_container.retrieveCurrentStatus(true);
            if (CAT.isDebugEnabled()) {
                if (isDirectTrigger(context, preq)) {
                    CAT.debug("    => REASON: DirectTrigger");
                } else {
                    CAT.debug("    => REASON: WorkFlow");
                }
            }
            context.prohibitContinue();
        } else {
            throw new XMLException("This should not happen: No submit trigger, no direct trigger, no final page and no workflow???");
        }

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
            renderContextResources(context, resdoc);
            addResponseHeadersAndType(context, resdoc);
        }
        return resdoc;
    }

    private IHandlerContainer getIHandlerContainer() {
       return handlerContainer; 
    }
    
    @Override
    public void setConfig(StateConfig config) {
        super.setConfig(config);
        handlerContainer = new IHandlerContainerImpl(config);
    }
    
    @Override
    public void stateConfigChanged() {
        handlerContainer = new IHandlerContainerImpl(config);
    }
    
    public Map<String, ? extends IWrapperConfig> getIWrapperConfigMap() {
        return getConfig().getIWrappers();
    }

    public boolean requiresToken() {
        return getConfig().requiresToken();
    }
    
}// DefaultIWrapperState
