package de.schlund.pfixcore.example;

import de.schlund.pfixcore.workflow.ContextResource;
import de.schlund.pfixxml.multipart.UploadFile;

public interface ContextFileUpload extends ContextResource {

    public void setComment(String comment);
    public void setFiles(UploadFile[] files);
    
}
