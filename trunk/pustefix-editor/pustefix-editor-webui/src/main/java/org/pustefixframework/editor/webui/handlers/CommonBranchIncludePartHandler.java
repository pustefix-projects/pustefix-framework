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

package org.pustefixframework.editor.webui.handlers;

import org.pustefixframework.editor.generated.EditorStatusCodes;
import org.pustefixframework.editor.webui.resources.CommonIncludesResource;
import org.pustefixframework.editor.webui.wrappers.CommonBranchIncludePart;
import org.pustefixframework.generated.CoreStatusCodes;

import de.schlund.pfixcore.generator.IHandler;
import de.schlund.pfixcore.generator.IWrapper;
import de.schlund.pfixcore.workflow.Context;

/**
 * Handles common include part branching
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public abstract class CommonBranchIncludePartHandler implements IHandler {

    protected abstract CommonIncludesResource getResource(Context context);

    public void handleSubmittedData(Context context, IWrapper wrapper)
            throws Exception {
        CommonBranchIncludePart input = (CommonBranchIncludePart) wrapper;
        if (input.getAction().equals("create")) {
            if (input.getTheme() == null || input.getTheme().equals("")) {
                input.addSCodeAction(CoreStatusCodes.MISSING_PARAM);
                return;
            }
            if (!this.getResource(context).createAndSelectBranch(
                    input.getTheme())) {
                input
                        .addSCodeAction(EditorStatusCodes.INCLUDES_BRANCH_CREATE_FAILED);
            }
        } else if (input.getAction().equals("delete")) {
            if (!this.getResource(context).deleteSelectedBranch()) {
                input
                        .addSCodeAction(EditorStatusCodes.INCLUDES_BRANCH_DELETE_FAILED);
            }
        }
    }

    public void retrieveCurrentStatus(Context context, IWrapper wrapper)
            throws Exception {
        // Do not insert any values
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
        // Always ask to select branch
        return true;
    }

}
