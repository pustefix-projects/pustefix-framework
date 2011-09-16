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

package org.pustefixframework.config.directoutputservice;

import java.util.List;

import org.pustefixframework.config.contextxmlservice.ServletManagerConfig;


/**
 * Provides configuration for {@link de.schlund.pfixxml.DirectOutputServlet}.  
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public interface DirectOutputServiceConfig extends ServletManagerConfig {

    /**
     * Returns the name of the corresponding {@link de.schlund.pfixxml.ContextXMLServlet}
     * instance. This instance is used in order to retrieve a {@link de.schlund.pfixcore.workflow.Context}
     * object for the session.
     * 
     * @return name of the corresponding servlet
     */
    String getExternalServletName();

    /**
     * If <code>true</code> the servlet synchronizes on the session (more precisely
     * on the context). This can be used to run not thread-safe application code.
     * 
     * @return flag indicating whether to synchronize on the user session
     */
    boolean isSynchronized();
    
    String getAuthConstraintRef();

    /**
     * Returns a list of pages configured for this servlet.
     * 
     * @return list of page configurations
     */
    List<? extends DirectOutputPageRequestConfig> getPageRequests();

    /**
     * Returns the page configuration for the page of the specified name.
     * 
     * @param page name of the page
     * @return configuration for <code>page</code> or <code>null</code> if
     * no page with the specified name is present
     */
    DirectOutputPageRequestConfig getPageRequest(String page);

}