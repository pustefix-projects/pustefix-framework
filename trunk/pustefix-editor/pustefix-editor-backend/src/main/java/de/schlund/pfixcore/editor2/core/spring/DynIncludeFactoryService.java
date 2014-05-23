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

import java.util.Collection;

import org.pustefixframework.editor.common.dom.IncludeFile;


/**
 * Service providing methods to retrieve all dynamic include files
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public interface DynIncludeFactoryService {
    /**
     * Returns a list of all files containing DynIncludes.
     * 
     * @return List of all DynInclude files
     */
    Collection<IncludeFile> getDynIncludeFiles();

    /**
     * Returns the include file for the specified path
     * 
     * @param path
     *            Path to the include file (relative to docroot)
     * @return Include file or <code>null</code> if <code>path</code> does
     *         not point to a valid include file
     */
    IncludeFile getIncludeFile(String path);
}
