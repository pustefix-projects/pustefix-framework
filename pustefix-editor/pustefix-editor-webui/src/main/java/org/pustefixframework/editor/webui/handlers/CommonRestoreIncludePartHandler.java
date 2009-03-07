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

package org.pustefixframework.editor.webui.handlers;

import org.pustefixframework.editor.generated.EditorStatusCodes;
import org.pustefixframework.editor.webui.resources.CommonIncludesResource;
import org.pustefixframework.editor.webui.wrappers.CommonRestoreIncludePart;

import de.schlund.pfixcore.generator.IHandler;
import de.schlund.pfixcore.generator.IWrapper;
import de.schlund.pfixcore.workflow.Context;

/**
 * Handles common include part restore from backup
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public abstract class CommonRestoreIncludePartHandler implements IHandler {

    protected abstract CommonIncludesResource getResource(Context context);

    public void handleSubmittedData(Context context, IWrapper wrapper)
            throws Exception {
        CommonRestoreIncludePart input = (CommonRestoreIncludePart) wrapper;
        int ret = this.getResource(context).restoreBackup(input.getVersion(),
                input.getHash());
        if (ret == 1) {
            input.addSCodeVersion(EditorStatusCodes.INCLUDES_INCLUDE_UNDEF);
        } else if (ret == 2) {
            input
                    .addSCodeVersion(EditorStatusCodes.INCLUDESUPLOAD_INCLUDE_HAS_CHANGED_RESTORE);
        }
    }

    public void retrieveCurrentStatus(Context context, IWrapper wrapper)
            throws Exception {
        // Do not prefill form
    }

    public boolean prerequisitesMet(Context context) throws Exception {
        // Always allow restore
        return true;
    }

    public boolean isActive(Context context) throws Exception {
        // Handler is only active, if there is a selected include
        return (this.getResource(context).getSelectedIncludePart() != null);
    }

    public boolean needsData(Context context) throws Exception {
        // Always ask for upload
        return true;
    }

}
