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
 */

package de.schlund.pfixcore.workflow.context;

import org.w3c.dom.Element;

import de.schlund.pfixcore.exception.PustefixApplicationException;
import de.schlund.pfixcore.exception.PustefixCoreException;
import de.schlund.pfixcore.workflow.PageFlowContext;
import de.schlund.pfixxml.ResultDocument;

/**
 * Interface for page flow engines. Only intended for internal use.  
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public interface PageFlow {
    
    /**
     * Checks whether a page is part of this pageflow.
     * 
     * @param pagename name of the page (without variant)
     * @param context contains information about the request and session state
     * @return <code>true</code> if the page is part of this page flow, 
     * <code>false</code> otherwise
     */
    boolean containsPage(String pagename, PageFlowContext context);
    
    /**
     * Triggers pageflow logic.
     * 
     * @param context contains information about the request and session state
     * @return name of the next page to show
     * @throws PustefixApplicationException if an exception is thrown by the
     * application code called by this method
     */
    String findNextPage(PageFlowContext context) throws PustefixApplicationException;
    
    /**
     * Return the name of this page flow (including variant).
     * 
     * @return name of this page flow
     */
    String getName();
    
    /**
     * Return the name of this page flow (without variant).
     * 
     * @return short name of this page flow
     */
    String getRootName();
    
    /**
     * Returns the name of the page that is to be shown, when the page flow has 
     * finished. This page is usually not part of the page flow.
     * 
     * @return name of the final page
     */
    String getFinalPage();
    
    /**
     * Checks whether a part of this page flow preceding the current step needs 
     * user input. The actual meaning depends on the implementation.
     * 
     * @param context contains information about the request and session state
     * @return <code>true</code> if preceding parts of this page flow require
     * user input, <code>false</code> otherwise
     * @throws PustefixApplicationException if an exception is thrown by the
     * application code called by this method
     */
    boolean precedingFlowNeedsData(PageFlowContext context) throws PustefixApplicationException;
    
    /**
     * Can be used to trigger arbitrary actions after a request for a page has 
     * been handled. For example this might be actions that should be triggered 
     * in order to modify the application state or select the next page to 
     * be shown.
     * 
     * @param resdoc XML document with the result tree generated for the page that
     * has just been handled.
     * @param context contains information about the request and session state
     * @throws PustefixApplicationException if an exception is thrown by the
     * application code called by this method
     * @throws PustefixCoreException if an error occurs within this pageflow's code
     */
    void hookAfterRequest(ResultDocument resdoc, PageFlowContext context) throws PustefixApplicationException, PustefixCoreException;
    
    /**
     * Signals whether the {@link #hookAfterRequest(ResultDocument, PageFlowContext)}
     * method shall be called after handling the current request. For performance
     * reasons this method should only return <code>true</code> if the hook method
     * is actually going to do something. If it returns <code>false</code> the
     * whole {@link ResultDocument} does not have to be created, which will save
     * CPU time and memory.
     * 
     * @param context contains information about the request and session state
     * @return <code>true</code> if and only if the <code>hookAfterRequest</code>
     * has to be called, <code>false</code> otherwise
     */
    boolean hasHookAfterRequest(PageFlowContext context);
    
    /**
     * Adds information about the current state of the pageflow to the
     * {@link ResultDocument}. This information might be used by the frontend,
     * for example to generate a "back" button, or to show progress information.
     * 
     * @param root XML element that is used for the pageflow information. All 
     * information about the pageflow should be attached below this element.
     * @param context contains information about the request and session state
     */
    void addPageFlowInfo(Element root, PageFlowContext context);
    
}
