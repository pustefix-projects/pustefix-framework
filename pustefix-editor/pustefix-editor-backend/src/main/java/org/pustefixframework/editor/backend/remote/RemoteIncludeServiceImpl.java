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

package org.pustefixframework.editor.backend.remote;

import java.util.Collection;
import java.util.LinkedList;

import org.pustefixframework.container.annotations.Inject;
import org.pustefixframework.editor.common.dom.Image;
import org.pustefixframework.editor.common.dom.IncludeFile;
import org.pustefixframework.editor.common.dom.IncludePart;
import org.pustefixframework.editor.common.dom.IncludePartThemeVariant;
import org.pustefixframework.editor.common.dom.Target;
import org.pustefixframework.editor.common.dom.Theme;
import org.pustefixframework.editor.common.exception.EditorParsingException;
import org.pustefixframework.editor.common.remote.service.RemoteIncludeService;
import org.pustefixframework.editor.common.remote.transferobjects.IncludePartThemeVariantReferenceTO;

import de.schlund.pfixcore.editor2.core.spring.IncludeFactoryService;
import de.schlund.pfixcore.editor2.core.spring.ProjectFactoryService;


public class RemoteIncludeServiceImpl extends RemoteCommonIncludeServiceImpl implements RemoteIncludeService {
    
    private IncludeFactoryService includeFactoryService;
    private ProjectFactoryService projectFactoryService;
    
    @Inject
    public void setIncludeFactoryService(IncludeFactoryService includeFactoryService) {
        this.includeFactoryService = includeFactoryService;
    }
    
    @Inject
    public void setProjectFactoryService(ProjectFactoryService projectFactoryService) {
        this.projectFactoryService = projectFactoryService;
    }
    
    public Collection<String> getImageDependencies(IncludePartThemeVariantReferenceTO reference, boolean recursive) throws EditorParsingException {
        IncludePartThemeVariant v = getIncludePartThemeVariantDOM(reference.path, reference.part, reference.theme);
        if (v == null) {
            return null;
        }
        LinkedList<String> images = new LinkedList<String>();
        for (Image image : v.getImageDependencies(recursive)) {
            images.add(image.getPath());
        }
        return images;
    }
    
    public Collection<String> getImageDependencies(IncludePartThemeVariantReferenceTO reference, String target, boolean recursive) throws EditorParsingException {
        IncludePartThemeVariant v = getIncludePartThemeVariantDOM(reference.path, reference.part, reference.theme);
        Target targetO = projectFactoryService.getProject().getTarget(target);
        if (v == null) {
            return null;
        }
        LinkedList<String> images = new LinkedList<String>();
        for (Image image : v.getImageDependencies(targetO, recursive)) {
            images.add(image.getPath());
        }
        return images;
    }
    
    public Collection<IncludePartThemeVariantReferenceTO> getIncludeDependencies(IncludePartThemeVariantReferenceTO reference, boolean recursive) throws EditorParsingException {
        IncludePartThemeVariant v = getIncludePartThemeVariantDOM(reference.path, reference.part, reference.theme);
        if (v == null) {
            return null;
        }
        LinkedList<IncludePartThemeVariantReferenceTO> includes = new LinkedList<IncludePartThemeVariantReferenceTO>();
        for (IncludePartThemeVariant iptv : v.getIncludeDependencies(recursive)) {
            IncludePartThemeVariantReferenceTO ref = new IncludePartThemeVariantReferenceTO();
            ref.path = iptv.getIncludePart().getIncludeFile().getPath();
            ref.part = iptv.getIncludePart().getName();
            ref.theme = iptv.getTheme().getName();
            includes.add(ref);
        }
        return includes;
    }
    
    public Collection<IncludePartThemeVariantReferenceTO> getIncludeDependencies(IncludePartThemeVariantReferenceTO reference, String target, boolean recursive) throws EditorParsingException {
        IncludePartThemeVariant v = getIncludePartThemeVariantDOM(reference.path, reference.part, reference.theme);
        Target targetO = projectFactoryService.getProject().getTarget(target);
        if (v == null) {
            return null;
        }
        LinkedList<IncludePartThemeVariantReferenceTO> includes = new LinkedList<IncludePartThemeVariantReferenceTO>();
        for (IncludePartThemeVariant iptv : v.getIncludeDependencies(targetO, recursive)) {
            IncludePartThemeVariantReferenceTO ref = new IncludePartThemeVariantReferenceTO();
            ref.path = iptv.getIncludePart().getIncludeFile().getPath();
            ref.part = iptv.getIncludePart().getName();
            ref.theme = iptv.getTheme().getName();
            includes.add(ref);
        }
        return includes;
    }
    
    protected IncludePartThemeVariant getIncludePartThemeVariantDOM(String path, String part, String theme) {
        return projectFactoryService.getProject().findIncludePartThemeVariant(path, part, theme);
    }
    
    protected IncludePart getIncludePartDOM(String path, String part) {
        IncludeFile file = getIncludeFileDOM(path);
        if (file == null) {
            return null;
        }
        return  file.getPart(part);
    }
    
    protected IncludeFile getIncludeFileDOM(String path) {
        try {
            return includeFactoryService.getIncludeFile(path);
        } catch (EditorParsingException e) {
            return null;
        }
    }

    public Collection<String> getPossibleThemes(String path, String part) {
        IncludePart includePart = getIncludePartDOM(path, part);
        if (includePart == null) {
            return null;
        }
        LinkedList<String> themes = new LinkedList<String>();
        for (Theme theme : includePart.getPossibleThemes()) {
            themes.add(theme.getName());
        }
        return themes;
    }
}
