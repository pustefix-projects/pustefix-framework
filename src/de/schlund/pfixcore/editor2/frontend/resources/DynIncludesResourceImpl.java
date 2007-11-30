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
import java.util.Map;
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

public class DynIncludesResourceImpl extends CommonIncludesResourceImpl
        implements DynIncludesResource {

    protected boolean securityMayCreateIncludePartThemeVariant(
            IncludePart includePart, Theme theme) {
        return SpringBeanLocator.getSecurityManagerService()
                .mayEditDynInclude();
    }

    protected IncludePartThemeVariant internalSelectIncludePart(
            Project project, String path, String part, String theme) {
        IncludeFile incFile = SpringBeanLocator.getDynIncludeFactoryService()
                .getIncludeFile(path);
        if (incFile == null) {
            return null;
        }
        IncludePart incPart = incFile.getPart(part);
        if (incPart == null) {
            return null;
        }
        for (Iterator i = incPart.getThemeVariants().iterator(); i.hasNext();) {
            IncludePartThemeVariant variant = (IncludePartThemeVariant) i
                    .next();
            if (variant.getTheme().getName().equals(theme)) {
                return variant;
            }
        }
        return null;
    }

    protected Collection getPossibleThemes(
            IncludePartThemeVariant selectedIncludePart, Project project,
            Collection dummy) {
        if (!SpringBeanLocator.getSecurityManagerService().mayEditDynInclude()) {
            // Do not present alternative themes to users who may not
            // edit DynIncludes at all
            return new TreeSet();
        }
        Collection pages = project.getAllPages();
        TreeSet themes = new TreeSet();
        for (Iterator i = pages.iterator(); i.hasNext();) {
            Page page = (Page) i.next();
            themes.addAll(page.getThemes().getThemes());
        }
        for (Iterator i = themes.iterator(); i.hasNext();) {
            Theme theme = (Theme) i.next();
            if (selectedIncludePart.getIncludePart().hasThemeVariant(theme)) {
                i.remove();
            }
        }
        return themes;
    }

    protected boolean securityMayEditIncludePartThemeVariant(
            IncludePartThemeVariant variant) {
        return SpringBeanLocator.getSecurityManagerService()
                .mayEditDynInclude();
    }

    protected void renderAllIncludes(ResultDocument resdoc, Element elem,
            Project project) {
        TreeSet incFiles = new TreeSet(SpringBeanLocator
                .getDynIncludeFactoryService().getDynIncludeFiles());
        Map directoryNodes = new HashMap();
        for (Iterator i = incFiles.iterator(); i.hasNext();) {
            IncludeFile incFile = (IncludeFile) i.next();
            String path = incFile.getPath();
            String dir;
            try {
                dir = path.substring(0, path.lastIndexOf(File.separator));
            } catch (StringIndexOutOfBoundsException e) {
                dir = "/";
            }
            Element directoryNode = (Element) directoryNodes.get(dir);
            if (directoryNode == null) {
                directoryNode = resdoc.createSubNode(elem, "directory");
                directoryNode.setAttribute("path", dir);
                String jsId = dir.replaceAll("\\.", "_dot_").replaceAll("/",
                        "_slash_");
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
            TreeSet incParts = new TreeSet(incFile.getParts());
            for (Iterator i2 = incParts.iterator(); i2.hasNext();) {
                IncludePart incPart = (IncludePart) i2.next();
                TreeSet incVariants = new TreeSet(incPart.getThemeVariants());
                for (Iterator i3 = incVariants.iterator(); i3.hasNext();) {
                    IncludePartThemeVariant incVariant = (IncludePartThemeVariant) i3
                            .next();
                    Element partNode = resdoc
                            .createSubNode(fileNode, "include");
                    partNode.setAttribute("part", incPart.getName());
                    partNode.setAttribute("theme", incVariant.getTheme()
                            .getName());
                    if (this.getSelectedIncludePart() != null
                            && this.getSelectedIncludePart().equals(incVariant)) {
                        partNode.setAttribute("selected", "true");
                    }
                }
            }
        }
    }

    protected Set<IncludeFile> getIncludeFilesInDirectory(String dirname,
            Project project) {
        TreeSet<IncludeFile> files = new TreeSet<IncludeFile>();
        for (Iterator i = SpringBeanLocator.getDynIncludeFactoryService()
                .getDynIncludeFiles().iterator(); i.hasNext();) {
            IncludeFile file = (IncludeFile) i.next();
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
        TreeSet<IncludePartThemeVariant> parts = new TreeSet<IncludePartThemeVariant>();
        IncludeFile incFile = SpringBeanLocator.getDynIncludeFactoryService()
                .getIncludeFile(filename);
        if (incFile != null) {
            for (Iterator i = incFile.getParts().iterator(); i.hasNext();) {
                IncludePart part = (IncludePart) i.next();
                for (Iterator j = part.getThemeVariants().iterator(); j
                        .hasNext();) {
                    IncludePartThemeVariant variant = (IncludePartThemeVariant) j
                            .next();
                    parts.add(variant);
                }
            }
        }
        return parts;
    }
}
