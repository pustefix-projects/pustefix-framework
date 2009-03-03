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
public interface IncludePartThemeVariant extends Comparable<IncludePartThemeVariant> {
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
     * Sets the content of this IncludePart for the selected theme.
     * Does the same as {@link #setXML(Node, boolean)} with <code>indent</code>
     * set to <code>true</code>.
     * 
     * @param xml
     *            XML Node containing the content to save
     * @throws EditorIOException
     * @throws EditorParsingException
     * @throws EditorSecurityException
     */
    void setXML(Node xml) throws EditorIOException, EditorParsingException,
            EditorSecurityException;

    /**
     * Sets the content of this IncludePart for the selected theme
     * 
     * @param xml XML Node containing the content to save
     * @param indent if <code>true</code> content will be saved in "pretty"
     *               format. 
     * @throws EditorIOException
     * @throws EditorParsingException
     * @throws EditorSecurityException
     */
    void setXML(Node xml, boolean indent) throws EditorIOException, EditorParsingException,
            EditorSecurityException;
    
    /**
     * Returns all include parts this IncludePart is depending on. Actually
     * IncludePartThemeVariant objects are returned to honor the fact, that this
     * IncludePart depends on a specific version of another IncludePart. This
     * method returns all variants of an IncludePart this part is depending on -
     * not just the variant for a specific ThemeList.
     * 
     * @param recursive
     *            If set to <code>true</code> all dependendencies, including
     *            those which are dependencies of other dependencies themselves
     *            are included in the returned list
     * @return All dependend include parts
     * @throws EditorParsingException
     */
    Collection<IncludePartThemeVariant> getIncludeDependencies(boolean recursive)
            throws EditorParsingException;

    /**
     * Returns all images this Include is depending on. This method returns all
     * variants of an IncludePart this part is depending on - not just the
     * variant for a specific ThemeList.
     * 
     * @param recursive
     *            If set to <code>true</code> all dependendencies, including
     *            those which are dependencies of other dependencies themselves
     *            are included in the returned list
     * @return All dependend images
     * @throws EditorParsingException
     * @see Image
     */
    Collection<Image> getImageDependencies(boolean recursive)
            throws EditorParsingException;

    /**
     * Returns all include parts this IncludePart is depending on. Actually
     * IncludePartThemeVariant objects are returned to honor the fact, that this
     * IncludePart depends on a specific version of another IncludePart.
     * This list may very depending on the target for which this part is
     * rendererd.
     * 
     * @param target
     *            Target to do the lookup for
     * @param recursive
     *            If set to <code>true</code> all dependendencies, including
     *            those which are dependencies of other dependencies themselves
     *            are included in the returned list
     * @return All dependend include parts
     * @throws EditorParsingException
     */
    Collection<IncludePartThemeVariant> getIncludeDependencies(Target target,
            boolean recursive) throws EditorParsingException;

    /**
     * Returns all images this Include is depending on.
     * This list may depend on the given target.
     * 
     * @param target
     *            Target to do the lookup for
     * @param recursive
     *            If set to <code>true</code> all dependendencies, including
     *            those which are dependencies of other dependencies themselves
     *            are included in the returned list
     * @return All dependend images
     * @throws EditorParsingException
     * @see Image
     */
    Collection<Image> getImageDependencies(Target target, boolean recursive)
            throws EditorParsingException;

    /**
     * Returns pages which are using this IncludePart (directly or inderectly)
     * 
     * @return All pages depending on this IncludePart
     * @see Page
     */
    Collection<Page> getAffectedPages();

    /**
     * Returns projects which are using this IncludePart. These are all projects
     * which contain at least one page returned by {@link #getAffectedPages()}.
     * 
     * @return All projects using this IncludePart
     */
    Collection<Project> getAffectedProjects();

    /**
     * Returns the MD5 checksum of the serialized XML code. This checksum can
     * for example be used to check, whether the include part has been changed.
     * If the part does not yet have any content, the special string "0" is
     * returned.
     * 
     * @return MD5 checksum for this part
     */
    String getMD5();
}
