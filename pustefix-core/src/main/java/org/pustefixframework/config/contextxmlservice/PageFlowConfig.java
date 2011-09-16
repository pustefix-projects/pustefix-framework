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
 * Provides configuration for a page flow.  
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public interface PageFlowConfig {

    /**
     *  Returns name of the page flow. The name is used to uniquely identify
     *  a pageflow in a servlet configuration.
     *  
     * @return name of the page flow
     */
    String getFlowName();

    /**
     * Returns name of the final page of the page flow. The final page is the
     * page that the flow will jump to, when the flow has been processed (no
     * page of the flow needs input).
     * 
     * @return the name of the final page or <code>null</code> if no final
     * page is defined
     */
    String getFinalPage();

    /**
     * If <code>true</code>, will force the page flow to stop at each page,
     * even if it does not require input.
     * 
     * @return flag indicating whether to always stop at the next page after
     * a submit 
     */
    boolean isStopNext();

    /**
     * Returns a list of configurations for each flow step.
     * 
     * @return configurations of the flow steps
     */
    List<? extends PageFlowStepConfig> getFlowSteps();

}