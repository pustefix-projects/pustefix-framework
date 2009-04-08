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

package org.pustefixframework.editor.webui.resources;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;

import org.pustefixframework.container.annotations.Inject;
import org.pustefixframework.editor.common.dom.Image;
import org.pustefixframework.editor.common.dom.Page;
import org.pustefixframework.editor.common.dom.Project;
import org.pustefixframework.editor.common.exception.EditorSecurityException;
import org.w3c.dom.Element;

import de.schlund.pfixcore.beans.InsertStatus;
import de.schlund.pfixcore.editor2.core.spring.ProjectPool;
import de.schlund.pfixcore.editor2.core.spring.SecurityManagerService;
import de.schlund.pfixxml.ResultDocument;

/**
 * Implementation of ImagesResource
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class ImagesResource {
    private SecurityManagerService securitymanager;
    private ProjectsResource projectsResource;
    private ProjectPool projectPool;

    private Image   selectedImage;

    public boolean selectImage(String path) {
        if (path == null || path.equals("")) {
            return false;
        }
        Project project = projectsResource.getSelectedProject();
        if (project == null) {
            return false;
        }
        Collection<Image> allimages = project.getAllImages();
        for (Iterator<Image> i = allimages.iterator(); i.hasNext();) {
            Image image = i.next();
            if (image.getPath().equals(path)) {
                this.selectedImage = image;
                return true;
            }
        }
        return false;
    }

    public void unselectImage() {
        this.selectedImage = null;
    }

    public Image getSelectedImage() {
        return this.selectedImage;
    }

    @InsertStatus
    public void insertStatus(ResultDocument resdoc, Element elem) throws Exception {
        Project project = projectsResource.getSelectedProject();

        if (project != null) {
            TreeSet<Image> allimages = new TreeSet<Image>(project.getAllImages());
            HashMap<String, Element> directoryNodes = new HashMap<String, Element>();
            for (Iterator<Image> i = allimages.iterator(); i.hasNext();) {
                Image image = i.next();
                String path = image.getPath();
                String directory;
                if (path.lastIndexOf("/") > 0) {
                    directory = path.substring(0, path.lastIndexOf("/"));
                } else {
                    directory = "/";
                }
                Element directoryNode = directoryNodes.get(directory);
                if (directoryNode == null) {
                    directoryNode = resdoc.createSubNode(elem, "directory");
                    directoryNode.setAttribute("path", directory);
                    directoryNodes.put(directory, directoryNode);
                }
                Element imageNode = resdoc.createSubNode(directoryNode, "image");
                imageNode.setAttribute("path", path);
                String filename;
                if (path.lastIndexOf("/") > 0 && path.lastIndexOf("/") < path.length()) {
                    filename = path.substring(path.lastIndexOf("/") + 1);
                } else {
                    filename = path;
                }
                imageNode.setAttribute("filename", filename);
                if (image.getLastModTime() == 0) {
                    imageNode.setAttribute("missing", "true");
                }
                if (this.selectedImage != null && image.equals(this.selectedImage)) {
                    imageNode.setAttribute("selected", "true");
                }
            }
            if (this.selectedImage != null) {
                Element currentImage = resdoc.createSubNode(elem, "currentimage");
                String path = this.selectedImage.getPath();
                String url;
                if (path.startsWith("docroot:/")) {
                    url = projectPool.getURIForProject(projectsResource.getSelectedProject())
                        + path.substring(10);
                } else {
                    url = projectPool.getURIForProject(projectsResource.getSelectedProject())
                    + path;
                }
                currentImage.setAttribute("url", url);
                currentImage.setAttribute("path", path);
                String filename;
                if (path.lastIndexOf("/") > 0 && path.lastIndexOf("/") < path.length()) {
                    filename = path.substring(path.lastIndexOf("/") + 1);
                } else {
                    filename = path;
                }
                currentImage.setAttribute("filename", filename);
                currentImage.setAttribute("modtime", Long.toString(this.selectedImage.getLastModTime()));
                if (securitymanager.mayEditImages(projectsResource.getSelectedProject())) {
                    currentImage.setAttribute("mayEdit", "true");
                } else {
                    currentImage.setAttribute("mayEdit", "false");
                }

                // Render backups
                Collection<String> backups = this.selectedImage.getBackupVersions();
                if (!backups.isEmpty()) {
                    Element backupsNode = resdoc.createSubNode(currentImage, "backups");
                    for (Iterator<String> i = backups.iterator(); i.hasNext();) {
                        String version = i.next();
                        ResultDocument.addTextChild(backupsNode, "option", version);
                    }
                }

                // Render affected pages
                Collection<Page> pages = this.selectedImage.getAffectedPages();
                if (!pages.isEmpty()) {
                    Element pagesNode = resdoc.createSubNode(currentImage, "pages");
                    HashMap<Project, Element> projectNodes = new HashMap<Project, Element>();
                    for (Iterator<Page> i = pages.iterator(); i.hasNext();) {
                        Page page = (Page) i.next();
                        Element projectNode;
                        if (projectNodes.containsKey(page.getProject())) {
                            projectNode = (Element) projectNodes.get(page.getProject());
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
            }
        }
    }

    public void reset() throws Exception {
        this.selectedImage = null;
    }

    public int restoreBackup(String version, long modtime) {
        if (this.selectedImage == null) {
            return 1;
        }
        if (this.selectedImage.getLastModTime() != modtime) {
            return 2;
        }

        try {
            if (this.selectedImage.restore(version)) {
                return 0;
            } else {
                return 1;
            }
        } catch (EditorSecurityException e) {
            return 1;
        }
    }

    @Inject
    public void setSecurityManagerService(SecurityManagerService securitymanager) {
        this.securitymanager = securitymanager;
    }

    @Inject
    public void setProjectsResource(ProjectsResource projectsResource) {
        this.projectsResource = projectsResource;
    }
    
    @Inject
    public void setProjectPool(ProjectPool projectPool) {
        this.projectPool = projectPool;
    }

}
