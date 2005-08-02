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

import de.schlund.pfixcore.editor2.core.dom.Image;
import de.schlund.pfixcore.workflow.ContextResource;

/**
 * ContextResource providing a list of the images for a project and methods to
 * replace an image.
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public interface ImagesResource extends ContextResource {
    /**
     * Selects an image using the path
     * 
     * @param path
     *            The path to the image file
     * @return <code>true</code> if image was found, <code>false</code>
     *         otherwise
     */
    boolean selectImage(String path);

    /**
     * Removes the selection (if there is any)
     */
    void unselectImage();

    /**
     * Return the selected image or <code>null</code> if no image has been
     * selected
     * 
     * @return Image that is selected at the moment
     */
    Image getSelectedImage();
    
    /**
     * Restores backup of an image
     * 
     * @param version String identifying the version
     * @return <code>true</code> on success, <code>false</code> otherwise
     */
    boolean restoreBackup(String version);
}
