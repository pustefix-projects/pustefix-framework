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
 */

package org.pustefixframework.config.contextxmlservice.parser.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.pustefixframework.config.contextxmlservice.PageFlowConfig;


/**
 * Stores configuration for a PageFlow
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class PageFlowConfigImpl implements PageFlowConfig {
    
    private String flowName = null;
    private String finalPage = null;
    private boolean stopNext = false;
    private ArrayList<PageFlowStepConfigImpl> flowSteps = new ArrayList<PageFlowStepConfigImpl>();
    
    public PageFlowConfigImpl(String name) {
        this.flowName = name;
    }
    
    /* (non-Javadoc)
     * @see de.schlund.pfixxml.config.PageFlowConfig#getFlowName()
     */
    public String getFlowName() {
        return this.flowName;
    }
    
    public void setFinalPage(String page) {
        this.finalPage = page;
    }
    
    /* (non-Javadoc)
     * @see de.schlund.pfixxml.config.PageFlowConfig#getFinalPage()
     */
    public String getFinalPage() {
        return this.finalPage;
    }
    
    public void setStopNext(boolean stop) {
        this.stopNext = stop;
    }
    
    /* (non-Javadoc)
     * @see de.schlund.pfixxml.config.PageFlowConfig#isStopNext()
     */
    public boolean isStopNext() {
        return this.stopNext;
    }
    
    public void addFlowStep(PageFlowStepConfigImpl config) {
        this.flowSteps.add(config);
    }
    
    /* (non-Javadoc)
     * @see de.schlund.pfixxml.config.PageFlowConfig#getFlowSteps()
     */
    public List<PageFlowStepConfigImpl> getFlowSteps() {
        return Collections.unmodifiableList(this.flowSteps);
    }
}
