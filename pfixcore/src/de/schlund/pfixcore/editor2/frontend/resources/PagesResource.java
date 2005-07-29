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

package de.schlund.pfixcore.editor2.frontend.resources;

import de.schlund.pfixcore.workflow.ContextResource;

/**
 * ContextResource providing a list of all pages for the current project and
 * methods to select a page.
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public interface PagesResource extends ContextResource {
    /**
     * Selects page matching page and variant name. If no variant for this page
     * is found, which is exactly matching the specified variant name, a parent
     * variant is returned. If no parent variant can be found neither, the
     * default variant is returned.
     * 
     * @param pageName
     *            Name of the page
     * @param variantName
     *            Name of the variant to look for
     * @return <code>true</code> if a page could be found, <code>false</code>
     *         otherwise
     */
    boolean selectPage(String pageName, String variantName);

    /**
     * Removes current selection (if there is any)
     *
     */
    void unselectPage();
}
