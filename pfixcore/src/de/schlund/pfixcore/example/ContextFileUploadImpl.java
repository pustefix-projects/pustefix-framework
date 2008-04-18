package de.schlund.pfixcore.example;

import org.w3c.dom.Element;

import de.schlund.pfixcore.beans.InitResource;
import de.schlund.pfixcore.beans.InsertStatus;
import de.schlund.pfixxml.ResultDocument;
import de.schlund.pfixxml.multipart.UploadFile;

public class ContextFileUploadImpl implements ContextFileUpload {

    private UploadFile[] uploadedFiles;
    private String       comment;

    @InitResource
    public void initialize() {
        // you could do some initialization here....
    }
    
    @InsertStatus
    public void printStatus(ResultDocument resdoc, Element elem) {
        if (comment != null) ResultDocument.addTextChild(elem, "comment", comment);
        if (uploadedFiles != null) {
            for (UploadFile file : uploadedFiles) {
                Element fileElem = resdoc.createSubNode(elem, "file");
                fileElem.setAttribute("name", file.getName());
                fileElem.setAttribute("mimetype", file.getMimeType());
                fileElem.setAttribute("exceedsSizeLimit", String.valueOf(file.exceedsSizeLimit()));
                if (!file.exceedsSizeLimit()) {
                    fileElem.setAttribute("localname", file.getLocalFile().getName());
                    fileElem.setAttribute("size", String.valueOf(file.getSize()));
                }
            }
        }
    }

    public void setFiles(UploadFile[] uploadedFiles) {
        this.uploadedFiles = uploadedFiles;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getComment() {
        return comment;
    }
    
    public UploadFile[] getFiles() {
        return uploadedFiles;
    }
}
