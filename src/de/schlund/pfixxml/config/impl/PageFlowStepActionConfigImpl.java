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

package de.schlund.pfixxml.config.impl;

import java.util.Properties;

import de.schlund.pfixcore.workflow.FlowStepAction;
import de.schlund.pfixxml.config.PageFlowStepActionConfig;

/**
 * Stores configuration for an action that is triggered by a PageFlow step.
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class PageFlowStepActionConfigImpl implements PageFlowStepActionConfig {
    
    private Class<? extends FlowStepAction> actionType = null;
    private Properties params = new Properties();

    public void setActionType(Class<? extends FlowStepAction> clazz) {
        this.actionType = clazz;
    }
    
    /* (non-Javadoc)
     * @see de.schlund.pfixxml.config.PageFlowStepActionConfig#getActionType()
     */
    public Class<? extends FlowStepAction> getActionType() {
        return this.actionType;
    }
    
    public void setParam(String name, String value) {
        this.params.setProperty(name, value);
    }
    
    public String getParam(String name) {
        return this.params.getProperty(name);
    }
    
    /* (non-Javadoc)
     * @see de.schlund.pfixxml.config.PageFlowStepActionConfig#getParams()
     */
    public Properties getParams() {
        return this.params;
    }
}
