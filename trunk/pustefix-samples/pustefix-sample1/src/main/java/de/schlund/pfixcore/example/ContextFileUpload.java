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

import org.w3c.dom.Element;

import de.schlund.pfixcore.beans.InitResource;
import de.schlund.pfixcore.beans.InsertStatus;
import de.schlund.pfixxml.ResultDocument;
import de.schlund.pfixxml.multipart.UploadFile;

public class ContextFileUpload {

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
