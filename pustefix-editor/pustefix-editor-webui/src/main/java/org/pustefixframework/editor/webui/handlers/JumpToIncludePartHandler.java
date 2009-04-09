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

import org.pustefixframework.container.annotations.Inject;
import org.pustefixframework.editor.webui.resources.DynIncludesResource;
import org.pustefixframework.editor.webui.resources.IncludesResource;
import org.pustefixframework.editor.webui.resources.ProjectsResource;
import org.pustefixframework.editor.webui.wrappers.JumpToIncludePart;

import de.schlund.pfixcore.generator.IHandler;
import de.schlund.pfixcore.generator.IWrapper;
import de.schlund.pfixcore.workflow.Context;

/**
 * Handles include part selection when jumping in editor
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class JumpToIncludePartHandler implements IHandler {
    
    private ProjectsResource projectsResource;
    private IncludesResource includesResource;
    private DynIncludesResource dynIncludesResource;

    public void handleSubmittedData(Context context, IWrapper wrapper)
            throws Exception {
        JumpToIncludePart input = (JumpToIncludePart) wrapper;
        if (input.getProjectURI() == null || input.getPath() == null
                || input.getPart() == null || input.getTheme() == null
                || input.getType() == null) {
            return;
        }
        String path = input.getPath();
        if (!path.startsWith("docroot:") && !path.startsWith("module:")) {
            if (!path.startsWith("/")) {
                path = "/" + path;
            }
            path = "docroot:" + path;
        }
        if (input.getType().equals("include")) {
            projectsResource.selectProject(input.getProjectURI());
            includesResource.selectIncludePart(path, input.getPart(), input.getTheme());
        } else if (input.getType().equals("dyninclude")) {
            projectsResource.selectProject(input.getProjectURI());
            dynIncludesResource.selectIncludePart(path, input.getPart(), input.getTheme());
        }
    }

    public void retrieveCurrentStatus(Context context, IWrapper wrapper)
            throws Exception {
        // Do not fill in form
    }

    public boolean prerequisitesMet(Context context) throws Exception {
        // Always allow jump target selection
        return true;
    }

    public boolean isActive(Context context) throws Exception {
        // Always try to receive input
        return true;
    }

    public boolean needsData(Context context) throws Exception {
        // Do never request input
        return false;
    }

    @Inject
    public void setProjectsResource(ProjectsResource projectsResource) {
        this.projectsResource = projectsResource;
    }

    @Inject
    public void setIncludesResource(IncludesResource includesResource) {
        this.includesResource = includesResource;
    }

    @Inject
    public void setDynIncludesResource(DynIncludesResource dynIncludesResource) {
        this.dynIncludesResource = dynIncludesResource;
    }

}
