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
package de.schlund.pfixcore.example;

import org.pustefixframework.web.mvc.InputHandler;
import org.springframework.beans.factory.annotation.Autowired;

import de.schlund.pfixcore.example.iwrapper.FileUpload;
import de.schlund.pfixxml.multipart.UploadFile;

/**
 * @author mleidig@schlund.de
 */
public class FileUploadHandler implements InputHandler<FileUpload> {

    @Autowired
    private ContextFileUpload ctxUpload;
    
	public void handleSubmittedData(FileUpload upload) {
	    UploadFile file=upload.getFile();
	    if(upload.getComment()!=null) ctxUpload.setComment(upload.getComment());
	    if(file!=null) ctxUpload.setFiles(new UploadFile[] {file});
    }
    
    public void retrieveCurrentStatus(FileUpload upload) {
    }
    
    public boolean needsData() {
        return false;
    }
    
    public boolean prerequisitesMet() {
        return true;
    }

    public boolean isActive() {
        return true;
    }
    
}
