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

import org.apache.log4j.Logger;


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
    protected           Logger   LOG            = Logger.getLogger(this.getClass());
    
    // These 3 methods are likely candidates to be overwritten...
    
    /**
     * <code>OnWorkError</code> is called when an error happens while working with the data inside a
     * handler's <code>handleSubmittedData()</code> routine.
     * If you really know what you are doing you can add additional output to the ResultDocument here,
     * but this is almost always a bad idea.
     * The default implementation simply calls the {@link renderDefault(IWrapperContainer container)} method.
     *
     * @param container an <code>IWrapperContainer</code> value
     * @see de.schlund.pfixcore.workflow.app.ResdocFinalizer#onWorkError(IWrapperContainer) 
     */
    public void onWorkError(IWrapperContainer container) throws Exception{
        renderDefault(container);
    }

    /**
     * <code>onRetrieveStatus</code> is called after a call to
     * IHandlerAction.inserCurrentStatus(container), that means whenever
     * all (currently active) IHandlers are called to insert their current status
     * Into the ResultDocument. Use this Routine to do additional stuff here.
     * If you really know what you are doing you can add additional output to the ResultDocument here,
     * but this is almost always a bad idea.
     * The default implementation simply calls the {@link renderDefault(IWrapperContainer container)} method.
     *
     * @param container an <code>IWrapperContainer</code> value
     * @see de.schlund.pfixcore.workflow.app.ResdocFinalizer#onRetrieveStatus(IWrapperContainer) 
     */
    public void onRetrieveStatus(IWrapperContainer container) throws Exception{
        renderDefault(container);
    }

    /**
     * <code>onSuccess</code> is called when there has been no error handling a request that submitted data.
     * If you really know what you are doing you can add additional output to the ResultDocument here,
     * but this is almost always a bad idea.
     * The default implementation simply calls the {@link renderDefault(IWrapperContainer container)} method.
     *
     * @param container an <code>IWrapperContainer</code> value
     * @see de.schlund.pfixcore.workflow.app.ResdocFinalizer#onSuccess(IWrapperContainer) 
     */
    public void onSuccess(IWrapperContainer container) throws Exception {
        renderDefault(container);
    }

    /**
     * The default implementation does nothing. You normally want to overwrite this but only if you really know what you are doing.
     * Adding output to the Resultdocument is almost always a bad idea. Doing additional other work may be useful, but keep in mind
     * that this method will not always be called (see the {@link onSuccess(IWrapperContainer container)} method for an example).
     *
     * @param container an <code>IWrapperContainer</code> value
     */
    protected void renderDefault(IWrapperContainer container) throws Exception {
        // 
    }
}

// ResdocSimpleFinalizer
