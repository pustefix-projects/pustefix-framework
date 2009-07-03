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
import java.util.Map;


/**
 * Abstract implementation of {@link ResourceLoader}, implementing most methods.  
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public abstract class AbstractResourceLoader implements ResourceLoader {

    public Resource getResource(URI uri) {
        return getResource(uri, null);
    }

    public Resource getResource(URI uri, Map<String, ?> parameters) {
        Resource[] resources = getResources(uri, parameters);
        if (resources != null) {
            return resources[0];
        } else {
            return null;
        }
    }

    public Resource[] getResources(URI uri) {
        return getResources(uri, null);
    }
}
