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
import java.util.Properties;

import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixxml.PfixServletRequest;
import de.schlund.pfixxml.PropertyObjectManager;
import de.schlund.pfixxml.ResultDocument;
import de.schlund.pfixxml.XMLException;

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
        boolean test = container.isPageAccessible(context) && container.areHandlerActive(context);
        return test;
    }
    
    /**
     * @see de.schlund.pfixcore.workflow.State#getDocument(Context, PfixServletRequest)
     */
    public ResultDocument getDocument(Context context, PfixServletRequest preq) throws Exception {
        IWrapperContainer container  = getIWrapperContainer(context);
        ResdocFinalizer   rfinal     = getResdocFinalizer(context);
        ResultDocument    resdoc     = new ResultDocument();
        
        if(CAT.isDebugEnabled()) {
            CAT.debug("[[[[[ " + context.getCurrentPageRequest().getName() + " ]]]]]"); 
        }
        container.initIWrappers(context, preq, resdoc);
        
        if (isSubmitTrigger(context, preq)) {
            if(CAT.isDebugEnabled()) {
                CAT.debug(">>> In SubmitHandling... ");
            }
            container.handleSubmittedData();
            if (container.errorHappened()) {
                if(CAT.isDebugEnabled()) {
                    CAT.debug("=> Can't continue, as errors happened during load/work.");
                }
                container.addErrorCodes();
                rfinal.onWorkError(container);
            } else {
                if(CAT.isDebugEnabled()) {
                    CAT.debug("... No error happened during work ...");
                }
                if (container.continueSubmit()) {
                    if(CAT.isDebugEnabled()) {
                        CAT.debug("... Container says he wants to stay on this page...\n" +
                            "=> retrieving current status.");
                    }
                    container.retrieveCurrentStatus(false);
                    rfinal.onRetrieveStatus(container);
                } else {
                    if(CAT.isDebugEnabled()) {
                        CAT.debug("... Container says he is ready: End of submit successfully...\n" +
                            "=> Finalizer.onSuccess() will decide if we stay on the page or continue with pageflow.");
                    }
                    rfinal.onSuccess(container);
                }
            }
        } else if (isDirectTrigger(context, preq) || context.finalPageIsRunning()) {
            if(CAT.isDebugEnabled()) {
                CAT.debug(">>> In DirectTriggerHandling...\n" +
                    "=> retrieving current status.");
            }
            container.retrieveCurrentStatus(false);
            rfinal.onRetrieveStatus(container);
        } else if (context.flowIsRunning()) {
            if(CAT.isDebugEnabled()) {
                CAT.debug(">>> In FlowHandling...");
            }
            if (container.needsData()) {
                if(CAT.isDebugEnabled()) {
                    CAT.debug("=> needing data, retrieving current status.");
                }
                container.retrieveCurrentStatus(false);
                rfinal.onRetrieveStatus(container);
            } else {
                if(CAT.isDebugEnabled()) {
                    CAT.debug("=> no need to handle, returning NULL.");
                }
                container.getAssociatedResultDocument().setSPDocument(null);
            }
        } else {
            throw new XMLException("This should not happen: No direct trigger, no submit trigger and no workflow???");
        }
        // We need to check because in the success case, the SPDocument
        // may well be set to null to trigger workflow continuation
        if (resdoc.getSPDocument() != null) {
            // container.retrieveCurrentStatus(true);
            container.addStringValues();
            container.addIWrapperStatus();
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
