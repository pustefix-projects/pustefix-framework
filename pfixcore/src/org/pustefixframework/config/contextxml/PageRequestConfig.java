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

package org.pustefixframework.config.contextxml;

import java.util.Map;
import java.util.Properties;

import de.schlund.pfixcore.auth.AuthConstraint;

/**
 * Provides configuration for a specific page.  
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public interface PageRequestConfig {

    /**
     * Returns the name of the page. This name includes the variant
     * string if applicable. The page name has to be unique for a servlet
     * and should be unique for the whole project.
     * 
     * @return name of the page
     */
    String getPageName();

    /**
     * If <code>true</code> a SSL connection is forced when this page
     * is requested.
     * 
     * @return flag indicating whether to use a secure connection for this page
     */
    boolean isSSL();

    /**
     * Returns the name of the bean that handles requests for this
     * page.
     * 
     * @return bean name
     */
    String getBeanName();

    AuthConstraint getAuthConstraint();
    
    public String getDefaultFlow();
    
    /**
     * Returns properties defined for this page.
     * 
     * @return properties for this page
     */
    Properties getProperties();
    
    public Map<String, ? extends ProcessActionPageRequestConfig> getProcessActions();
}