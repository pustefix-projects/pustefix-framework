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

import java.util.Map;
import java.util.Properties;

import de.schlund.pfixcore.generator.IWrapper;

/**
 * Provides configuration for a specific page.  
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public interface PageRequestConfig {

    /**
     * Enum type for activity handling policy. The policy will not be applied
     * on handlers with activeignore set to true.
     */
    public enum Policy {
        /**
         * Signal isActive() for the state if any handler is active.
         */
        ANY,
        /**
         * Signal isActive() for the state only if all handlers are active.
         */
        ALL,
        /**
         * Signal isActive() even if none of the handlers is active.
         */
        NONE
    }
    
    /**
     * Returns the name of the page. This name includes the variant
     * string if applicable. The page name has to be unique for a servlet
     * and should be unique for the whole project.
     * 
     * @return name of the page
     */
    String getPageName();

    /**
     * Whether to store the XML tree returned by the state. If <code>true</code>
     * the XML tree is stored. In most cases, there is no need to explicitly
     * store the XML tree.
     * 
     * @return flag indicating wheter to store the XML tree
     * @see de.schlund.pfixxml.SPDocument
     */
    boolean isStoreXML();

    /**
     * If <code>true</code> a SSL connection is forced when this page
     * is requested.
     * 
     * @return flag indicating whether to use a secure connection for this page
     */
    boolean isSSL();

    /**
     * Returns the class that is used to construct the state that
     * serves this page.
     * 
     * @return class of the state for this page
     */
    Class getState();

    /**
     * Returns the policy for the <code>isActive()</code> method.
     * 
     * @return policy for isActive() check
     */
    Policy getIWrapperPolicy();

    /**
     * Returns the class of the finalizer for the page (use with caution).
     * 
     * @return finalizer class or <code>null</code> if there is no finalizer
     */
    Class getFinalizer();

    /**
     * Returns the list of IWrappers for this page. IWrappers are used
     * for input handling. The map returned has the form prefix => IWrapperConfig.
     * 
     * @return list of IWrappers
     */
    Map<String, ? extends IWrapperConfig> getIWrappers();

    /**
     * Returns the lis of auxiliary IWrappers for the page. They are only
     * used on an authentication page to supply additinal information. These
     * wrappers are used on each request - not only on explicit authentication
     * requests, so extreme care should be taken when choosing prefixes, as they
     * are global.
     * 
     * @return map containinge prefix to authwrapper mappings
     */
    Map<String, Class<? extends IWrapper>> getAuxWrappers();

    /**
     * Returns context resources defined for this page. The map has the form
     * tagname => context resource class. Each context resource specified here
     * will be included in the result XML tree.
     * 
     * @return mapping of tagname to context resource class
     */
    Map<String, Class> getContextResources();

    /**
     * Returns properties defined for this page.
     * 
     * @return properties for this page
     */
    Properties getProperties();

    /**
     * Return prefix of the auth wrapper. This is only valid for an
     * authentication page.
     * 
     * @return prefix for the auth wrapper or <code>null</code> if this
     * is not an auth page
     */
    String getAuthWrapperPrefix();

    /**
     * Return class of the auth wrapper. This is only valid for an
     * authentication page.
     * 
     * @return class of the auth wrapper or <code>null</code> if this
     * is not an auth page
     */
    Class<? extends IWrapper> getAuthWrapperClass();
    
}