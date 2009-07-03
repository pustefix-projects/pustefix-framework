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

import java.util.Map;


/**
 * Orders resources if more than one resource is available for a URI.
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public interface ResourceSelector {
    /**
     * Orders a list of <code>resources</code> using the given
     * <code>parameters</code>. An implementation should order the
     * resources in such a way that "better matching" resources are
     * first on the list. If two resources are "equally matching" their
     * order should be preserved.
     * 
     * @param resources
     * @param parameters list of parameters that was given by the client
     *  requesting the resource(s)
     * @return ordered list of resources or <code>null</code> if no resources
     *  are returned
     */
    Resource[] selectResources(Resource[] resources, Map<String, ?> parameters);
}
