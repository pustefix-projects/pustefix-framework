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

import de.schlund.pfixcore.workflow.*;
import de.schlund.pfixxml.*;
import java.util.Properties;

/**
 * DefaultIWrapperState.java
 *
 *
 * Created: Wed Oct 10 10:31:49 2001
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 * @version
 *
 *
 */

public class DefaultIWrapperState extends StaticState {
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

        preq.startLogEntry();
        container.initIWrappers(context, preq, resdoc);
        preq.endLogEntry("CONTAINER_INIT_IWRAPPERS", 5);
        
        if (isSubmitTrigger(context, preq)) {
            CAT.debug(">>> In SubmitHandling...");
            preq.startLogEntry();
            container.handleSubmittedData();
            preq.endLogEntry("CONTAINER_HANDLE_SUBMITTED_DATA", 300);
            if (container.errorHappened()) {
                CAT.debug("    => Can't continue, as errors happened during load/work.");
                container.addErrorCodes();
                rfinal.onWorkError(container);
            } else {
                CAT.debug("    => No error happened during work ...");
                if (context.getJumpToPageRequest() != null) {
                    CAT.debug("... Context has a jumppage set: [" + context.getJumpToPageRequest() + "]");
                    CAT.debug("    => continue there...");
                    container.getAssociatedResultDocument().setContinue(true);
                } else if (container.stayAfterSubmit()) {
                    CAT.debug("... Container says he wants to stay on this page:");
                    CAT.debug("    => retrieving current status.");
                    preq.startLogEntry();
                    container.retrieveCurrentStatus();
                    preq.endLogEntry("CONTAINER_RETRIEVE_CS_SUCCESS_STAY", 5);
                } else {
                    CAT.debug("... Container says he is ready:");
                    CAT.debug("    => end of submit reached successfully.");
                    if (context.isCurrentPageRequestInCurrentFlow()) {
                        CAT.debug(">>> Page is part of current pageflow:");
                        CAT.debug("    => signal to continue with pagflow by setting success flag to true...");
                        container.getAssociatedResultDocument().setContinue(true);
                    } else if (context.isCurrentPageFlowRequestedByUser()) {
                        CAT.debug(">>> Page not part of current pageflow, but flow is explicitely set from request data:");
                        CAT.debug("    => signal to continue with pagflow by setting success flag to true...");
                        container.getAssociatedResultDocument().setContinue(true);
                    } else {
                        CAT.debug(">>> NO continuable pageflow set:");
                        CAT.debug("    => retrieving current status and stay here...");
                        preq.startLogEntry();
                        container.retrieveCurrentStatus();
                        preq.endLogEntry("CONTAINER_RETRIEVE_CS_SUCCESS_STAY_NOWF", 5);
                    }
                }
                rfinal.onSuccess(container);
            }
        } else if (isDirectTrigger(context, preq) || context.finalPageIsRunning() || context.flowIsRunning()) {
            CAT.debug(">>> Retrieving current status...");
            preq.startLogEntry();
            container.retrieveCurrentStatus();
            if (isDirectTrigger(context,preq)) {
                CAT.debug("    => REASON: DirectTrigger");
                preq.endLogEntry("CONTAINER_RETRIEVE_CS_DIRECT", 5);
            } else if (context.finalPageIsRunning()) {
                CAT.debug("    => REASON: FinalPage");
                preq.endLogEntry("CONTAINER_RETRIEVE_CS_FINAL", 5);
            } else {
                CAT.debug("    => REASON: WorkFlow");
                preq.endLogEntry("CONTAINER_RETRIEVE_CS_FLOW", 5);
            }
            rfinal.onRetrieveStatus(container);
        } else {
            throw new XMLException("This should not happen: No submit trigger, no direct trigger, no final page and no workflow???");
        }
        // We need to check because in the success case, there's no need to add anything to the
        // SPDocument, as we will advance in the pageflow anyway  
        if (!resdoc.wantsContinue()) {
            container.addStringValues();
            container.addIWrapperStatus();
            renderContextResources(context, resdoc);
        } else if (context.currentFlowStepWantsPostProcess()) {
            // but we need the Status of the Context Resources in this case to base out decision on
            // where to jump to.
            renderContextResources(context, resdoc);
        }
        
        return resdoc;
    }
    

    // Eeek, unfortunately we can't use a flyweight here... (somewhere we need to store state after all)
    protected IWrapperContainer getIWrapperContainer(Context context) throws XMLException  {
        Properties        props     = context.getPropertiesForCurrentPageRequest();
        String            classname = props.getProperty(IWrapperSimpleContainer.PROP_CONTAINER);
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
        Properties props     = context.getPropertiesForCurrentPageRequest();
        String     classname = props.getProperty(ResdocSimpleFinalizer.PROP_FINALIZER);
        if (classname == null) {
            classname = DEF_FINALIZER;
        }
        
        ResdocFinalizer fin = ResdocFinalizerFactory.getInstance().getResdocFinalizer(classname);
        
        if (fin == null) {
            throw new RuntimeException("No finalizer found: classname = " + classname);
        }
        
        return fin;
    }

    // Remember, a IHandlerContainer is a flyweight!!!
    protected IHandlerContainer getIHandlerContainer(Context context) throws Exception {
        Properties                props = context.getProperties();
        PropertyObjectManager     pom   = PropertyObjectManager.getInstance();
        IHandlerContainerManager  ihcm  = (IHandlerContainerManager) pom.getInstance().getPropertyObject(props, IHDL_CONT_MANAGER);
        return ihcm.getIHandlerContainer(context);
    }

    
}// DefaultIWrapperState
