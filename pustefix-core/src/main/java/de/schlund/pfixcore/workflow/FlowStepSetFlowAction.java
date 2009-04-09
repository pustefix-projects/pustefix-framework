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
 *
 */

package de.schlund.pfixcore.workflow;


import java.util.HashMap;

import de.schlund.pfixxml.ResultDocument;

/**
 * @author: jtl@schlund.de
 *
 */

public class FlowStepSetFlowAction implements FlowStepAction {
    private String pageflow = null;
    
    public void setData(HashMap<String, String> data) {
        pageflow = data.get("pageflow");
    }

    public void doAction(Context context, ResultDocument resdoc) throws Exception {
        if (pageflow != null && !pageflow.equals("")) {
            context.setCurrentPageFlow(pageflow);
        }
    }
}