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
import java.util.Map;

import org.pustefixframework.container.annotations.Inject;
import org.pustefixframework.editor.common.dom.Image;
import org.pustefixframework.editor.common.dom.IncludePartThemeVariant;
import org.pustefixframework.editor.common.dom.Page;
import org.pustefixframework.editor.common.dom.Target;
import org.pustefixframework.editor.common.dom.Theme;
import org.pustefixframework.editor.common.exception.EditorIOException;
import org.pustefixframework.editor.common.exception.EditorParsingException;
import org.pustefixframework.editor.common.remote.service.RemoteTargetService;
import org.pustefixframework.editor.common.remote.transferobjects.IncludePartThemeVariantReferenceTO;
import org.pustefixframework.editor.common.remote.transferobjects.TargetTO;
import org.pustefixframework.editor.common.util.XMLSerializer;
import org.w3c.dom.Node;

import de.schlund.pfixcore.editor2.core.spring.ProjectFactoryService;


public class RemoteTargetServiceImpl implements RemoteTargetService {
    
    private ProjectFactoryService projectFactoryService;
    
    @Inject
    public void setProjectFactoryService(ProjectFactoryService projectFactoryService) {
        this.projectFactoryService = projectFactoryService;
    }
    
    public TargetTO getTarget(String name) {
        Target target = projectFactoryService.getProject().getTarget(name);
        if (target == null) {
            return null;
        }
        TargetTO to = new TargetTO();
        to.name = name;
        to.parentXML = target.getParentXML().getName();
        to.parentXSL = target.getParentXSL().getName();
        to.type = target.getType();
        for (Target t : target.getAuxDependencies()) {
            to.auxDependencies.add(t.getName());
        }
        for (Page p : target.getAffectedPages()) {
            to.pages.add(p.getFullName());
        }
        Map<String, Object> params = target.getParameters();
        for (String key : params.keySet()) {
            String value = params.get(key).toString();
            to.parameters.put(key, value);
        }
        for (Theme t : target.getThemeList().getThemes()) {
            to.themes.add(t.getName());
        }
        return to;
    }
    
    public String getTargetXML(String name) throws EditorIOException, EditorParsingException {
        Target target = projectFactoryService.getProject().getTarget(name);
        if (target == null) {
            return null;
        }
        Node node = target.getContentXML();
        return (new XMLSerializer()).serializeNode(node);
    }

    public Collection<String> getImageDependencies(String targetName, boolean recursive) throws EditorParsingException {
        Target target = projectFactoryService.getProject().getTarget(targetName);
        LinkedList<String> images = new LinkedList<String>();
        for (Image i : target.getImageDependencies(recursive)) {
            images.add(i.getPath());
        }
        return images;
    }

    public Collection<IncludePartThemeVariantReferenceTO> getIncludeDependencies(String targetName, boolean recursive) throws EditorParsingException {
        Target target = projectFactoryService.getProject().getTarget(targetName);
        LinkedList<IncludePartThemeVariantReferenceTO> parts = new LinkedList<IncludePartThemeVariantReferenceTO>();
        for (IncludePartThemeVariant v : target.getIncludeDependencies(recursive)) {
            IncludePartThemeVariantReferenceTO ref = new IncludePartThemeVariantReferenceTO();
            ref.path = v.getIncludePart().getIncludeFile().getPath();
            ref.part = v.getIncludePart().getName();
            ref.theme = v.getTheme().getName();
            parts.add(ref);
        }
        return parts;
    }
    
}
