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

import de.schlund.pfixcore.example.iwrapper.FileUpload;
import de.schlund.pfixcore.generator.IHandler;
import de.schlund.pfixcore.generator.IWrapper;
import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixxml.multipart.UploadFile;

/**
 * @author mleidig@schlund.de
 */
public class FileUploadHandler implements IHandler {

	public void handleSubmittedData(Context context,IWrapper wrapper) throws Exception {
	    FileUpload upload=(FileUpload)wrapper;
	    UploadFile file=upload.getFile();
	    ContextFileUpload ctxUpload=context.getContextResourceManager().getResource(ContextFileUpload.class);
	    if(upload.getComment()!=null) ctxUpload.setComment(upload.getComment());
	    if(file!=null) ctxUpload.setFiles(new UploadFile[] {file});
    }
    
    public void retrieveCurrentStatus(Context context,IWrapper wrapper) throws Exception {
       
    }
    
    public boolean needsData(Context context) throws Exception {
        return false;
    }
    
    public boolean prerequisitesMet(Context context) throws Exception {
        return true;
    }

    public boolean isActive(Context context) throws Exception {
        return true;
    }
    
}
