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

package org.pustefixframework.container.spring.beans.internal;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

import org.pustefixframework.resource.InputStreamResource;

/**
 * Resource from an OSGi bundle.  
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class BundleResource implements InputStreamResource {

    private URI uri;
    private URI originalURI;
    private URL url;

    public BundleResource(URI uri, URI originallyRequestedURI, URL url) {
        this.originalURI = uri;
        if (originallyRequestedURI != null) {
            this.uri = originallyRequestedURI;
        } else {
            this.originalURI = uri;
        }
        this.url = url;
    }

    public InputStream getInputStream() throws IOException {
        return url.openStream();
    }

    public URI getOriginalURI() {
        return originalURI;
    }

    public URI[] getSupplementaryURIs() {
        return null;
    }

    public URI getURI() {
        return uri;
    }

}
