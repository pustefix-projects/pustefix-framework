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

import java.io.File;
import java.util.Collection;

import org.pustefixframework.editor.common.exception.EditorIOException;
import org.pustefixframework.editor.common.exception.EditorSecurityException;


/**
 * Represents an image that is used by one or several include parts.
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public interface Image extends Comparable<Image> {
    /**
     * Returns the path to the image file (relative to docroot)
     * 
     * @return Path to image file
     */
    String getPath();
    
    /**
     * Returns Enumeration containing all Page objects which are affected by
     * this Image. This list is allways recursive, which means all pages which
     * are directly or indirectly depending on this Image are returned.
     * 
     * @return All affected pages of this Target
     * @see Page
     */
    Collection<Page> getAffectedPages();
    
    /**
     * Replaces the current image file with the new one.
     * 
     * @param newFile File with new image to use
     * @throws EditorSecurityException 
     */
    void replaceFile(File newFile) throws EditorIOException,
            EditorSecurityException;
    
    /**
     * Returns the last time this image was modified in microseconds
     * since 01/01/1970 00:00:00 GMT. If the file cannot be found
     * <code>0</code> is returned.
     * 
     * @return Time of last modification
     */
    long getLastModTime();
    
    /**
     * Returns a list containing all versions available for this image as
     * {@link String} objects. The returned strings can be used to restore a
     * backup using the {@link #restore(String)} method.
     * 
     * @return Collection of versions (as {@link String} objects)
     */
    Collection<String> getBackupVersions();
    
    /**
     * Restores an old version of this image.
     * 
     * @param version
     *            String identifying the backup version
     * @return <code>true</code> if version was found and restored,
     *         <code>false</code> on error
     * @throws EditorSecurityException 
     */
    boolean restore(String version) throws EditorSecurityException;
}
