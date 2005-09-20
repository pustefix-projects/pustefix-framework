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

package de.schlund.pfixcore.util;

import de.schlund.pfixcore.workflow.State;
import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixxml.PfixServletRequest;

/**
 * 
 * @author Benjamin Reitzammer <benjamin@schlund.de>
 * @version $Id$
 */
public class StateUtils {
  
  
    /**
     * 
     */
    public static boolean isDirectTrigger(Context context, PfixServletRequest preq) {
        RequestParam sdreq = preq.getRequestParam(State.SENDDATA);
        return (!context.flowIsRunning() && (context.jumpToPageIsRunning() || sdreq == null || !sdreq.isTrue()));
    }
    
    
    /**
     * 
     */
    public static boolean isSubmitTrigger(Context context, PfixServletRequest preq) {
        return (isSubmitTriggerAny(context, preq.getRequestParam(State.SENDDATA)));
    }
    
    
    /**
     * 
     */
    public static boolean isSubmitAuthTrigger(Context context, PfixServletRequest preq) {
        return isSubmitTriggerAny(context, preq.getRequestParam(State.SENDAUTHDATA));
    }
    
    
    // ============ private Helper methods ============
    
    
    /**
     * 
     */
    private static boolean isSubmitTriggerAny(Context context, RequestParam sdreq) {
        return (!context.flowIsRunning() 
                    && !context.finalPageIsRunning() 
                    && !context.jumpToPageIsRunning() 
                    && sdreq != null 
                    && sdreq.isTrue());
    }  
}
