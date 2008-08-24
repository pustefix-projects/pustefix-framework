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

package org.pustefixframework.config.contextxmlservice.parser.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.pustefixframework.config.contextxmlservice.PageFlowStepConfig;


/**
 * Stores configuration for a PageFlow.
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class PageFlowStepConfigImpl implements PageFlowStepConfig {
    
    private String page = null;
    private boolean stopHere = false;
    private ArrayList<PageFlowStepActionConditionConfigImpl> continueConditions = new ArrayList<PageFlowStepActionConditionConfigImpl>();
    private boolean applyAllConditions = false;

    
    public void setPage(String page) {
        this.page = page;
    }
    
    /* (non-Javadoc)
     * @see de.schlund.pfixxml.config.PageFlowStepConfig#getPage()
     */
    public String getPage() {
        return this.page;
    }
    
    public void setStopHere(boolean stop) {
        this.stopHere = stop;
    }
    
    /* (non-Javadoc)
     * @see de.schlund.pfixxml.config.PageFlowStepConfig#isStopHere()
     */
    public boolean isStopHere() {
        return this.stopHere;
    }
    
    public void addActionCondition(PageFlowStepActionConditionConfigImpl config) {
        this.continueConditions.add(config);
    }
    
    /* (non-Javadoc)
     * @see de.schlund.pfixxml.config.PageFlowStepConfig#getActionConditions()
     */
    public List<PageFlowStepActionConditionConfigImpl> getActionConditions() {
        return Collections.unmodifiableList(this.continueConditions);
    }
    
    public void setApplyAllConditions(boolean applyAll) {
        this.applyAllConditions  = applyAll;
    }
    
    /* (non-Javadoc)
     * @see de.schlund.pfixxml.config.PageFlowStepConfig#isApplyAllConditions()
     */
    public boolean isApplyAllConditions() {
        return this.applyAllConditions;
    }
}
