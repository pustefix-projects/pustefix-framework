/*
 * Thisis file is part of PFIXCORE.
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

import de.schlund.pfixcore.util.PropertiesUtils;
import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixcore.workflow.ContextResource;
import de.schlund.pfixcore.workflow.ContextResourceManager;
import de.schlund.pfixcore.workflow.StateImpl;
import de.schlund.pfixxml.PerfEventType;
import de.schlund.pfixxml.PfixServletRequest;
import de.schlund.pfixxml.ResultDocument;
import de.schlund.pfixxml.SPDocument;
import de.schlund.pfixxml.XMLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

/**
 * StaticState.java
 *
 *
 * Created: Wed Oct 10 09:50:19 2001
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 *
 *
 */

public class StaticState extends StateImpl {
    private static final String CONTINUEONSUBMIT  = "state.continueonsubmit";
    private static final String NEEDSDATA         = "state.needsdata";
    
    /**
     * @see de.schlund.pfixcore.workflow.State#getDocument(Context, PfixServletRequest)
     */
    public ResultDocument getDocument(Context context, PfixServletRequest preq) throws Exception {
        ResultDocument resdoc = createDefaultResultDocument(context);
        Properties     props  = context.getPropertiesForCurrentPageRequest();

        String autocontinue = props.getProperty(CONTINUEONSUBMIT);
        if (autocontinue != null && autocontinue.equals("true") && isSubmitTrigger(context, preq) &&
            (context.isCurrentPageRequestInCurrentFlow() || context.isCurrentPageFlowRequestedByUser())) {
            // We continue, despite the fact that this is a StaticState
            return resdoc;
        } else {
            context.prohibitContinue();
            return resdoc;
        }
    }

    public boolean needsData(Context context, PfixServletRequest preq) throws Exception {
        Properties props     = context.getPropertiesForCurrentPageRequest();
        String     needsdata = props.getProperty(NEEDSDATA);
        if (needsdata != null && needsdata.equals("false")) {
            return false;
        } else {
            return true;
        }
    }


}// StaticState
