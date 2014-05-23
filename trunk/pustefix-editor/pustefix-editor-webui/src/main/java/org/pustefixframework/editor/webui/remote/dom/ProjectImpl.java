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

package org.pustefixframework.editor.webui.remote.dom;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

import org.pustefixframework.editor.common.dom.AbstractProject;
import org.pustefixframework.editor.common.dom.Image;
import org.pustefixframework.editor.common.dom.IncludeFile;
import org.pustefixframework.editor.common.dom.IncludePartThemeVariant;
import org.pustefixframework.editor.common.dom.Page;
import org.pustefixframework.editor.common.dom.Target;
import org.pustefixframework.editor.common.dom.Variant;
import org.pustefixframework.editor.common.remote.transferobjects.IncludePartThemeVariantReferenceTO;
import org.pustefixframework.editor.common.remote.transferobjects.PageTO;
import org.pustefixframework.editor.common.remote.transferobjects.ProjectTO;
import org.pustefixframework.editor.common.remote.transferobjects.TargetTO;
import org.pustefixframework.editor.webui.remote.dom.util.RemoteServiceUtil;



public class ProjectImpl extends AbstractProject {
    
    private RemoteServiceUtil remoteServiceUtil;
    private String name;
    private String comment;
    private boolean includePartsEditableByDefault;
    private Map<String, String> prefixToNamespaceMappings;
    private Object initLock = new Object();
    private boolean initialized = false;

    public ProjectImpl(RemoteServiceUtil remoteServiceUtil) {
        this.remoteServiceUtil = remoteServiceUtil;
    }
    
    private void init() {
        synchronized (initLock) {
            if (initialized) {
                return;
            }
            ProjectTO  projectTO = getProjectTO();
            this.name = projectTO.name;
            this.comment = projectTO.comment;
            this.includePartsEditableByDefault = projectTO.includePartsEditableByDefault;
            this.prefixToNamespaceMappings = projectTO.prefixToNamespaceMappings;
            initialized = true;
        }
    }
    
    public IncludePartThemeVariant findIncludePartThemeVariant(String file, String part, String theme) {
        ProjectTO projectTO = getProjectTO();
        IncludePartThemeVariantReferenceTO reference = new IncludePartThemeVariantReferenceTO();
        reference.path = file;
        reference.part = part;
        reference.theme = theme;
        if (projectTO.includeParts.contains(reference)) {
            return new IncludePartThemeVariantImpl(remoteServiceUtil, file, part, theme);
        }
        return null;
    }
    
    public Collection<Image> getAllImages() {
        LinkedList<Image> images = new LinkedList<Image>();
        for (String imagePath : getProjectTO().images) {
            images.add(new ImageImpl(remoteServiceUtil, imagePath));
        }
        return images;
    }
    
    public Collection<IncludePartThemeVariant> getAllIncludeParts() {
        LinkedList<IncludePartThemeVariant> includes = new LinkedList<IncludePartThemeVariant>();
        for (IncludePartThemeVariantReferenceTO reference : getProjectTO().includeParts) {
            includes.add(new IncludePartThemeVariantImpl(remoteServiceUtil, reference.path, reference.part, reference.theme));
        }
        return includes;
    }
    
    public Collection<Page> getAllPages() {
        LinkedList<Page> pages = new LinkedList<Page>();
        for (String pageName : getProjectTO().pages) {
            pages.add(new PageImpl(remoteServiceUtil, pageName));
        }
        return pages;
    }
    
    public String getComment() {
        init();
        return comment;
    }
    
    public IncludeFile getDynIncludeFile(String path) {
        if (getProjectTO().dynIncludeFiles.contains(path)) {
            return new DynIncludeFileImpl(remoteServiceUtil, path);
        }
        return null;
    }
    
    public Collection<IncludeFile> getDynIncludeFiles() {
        LinkedList<IncludeFile> includeFiles = new LinkedList<IncludeFile>();
        for (String path : getProjectTO().dynIncludeFiles) {
            includeFiles.add(new DynIncludeFileImpl(remoteServiceUtil, path));
        }
        return includeFiles;
    }
    
    public String getName() {
        init();
        return name;
    }
    
    public Page getPage(String pagename, Variant variant) {
        if (variant != null) {
            pagename += "::" + variant.getName();
        }
        PageTO page = remoteServiceUtil.getRemotePageService().getPage(pagename);
        if (page != null) {
            return new PageImpl(remoteServiceUtil, page);
        }
        return null;
    }
    
    public Collection<Page> getPageByName(String name) {
        LinkedList<Page> pages = new LinkedList<Page>();
        for (PageTO page : remoteServiceUtil.getRemotePageService().getPageByName(name)) {
            pages.add(new PageImpl(remoteServiceUtil, page));
        }
        return pages;
    }
    
    public Map<String, String> getPrefixToNamespaceMappings() {
        init();
        return prefixToNamespaceMappings;
    }
    
    public Target getTarget(String name) {
        TargetTO targetTO = remoteServiceUtil.getRemoteTargetService().getTarget(name, false);
        if (targetTO != null) {
            return new TargetImpl(remoteServiceUtil, name);
        }
        return null;
    }
    
    public Collection<Page> getTopPages() {
        LinkedList<Page> pages = new LinkedList<Page>();
        for (String pageName : getProjectTO().topPages) {
            pages.add(new PageImpl(remoteServiceUtil, pageName));
        }
        return pages;
    }
    
    public boolean hasIncludePart(String file, String part, String theme) {
        IncludePartThemeVariantReferenceTO reference = new IncludePartThemeVariantReferenceTO();
        reference.path = file;
        reference.part = part;
        reference.theme = theme;
        return getProjectTO().includeParts.contains(reference);
    }
    
    private ProjectTO getProjectTO() {
        // This is not thread-safe by intention:
        // Other threads might see an old state, but this does not matter
        // as each thread has to call this method in order to be sure
        // that it is using the newest data available on the server.
        return remoteServiceUtil.getRemoteProjectService().getProject();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof ProjectImpl)) {
            return false;
        }
        ProjectImpl p = (ProjectImpl) obj;
        return remoteServiceUtil.equals(p.remoteServiceUtil);
    }

    @Override
    public int hashCode() {
        return remoteServiceUtil.hashCode();
    }

    public boolean isIncludePartsEditableByDefault() {
        return includePartsEditableByDefault;
    }
    
}
