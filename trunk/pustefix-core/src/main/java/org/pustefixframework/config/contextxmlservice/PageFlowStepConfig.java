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

package org.pustefixframework.config.contextxmlservice;

import java.util.List;

/**
 * Provides configuration for a step of a page flow.
 * 
 * @see PageFlowConfig
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public interface PageFlowStepConfig {

    /**
     * Returns the name of the page this flow step refers to.
     * 
     * @return name of the page used by this flow step
     */
    String getPage();

    /**
     * If <code>true</code> forces the pageflow to stop at this step when
     * coming from a step before this step. If <code>false</code> the flow
     * will only stop at this step if the page needs input.
     * 
     * @return flag indicating wheter to always stop at this step
     */
    boolean isStopHere();

    /**
     * Returns a list of conditions and their corresponding actions.
     * 
     * @return list of conditions for pageflow actions
     */
    List<? extends PageFlowStepActionConditionConfig> getActionConditions();

    /**
     * Signals whether to check for all conditions.
     * If <code>true</code> all conditions for pageflow actions will be 
     * checked and their corresponding actions be executed. If 
     * <code>false</code> only the actions for the first matching condition
     * will be executed. 
     * 
     * @return flag indicating whether to check all conditions for pageflow
     * actions
     */
    boolean isApplyAllConditions();

}