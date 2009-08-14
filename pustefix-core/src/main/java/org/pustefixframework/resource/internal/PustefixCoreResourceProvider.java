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

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;

import org.pustefixframework.resource.Resource;
import org.pustefixframework.resource.ResourceLoader;
import org.pustefixframework.resource.ResourceProvider;
import org.pustefixframework.resource.support.URLResourceImpl;


/**
 * Provides resources from Pustefix Core's PFX-INF directory using
 * the pustefixcore scheme.  
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class PustefixCoreResourceProvider implements ResourceProvider {
    
    private final static String[] SUPPORTED_SCHEMES = new String[] { "pustefixcore" };
    
    public Resource[] getResources(URI uri, URI originallyRequestedURI, ResourceLoader resourceLoader) {
        if (uri.getScheme() == null || !uri.getScheme().equals("pustefixcore")) {
            throw new IllegalArgumentException("Cannot handle URI \"" + uri.toASCIIString() + "\": Scheme is not supported");
        }
        if (uri.getAuthority() != null && uri.getAuthority().trim().length() != 0) {
            throw new IllegalArgumentException("Authority part is not allowed in URI \"" + uri.toASCIIString() + "\"");
        }
        if (uri.getPath() == null || !uri.getPath().startsWith("/")) {
            throw new IllegalArgumentException("Error: URI \"" + uri.toASCIIString() + "\" does not specify an absolute path");
        }
        Enumeration<URL> en;
        try {
            en = this.getClass().getClassLoader().getResources("PUSTEFIX-INF" + uri.getPath());
        } catch (IOException e) {
            return null;
        }
        ArrayList<URLResourceImpl> resources = new ArrayList<URLResourceImpl>();
        while (en != null && en.hasMoreElements()) {
            URL url = (URL) en.nextElement();
            URLResourceImpl resource = new URLResourceImpl(uri, originallyRequestedURI, url);
            resources.add(resource);
        }
        if (resources.size() == 0) {
            return null;
        }
        return resources.toArray(new Resource[resources.size()]);
    }

    public String[] getSchemes() {
        return SUPPORTED_SCHEMES;
    }

}
