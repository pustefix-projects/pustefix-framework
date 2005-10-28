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

import de.schlund.pfixcore.editor2.frontend.resources.CommonIncludesResource;
import de.schlund.pfixcore.editor2.frontend.wrappers.CommonBranchIncludePart;
import de.schlund.pfixcore.generator.IHandler;
import de.schlund.pfixcore.generator.IWrapper;
import de.schlund.pfixcore.workflow.Context;
import de.schlund.util.statuscodes.StatusCodeLib;

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
                input.addSCodeAction(StatusCodeLib.PFIXCORE_GENERATOR_MISSING_PARAM);
                return;
            }
            if (!this.getResource(context).createAndSelectBranch(
                    input.getTheme())) {
                input
                        .addSCodeAction(StatusCodeLib.PFIXCORE_EDITOR_INCLUDES_BRANCH_CREATE_FAILED);
            }
        } else if (input.getAction().equals("delete")) {
            if (!this.getResource(context).deleteSelectedBranch()) {
                input
                        .addSCodeAction(StatusCodeLib.PFIXCORE_EDITOR_INCLUDES_BRANCH_DELETE_FAILED);
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
