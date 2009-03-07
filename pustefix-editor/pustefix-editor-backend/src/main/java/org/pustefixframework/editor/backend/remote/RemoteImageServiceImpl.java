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

package org.pustefixframework.editor.backend.remote;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;

import org.pustefixframework.container.annotations.Inject;
import org.pustefixframework.editor.common.dom.Image;
import org.pustefixframework.editor.common.dom.Page;
import org.pustefixframework.editor.common.exception.EditorIOException;
import org.pustefixframework.editor.common.exception.EditorSecurityException;
import org.pustefixframework.editor.common.remote.service.RemoteImageService;
import org.pustefixframework.editor.common.remote.transferobjects.ImageTO;

import de.schlund.pfixcore.editor2.core.spring.ImageFactoryService;


public class RemoteImageServiceImpl implements RemoteImageService {
    
    private ImageFactoryService imageFactoryService;
    
    @Inject
    public void setImageFactoryService(ImageFactoryService imageFactoryService) {
        this.imageFactoryService = imageFactoryService;
    }
    
    public ImageTO getImage(String path) {
        ImageTO to = new ImageTO();
        Image img = imageFactoryService.getImage(path); 
        to.path = path;
        to.lastModTime = img.getLastModTime();
        for (Page page : img.getAffectedPages()) {
            to.affectedPages.add(page.getFullName());
        }
        return to;
    }
    
    public Collection<String> listImageVersions(String imagePath) {
        Image img = imageFactoryService.getImage(imagePath);
        return img.getBackupVersions();
    }
    
    public void replaceFile(String path, byte[] newContent) throws EditorIOException, EditorSecurityException {
        File tempFile;
        try {
            tempFile = File.createTempFile("imageupload", null);
            BufferedOutputStream s = new BufferedOutputStream(new FileOutputStream(tempFile));
            s.write(newContent, 0, newContent.length);
            s.flush();
            s.close();
        } catch (IOException e) {
            throw new RuntimeException("Error while writing image data to temporary file", e);
        }

        Image img = imageFactoryService.getImage(path);
        img.replaceFile(tempFile);
        tempFile.delete();
    }
    
    public boolean restoreImage(String imagePath, String version) throws EditorSecurityException {
        Image img = imageFactoryService.getImage(imagePath);
        return img.restore(version);
    }
    
}
