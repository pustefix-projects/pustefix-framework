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

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;

import org.pustefixframework.container.annotations.Inject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.schlund.pfixcore.beans.InitResource;
import de.schlund.pfixcore.beans.InsertStatus;
import de.schlund.pfixcore.editor2.core.dom.Image;
import de.schlund.pfixcore.editor2.core.dom.IncludePartThemeVariant;
import de.schlund.pfixcore.editor2.core.dom.Page;
import de.schlund.pfixcore.editor2.core.dom.Project;
import de.schlund.pfixcore.editor2.core.dom.Target;
import de.schlund.pfixcore.editor2.core.dom.TargetType;
import de.schlund.pfixcore.editor2.core.exception.EditorParsingException;
import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixxml.ResultDocument;

public class TargetsResource {

    private ProjectsResource projectsResource;

    private Target  selectedTarget;

    @InitResource
    public void init(Context context) throws Exception {
        this.selectedTarget = null;
    }

    @InsertStatus
    public void insertStatus(ResultDocument resdoc, Element elem) throws Exception {
        Project project = projectsResource.getSelectedProject();
        if (project != null) {
            for (Iterator<Page> i = project.getAllPages().iterator(); i.hasNext();) {
                Page page = i.next();
                Target pageTarget = page.getPageTarget();
                // Null targets may exist because dummy page objects (for pages
                // which are only in navigation, not in target section) might
                // exist.
                if (pageTarget != null) {
                    this.renderTarget(pageTarget, elem);
                }
            }
        }
        if (this.selectedTarget != null) {
            Element currentTarget = resdoc.createSubNode(elem, "currenttarget");
            currentTarget.setAttribute("name", this.selectedTarget.getName());

            // Render parameters
            Map<String, Object> params = this.selectedTarget.getParameters();
            for (Iterator<String> i = params.keySet().iterator(); i.hasNext();) {
                String key = i.next();
                String value = params.get(key).toString();
                Element param = resdoc.createSubNode(currentTarget, "param");
                param.setAttribute("name", key);
                param.setAttribute("value", value);
            }

            // Render affected pages
            Collection<Page> pages = this.selectedTarget.getAffectedPages();
            if (!pages.isEmpty()) {
                Element pagesNode = resdoc.createSubNode(currentTarget, "pages");
                HashMap<Project, Element> projectNodes = new HashMap<Project, Element>();
                for (Iterator<Page> i = pages.iterator(); i.hasNext();) {
                    Page page = i.next();
                    Element projectNode;
                    if (projectNodes.containsKey(page.getProject())) {
                        projectNode = projectNodes.get(page.getProject());
                    } else {
                        projectNode = resdoc.createSubNode(pagesNode, "project");
                        projectNode.setAttribute("name", page.getProject().getName());
                        projectNodes.put(page.getProject(), projectNode);
                    }
                    Element pageNode = resdoc.createSubNode(projectNode, "page");
                    pageNode.setAttribute("name", page.getName());
                    if (page.getVariant() != null) {
                        pageNode.setAttribute("variant", page.getVariant().getName());
                    }
                }
            }

            // Render includes
            TreeSet<IncludePartThemeVariant> includes = new TreeSet<IncludePartThemeVariant>(this.selectedTarget.getIncludeDependencies(false));
            // All includes are needed later to render images
            TreeSet<IncludePartThemeVariant> allincludes = new TreeSet<IncludePartThemeVariant>(includes);
            if (!includes.isEmpty()) {
                Element includesNode = resdoc.createSubNode(currentTarget, "includes");
                for (Iterator<IncludePartThemeVariant> i = includes.iterator(); i.hasNext();) {
                    IncludePartThemeVariant variant = i.next();
                    this.renderInclude(variant, includesNode);
                    allincludes.addAll(variant.getIncludeDependencies(this.selectedTarget, true));
                }
            }

            // Render images
            // Get images for current target and all include parts
            TreeSet<Image> images = new TreeSet<Image>(this.selectedTarget.getImageDependencies(false));
            for (Iterator<IncludePartThemeVariant> i = allincludes.iterator(); i.hasNext();) {
                IncludePartThemeVariant variant = i.next();
                images.addAll(variant.getImageDependencies(this.selectedTarget, true));
            }
            if (!images.isEmpty()) {
                Element imagesNode = resdoc.createSubNode(currentTarget, "images");
                for (Iterator<Image> i = images.iterator(); i.hasNext();) {
                    Image image = i.next();
                    Element imageNode = resdoc.createSubNode(imagesNode, "image");
                    imageNode.setAttribute("path", image.getPath());
                    imageNode.setAttribute("modtime", Long.toString(image.getLastModTime()));
                }
            }

            // Render content XML
            Document content = this.selectedTarget.getContentXML();
            if (content != null) {
                Element contentNode = resdoc.createSubNode(currentTarget, "content");
                contentNode.appendChild(contentNode.getOwnerDocument().importNode(content.getDocumentElement(), true));
            }
        }
    }

    private void renderInclude(IncludePartThemeVariant variant, Element parent) {
        Document doc = parent.getOwnerDocument();
        Element includeNode = doc.createElement("include");
        parent.appendChild(includeNode);
        includeNode.setAttribute("part", variant.getIncludePart().getName());
        includeNode.setAttribute("path", variant.getIncludePart().getIncludeFile().getPath());
        includeNode.setAttribute("theme", variant.getTheme().getName());

        try {
            TreeSet<IncludePartThemeVariant> variants = new TreeSet<IncludePartThemeVariant>(variant.getIncludeDependencies(this.selectedTarget, false));
            for (Iterator<IncludePartThemeVariant> i = variants.iterator(); i.hasNext();) {
                IncludePartThemeVariant variant2 = i.next();
                this.renderInclude(variant2, includeNode);
            }
        } catch (EditorParsingException e) {
            // Omit dependencies for this part
        }
    }

    private void renderTarget(Target target, Element parent) {
        renderTarget(target, parent, false);
    }

    private void renderTarget(Target target, Element parent, boolean forceAux) {
        Document doc = parent.getOwnerDocument();
        Element node = doc.createElement("target");
        parent.appendChild(node);
        node.setAttribute("name", target.getName());
        if (this.selectedTarget != null && this.selectedTarget.equals(target)) {
            node.setAttribute("selected", "true");
        }
        if (forceAux) {
            node.setAttribute("type", "aux");
        } else {
            if (target.getType() == TargetType.TARGET_XML) {
                node.setAttribute("type", "xml");
            } else if (target.getType() == TargetType.TARGET_XSL) {
                node.setAttribute("type", "xsl");
            } else if (target.getType() == TargetType.TARGET_AUX) {
                node.setAttribute("type", "aux");
            }
        }
        if (target.isLeafTarget()) {
            node.setAttribute("leaf", "true");
        } else {
            node.setAttribute("leaf", "false");
            this.renderTarget(target.getParentXML(), node);
            this.renderTarget(target.getParentXSL(), node);
        }
        for (Iterator<Target> i = target.getAuxDependencies().iterator(); i.hasNext();) {
            Target auxtarget = i.next();
            this.renderTarget(auxtarget, node, true);
        }
    }

    public void reset() throws Exception {
        this.selectedTarget = null;
    }

    public boolean selectTarget(String targetName) {
        Project project = projectsResource.getSelectedProject();
        Target target = project.getTarget(targetName);
        if (target == null) {
            return false;
        } else {
            this.selectedTarget = target;
            return true;
        }
    }

    public void unselectTarget() {
        this.selectedTarget = null;
    }

    @Inject
    public void setProjectsResource(ProjectsResource projectsResource) {
        this.projectsResource = projectsResource;
    }
}
