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
