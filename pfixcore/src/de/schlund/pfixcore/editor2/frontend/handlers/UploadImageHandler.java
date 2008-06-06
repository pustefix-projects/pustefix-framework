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

import java.io.File;
import java.io.FileInputStream;

import org.pustefixframework.editor.EditorStatusCodes;

import de.schlund.pfixcore.editor2.core.dom.Image;
import de.schlund.pfixcore.editor2.frontend.util.EditorResourceLocator;
import de.schlund.pfixcore.editor2.frontend.wrappers.UploadImage;
import de.schlund.pfixcore.generator.IHandler;
import de.schlund.pfixcore.generator.IWrapper;
import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixxml.ImageInfo;

/**
 * Handles image upload
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class UploadImageHandler implements IHandler {

    public void handleSubmittedData(Context context, IWrapper wrapper)
            throws Exception {
        UploadImage input = (UploadImage) wrapper;
        File uploadFile = input.getImageFile();
        ImageInfo info = new ImageInfo();
        info.setInput(new FileInputStream(uploadFile));
        if (!info.check()) {
            input.addSCodeImageFile(EditorStatusCodes.IMAGESUPLOAD_IMAGEUPL_NOFILE);
            return;
        }
        String mimeType = info.getMimeType();
        Image image = EditorResourceLocator.getImagesResource(context)
                .getSelectedImage();
        if (image == null) {
            input.addSCodeImageFile(EditorStatusCodes.IMAGESUPLOAD_IMAGEUPL_WRONGTYPE);
        }
        String imagePath = image.getPath();
        if (imagePath.lastIndexOf('/') == -1 || imagePath.lastIndexOf('/') == 0) {
            input.addSCodeImageFile(EditorStatusCodes.IMAGESUPLOAD_FILE_IS_IN_ROOT);
            return;
        }
        String suffix = imagePath.substring(imagePath.lastIndexOf("."));
        if ((mimeType.equals("image/jpeg") && suffix.equals(".jpg"))
                || (mimeType.equals("image/png") && suffix.equals(".png"))
                || (mimeType.equals("image/gif") && suffix.equals(".gif"))) {
            if (image.getLastModTime() != input.getLastModTime().longValue()) {
                input
                        .addSCodeImageFile(EditorStatusCodes.IMAGESUPLOAD_IMAGEUPL_HASCHANGED);
                return;
            }
            image.replaceFile(uploadFile);
        } else {
            if (suffix.equals(".jpg")) {
                input
                        .addSCodeImageFile(EditorStatusCodes.IMAGESUPLOAD_IMAGEUPL_WRONGTYPEJPG);
            } else if (suffix.equals(".png")) {
                input
                        .addSCodeImageFile(EditorStatusCodes.IMAGESUPLOAD_IMAGEUPL_WRONGTYPEPNG);
            } else if (suffix.equals(".gif")) {
                input
                        .addSCodeImageFile(EditorStatusCodes.IMAGESUPLOAD_IMAGEUPL_WRONGTYPEGIF);
            } else {
                input
                        .addSCodeImageFile(EditorStatusCodes.IMAGESUPLOAD_IMAGEUPL_WRONGTYPE);
            }
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
        return (EditorResourceLocator.getImagesResource(context)
                .getSelectedImage() != null);
    }

    public boolean needsData(Context context) throws Exception {
        // Always ask for upload
        return true;
    }

}
