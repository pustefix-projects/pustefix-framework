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
 * Stores configuration for a PageFlow
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class PageFlowConfig {
    
    private String flowName = null;
    private String finalPage = null;
    private boolean stopNext = false;
    private ArrayList<PageFlowStepConfig> flowSteps = new ArrayList<PageFlowStepConfig>();
    
    public PageFlowConfig(String name) {
        this.flowName = name;
    }
    
    public String getFlowName() {
        return this.flowName;
    }
    
    public void setFinalPage(String page) {
        this.finalPage = page;
    }
    
    public String getFinalPage() {
        return this.finalPage;
    }
    
    public void setStopNext(boolean stop) {
        this.stopNext = stop;
    }
    
    public boolean isStopNext() {
        return this.stopNext;
    }
    
    public void addFlowStep(PageFlowStepConfig config) {
        this.flowSteps.add(config);
    }
    
    public PageFlowStepConfig[] getFlowSteps() {
        return this.flowSteps.toArray(new PageFlowStepConfig[0]);
    }
}
