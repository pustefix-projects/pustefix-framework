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

import org.pustefixframework.container.annotations.Inject;
import org.pustefixframework.editor.generated.EditorStatusCodes;

import de.schlund.pfixcore.editor2.frontend.resources.ProjectsResource;
import de.schlund.pfixcore.editor2.frontend.resources.TargetsResource;
import de.schlund.pfixcore.editor2.frontend.wrappers.SelectTarget;
import de.schlund.pfixcore.generator.IHandler;
import de.schlund.pfixcore.generator.IWrapper;
import de.schlund.pfixcore.workflow.Context;

/**
 * Handles target selection
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class SelectTargetHandler implements IHandler {

    private TargetsResource targetsResource;
    private ProjectsResource projectsResource;

    public void handleSubmittedData(Context context, IWrapper wrapper)
            throws Exception {
        SelectTarget input = (SelectTarget) wrapper;
        if (!targetsResource.selectTarget(input.getTargetName())) {
            input.addSCodeTargetName(EditorStatusCodes.TARGETS_TARGET_UNDEF);
        }
    }

    public void retrieveCurrentStatus(Context context, IWrapper wrapper)
            throws Exception {
        // Do not insert any data
    }

    public boolean prerequisitesMet(Context context) throws Exception {
        // Allow only if project is selected
        return (projectsResource.getSelectedProject() != null);
    }

    public boolean isActive(Context context) throws Exception {
        // Allways allow target selection
        return true;
    }

    public boolean needsData(Context context) throws Exception {
        // Always ask to select target
        return true;
    }

    @Inject
    public void setTargetsResource(TargetsResource targetsResource) {
        this.targetsResource = targetsResource;
    }

    @Inject
    public void setProjectsResource(ProjectsResource projectsResource) {
        this.projectsResource = projectsResource;
    }

}
