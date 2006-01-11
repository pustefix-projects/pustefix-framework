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

import java.util.Collection;

import de.schlund.pfixcore.editor2.core.dom.Image;
import de.schlund.pfixcore.editor2.core.dom.IncludePartThemeVariant;
import de.schlund.pfixcore.editor2.core.exception.EditorSecurityException;

/**
 * Service providing methods to create and restore backups.
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public interface BackupService {
    /**
     * Creates a new backup of the supplied image
     * 
     * @param image
     *            Image to backup
     * @throws EditorSecurityException 
     */
    void backupImage(Image image) throws EditorSecurityException;

    /**
     * Restores an old version of an image
     * 
     * @param image
     *            Image to restore
     * @param version
     *            String identifying the backup version
     * @return <code>true</code> if version was found and restored,
     *         <code>false</code> on error
     * @throws EditorSecurityException 
     */
    boolean restoreImage(Image image, String version) throws EditorSecurityException;

    /**
     * Returns a list containing all versions available for an image as
     * {@link String} objects. The returned strings can be used to restore a
     * backup using the {@link #restoreImage(Image, String)} method.
     * 
     * @param image
     *            Image to list versions for
     * @return Collection of versions (as {@link String} objects)
     */
    Collection<String> listImageVersions(Image image);

    /**
     * Creates a new backup of the supplied include part
     * 
     * @param include
     *            Include part to backup
     * @throws EditorSecurityException 
     */
    void backupInclude(IncludePartThemeVariant include) throws EditorSecurityException;

    /**
     * Restores an old version of an include part
     * 
     * @param include
     *            Include part to restore
     * @param version
     *            String identifying the version to restore
     * @return <code>true</code> if version was found and restored,
     *         <code>false</code> on error
     * @throws EditorSecurityException 
     */
    boolean restoreInclude(IncludePartThemeVariant include, String version) throws EditorSecurityException;

    /**
     * Returns a list containing all versions available for an include part as
     * {@link String} objects. The returned strings can be used to restore a
     * backup using the {@link #restoreInclude(IncludePartThemeVariant, String)}
     * method.
     * 
     * @param include
     *            Include Part to list versions for
     * @return Collection of versions (as {@link String} objects)
     */
    Collection<String> listIncludeVersions(IncludePartThemeVariant include);
}
