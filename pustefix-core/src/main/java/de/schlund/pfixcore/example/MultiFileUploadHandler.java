package de.schlund.pfixcore.example;

import java.util.ArrayList;
import java.util.List;

import de.schlund.pfixcore.example.iwrapper.MultiFileUpload;
import de.schlund.pfixcore.generator.IHandler;
import de.schlund.pfixcore.generator.IWrapper;
import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixxml.multipart.UploadFile;

/**
 * @author mleidig@schlund.de
 */
public class MultiFileUploadHandler implements IHandler {

	public void handleSubmittedData(Context context,IWrapper wrapper) throws Exception {
	    MultiFileUpload upload=(MultiFileUpload)wrapper;
	    ContextFileUpload ctxUpload=context.getContextResourceManager().getResource(ContextFileUpload.class);
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
