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

import org.apache.log4j.Category;

import de.schlund.pfixcore.workflow.Context;

/**
 * Default implementation of the ResdocFinalizer interface.
 * <br/>
 *
 * Created: Fri Oct 12 22:00:21 2001
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 *
 *
 */

public class ResdocSimpleFinalizer implements ResdocFinalizer {
    public static final String   PROP_FINALIZER = "resdocfinalizer";
    public static final String   PROP_ONSUCCESS = "resdocfinalizer.onsuccess";
    protected           Category CAT            = Category.getInstance(this.getClass().getName());
    
    // These 3 methods are likely candidates to be overwritten...
    
    /**
     * <code>changedDocOnWorkError</code> is called when an error
     * happens while working with the data inside a handler's <code>handleSubmittedData()</code> routine.
     * You may add additional output to the ResultDocument here.
     * The default implementation simply calls the {@link renderDefault} method.
     *
     * @param container an <code>IWrapperContainer</code> value
     * @see de.schlund.pfixcore.workflow.app.ResdocFinalizer#onWorkError(IWrapperContainer) 
     */
    public void onWorkError(IWrapperContainer container) throws Exception{
        renderDefault(container);
    }

    /**
     * <code>onInsertStatus</code> is called after a call to
     * IHandlerAction.inserCurrentStatus(container), that means whenever
     * all (currently active) IHandlers are called to insert their current status
     * Into the ResultDocument. Use this Routine to add additional output there.
     * The default implementation simply calls the {@link renderDefault} method.
     *
     * @param container an <code>IWrapperContainer</code> value
     * @see de.schlund.pfixcore.workflow.app.ResdocFinalizer#onRetrieveStatus(IWrapperContainer) 
     */
    public void onRetrieveStatus(IWrapperContainer container) throws Exception{
        renderDefault(container);
    }

    /**
     * <code>changedDocOnSuccess</code> is called when there has been no error handling the request.
     * The default implementation simply sets the SPDocument in the container's associated ResultDocument to "null".
     *
     * @param container an <code>IWrapperContainer</code> value
     * @see de.schlund.pfixcore.workflow.app.ResdocFinalizer#onSuccess(IWrapperContainer) 
     */
    public void onSuccess(IWrapperContainer container) throws Exception {
        Context    context = container.getAssociatedContext();
        Properties props   = context.getPropertiesForCurrentPageRequest();
        String     onsucc  = props.getProperty(PROP_ONSUCCESS);
        boolean    inflow  = context.isCurrentPageRequestInCurrentFlow();
        
        if (onsucc != null && onsucc.equals("continue")) {
            container.getAssociatedResultDocument().setSPDocument(null);
        } else if (onsucc != null && onsucc.equals("stop")) {
            renderDefault(container);
        } else if ((onsucc != null && (onsucc.equals("auto") || onsucc.equals(""))) || onsucc == null) {
            if (inflow) {
                container.getAssociatedResultDocument().setSPDocument(null);
            } else {
                renderDefault(container);
            }
        }
    }

    /**
     * The default implementation does nothing. You normally want to overwrite this.
     *
     * @param container an <code>IWrapperContainer</code> value
     */
    protected void renderDefault(IWrapperContainer container) throws Exception {
        // 
    }
}

// ResdocSimpleFinalizer
