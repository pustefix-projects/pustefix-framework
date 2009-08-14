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

/**
 * Resource that represents the content of an XML include part.
 * An instance of this resource always represents a specific version
 * (theme) of an include part.
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public interface IncludePartResource extends DOMElementResource, ThemedResource {

    /**
     * Returns the name of this include part. The name is an identifier
     * identifying an include part within a file. My return <code>null</code>
     * if no name is defined in the origin of this resource.
     * 
     * @return include part name or <code>null</code>
     */
    String getIncludePartName();

    /**
     * Returns the resource this include part has been read from.
     * If this include part was not read from a file (or file like resource)
     * this method returns <code>null</code>.
     * 
     * @return origin of this include part or <code>null</code>
     */
    InputStreamResource getOriginResource();
}
