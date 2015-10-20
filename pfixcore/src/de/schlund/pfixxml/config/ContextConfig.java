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

import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Provides configuration for a context instance.
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public interface ContextConfig {

    /**
     * Returns name of auth page or <code>null</code> if there is no need for
     * the context to require user authentication.
     * @return
     */
    String getAuthPage();

    /**
     * Returns name of the flow to use when the user enters the site without
     * specifying a specific page.
     * 
     * @return name of default flow
     */
    String getDefaultFlow();

    /**
     * Returns a list of the configuration for all context resources that should
     * be created by the context.
     * 
     * @return list of configuration for alle context resources
     */
    List<? extends ContextResourceConfig> getContextResourceConfigs();

    /**
     * Returns the configuration for the context resource of the specified class
     * 
     * @param clazz class of the context resource
     * @return configuration object for the context resource
     */
    ContextResourceConfig getContextResourceConfig(Class clazz);

    /**
     * Returns a map that maps interfaces to the corresponding context resource
     * configurations.
     * 
     * @return map containing interface to context resource configuration mapping
     */
    Map<Class, ? extends ContextResourceConfig> getInterfaceToContextResourceMap();

    /**
     * Returns a list of configurations for all pageflows.
     * 
     * @return list of pageflow configurations
     */
    List<? extends PageFlowConfig> getPageFlowConfigs();

    /**
     * Returns the configuration for the pageflow specified by <code>name</code>.
     * 
     * @param name name of the pageflow
     * @return pageflow configuration
     */
    PageFlowConfig getPageFlowConfig(String name);

    /**
     * Returns a list of configurations for all pagerequests.
     * 
     * @return list of all pagerequest configurations.
     */
    List<? extends PageRequestConfig> getPageRequestConfigs();

    /**
     * Returns the configuration for the pagerequest specified by <code>name</code>.
     * 
     * @param name name of the pagerequest
     * @return pagerequest configuration
     */
    PageRequestConfig getPageRequestConfig(String name);

    /**
     * Returns a list of all start interceptors.
     * 
     * @return list of start interceptors
     */
    List<Class> getStartInterceptors();

    /**
     * Returns a list of all end interceptors.
     * 
     * @return list of end interceptors
     */
    List<Class> getEndInterceptors();

    /**
     * Returns the path to the file containing the navigation tree. The path
     * is specified relative to the Pustefix docroot.
     * 
     * @return path to navigation structure XML file
     */
    String getNavigationFile();

    /**
     * Returns configuration properties for the context instance.
     * 
     * @return properties specifying additional configuration options for
     * the context
     */
    Properties getProperties();

    /**
     * Returns true if the context shall synchronize on session objects to ensure
     * that only one request is concurrently processed per session. 
     * 
     * @return flag indicating whether to synchronize on sessions
     */
    boolean isSynchronized();

    RoleConfig getRoleConfig(String roleName);
    Map<String,RoleConfig> getRoleConfigs();
    boolean hasRoleConfigs();
    
}