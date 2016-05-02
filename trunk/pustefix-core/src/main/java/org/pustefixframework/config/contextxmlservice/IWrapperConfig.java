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

import de.schlund.pfixcore.generator.IHandler;
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
     * If <code>true</code> the state will not check whether the correspondings
     * handler isActive() method returns true when checking whether a page is
     * accessible.
     * 
     * @return flag indicating handling of active state
     */
    boolean doCheckActive();

    /**
     * If <code>true</code> the IWrapper should enable logging.
     * 
     * @return flag indicating whether to enable logging
     */
    boolean getLogging();
    
    /**
     * Returns the scope in which the corresponding handler should be
     * instantiated.
     * 
     * @return Scope of the handler
     */
    String getScope();
    
    /**
     * Return the handler associated with this wrapper.
     * 
     * @return matching handler
     */
    IHandler getHandler();
    
    String getTenant();
    
}