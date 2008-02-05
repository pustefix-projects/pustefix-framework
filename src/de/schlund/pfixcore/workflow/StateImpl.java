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

package de.schlund.pfixcore.workflow;

import org.apache.log4j.Logger;

import de.schlund.pfixcore.util.StateUtil;
import de.schlund.pfixxml.PfixServletRequest;
import de.schlund.pfixxml.ResultDocument;

/**
 * @author jtl
 */

public abstract class StateImpl implements State {
  
    protected final static Logger CAT = Logger.getLogger(StateImpl.class);

    public  static final String PROP_INSERTCR = "insertcr";

    /**
     * @see de.schlund.pfixcore.util.StateUtil#isDirectTrigger(Context, PfixServletRequest)
     */
    public final boolean isDirectTrigger(Context context, PfixServletRequest preq) {
        return StateUtil.isDirectTrigger(context, preq);
    }
    

    /**
     * @see de.schlund.pfixcore.util.StateUtil#isSubmitTrigger(Context, PfixServletRequest)
     */    
    public final boolean isSubmitTrigger(Context context, PfixServletRequest preq) {
        return StateUtil.isSubmitTrigger(context, preq);
    }


    /**
     * @see de.schlund.pfixcore.util.StateUtil#isSubmitAuthTrigger(Context, PfixServletRequest)
     */    
    public final boolean isSubmitAuthTrigger(Context context, PfixServletRequest preq) {
        return StateUtil.isSubmitAuthTrigger(context, preq);
    }
    

    /**
     * @see de.schlund.pfixcore.util.StateUtil#createDefaultResultDocument(Context)
     */
    protected ResultDocument createDefaultResultDocument(Context context) throws Exception {
        return StateUtil.createDefaultResultDocument(context);
    }
    
    
    /**
     * @see de.schlund.pfixcore.util.StateUtil#renderContextResources(Context, ResultDocument)
     */
    protected void renderContextResources(Context context, ResultDocument resdoc) throws Exception {
        StateUtil.renderContextResources(context, resdoc);
    }

    
    /**
     * @see de.schlund.pfixcore.util.StateUtil#addResponseHeadersAndType(Context, ResultDocument)
     */
    protected void addResponseHeadersAndType(Context context, ResultDocument resdoc) {
        StateUtil.addResponseHeadersAndType(context, resdoc);
    }


    /**
     * This default implementation returns <code>true</code>. You may want to override this.
     * 
     * @param context The Context of the current session/user.
     * 
     * @param preq The current PfixServletRequest object, representing the request
     * being processed 
     *
     * @exception Exception if anything goes wrong in the process.
     */    
    public boolean isAccessible(Context context, PfixServletRequest preq) throws Exception {
        return true;
    }

    /**
     * This default implementation returns <code>true</code>. You may want to override this.
     * 
     * @param context The Context of the current session/user.
     * 
     * @param preq The current PfixServletRequest object, representing the request
     * being processed 
     *
     * @exception Exception if anything goes wrong in the process.
     */    
    public boolean needsData(Context context, PfixServletRequest preq) throws Exception {
        return true;
    }
    
    /**
     * You need to implement the state logic (aka business logic) in this method.
     *
     * @param context The Context of the current session/user.
     * 
     * @param preq The current PfixServletRequest object, representing the request
     * being processed 
     *
     * @exception Exception if anything goes wrong in the process.
     */
    public abstract ResultDocument getDocument(Context context, PfixServletRequest preq) throws Exception;

}
