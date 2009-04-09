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

package de.schlund.pfixcore.editor2.core.spring;

import de.schlund.pfixxml.config.GlobalConfig;

/**
 * Implementation using {@link de.schlund.pfixxml.config.GlobalConfig} to get
 * Pustefix docroot
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class PathResolverServiceImpl implements PathResolverService {
    private String docroot;

    /**
     * Constructor makes use of <code>PathFactory</code> to get docroot.
     */
    public PathResolverServiceImpl() {
        docroot = GlobalConfig.getDocroot();
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.schlund.pfixcore.editor2.core.spring.PathResolverService#resolve(java.lang.String)
     */
    public String resolve(String path) {
        if (path.startsWith("docroot:")) {
            path=path.substring(9);
        } else if (path.startsWith("module:")) {
            throw new IllegalArgumentException("Modules are currently not supported");
        }
        if (path.startsWith("/")) {
            return docroot + path;
        } else {
            return docroot + "/" + path;
        }

    }

}
