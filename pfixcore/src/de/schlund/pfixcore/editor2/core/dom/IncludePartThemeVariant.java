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

package de.schlund.pfixcore.editor2.core.dom;

import java.util.Collection;

import org.w3c.dom.Node;

import de.schlund.pfixcore.editor2.core.exception.EditorIOException;
import de.schlund.pfixcore.editor2.core.exception.EditorParsingException;
import de.schlund.pfixcore.editor2.core.exception.EditorSecurityException;

/**
 * Represents the piece of an include part containing all information
 * corresponding to a specific theme.
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public interface IncludePartThemeVariant extends Comparable {
    /**
     * Returns theme of this part
     * 
     * @return Theme this part is used for
     */
    Theme getTheme();

    /**
     * Returns the include part this variant belongs to
     * 
     * @return IncludePart for this theme specific part
     */
    IncludePart getIncludePart();

    /**
     * Returns the XML Node containing the content of this InlcudePart for the
     * selected theme
     * 
     * @return XML Node for the given theme
     */
    Node getXML();

    /**
     * Sets the content of this IncludePart for the selected theme
     * 
     * @param xml
     *            XML Node containing the content to save
     * @throws EditorIOException 
     * @throws EditorParsingException 
     * @throws EditorSecurityException 
     */
    void setXML(Node xml) throws EditorIOException, EditorParsingException, EditorSecurityException;

    /**
     * Returns all include parts this IncludePart is depending on. Actually
     * IncludePartThemeVariant objects are returned to honor the fact, that this
     * IncludePart depends on a specific version of another IncludePart.
     * This method returns all variants of an IncludePart this part is
     * depending on - not just the variant for a specific ThemeList.
     * 
     * @param recursive
     *            If set to <code>true</code> all dependendencies, including
     *            those which are dependencies of other dependencies themselves
     *            are included in the returned list
     * @return All dependend include parts
     * @throws EditorParsingException 
     */
    Collection getIncludeDependencies(boolean recursive) throws EditorParsingException;

    /**
     * Returns all images this Include is depending on.
     * This method returns all variants of an IncludePart this part is
     * depending on - not just the variant for a specific ThemeList.
     * 
     * @param recursive
     *            If set to <code>true</code> all dependendencies, including
     *            those which are dependencies of other dependencies themselves
     *            are included in the returned list
     * @return All dependend images
     * @throws EditorParsingException 
     * @see Image
     */
     Collection getImageDependencies(boolean recursive) throws EditorParsingException;

     /**
      * Returns all include parts this IncludePart is depending on. Actually
      * IncludePartThemeVariant objects are returned to honor the fact, that this
      * IncludePart depends on a specific version of another IncludePart
      * 
      * @param themes Themes to select the variants for
      * @param recursive
      *            If set to <code>true</code> all dependendencies, including
      *            those which are dependencies of other dependencies themselves
      *            are included in the returned list
      * @return All dependend include parts
      * @throws EditorParsingException 
      */
     Collection getIncludeDependencies(ThemeList themes, boolean recursive) throws EditorParsingException;

     /**
      * Returns all images this Include is depending on.
      * 
      * @param themes Themes to select the variants for
      * @param recursive
      *            If set to <code>true</code> all dependendencies, including
      *            those which are dependencies of other dependencies themselves
      *            are included in the returned list
      * @return All dependend images
      * @throws EditorParsingException 
      * @see Image
      */
      Collection getImageDependencies(ThemeList themes, boolean recursive) throws EditorParsingException;
     
    /**
     * Returns pages which are using this IncludePart (directly or inderectly)
     * 
     * @return All pages depending on this IncludePart
     * @see Page
     */
    Collection getAffectedPages();
}
