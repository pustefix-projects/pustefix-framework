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

import java.util.Properties;

/**
 * Provides configuration for an instance of {@link de.schlund.pfixxml.ServletManager} or one of its
 * child classes.  
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public interface ServletManagerConfig {

    /**
     * If <code>true</code> a secure communication channel should be used for
     * the servlet.
     * 
     * @return flag indicating whether to force SSL for the servlet
     */
    boolean isSSL();

    /**
     * Returns configuration properties for the servlet.
     * 
     * @return configuration parameters
     */
    Properties getProperties();

    /**
     * Returns true if the data base that was used to create this configuration
     * instance has changed and the configuration should be reloaded.
     * 
     * @return flag indicating whether the configuration should be reloaded
     */
    boolean needsReload();

}