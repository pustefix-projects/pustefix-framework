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

package de.schlund.pfixxml.resources;

import java.net.URI;

/**
 * Creates docroot resources using environment dependend methods.
 * A class implementing this interface provides Pustefix docroot 
 * resources. This task can be performed by using files in a directory
 * on the filesystem, files from a JAR file or any other mechanism
 * that can provide file like resources. 
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public interface DocrootResourceProvider {
    /**
     * Returns an instance of {@link DocrootResource} that represents
     * the resource specified by the given URI. A resource is always 
     * returned unless the URI provided is invalid or does not use the
     * pfixroot URI scheme. A DocrootResource object is returned even if
     * the specified resource does not exist.
     * 
     * @param uri URI using the pfixroot scheme
     * @return A DocrootResource that can be used to access the resource
     *  specified by the URI.  
     */
    DocrootResource getDocrootResource(URI uri);
}
