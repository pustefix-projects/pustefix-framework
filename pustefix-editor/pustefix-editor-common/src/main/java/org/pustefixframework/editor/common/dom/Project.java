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

package org.pustefixframework.editor.common.dom;

import java.util.Collection;
import java.util.Map;

/**
 * Represents a Pustefix project. A project is the top-level organization unit
 * of Pustefix. All pages belonging to a project build a functional unit, having
 * remote dependencies to the "common" and "core" project only.
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public interface Project {
    /**
     * Returns the unique name identifying the project. Each project name is
     * unique within a given Pustefix environment. The name of the project
     * matches the name of the directory on the filesystem, which contains the
     * file of the project.
     * 
     * @return The name identifying this Project object
     */
    String getName();

    /**
     * Returns comment describing the project
     * 
     * @return Comment stored for this Project
     */
    String getComment();

    /**
     * Returns all pages belonging to this project
     * 
     * @return Enumeration containing the page objects of this Project
     * @see Page
     */
    Collection<Page> getAllPages();

    /**
     * Returns all pages of this project which are at the top-level in
     * navigation
     * 
     * @return Enumeration containing the top-level page objects of this Project
     * @see Page
     */
    Collection<Page> getTopPages();

    /**
     * Returns page belonging to this Project. To retrieve a certain variant of
     * a page, use "pagename::variant:subvariant" as the pagename
     * 
     * @param pagename
     *            Name of the page, optionally including variant name
     * @param variant
     *            Variant to return page for. If <code>null</code> default
     *            page (without any variant) is returned.
     * @return Corresponding Page object or <code>null</code> if no page with
     *         the specified name / variant can be found
     */
    Page getPage(String pagename, Variant variant);

    /**
     * Returns all variants of a page with the specified name.
     * 
     * @param name
     *            Name of the page
     * @return All variants of the page with name <code>name</code>
     */
    Collection<Page> getPageByName(String name);

    /**
     * Returns target belonging to this project.
     * 
     * @param name
     *            Name of the target (either virtual name or path)
     * @return Target object or <code>null</code> if no target can be found
     *         for the name
     */
    Target getTarget(String name);

    /**
     * Returns a {@link Collection} of all {@link IncludePartThemeVariant}
     * objects, which are used by this project. This list may not be complete
     * unless all pages of this project are up to date.
     * 
     * @return List of IncludePartThemeVariants for this project
     */
    Collection<IncludePartThemeVariant> getAllIncludeParts();

    /**
     * Returns a {@link Collection} of all {@link Image} objects, which are used
     * by this project. This list may not be complete unless all pages of this
     * project are up to date.
     * 
     * @return List of Images for this project
     */
    Collection<Image> getAllImages();

    /**
     * Finds an IncludePartThemeVariant using the supplied identifiers
     * 
     * @param file
     *            (relative) path to the file that contains the include part
     * @param part
     *            Name of the part
     * @param theme
     *            Name of the theme
     * @return The corresponding IncludePartThemeVariant or <code>null</code>
     *         if the specified part does not exist or is not used by this
     *         project
     */
    IncludePartThemeVariant findIncludePartThemeVariant(String file,
            String part, String theme);
    
    /**
     * Finds out if the given IncludePart is part of this <code>Project</code>. 
     * @param file (relative) path to the file that contains the include part
     * @param part Name of the part
     * @param theme Name of the theme
     * @return
     */
    boolean hasIncludePart(String file, String part, String theme);
    
    /**
     * Returns a map with the configured XML prefix to namespace mappings.
     * This mapping is used when a new include file is created or when
     * XML data with prefixes but without namespace-urls is imported.
     * In the returned map keys are prefixes and values are namespace-urls.
     * 
     * @return Map of pre-configured prefix-to-namespace-url mappings.
     */
    Map<String, String> getPrefixToNamespaceMappings();
    
    /**
     * Returns a list of all files containing DynIncludes.
     * 
     * @return List of all DynInclude files
     */
    Collection<IncludeFile> getDynIncludeFiles();

    /**
     * Returns the dyninclude file for the specified path
     * 
     * @param path
     *            Path to the include file (relative to docroot)
     * @return Include file or <code>null</code> if <code>path</code> does
     *         not point to a valid include file
     */
    IncludeFile getDynIncludeFile(String path);
}
