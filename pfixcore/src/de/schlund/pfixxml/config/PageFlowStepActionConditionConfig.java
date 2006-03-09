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

package de.schlund.pfixxml.config;

import java.util.ArrayList;

/**
 * Stores configuration for a condtion that triggers PageFlow actions.
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class PageFlowStepActionConditionConfig {
    
    private String xPathExpression = null;
    private ArrayList<PageFlowStepActionConfig> actions = new ArrayList<PageFlowStepActionConfig>();

    
    public void setXPathExpression(String xpath) {
        this.xPathExpression = xpath;
    }
    
    public String getXPathExpression() {
        return this.xPathExpression;
    }
    
    public void addAction(PageFlowStepActionConfig action) {
        this.actions.add(action);
    }
    
    public PageFlowStepActionConfig[] getActions() {
        return this.actions.toArray(new PageFlowStepActionConfig[0]);
    }
}
