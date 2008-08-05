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

package org.pustefixframework.config.directoutputservice;

import java.util.Properties;

/**
 * Provides configuration for a direct output page. This configuration is 
 * used by {@link de.schlund.pfixxml.DirectOutputServlet} to configure the
 * pages provided.   
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public interface DirectOutputPageRequestConfig {

    /**
     * Returns name identifying the page.
     * 
     * @return name of the page
     */
    String getPageName();
    
    String getAuthConstraintRef();

    /**
     * Returns the name of the bean that represents the state.
     * 
     * @return name of the bean representing the state
     */
    String getBeanName();

    /**
     * Returns extra configuration parameters.
     * 
     * @return configuration parameters
     */
    Properties getProperties();

}