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

package de.schlund.pfixcore.editor2.frontend.resources;

import java.util.Iterator;

import org.pustefixframework.container.annotations.Inject;
import org.w3c.dom.Element;

import de.schlund.pfixcore.beans.InitResource;
import de.schlund.pfixcore.beans.InsertStatus;
import de.schlund.pfixcore.editor2.core.dom.Project;
import de.schlund.pfixcore.editor2.core.spring.ProjectFactoryService;
import de.schlund.pfixcore.lucefix.ContextSearch;
import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixxml.ResultDocument;

public class ProjectsResource {
    private Project selectedProject;

    private ProjectFactoryService projectfactory;
    
    private PagesResource pagesResource;
    private TargetsResource targetsResource;
    private ImagesResource imagesResource;
    private IncludesResource includesResource;
    private ContextSearch contextSearch;
    
    @Inject
    public void setProjectFactoryService(ProjectFactoryService projectfactory) {
        this.projectfactory = projectfactory;
    }

    @InitResource
    public void init(Context context) throws Exception {
        this.selectedProject = null;
    }

    @InsertStatus
    public void insertStatus(ResultDocument resdoc, Element elem) throws Exception {
        for (Iterator<Project> i = projectfactory.getProjects().iterator(); i.hasNext();) {
            Project project = i.next();
            Element projectElement = resdoc.createSubNode(elem, "project");
            projectElement.setAttribute("name", project.getName());
            projectElement.setAttribute("comment", project.getComment());
            if (this.selectedProject != null && project.equals(this.selectedProject)) {
                projectElement.setAttribute("selected", "true");
            }
        }
    }

    public void reset() {
        this.selectedProject = null;
    }

    public boolean selectProject(String projectName) {
        Project project = projectfactory.getProjectByName(projectName);
        if (project == null) {
            return false;
        } else {
            this.selectedProject = project;
            // Reset page selection
            pagesResource.unselectPage();
            targetsResource.unselectTarget();
            imagesResource.unselectImage();
            includesResource.unselectIncludePart();
            contextSearch.resetData();
            return true;
        }
    }

    public Project getSelectedProject() {
        return this.selectedProject;
    }

    public boolean selectProjectByTargetGeneratorName(String targetGenerator) {
        Project project = projectfactory.getProjectByPustefixTargetGeneratorName(targetGenerator);
        if (project == null) {
            return false;
        } else {
            this.selectedProject = project;
            // Reset page selection
            pagesResource.unselectPage();
            targetsResource.unselectTarget();
            imagesResource.unselectImage();
            includesResource.unselectIncludePart();
            return true;
        }
    }

    @Inject
    public void setPagesResource(PagesResource pagesResource) {
        this.pagesResource = pagesResource;
    }

    @Inject
    public void setTargetsResource(TargetsResource targetsResource) {
        this.targetsResource = targetsResource;
    }

    @Inject
    public void setImagesResource(ImagesResource imagesResource) {
        this.imagesResource = imagesResource;
    }

    @Inject
    public void setIncludesResource(IncludesResource includesResource) {
        this.includesResource = includesResource;
    }

    @Inject
    public void setContextSearch(ContextSearch contextSearch) {
        this.contextSearch = contextSearch;
    }

}
