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

import de.schlund.pfixcore.generator.IWrapper;


/**
 * Provides configuration for {@link de.schlund.pfixcore.generator.IWrapper} instances.
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public interface IWrapperConfig {

    /**
     * Returns the prefix that is used for request parameters associated with
     * this wrapper instance.
     * 
     * @return prefix for request parameters
     */
    String getPrefix();

    /**
     * Returns the class that shall be used for instances of the wrapper.
     * 
     * @return wrapper class
     */
    Class<? extends IWrapper> getWrapperClass();

    /**
     * Specifies whether the next page in the current pageflow should be triggered
     * when a sumit to this specific wrapper is triggered. 
     * 
     * @return continue flag
     */
    boolean isContinue();

    /**
     * If <code>true</code> the state will not check whether the correspondings
     * handler isActive() method returns true when checking whether a page is
     * accessible.
     * 
     * @return flag indicating handling of active state
     */
    boolean isActiveIgnore();

    /**
     * If <code>true</code> the <code>retrieveCurrentStatus()</code> method
     * should always be called on the corresponding IHandler.
     * 
     * @return flag indicating retrieveCurrentStatus() handling
     */
    boolean isAlwaysRetrieve();

    /**
     * If <code>true</code> the IWrapper should enable logging.
     * 
     * @return flag indicating whether to enable logging
     */
    boolean getLogging();

}