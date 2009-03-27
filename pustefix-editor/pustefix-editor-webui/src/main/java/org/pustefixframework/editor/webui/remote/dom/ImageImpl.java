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

package org.pustefixframework.editor.webui.remote.dom;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;

import org.pustefixframework.editor.common.dom.AbstractImage;
import org.pustefixframework.editor.common.dom.Image;
import org.pustefixframework.editor.common.dom.Page;
import org.pustefixframework.editor.common.exception.EditorIOException;
import org.pustefixframework.editor.common.exception.EditorSecurityException;
import org.pustefixframework.editor.common.remote.transferobjects.ImageTO;
import org.pustefixframework.editor.webui.remote.dom.util.RemoteServiceUtil;



public class ImageImpl extends AbstractImage {
    
    private RemoteServiceUtil remoteServiceUtil;
    private String path;

    public ImageImpl(RemoteServiceUtil remoteServiceUtil, String path) {
        this.remoteServiceUtil = remoteServiceUtil;
        this.path = path;
    }
    
    public Collection<Page> getAffectedPages() {
        ImageTO imageTO = getImageTO();
        LinkedList<Page> pages = new LinkedList<Page>();
        for (String pageName : imageTO.affectedPages) {
            pages.add(new PageImpl(remoteServiceUtil, pageName));
        }
        return pages;
    }
    
    public Collection<String> getBackupVersions() {
        return remoteServiceUtil.getRemoteImageService().listImageVersions(getPath());
    }
    
    public long getLastModTime() {
        return getImageTO().lastModTime;
    }
    
    public String getPath() {
        // No update needed, this property cannot change
        return path;
    }
    
    // dump if we have Java 5
    private static byte[] copy(byte[] orig, int len) {
        byte[] copy;
        
        copy = new byte[len];
        System.arraycopy(orig, 0, copy, 0, Math.min(orig.length, len));
        return copy;
    }

    public void replaceFile(File newFile) throws EditorIOException, EditorSecurityException {
        try {
            byte[] buffer = new byte[4096];
            FileInputStream s = new FileInputStream(newFile);
            int totalBytesRead = 0;
            int bytesRead;
            while ((bytesRead = s.read(buffer, totalBytesRead, buffer.length - totalBytesRead)) != -1) {
                totalBytesRead += bytesRead;
                if (buffer.length <= totalBytesRead) {
                    buffer = copy(buffer, buffer.length * 2);
                }
            }
            buffer = copy(buffer, totalBytesRead);
            remoteServiceUtil.getRemoteImageService().replaceFile(getPath(), buffer);
        } catch (IOException e) {
            throw new EditorIOException("Error while reading file " + newFile.getAbsolutePath(), e);
        }
    }
    
    public boolean restore(String version) throws EditorSecurityException {
        return remoteServiceUtil.getRemoteImageService().restoreImage(getPath(), version);
    }
    
    private ImageTO getImageTO() {
        return remoteServiceUtil.getRemoteImageService().getImage(getPath());
    }

    @Override
    public int compareTo(Image image) {
        if (image instanceof ImageImpl) {
            ImageImpl i = (ImageImpl) image;
            if (this.remoteServiceUtil.equals(i.remoteServiceUtil)) {
                return super.compareTo(image);
            } else {
                return -1;
            }
        } else {
            return -1;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) {
            return false;
        }
        if (obj instanceof ImageImpl) {
            ImageImpl i = (ImageImpl) obj;
            return this.remoteServiceUtil.equals(i.remoteServiceUtil);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return ("IMAGE: " + super.hashCode() + remoteServiceUtil.hashCode()).hashCode();
    }
}
