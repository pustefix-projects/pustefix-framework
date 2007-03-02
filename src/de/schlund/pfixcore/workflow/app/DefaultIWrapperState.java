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

package de.schlund.pfixcore.workflow.app;


import org.apache.log4j.Logger;

import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixcore.workflow.StateImpl;
import de.schlund.pfixxml.PfixServletRequest;
import de.schlund.pfixxml.PropertyObjectManager;
import de.schlund.pfixxml.ResultDocument;
import de.schlund.pfixxml.XMLException;
import de.schlund.pfixxml.config.PageRequestConfig;
import de.schlund.pfixxml.perflogging.PerfEvent;
import de.schlund.pfixxml.perflogging.PerfEventType;

/**
 * DefaultIWrapperState.java
 *
 *
 * Created: Wed Oct 10 10:31:49 2001
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 */

public class DefaultIWrapperState extends StateImpl {
    private static Logger LOG               = Logger.getLogger(DefaultIWrapperState.class);
    private static String DEF_WRP_CONTAINER = "de.schlund.pfixcore.workflow.app.IWrapperSimpleContainer";
    private static String DEF_FINALIZER     = "de.schlund.pfixcore.workflow.app.ResdocSimpleFinalizer";

    private static String IHDL_CONT_MANAGER = "de.schlund.pfixcore.workflow.app.IHandlerContainerManager";
    
    /**
     * @see de.schlund.pfixcore.workflow.State#isAccessible(Context, PfixServletRequest)
     */
    public boolean isAccessible(Context context, PfixServletRequest preq) throws Exception {
        IHandlerContainer container = getIHandlerContainer(context);
        return (container.isPageAccessible(context) && container.areHandlerActive(context));
    }

    /**
     * @see de.schlund.pfixcore.workflow.State#needsData(Context, PfixServletRequest)
     */
    public boolean needsData(Context context, PfixServletRequest preq) throws Exception {
        CAT.debug(">>> Checking needsData()...");
        IWrapperContainer container = getIWrapperContainer(context);
        container.initIWrappers(context, preq, new ResultDocument());

        boolean retval = container.needsData();
        if (retval) {
            CAT.debug("    TRUE! now going to retrieve the current status.");
        } else {
            CAT.debug("    FALSE! continue with pageflow check.");
        }
        return retval;
    }
    
    /**
     * @see de.schlund.pfixcore.workflow.State#getDocument(Context, PfixServletRequest)
     */
    public ResultDocument getDocument(Context context, PfixServletRequest preq) throws Exception {
        IWrapperContainer container  = getIWrapperContainer(context);
        ResdocFinalizer   rfinal     = getResdocFinalizer(context);
        ResultDocument    resdoc     = new ResultDocument();
        
        CAT.debug("[[[[[ " + context.getCurrentPageRequest().getName() + " ]]]]]"); 

        PerfEvent pe = new PerfEvent(PerfEventType.PAGE_INITIWRAPPERS, context.getCurrentPageRequest().toString());
        
        pe.start();
        container.initIWrappers(context, preq, resdoc);
        pe.save();
        
        if (isSubmitTrigger(context, preq)) {
            CAT.debug(">>> In SubmitHandling...");
            
            pe = new PerfEvent(PerfEventType.PAGE_HANDLESUBMITTEDDATA, context.getCurrentPageRequest().toString());
            pe.start();
            container.handleSubmittedData();
            pe.save();
         
            if (container.errorHappened()) {
                CAT.debug("    => Can't continue, as errors happened during load/work.");
                rfinal.onWorkError(container);
                context.prohibitContinue();
            } else {
                CAT.debug("    => No error happened during work ...");
                if (!context.isJumpToPageSet() && container.stayAfterSubmit()) {
                    CAT.debug("... Container says he wants to stay on this page and no jumptopage is set: Setting prohibitcontinue=true");
                    context.prohibitContinue();
                } else {
                    CAT.debug("... Container says he is ready.");
                }

                CAT.debug("    => end of submit reached successfully.");
                CAT.debug("    => retrieving current status.");
                pe = new PerfEvent(PerfEventType.PAGE_RETRIEVECURRENTSTATUS, context.getCurrentPageRequest().toString());
                pe.start();
                container.retrieveCurrentStatus();
                pe.save();

                rfinal.onSuccess(container);
            }
        } else if (isDirectTrigger(context, preq) || context.finalPageIsRunning() || context.flowIsRunning()) {
            CAT.debug(">>> Retrieving current status...");
            
            pe = new PerfEvent(PerfEventType.PAGE_RETRIEVECURRENTSTATUS, context.getCurrentPageRequest().toString());
            pe.start();
            container.retrieveCurrentStatus();
            pe.save();
            if (isDirectTrigger(context,preq)) {
                // nothing
                CAT.debug("    => REASON: DirectTrigger");
            } else if (context.finalPageIsRunning()) {
                // nothing
                CAT.debug("    => REASON: FinalPage");
            } else {
                // nothing, too
                CAT.debug("    => REASON: WorkFlow");
            }
            rfinal.onRetrieveStatus(container);
            context.prohibitContinue();
        } else {
            throw new XMLException("This should not happen: No submit trigger, no direct trigger, no final page and no workflow???");
        }

        // We want to optimise away the case where the context tells us that we don't need to supply
        // a full document as the context will - because of the current state of the context
        // itself - not use the returned document for displaying the page anyway. The context is
        // responsible to only return false when it can be 100% sure that the document is not needed.
        // Most notably this is NOT the case whenever the current flow step has pageflow actions attached, because
        // those can possibly call prohibitContinue() which in turn would force the context to display the current page.
        // See the implementation of Context.stateMustSupplyFullDocument() for details.
        if (context.stateMustSupplyFullDocument()) {
            container.addStringValues();
            container.addErrorCodes();
            container.addIWrapperStatus();
            renderContextResources(context, resdoc);
            addResponseHeadersAndType(context, resdoc);
        }
        return resdoc;
    }
    
    // Eeek, unfortunately we can't use a flyweight here... (somewhere we need to store state after all)
    protected IWrapperContainer getIWrapperContainer(Context context) throws XMLException  {
        PageRequestConfig config    = context.getConfigForCurrentPageRequest();
        String            classname = config.getProperties().getProperty(IWrapperSimpleContainer.PROP_CONTAINER);
        IWrapperContainer obj       = null;
        
        if (classname == null) {
            classname = DEF_WRP_CONTAINER;
        }
        
        try {
            obj = (IWrapperContainer) Class.forName(classname).newInstance();
        } catch (InstantiationException e) {
            throw new XMLException("unable to instantiate class [" + classname + "] :" + e.getMessage());
        } catch (IllegalAccessException e) {
            throw new XMLException("unable access class [" + classname + "] :" + e.getMessage());
        } catch (ClassNotFoundException e) {
            throw new XMLException("unable to find class [" + classname + "] :" + e.getMessage());
        } catch (ClassCastException e) {
            throw new XMLException("class [" + classname + "] does not implement the interface IWrapperContainer :" + e.getMessage());
        }
        
        return obj;
    }

    // Remember, a ResdocFinalizer is a flyweight!!!
    protected ResdocFinalizer getResdocFinalizer(Context context) throws XMLException {
        PageRequestConfig config = context.getConfigForCurrentPageRequest();
        Class clazz = config.getFinalizer();
        String classname = DEF_FINALIZER;
        if (clazz != null) {
            classname = clazz.getName();
        }
        
        ResdocFinalizer fin = ResdocFinalizerFactory.getInstance().getResdocFinalizer(classname);
        
        if (fin == null) {
            throw new RuntimeException("No finalizer found: classname = " + classname);
        }
        
        return fin;
    }

    // Remember, a IHandlerContainer is a flyweight!!!
    protected IHandlerContainer getIHandlerContainer(Context context) throws Exception {
        PropertyObjectManager pom      = PropertyObjectManager.getInstance();
        // Use context config object as dummy configuration object to make sure
        // each context (server) has its own IHandlerContainerManager
        IHandlerContainerManager  ihcm = 
            (IHandlerContainerManager) PropertyObjectManager.getInstance().getConfigurableObject(context.getContextConfig(), IHDL_CONT_MANAGER);
        return ihcm.getIHandlerContainer(context);
    }

    
}// DefaultIWrapperState
