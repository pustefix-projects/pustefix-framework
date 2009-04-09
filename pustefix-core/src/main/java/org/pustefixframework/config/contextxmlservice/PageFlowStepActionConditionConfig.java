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
 * Provides configuration for conditional pageflow actions  
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public interface PageFlowStepActionConditionConfig {

    /**
     * Returns a XPath expression that is evaluated in order to check, whether
     * the pageflow actions corresponding to this condition should be executed.
     * The actions will be run if and only if the evaluation of the XPathExpression
     * in the context of the SPDocument returns <code>true</code>.
     * 
     * @return XPath expression containing condition
     */
    String getXPathExpression();

    /**
     * List of actions to run if the condition is met.
     * 
     * @return actions to run on condition
     */
    List<? extends PageFlowStepActionConfig> getActions();

}