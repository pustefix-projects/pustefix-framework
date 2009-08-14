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

package org.pustefixframework.resource;

import java.net.URI;


/**
 * Provides access to resources for a specific URI scheme.  
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public interface ResourceProvider {
    /**
     * Returns all resources for uri or <code>null</code> if no resources
     * are found.
     * 
     * @param uri URI specifying the resources
     * @param originallyRequestedURI URI that was originally used to request
     *  the resource or <code>null</code> if identical to <code>uri</code>.
     * @param resourceLoader the resource loader that should be used in order
     *  to resolve URIs which are part of this URI. The resource provider has
     *  to ensure that this process does not result in a recursive loop.
     * @return list of resources or <code>null</code>
     */
    Resource[] getResources(URI uri, URI originallyRequestedURI, ResourceLoader resourceLoader);
    
    /**
     * Returns the list of schemes supported by this provider.
     * 
     * @return list of supported schemes
     */
    String[] getSchemes();
}
