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

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.pustefixframework.resource.Resource;
import org.pustefixframework.resource.ResourceProvider;
import org.pustefixframework.resource.support.URLResourceImpl;


/**
 * Provides resources from OSGi bundles. The bundle symbolic name is
 * retrieved from the authority part of the URI. If no authority part
 * is specified, the bundle this provider is running in, is used.  
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class BundleResourceProvider implements ResourceProvider {

    private BundleContext bundleContext;
    
    private final static String[] SUPPORTED_SCHEMES = new String[] {"bundle"};

    public BundleResourceProvider(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public Resource[] getResources(URI uri, URI originallyRequestedURI) {
        if (uri.getScheme() == null || !uri.getScheme().equals("bundle")) {
            throw new IllegalArgumentException("Cannot handle URI \"" + uri.toASCIIString() + "\": Scheme is not supported");
        }
        if (uri.getPath() == null || !uri.getPath().startsWith("/")) {
            throw new IllegalArgumentException("Error: URI \"" + uri.toASCIIString() + "\" does not specify an absolute path");
        }
        Bundle bundle = null;
        String bundleSymbolicName = uri.getAuthority();
        if (bundleSymbolicName == null || bundleSymbolicName.trim().length() == 0) {
            bundle = bundleContext.getBundle();
            bundleSymbolicName = bundle.getSymbolicName();
            String uriString = uri.toASCIIString();
            uriString = "bundle://" + bundleSymbolicName + uriString.substring(7);
            if (originallyRequestedURI == null) {
                originallyRequestedURI = uri;
            }
            try {
                uri = new URI(uriString);
            } catch (URISyntaxException e) {
                throw new RuntimeException("Error while trying to create URI \"" + uriString + "\"", e);
            }
            
        } else {
            Bundle[] allBundles = bundleContext.getBundles();
            for (int i = 0; i < allBundles.length; i++) {
                Bundle possibleBundle = allBundles[i];
                if (possibleBundle.getSymbolicName().equals(bundleSymbolicName) && possibleBundle.getState() >= Bundle.INSTALLED) {
                    bundle = possibleBundle;
                    break;
                }
            }
        }
        if (bundle == null || uri.getPath() == null) {
            return null;
        }
        int lastSlashIndex = uri.getPath().lastIndexOf('/');
        String path = null;
        String filename = null;
        if (lastSlashIndex == -1) {
            path = "/";
            filename = uri.getPath();
        } else {
            path = uri.getPath().substring(0, lastSlashIndex);
            if (path.startsWith("/") && path.length() > 1) {
                path = path.substring(1);
            }
            filename = uri.getPath().substring(lastSlashIndex + 1);
        }
        if (filename.trim().length() == 0) {
            return null;
        }
        ArrayList<URLResourceImpl> resources = new ArrayList<URLResourceImpl>();
        @SuppressWarnings("unchecked")
        Enumeration<URL> en = bundle.findEntries(path, filename, false);
        while (en.hasMoreElements()) {
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
