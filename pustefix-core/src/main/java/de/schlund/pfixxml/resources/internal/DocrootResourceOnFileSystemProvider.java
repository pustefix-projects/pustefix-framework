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

package de.schlund.pfixxml.resources.internal;

import java.net.URI;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.pustefixframework.live.LiveResolver;

import de.schlund.pfixcore.exception.PustefixRuntimeException;
import de.schlund.pfixxml.config.EnvironmentProperties;
import de.schlund.pfixxml.resources.DocrootResourceProvider;
import de.schlund.pfixxml.resources.Resource;

/**
 * Provider using a path on the local file system to resolve docroot resources.
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class DocrootResourceOnFileSystemProvider extends DocrootResourceProvider {

    private final static Logger LOG = LoggerFactory.getLogger(DocrootResourceOnFileSystemProvider.class);

    private String docroot;

    public DocrootResourceOnFileSystemProvider(String docroot) {
        this.docroot = docroot;
    }

    public Resource getResource(URI uri) {

        boolean checkLive;
        // Ensure resources are read from real docroot in production environment
        checkLive = EnvironmentProperties.getProperties().getProperty("mode") != null
            && !EnvironmentProperties.getProperties().getProperty("mode").equals("prod");

        if (checkLive) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Getting live resource for " + uri);
            }
            try {
                URL resolvedLiveDocroot = LiveResolver.getInstance().resolveLiveDocroot(docroot, uri.getPath());
                if (resolvedLiveDocroot != null) {
                    Resource res = new DocrootResourceOnFileSystemImpl(uri, resolvedLiveDocroot.getFile());
                    if(res.exists()) {
                    	return res;
                    }
                }
            } catch (Exception e) {
                throw new PustefixRuntimeException(e);
            }
        }
        return new DocrootResourceOnFileSystemImpl(uri, docroot);
    }

}
