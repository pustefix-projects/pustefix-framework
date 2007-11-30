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

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.w3c.dom.Element;

import de.schlund.pfixcore.editor2.core.dom.IncludeFile;
import de.schlund.pfixcore.editor2.core.dom.IncludePart;
import de.schlund.pfixcore.editor2.core.dom.IncludePartThemeVariant;
import de.schlund.pfixcore.editor2.core.dom.Page;
import de.schlund.pfixcore.editor2.core.dom.Project;
import de.schlund.pfixcore.editor2.core.dom.Theme;
import de.schlund.pfixcore.editor2.frontend.util.SpringBeanLocator;
import de.schlund.pfixxml.ResultDocument;

public class IncludesResourceImpl extends CommonIncludesResourceImpl implements
        IncludesResource {

    protected boolean securityMayCreateIncludePartThemeVariant(
            IncludePart includePart, Theme theme) {
        return SpringBeanLocator.getSecurityManagerService()
                .mayCreateIncludePartThemeVariant(includePart, theme);
    }

    protected IncludePartThemeVariant internalSelectIncludePart(
            Project project, String path, String part, String theme) {
        IncludePartThemeVariant variant = project.findIncludePartThemeVariant(
                path, part, theme);
        return variant;
    }

    protected Collection getPossibleThemes(
            IncludePartThemeVariant selectedIncludePart, Project project,
            Collection pages) {
        TreeSet themes = new TreeSet();
        for (Iterator i = selectedIncludePart.getIncludePart()
                .getPossibleThemes().iterator(); i.hasNext();) {
            Theme theme = (Theme) i.next();
            if (!selectedIncludePart.getIncludePart().hasThemeVariant(theme)) {
                boolean canUseTheme = false;
                if (!SpringBeanLocator.getSecurityManagerService()
                        .mayCreateIncludePartThemeVariant(
                                selectedIncludePart.getIncludePart(), theme)) {
                    // Omit current theme
                    continue;
                }
                for (Iterator i2 = pages.iterator(); i2.hasNext();) {
                    Page page = (Page) i2.next();
                    if (!page.getProject().equals(project)) {
                        // Ignore pages in other projects
                        continue;
                    }
                    for (Iterator i3 = page.getThemes().getThemes().iterator(); i3
                            .hasNext();) {
                        Theme pageTheme = (Theme) i3.next();
                        if (pageTheme.equals(theme)) {
                            canUseTheme = true;
                            break;
                        }
                        if (pageTheme.equals(selectedIncludePart.getTheme())) {
                            // Ignore pages which will not be using the new
                            // variant
                            break;
                        }
                    }
                    if (canUseTheme) {
                        // Do not search for more pages
                        break;
                    }
                }
                if (canUseTheme) {
                    themes.add(theme);
                }
            }
        }
        return themes;
    }

    protected boolean securityMayEditIncludePartThemeVariant(
            IncludePartThemeVariant variant) {
        return SpringBeanLocator.getSecurityManagerService()
                .mayEditIncludePartThemeVariant(variant);
    }

    protected void renderAllIncludes(ResultDocument resdoc, Element elem,
            Project project) {
        TreeSet includes = new TreeSet(project.getAllIncludeParts());
        HashMap directoryNodes = new HashMap();
        HashMap fileNodes = new HashMap();
        for (Iterator i = includes.iterator(); i.hasNext();) {
            IncludePartThemeVariant include = (IncludePartThemeVariant) i
                    .next();
            String path = include.getIncludePart().getIncludeFile().getPath();
            Element fileNode = (Element) fileNodes.get(path);
            if (fileNode == null) {
                String directory;
                try {
                    directory = path.substring(0, path
                            .lastIndexOf(File.separator));
                } catch (StringIndexOutOfBoundsException e) {
                    directory = "/";
                }
                Element directoryNode = (Element) directoryNodes.get(directory);
                if (directoryNode == null) {
                    directoryNode = resdoc.createSubNode(elem, "directory");
                    directoryNode.setAttribute("path", directory);
                    String jsId = directory.replaceAll("\\.", "_dot_")
                            .replaceAll("/", "_slash_");
                    directoryNode.setAttribute("jsId", jsId);
                    if (this.isDirectoryOpen(directory)) {
                        directoryNode.setAttribute("open", "true");
                    }
                    directoryNodes.put(directory, directoryNode);
                }
                fileNode = resdoc.createSubNode(directoryNode, "file");
                fileNode.setAttribute("path", path);
                if (this.isFileOpen(path)) {
                    fileNode.setAttribute("open", "true");
                }
                fileNodes.put(path, fileNode);
            }

            Element includeNode = resdoc.createSubNode(fileNode, "include");
            includeNode
                    .setAttribute("part", include.getIncludePart().getName());
            includeNode.setAttribute("theme", include.getTheme().getName());
            if (this.getSelectedIncludePart() != null
                    && include.equals(this.getSelectedIncludePart())) {
                includeNode.setAttribute("selected", "true");
            }
        }
    }

    protected Set<IncludeFile> getIncludeFilesInDirectory(String dirname,
            Project project) {
        Collection<IncludePartThemeVariant> parts = project
                .getAllIncludeParts();
        TreeSet<IncludeFile> files = new TreeSet<IncludeFile>();
        for (Iterator i = parts.iterator(); i.hasNext();) {
            IncludePartThemeVariant part = (IncludePartThemeVariant) i.next();
            IncludeFile file = part.getIncludePart().getIncludeFile();
            String path = file.getPath();
            try {
                path = path.substring(0, path.lastIndexOf('/'));
            } catch (StringIndexOutOfBoundsException e) {
                path = "/";
            }
            if (path.equals(dirname) && !files.contains(file)) {
                files.add(file);
            }
        }
        return files;
    }

    protected Set<IncludePartThemeVariant> getIncludePartsInFile(
            String filename, Project project) {
        Collection<IncludePartThemeVariant> allparts = project
                .getAllIncludeParts();
        TreeSet<IncludePartThemeVariant> parts = new TreeSet<IncludePartThemeVariant>();
        for (Iterator i = allparts.iterator(); i.hasNext();) {
            IncludePartThemeVariant part = (IncludePartThemeVariant) i.next();
            IncludeFile file = part.getIncludePart().getIncludeFile();
            if (file.getPath().equals(filename)) {
                parts.add(part);
            }
        }
        return parts;
    }
}
