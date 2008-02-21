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
import java.util.Map;

import org.w3c.dom.Document;

import de.schlund.pfixcore.editor2.core.exception.EditorIOException;
import de.schlund.pfixcore.editor2.core.exception.EditorParsingException;

/**
 * Represents target objects generated by the Pustefix generator
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public interface Target {
    /**
     * Returns a String identifying this Target. Each name is unique within a
     * specific project (but not within all projects)
     * 
     * @return Name of this Target
     */
    String getName();

    /**
     * Returns the type of this target. Type is either TARGET_XML or TARGET_XSL
     * 
     * @return TARGET_XML or TARGET_XSL
     * @see TargetType#TARGET_XML
     * @see TargetType#TARGET_XSL
     */
    TargetType getType();

    /**
     * Indicates whether this Target is a leaf target. Leaf targets do not have
     * parents but a represented by physical files instead.
     * 
     * @return <code>true</code> if this Target does not have any parents,
     *         <code>false</code> otherwise
     */
    boolean isLeafTarget();

    /**
     * Returns XML DOM tree of this Target
     * 
     * @return DOM Document representing the content of this Target
     * @throws EditorIOException
     * @throws EditorParsingException
     */
    Document getContentXML() throws EditorIOException, EditorParsingException;

    /**
     * Returns parent Target that provides XML for generation of this Target.
     * Each Target (except leaf targets) has a XML parent.
     * 
     * @return XML parent Target or <code>null</code> if this is a leaf target
     */
    Target getParentXML();

    /**
     * Returns parent Target that provides XSL for generation of this Target.
     * Each Target (except leaf targets) has a XSL parent.
     * 
     * @return XSL parent Target or <code>null</code> if this is a leaf target
     */
    Target getParentXSL();

    /**
     * Returns all auxilliary targets this Target is depending on. This list
     * does not include the XML and XSL parent, but only those targets whose
     * dependency is defined manually.
     * 
     * @return All auxilliary parent targets
     */
    Collection<Target> getAuxDependencies();

    /**
     * Returns all include parts this Target is depending on. The returned
     * objects are actually of type IncludePartThemeVariant.
     * 
     * @param recursive
     *            If set to <code>true</code> all dependendencies, including
     *            those which are dependencies of other dependencies themselves
     *            are included in the returned list
     * @return All dependend include parts
     * @throws EditorParsingException
     * @see IncludePart
     * @see IncludePartThemeVariant
     */
    Collection<IncludePartThemeVariant> getIncludeDependencies(boolean recursive)
            throws EditorParsingException;

    /**
     * Returns all images this Target is depending on.
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
     * Returns Enumeration containing all Page objects which are affected by
     * this Target. This list is allways recursive, which means all pages which
     * are directly or indirectly depending on this Target are returned.
     * 
     * @return All affected pages of this Target
     * @see Page
     */
    Collection<Page> getAffectedPages();

    /**
     * Returns parameters used during generation. Map keys are parameter names,
     * values their corresponding contents.
     * 
     * @return Parameters used by the XSL processor
     */
    Map<String, Object> getParameters();
    
    /**
     * Returns a list representing the themes used by this target 
     * 
     * @return List of themes used by this target
     */
    ThemeList getThemeList();

    /**
     * Returns project this Target belongs to
     * 
     * @return Project this Target is part of
     */
    Project getProject();
}