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

import java.util.ArrayList;
import java.util.List;

import org.pustefixframework.web.mvc.InputHandler;
import org.springframework.beans.factory.annotation.Autowired;

import de.schlund.pfixcore.example.iwrapper.MultiFileUpload;
import de.schlund.pfixxml.multipart.UploadFile;

/**
 * @author mleidig@schlund.de
 */
public class MultiFileUploadHandler implements InputHandler<MultiFileUpload> {

    @Autowired
    private ContextFileUpload ctxUpload;
    
	public void handleSubmittedData(MultiFileUpload upload) {
        if(upload.getComment()!=null) ctxUpload.setComment(upload.getComment());
        List<UploadFile> files=new ArrayList<UploadFile>();
	    String[] keys=upload.getKeysFile();
	    for(String key:keys) {
	        UploadFile file=upload.getFile(key);
	        files.add(file);
	    }
	    if(files.size()>0) {
	        UploadFile[] fileArray=new UploadFile[files.size()];
	        ctxUpload.setFiles(files.toArray(fileArray));
	    }
    }
    
    public void retrieveCurrentStatus(MultiFileUpload upload) {
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
