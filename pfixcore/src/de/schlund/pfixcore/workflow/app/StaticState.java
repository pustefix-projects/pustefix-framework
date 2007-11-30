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

import java.util.Properties;

import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixcore.workflow.StateImpl;
import de.schlund.pfixxml.PfixServletRequest;
import de.schlund.pfixxml.ResultDocument;

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
    // private static final String CONTINUEONSUBMIT  = "state.continueonsubmit";
    private static final String NEEDSDATA         = "state.needsdata";
    
    /**
     * @see de.schlund.pfixcore.workflow.State#getDocument(Context, PfixServletRequest)
     */
    @Override
    public ResultDocument getDocument(Context context, PfixServletRequest preq) throws Exception {
        ResultDocument resdoc = createDefaultResultDocument(context);
        Properties     props  = context.getPropertiesForCurrentPageRequest();

        context.prohibitContinue();
        return resdoc;
    }

    @Override
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
