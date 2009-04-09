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

import java.util.Properties;

import de.schlund.pfixcore.workflow.FlowStepAction;

/**
 * Provides configuration for a pageflow action. A pageflow action
 * is attached to a condition.
 * 
 * @see PageFlowStepActionConditionConfig 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public interface PageFlowStepActionConfig {

    /**
     * Returns the class that is used to construct the action object.
     * 
     * @return class of the action object
     */
    Class<? extends FlowStepAction> getActionType();

    /**
     * Returns a properties map containing parameters for the action.
     * 
     * @return parameters for the action
     */
    Properties getParams();

}