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

package de.schlund.pfixcore.editor2.core.spring;

import de.schlund.pfixcore.editor2.core.dom.Project;
import de.schlund.pfixcore.editor2.core.dom.ThemeList;
import de.schlund.pfixcore.editor2.core.dom.Variant;
import de.schlund.pfixcore.editor2.core.spring.internal.MutablePage;

/**
 * Service providing methods to create Page objects
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public interface PageFactoryService {
    /**
     * Creates a page using the specified parameters
     * 
     * @param pageName
     *            Name of the page to create (without variant)
     * @param variant
     *            Variant of the page (or <code>null</code> for default)
     * @param handler
     *            String describing the handler being used to serve this page
     * @param themes
     *            List of the themes being used by this page
     * @param parent
     *            Parent of this page in navigation (or <code>null</code> if
     *            this is a top-level page)
     * @param childs
     *            Childs of this page in navigation
     * @param project
     *            Project this page belongs to
     * @param tgen
     *            TargetGenerator used for this page's target
     * @param pinfo
     *            Used to identify this page within the Pustefix generator
     */
    MutablePage getMutablePage(String pageName, Variant variant, String handler,
            ThemeList themes, Project project);
}
