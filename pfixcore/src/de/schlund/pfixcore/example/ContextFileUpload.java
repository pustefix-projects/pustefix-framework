package de.schlund.pfixcore.example;

import de.schlund.pfixxml.multipart.UploadFile;

public interface ContextFileUpload {

    public void setComment(String comment);
    public void setFiles(UploadFile[] files);
    
}
