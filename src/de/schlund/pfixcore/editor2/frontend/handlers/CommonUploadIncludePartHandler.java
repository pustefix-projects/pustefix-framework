/*
 * This file is part of PFIXCORE.
 *
 * PFIXCORE is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * PFIXCORE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with PFIXCORE; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package de.schlund.pfixcore.editor2.frontend.handlers;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import de.schlund.pfixcore.editor2.core.exception.EditorException;
import de.schlund.pfixcore.editor2.core.exception.EditorIncludeHasChangedException;
import de.schlund.pfixcore.editor2.frontend.resources.CommonIncludesResource;
import de.schlund.pfixcore.editor2.frontend.wrappers.CommonUploadIncludePart;
import de.schlund.pfixcore.generator.IHandler;
import de.schlund.pfixcore.generator.IWrapper;
import de.schlund.pfixcore.workflow.Context;
import de.schlund.util.statuscodes.StatusCodeLib;

/**
 * Handles common include part upload
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public abstract class CommonUploadIncludePartHandler implements IHandler {

    protected abstract CommonIncludesResource getResource(Context context);

    public void handleSubmittedData(Context context, IWrapper wrapper)
        throws Exception {
        CommonUploadIncludePart input = (CommonUploadIncludePart) wrapper;
        // System.out.println("In HSD");
        if (input.getDoUpload() != null && input.getDoUpload().booleanValue()
            && input.getContent() != null && input.getHash() != null) {
            // System.out.println("In HSD: HASH: " + input.getHash());
            try {
                this.getResource(context).setContent(input.getContent(), input.getHash());
                input.setHash(this.getResource(context).getMD5());
            } catch (SAXException e) {
                Logger.getLogger(this.getClass()).warn(e);
                input.addSCodeContent(StatusCodeLib.PFIXCORE_EDITOR_INCLUDESUPLOAD_PARSE_ERR);
            } catch (EditorIncludeHasChangedException e) {
                input.setHash(e.getNewHash());
                input.setContent(e.getMerged());
                input.addSCodeContent(StatusCodeLib.PFIXCORE_EDITOR_INCLUDESUPLOAD_INCLUDE_HAS_CHANGED);
                return;
            } catch (EditorException e) {
                input.addSCodeContent(StatusCodeLib.PFIXCORE_EDITOR_INCLUDESUPLOAD_GEN_ERR);
                return;
            }
        }
    }

    public void retrieveCurrentStatus(Context context, IWrapper wrapper)
            throws Exception {
        // Insert XML and hash value
        CommonUploadIncludePart input = (CommonUploadIncludePart) wrapper;
        if (input.getDoUpload() == null || !input.getDoUpload().booleanValue()) {
            input.setContent(this.getResource(context).getContent());
            input.setHash(this.getResource(context).getMD5());
        }
    }

    public boolean prerequisitesMet(Context context) throws Exception {
        // Always allow upload
        return true;
    }

    public boolean isActive(Context context) throws Exception {
        // Handler is only active, if part is selected
        return (this.getResource(context).getSelectedIncludePart() != null);
    }

    public boolean needsData(Context context) throws Exception {
        // Always ask to upload include part
        return true;
    }

}
