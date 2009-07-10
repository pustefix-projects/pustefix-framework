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
 * Base class for all resources.  
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public interface Resource {

    /**
     * Returns exactly the URI that was used to get this resource
     * instance.
     * 
     * @return URI that was used to retrieve this resource
     */
    URI getURI();
    
    /**
     * Returns the URI of the origin of this resource. This URI must
     * return this and only this resource, this means it may not
     * be ambiguous and point to another or more than one resource. 
     * 
     * @return unique URI, referring to only this resource
     */
    URI getOriginalURI();
    
    /**
     * This method may return a list of URIs that refer to this resource.
     * This list must not contain the URIs returned by {@link #getURI()}
     * or {@link #getOriginalURI()}. The implementation of this method
     * is optional. Resource implementations not supporting this method
     * must return <code>null</code>. If no supplementary URIs are known
     * for this resource, <code>null</code> should be returned, too.
     * 
     * @return a list of URIs known to be pointing to this resource or
     *  <code>null</code>
     */
    URI[] getSupplementaryURIs();
    
}
