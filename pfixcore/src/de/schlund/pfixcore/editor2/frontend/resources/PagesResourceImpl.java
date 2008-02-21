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
import java.util.TreeSet;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.schlund.pfixcore.editor2.core.dom.Image;
import de.schlund.pfixcore.editor2.core.dom.IncludePartThemeVariant;
import de.schlund.pfixcore.editor2.core.dom.Page;
import de.schlund.pfixcore.editor2.core.dom.Project;
import de.schlund.pfixcore.editor2.core.dom.Target;
import de.schlund.pfixcore.editor2.core.dom.TargetType;
import de.schlund.pfixcore.editor2.frontend.util.EditorResourceLocator;
import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixxml.ResultDocument;

public class PagesResourceImpl implements PagesResource {
    Page selectedPage;

    private Context context;

    public void init(Context context) throws Exception {
        this.selectedPage = null;
        this.context = context;
    }

    public void insertStatus(ResultDocument resdoc, Element elem)
            throws Exception {
        Project project = EditorResourceLocator.getProjectsResource(
                this.context).getSelectedProject();
        if (project != null) {
            // Make sure pages are in right order
            TreeSet<Page> pages = new TreeSet<Page>(project.getTopPages());
            for (Iterator<Page> i = pages.iterator(); i.hasNext();) {
                Page page = i.next();
                this.renderPageElement(page, elem);
            }
        }
        if (this.selectedPage != null) {
            Element currentPage = resdoc.createSubNode(elem, "currentpage");
            currentPage.setAttribute("name", this.selectedPage.getName());

            Element targetsElement = resdoc.createSubNode(currentPage,
                    "targets");
            
            Target pageTarget = this.selectedPage.getPageTarget();
            // Null targets may exist because dummy page objects (for pages
            // which are only in navigation, not in target section) might
            // exist.
            if (pageTarget != null) {
                this.renderTarget(pageTarget, targetsElement);
                
                // Sort include parts
                TreeSet<IncludePartThemeVariant> includes = new TreeSet<IncludePartThemeVariant>(this.selectedPage.getPageTarget().getIncludeDependencies(true));
                if (!includes.isEmpty()) {
                    Element includesElement = resdoc.createSubNode(currentPage, "includes");
                    for (Iterator<IncludePartThemeVariant> i = includes.iterator(); i.hasNext();) {
                        IncludePartThemeVariant part = i.next();
                        Element includeElement = resdoc.createSubNode(includesElement, "include");
                        includesElement.appendChild(includeElement);
                        includeElement.setAttribute("path", part.getIncludePart().getIncludeFile().getPath());
                        includeElement.setAttribute("part", part.getIncludePart().getName());
                        includeElement.setAttribute("theme", part.getTheme().getName());
                    }
                }
                
                // Sort images
                TreeSet<Image> images = new TreeSet<Image>(this.selectedPage.getPageTarget().getImageDependencies(true));
                if (!images.isEmpty()) {
                    Element imagesElement = resdoc.createSubNode(currentPage, "images");
                    for (Iterator<Image> i = images.iterator(); i.hasNext();) {
                        Image image = i.next();
                        Element imageElement = resdoc.createSubNode(imagesElement, "image");
                        imageElement.setAttribute("path", image.getPath());
                        imageElement.setAttribute("modtime", Long.toString(image.getLastModTime()));
                    }
                }
            }
        }
    }

    private void renderTarget(Target target, Element parent) {
        Document doc = parent.getOwnerDocument();
        Element targetElement = doc.createElement("target");
        parent.appendChild(targetElement);
        targetElement.setAttribute("name", target.getName());
        if (target.getType() == TargetType.TARGET_XML) {
            targetElement.setAttribute("type", "xml");
        } else if (target.getType() == TargetType.TARGET_XSL) {
            targetElement.setAttribute("type", "xsl");
        }
        if (target.isLeafTarget()) {
            targetElement.setAttribute("leaf", "true");
        } else {
            targetElement.setAttribute("leaf", "false");
            this.renderTarget(target.getParentXML(), targetElement);
            this.renderTarget(target.getParentXSL(), targetElement);
        }
    }

    private void renderPageElement(Page page, Element parent) {
        Document doc = parent.getOwnerDocument();
        Element node = doc.createElement("page");
        parent.appendChild(node);
        node.setAttribute("name", page.getName());
        if (page.getVariant() != null) {
            node.setAttribute("variant", page.getVariant().getName());
        } else {
            // Render subpages for default variant only
            // Make sure pages are in right order
            TreeSet<Page> pages = new TreeSet<Page>(page.getSubPages());
            for (Iterator<Page> i = pages.iterator(); i.hasNext();) {
                Page subpage = i.next();
                this.renderPageElement(subpage, node);
            }
        }
        node.setAttribute("handler", page.getHandlerPath());
        if (this.selectedPage != null && page.equals(selectedPage)) {
            node.setAttribute("selected", "true");
        }

    }

    public void reset() throws Exception {
        this.selectedPage = null;
    }

    public boolean selectPage(String pageName, String variantName) {
        Project project = EditorResourceLocator.getProjectsResource(
                this.context).getSelectedProject();
        if (project == null) {
            return false;
        }
        Page page = null;
        if (variantName == null || variantName.equals("")) {
            page = project.getPage(pageName, null);
        } else {
            // Make sure page variants are ordered
            for (Iterator<Page> i = project.getPageByName(pageName).iterator(); i
                    .hasNext();) {
                Page page2 = i.next();
                // If variant is matching exactly, we have what we want
                if (page2.getVariant() != null
                        && page2.getVariant().getName().equals(variantName)) {
                    page = page2;
                    break;
                }
                if (page == null && page2.getVariant() == null) {
                    // If we have not yet found a matching variant
                    // use default variant until we find a better one
                    page = page2;
                } else {
                    // Check if the new variant is better matching
                    // than the current one
                    if (page == null) {
                        // Check using string
                        if (variantName
                                .startsWith(page2.getVariant().getName())) {
                            page = page2;
                        }
                    } else {
                        // New variant is matching string and more
                        // specific than current one
                        if (variantName
                                .startsWith(page2.getVariant().getName())) {
                            if (page.getVariant() == null) {
                                // The current variant is the default one
                                // so each other matching variant is better
                                page = page2;
                            } else if (page2.getVariant().isChildOf(
                                    page.getVariant())) {
                                // New variant is more specific
                                page = page2;
                            }
                        }
                    }
                }

            }
        }
        if (page == null) {
            // Found no matching page
            return false;
        } else {
            this.selectedPage = page;
            return true;
        }
    }

    public void unselectPage() {
        this.selectedPage = null;
    }

}
