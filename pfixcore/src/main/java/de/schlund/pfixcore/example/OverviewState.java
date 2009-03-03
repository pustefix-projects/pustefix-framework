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

package de.schlund.pfixcore.example;

import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixcore.workflow.ContextResourceManager;
import de.schlund.pfixcore.workflow.app.StaticState;
import de.schlund.pfixxml.PfixServletRequest;

/**
 * OverviewState.java
 *
 *
 * Created: Tue Oct 23 01:55:30 2001
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 *
 *
 */

public class OverviewState extends StaticState {

    public boolean isAccessible(Context context, PfixServletRequest preq) throws Exception {
        ContextResourceManager crm = context.getContextResourceManager();
        ContextTrouser         ct  = (ContextTrouser) crm.getResource("de.schlund.pfixcore.example.ContextTrouser");
        ContextTShirt          cs  = (ContextTShirt) crm.getResource("de.schlund.pfixcore.example.ContextTShirt");
        ContextAdultInfo       cai = (ContextAdultInfo) crm.getResource("de.schlund.pfixcore.example.ContextAdultInfo");

        if (!cs.needsData() && !cai.needsData() &&
            (!cai.getAdult().booleanValue() || (cai.getAdult().booleanValue() && !ct.needsData()))) {
            return  true;
        } else {
            return false;
        }
    }
    
}// OverviewState
 
