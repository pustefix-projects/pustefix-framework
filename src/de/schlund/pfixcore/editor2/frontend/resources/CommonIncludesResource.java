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

import java.util.SortedSet;

import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;

import de.schlund.pfixcore.editor2.core.dom.IncludeFile;
import de.schlund.pfixcore.editor2.core.dom.IncludePartThemeVariant;
import de.schlund.pfixcore.editor2.core.exception.EditorException;
import de.schlund.pfixcore.editor2.core.exception.EditorIOException;
import de.schlund.pfixcore.editor2.core.exception.EditorParsingException;
import de.schlund.pfixcore.editor2.core.exception.EditorSecurityException;
import de.schlund.pfixcore.workflow.ContextResource;

/**
 * Common ContextResource implementation for include parts 
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public interface CommonIncludesResource extends ContextResource {
    /**
     * Selects an include part using the supplied parameters
     * 
     * @param path
     *            The path to the includes file
     * @return <code>true</code> if include part was found, <code>false</code>
     *         otherwise
     */
    boolean selectIncludePart(String path, String part, String theme);

    /**
     * Removes the selection (if there is any)
     */
    void unselectIncludePart();

    /**
     * Return the selected include part or <code>null</code> if no part has
     * been selected
     * 
     * @return Include part that is selected at the moment
     */
    IncludePartThemeVariant getSelectedIncludePart();

    /**
     * Restores backup of an include part
     * 
     * @param version
     *            String identifying the version
     * @param hash
     *            Hash value of the last version the user has seen
     * @return <code>0</code> on success, <code>1</code> if include part
     *         could not be restored, <code>2</code> if include part has been
     *         changed by another user in the meantime
     */
    int restoreBackup(String version, String hash);

    /**
     * Returns MD5 checksum for selected include part
     * 
     * @return MD5 checksum
     */
    String getMD5();

    /**
     * Returns serialized XML content of the selected include part
     * 
     * @return Serialized XML
     */
    String getContent();

    /**
     * Returns a flag indicating whether the content is indented. Actually
     * the method returns <code>true</code> if the content is not starting
     * with whitespace.
     * 
     * @return <code>true</code> if content has now surrounding whitespace
     */
    boolean isContentIndented();
    
    /**
     * Sets new content for the selected include part
     * 
     * @param content
     *            New content to set
     * @param indent
     *            flag indicating whether to fix indention of content
     * @param hash
     *            Hash value of the last version the user has seen
     * @throws TransformerException
     *             If <code>content</code> does not contain valid XML input
     * @throws EditorException
     *             If a general error occurs.
     * @throws EditorIncludeHasChangedException
     *             If include part has been changed by another user, while this
     *             user has been editing the part.
     */
    void setContent(String content, boolean indent, String hash) throws SAXException,
            EditorException;

    /**
     * Creates a new branch for the specified theme and selects it
     * 
     * @param theme
     *            Name of theme to create branch for
     * @return <code>true</code> on success, <code>false</code> otherwise
     * @throws EditorSecurityException 
     * @throws EditorParsingException 
     * @throws EditorIOException 
     */
    boolean createAndSelectBranch(String theme) throws EditorIOException, EditorParsingException, EditorSecurityException;

    /**
     * Deletes the branch that is currently selected
     * 
     * @return <code>true</code> on success, <code>false</code> otherwise
     */
    boolean deleteSelectedBranch();
    
    /**
     * Marks a directory tree as open and returns a list of all files
     * within the directory. This method is used by the webservice.
     * 
     * @param name pathname of the directory
     * @return All files in the directory
     */
    SortedSet<IncludeFile> openDirectoryTree(String name);
 
    /**
     * Marks a file tree as open and returns a list of all parts
     * within the file. If the parent directory is not already open,
     * it is opened, too. This method is used by the webservice.
     * 
     * @param name pathname of the file
     * @return All parts in the file
     */
    SortedSet<IncludePartThemeVariant> openFileTree(String name);
    
    /**
     * 
     * Marks a directory as closed. Also closes all files within the
     * directory. This method is used by by the webservice.
     * 
     * @param name pathname of the directory
     */
    void closeDirectoryTree(String name);

    /**
     * 
     * Marks a file as closed. This method is used by by the webservice.
     * 
     * @param name pathname of the file
     */
    void closeFileTree(String name);
}
