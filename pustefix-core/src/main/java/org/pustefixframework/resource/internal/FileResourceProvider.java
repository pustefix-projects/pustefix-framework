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

package org.pustefixframework.resource.internal;

import java.io.File;
import java.net.URI;

import org.pustefixframework.resource.FileResource;
import org.pustefixframework.resource.Resource;
import org.pustefixframework.resource.ResourceProvider;
import org.pustefixframework.resource.support.FileResourceImpl;


/**
 * Provides resources from the file system using the file scheme.  
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class FileResourceProvider implements ResourceProvider {
    
    private final static String[] SUPPORTED_SCHEMES = new String[] { "file" };
    
    public Resource[] getResources(URI uri, URI originallyRequestedURI) {
        if (uri.getScheme() == null || !uri.getScheme().equals("file")) {
            throw new IllegalArgumentException("Cannot handle URI \"" + uri.toASCIIString() + "\": Scheme is not supported");
        }
        if (uri.getAuthority() != null && uri.getAuthority().trim().length() != 0) {
            throw new IllegalArgumentException("Authority part is not allowed in URI \"" + uri.toASCIIString() + "\"");
        }
        if (uri.getPath() == null || !uri.getPath().startsWith("/")) {
            throw new IllegalArgumentException("Error: URI \"" + uri.toASCIIString() + "\" does not specify an absolute path");
        }
        File file = new File(uri.getPath());
        FileResource resource = new FileResourceImpl(uri, originallyRequestedURI, file);
        return new Resource[] { resource };
    }

    public String[] getSchemes() {
        return SUPPORTED_SCHEMES;
    }

}
