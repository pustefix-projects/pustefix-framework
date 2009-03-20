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


import java.util.HashMap;

import org.apache.log4j.Logger;

import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixcore.workflow.FlowStepAction;
import de.schlund.pfixxml.ResultDocument;

/**
 * @author: jtl
 *
 *
 */

public class MsgFlowStepAction implements FlowStepAction {
    private String          text;
    private final static Logger LOG = Logger.getLogger(MsgFlowStepAction.class);
    
    public void setData(HashMap<String, String> data) {
        this.text = data.get("text");
    }

    public void doAction(Context context, ResultDocument resdoc) throws Exception {
        LOG.error(text);
    }

}