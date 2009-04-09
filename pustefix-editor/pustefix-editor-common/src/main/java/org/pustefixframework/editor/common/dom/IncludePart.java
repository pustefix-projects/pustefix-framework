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

package org.pustefixframework.editor.common.dom;

import java.util.Collection;

import org.pustefixframework.editor.common.exception.EditorIOException;
import org.pustefixframework.editor.common.exception.EditorParsingException;
import org.pustefixframework.editor.common.exception.EditorSecurityException;
import org.w3c.dom.Node;


/**
 * Represents an include part which can be regarded as a "piece of XML" that can
 * be included in other documents.
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public interface IncludePart extends Comparable<IncludePart> {
    /**
     * Returns the name of this IncludePart. The name is used to distinctly
     * identify an IncludePart within an IncludeFile.
     * 
     * @return Unique name identifying this IncludePart
     */
    String getName();

    /**
     * Returns the IncludeFile this IncludePart is part of.
     * 
     * @return IncludeFile corresponding to this IncludePart
     */
    IncludeFile getIncludeFile();

    /**
     * Returns a XML Node representing the whole include part
     * 
     * @return XML for this IncludePart including all themes
     */
    Node getContentXML();

    /**
     * Returns variant of this IncludePart for a specific theme
     * 
     * @param theme
     *            Theme to return the variant for or <code>null</code> for the
     *            "default" theme
     * @return Variant of this IncludePart for theme or <code>null</code> if
     *         no variant for the theme can be found
     */
    IncludePartThemeVariant getThemeVariant(Theme theme);

    /**
     * Creates a new theme variant for this IncludePart. If there is already a
     * variant for the specified theme, the existing variant is returned.
     * 
     * @param theme
     *            Theme to create a variant for.
     * @return New IncludePartThemeVariant object for the specified theme
     * @throws EditorSecurityException 
     * @throws EditorParsingException 
     * @throws EditorIOException 
     */
    IncludePartThemeVariant createThemeVariant(Theme theme) throws EditorIOException, EditorParsingException, EditorSecurityException;

    /**
     * Return true, if there is a variant for the specified theme. A variant is
     * not considered to "exist" until it has been filled with content.
     * 
     * @param theme
     *            Theme to find a variant for
     * @return <code>true</code> if theme variant is existing,
     *         <code>false</code> otherwise
     */
    boolean hasThemeVariant(Theme theme);

    /**
     * Deletes an IncludePartThemeVariant (only possible for non "default"
     * variants).
     * 
     * @param variant
     *            Variant to delete
     * @throws EditorSecurityException
     *             If deletion of this variant is not allowed.
     * @throws EditorIOException
     *             If I/O operations on inlude file failed
     * @throws EditorParsingException
     *             If parsing of include file failed
     */
    void deleteThemeVariant(IncludePartThemeVariant variant)
            throws EditorSecurityException, EditorIOException,
            EditorParsingException;

    /**
     * Returns a list of all theme variants, which are existing for this
     * IncludePart
     * 
     * @return All theme variants for this IncludePart
     * @see IncludePartThemeVariant
     */
    Collection<IncludePartThemeVariant> getThemeVariants();

    /**
     * Returns a list of all themes, which could be possibly used for this
     * IncludePart. This does not necessarily mean, that there is also an
     * existing variant for each theme returned.
     * 
     * @return List of possible themes
     * @see Theme
     */
    Collection<Theme> getPossibleThemes();
}