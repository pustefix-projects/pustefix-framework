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

package org.pustefixframework.editor.backend.remote;

import org.pustefixframework.editor.common.dom.Image;
import org.pustefixframework.editor.common.dom.IncludeFile;
import org.pustefixframework.editor.common.dom.IncludePartThemeVariant;
import org.pustefixframework.editor.common.dom.Page;
import org.pustefixframework.editor.common.dom.Project;
import org.pustefixframework.editor.common.remote.service.RemoteProjectService;
import org.pustefixframework.editor.common.remote.transferobjects.IncludePartThemeVariantReferenceTO;
import org.pustefixframework.editor.common.remote.transferobjects.ProjectTO;

import de.schlund.pfixcore.editor2.core.spring.ProjectFactoryService;


public class RemoteProjectServiceImpl implements RemoteProjectService {
    
    private ProjectFactoryService projectFactoryService;
    
    public void setProjectFactoryService(ProjectFactoryService projectFactoryService) {
        this.projectFactoryService = projectFactoryService;
    }
    
    public ProjectTO getProject() {
        Project project =  projectFactoryService.getProject();
        ProjectTO to = new ProjectTO();
        to.name = project.getName();
        to.comment = project.getComment();
        to.includePartsEditableByDefault = project.isIncludePartsEditableByDefault();
        to.prefixToNamespaceMappings = project.getPrefixToNamespaceMappings();
        for (IncludeFile f : project.getDynIncludeFiles()) {
            to.dynIncludeFiles.add(f.getPath());
        }
        for (Image i : project.getAllImages()) {
            to.images.add(i.getPath());
        }
        for (IncludePartThemeVariant v : project.getAllIncludeParts()) {
            IncludePartThemeVariantReferenceTO ref = new IncludePartThemeVariantReferenceTO();
            ref.path = v.getIncludePart().getIncludeFile().getPath();
            ref.part = v.getIncludePart().getName();
            ref.theme = v.getTheme().getName();
            to.includeParts.add(ref);
        }
        for (Page p : project.getAllPages()) {
            to.pages.add(p.getFullName());
        }
        for (Page p : project.getTopPages()) {
            to.topPages.add(p.getFullName());
        }
        return to;
    }
    
}
