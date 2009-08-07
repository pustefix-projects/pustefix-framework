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

package org.pustefixframework.extension;

import java.util.List;

import org.pustefixframework.config.contextxmlservice.ProcessActionPageRequestConfig;
import org.pustefixframework.config.contextxmlservice.ProcessActionStateConfig;

/**
 * Extension for a {@link PageRequestProcessActionConfigExtensionPoint}.  
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public interface PageRequestProcessActionConfigExtension extends Extension {

    /**
     * Returns the list of {@link ProcessActionConfig} this extension provides.
     * 
     * @return list of IWrapper configurations
     */
    List<ProcessActionConfig> getProcessActionConfigs();

    /**
     * Simple class that is used as a holder for a pair of of a
     * {@link ProcessActionPageRequestConfig} and a
     * {@link ProcessActionStateConfig}.
     * 
     * @author Sebastian Marsching <sebastian.marsching@1und1.de>
     */
    public interface ProcessActionConfig {

        /**
         * Returns name of the process action.
         * 
         * @return process action name
         */
        String getName();

        /**
         * Returns process action configuration for the page request.
         * 
         * @return process action page request configuration
         */
        ProcessActionPageRequestConfig getProcessActionPageRequestConfig();

        /**
         * Returns process action configuration for the state. May return 
         * <code>null</code>.
         * 
         * @return process action state configuration
         */
        ProcessActionStateConfig getProcessActionStateConfig();
    }
}
