package de.schlund.pfixxml.multipart;

import java.io.File;

public interface UploadFile {

    public String getName();
    public String getMimeType();
    public long getSize();
    public boolean exceedsSizeLimit();
    public File getLocalFile();
    
}
