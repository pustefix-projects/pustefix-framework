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

package de.schlund.pfixcore.editor2.frontend.handlers;

import org.pustefixframework.container.annotations.Inject;
import org.pustefixframework.editor.generated.EditorStatusCodes;

import de.schlund.pfixcore.editor2.frontend.resources.ImagesResource;
import de.schlund.pfixcore.editor2.frontend.wrappers.RestoreImage;
import de.schlund.pfixcore.generator.IHandler;
import de.schlund.pfixcore.generator.IWrapper;
import de.schlund.pfixcore.workflow.Context;

/**
 * Handles image restore from backup
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class RestoreImageHandler implements IHandler {

    private ImagesResource imagesResource;

    public void handleSubmittedData(Context context, IWrapper wrapper)
            throws Exception {
        RestoreImage input = (RestoreImage) wrapper;
        int ret = imagesResource.restoreBackup(input.getVersion(), input.getLastModTime().longValue());
        if (ret == 1) {
            input.addSCodeVersion(EditorStatusCodes.IMAGES_IMAGE_UNDEF);
        } else if (ret == 2) {
            input.addSCodeVersion(EditorStatusCodes.IMAGESUPLOAD_IMAGEUPL_HASCHANGED);
        }
    }

    public void retrieveCurrentStatus(Context context, IWrapper wrapper)
            throws Exception {
        // Do not prefill form
    }

    public boolean prerequisitesMet(Context context) throws Exception {
        // Always allow upload
        return true;
    }

    public boolean isActive(Context context) throws Exception {
        // Handler is only active, if there is a selected image
        return (imagesResource.getSelectedImage() != null);
    }

    public boolean needsData(Context context) throws Exception {
        // Always ask for upload
        return true;
    }

    @Inject
    public void setImagesResource(ImagesResource imagesResource) {
        this.imagesResource = imagesResource;
    }

}
