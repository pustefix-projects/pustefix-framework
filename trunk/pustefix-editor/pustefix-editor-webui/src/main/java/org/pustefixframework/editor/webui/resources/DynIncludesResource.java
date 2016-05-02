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

package org.pustefixframework.editor.webui.resources;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.pustefixframework.editor.common.dom.IncludeFile;
import org.pustefixframework.editor.common.dom.IncludePart;
import org.pustefixframework.editor.common.dom.IncludePartThemeVariant;
import org.pustefixframework.editor.common.dom.Page;
import org.pustefixframework.editor.common.dom.Project;
import org.pustefixframework.editor.common.dom.Theme;
import org.w3c.dom.Element;

import de.schlund.pfixxml.ResultDocument;

public class DynIncludesResource extends CommonIncludesResource {
    @Override
    protected IncludePartThemeVariant internalSelectIncludePart(Project project, String path, String part, String theme) {
        IncludeFile incFile = project.getDynIncludeFile(path);
        if (incFile == null) {
            return null;
        }
        IncludePart incPart = incFile.getPart(part);
        if (incPart == null) {
            return null;
        }
        for (Iterator<IncludePartThemeVariant> i = incPart.getThemeVariants().iterator(); i.hasNext();) {
            IncludePartThemeVariant variant = i.next();
            if (variant.getTheme().getName().equals(theme)) {
                return variant;
            }
        }
        return null;
    }

    @Override
    protected Collection<Theme> getPossibleThemes(IncludePartThemeVariant selectedIncludePart, Project project, Collection<Page> dummy) {
        if (!securitymanager.mayEditIncludes(project)) {
            // Do not present alternative themes to users who may not
            // edit DynIncludes at all
            return new TreeSet<Theme>();
        }
        Collection<Page> pages = project.getAllPages();
        TreeSet<Theme> themes = new TreeSet<Theme>();
        for (Iterator<Page> i = pages.iterator(); i.hasNext();) {
            Page page = i.next();
            themes.addAll(page.getThemes().getThemes());
        }
        for (Iterator<Theme> i = themes.iterator(); i.hasNext();) {
            Theme theme = i.next();
            if (selectedIncludePart.getIncludePart().hasThemeVariant(theme)) {
                i.remove();
            }
        }
        return themes;
    }

    @Override
    protected void renderAllIncludes(ResultDocument resdoc, Element elem, Project project) {
        TreeSet<IncludeFile> incFiles = new TreeSet<IncludeFile>(project.getDynIncludeFiles());
        Map<String, Element> directoryNodes = new HashMap<String, Element>();
        for (Iterator<IncludeFile> i = incFiles.iterator(); i.hasNext();) {
            IncludeFile incFile = i.next();
            String path = incFile.getPath();
            String dir;
            try {
                dir = path.substring(0, path.lastIndexOf(File.separator));
            } catch (StringIndexOutOfBoundsException e) {
                dir = "/";
            }
            Element directoryNode = directoryNodes.get(dir);
            if (directoryNode == null) {
                directoryNode = resdoc.createSubNode(elem, "directory");
                directoryNode.setAttribute("path", dir);
                String jsId = dir.replaceAll("\\.", "_dot_").replaceAll("/", "_slash_");
                directoryNode.setAttribute("jsId", jsId);
                if (this.isDirectoryOpen(dir)) {
                    directoryNode.setAttribute("open", "true");
                }
                directoryNodes.put(dir, directoryNode);
            }
            Element fileNode = resdoc.createSubNode(directoryNode, "file");
            fileNode.setAttribute("path", path);
            if (this.isFileOpen(path)) {
                fileNode.setAttribute("open", "true");
            }
            TreeSet<IncludePart> incParts = new TreeSet<IncludePart>(incFile.getParts());
            for (Iterator<IncludePart> i2 = incParts.iterator(); i2.hasNext();) {
                IncludePart incPart = i2.next();
                TreeSet<IncludePartThemeVariant> incVariants = new TreeSet<IncludePartThemeVariant>(incPart.getThemeVariants());
                for (Iterator<IncludePartThemeVariant> i3 = incVariants.iterator(); i3.hasNext();) {
                    IncludePartThemeVariant incVariant = i3.next();
                    Element partNode = resdoc.createSubNode(fileNode, "include");
                    partNode.setAttribute("part", incPart.getName());
                    partNode.setAttribute("theme", incVariant.getTheme().getName());
                    if (this.getSelectedIncludePart() != null && this.getSelectedIncludePart().equals(incVariant)) {
                        partNode.setAttribute("selected", "true");
                    }
                }
            }
        }
    }

    @Override
    protected Set<IncludeFile> getIncludeFilesInDirectory(String dirname, Project project) {
        TreeSet<IncludeFile> files = new TreeSet<IncludeFile>();
        for (Iterator<IncludeFile> i = project.getDynIncludeFiles().iterator(); i.hasNext();) {
            IncludeFile file = i.next();
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

    @Override
    protected Set<IncludePartThemeVariant> getIncludePartsInFile(String filename, Project project) {
        TreeSet<IncludePartThemeVariant> parts = new TreeSet<IncludePartThemeVariant>();
        IncludeFile incFile = project.getDynIncludeFile(filename);
        if (incFile != null) {
            for (Iterator<IncludePart> i = incFile.getParts().iterator(); i.hasNext();) {
                IncludePart part = i.next();
                for (Iterator<IncludePartThemeVariant> j = part.getThemeVariants().iterator(); j.hasNext();) {
                    IncludePartThemeVariant variant = j.next();
                    parts.add(variant);
                }
            }
        }
        return parts;
    }
}
