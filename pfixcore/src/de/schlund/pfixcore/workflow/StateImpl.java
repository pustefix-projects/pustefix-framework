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

import java.util.*;
import javax.servlet.http.*;
import org.w3c.dom.*;
import org.apache.log4j.*;
import de.schlund.pfixxml.*;

/**
 * @author jtl
 *
 *
 */

public abstract class StateImpl implements State {
    protected            Category CAT          = Category.getInstance(this.getClass().getName());
    private final static String   SENDDATA     = "__sendingdata";
    private final static String   SENDAUTHDATA = "__sendingauthdata";
    
    public final boolean isDirectTrigger(Context context, PfixServletRequest preq) {
        RequestParam sdreq = preq.getRequestParam(SENDDATA);
        return (!context.flowIsRunning() && (context.jumpToPageIsRunning() || !requestParamSaysSubmit(sdreq)));
    }
    
    public final boolean isSubmitTrigger(Context context, PfixServletRequest preq) {
        RequestParam sdreq = preq.getRequestParam(SENDDATA);
        return (isSubmitTriggerAny(context, sdreq));
    }
    
    public final boolean isSubmitAuthTrigger(Context context, PfixServletRequest preq) {
        RequestParam sdreq = preq.getRequestParam(SENDAUTHDATA);
        return (isSubmitTriggerAny(context, sdreq));
    }

    // private
    private boolean requestParamSaysSubmit(RequestParam sdreq) {
        if (sdreq != null) {
            String sd = sdreq.getValue();
            return (sd.equals("true") || sd.equals("1") || sd.equals("yes"));
        }
        return false;
    }
    
    
    private boolean isSubmitTriggerAny(Context context, RequestParam sdreq) {
        return (!context.flowIsRunning() && !context.finalPageIsRunning() &&
                !context.jumpToPageIsRunning() && requestParamSaysSubmit(sdreq));
    }

    // You may want to overwrite this 
    public boolean isAccessible(Context context, PfixServletRequest preq) throws Exception {
        return true;
    }

    // You may want to overwrite this 
    public boolean needsData(Context context, PfixServletRequest preq) throws Exception {
        return true;
    }
    
    // You need to implement the state logic in this method.
    public abstract ResultDocument getDocument(Context context, PfixServletRequest preq) throws Exception;
    
}
