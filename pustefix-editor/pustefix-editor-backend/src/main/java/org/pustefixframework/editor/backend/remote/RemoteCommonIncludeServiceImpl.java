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

package org.pustefixframework.editor.backend.remote;

import java.util.Collection;
import java.util.Collections;

import org.pustefixframework.editor.common.dom.IncludeFile;
import org.pustefixframework.editor.common.dom.IncludePart;
import org.pustefixframework.editor.common.dom.IncludePartThemeVariant;
import org.pustefixframework.editor.common.dom.Page;
import org.pustefixframework.editor.common.exception.EditorIOException;
import org.pustefixframework.editor.common.exception.EditorParsingException;
import org.pustefixframework.editor.common.exception.EditorSecurityException;
import org.pustefixframework.editor.common.remote.service.RemoteCommonIncludeService;
import org.pustefixframework.editor.common.remote.transferobjects.IncludeFileTO;
import org.pustefixframework.editor.common.remote.transferobjects.IncludePartTO;
import org.pustefixframework.editor.common.remote.transferobjects.IncludePartThemeVariantReferenceTO;
import org.pustefixframework.editor.common.remote.transferobjects.IncludePartThemeVariantTO;
import org.pustefixframework.editor.common.util.XMLSerializer;
import org.w3c.dom.Node;

import de.schlund.pfixcore.editor2.core.spring.BackupService;
import de.schlund.pfixcore.editor2.core.spring.ThemeFactoryService;


public abstract class RemoteCommonIncludeServiceImpl implements RemoteCommonIncludeService {
    
    protected BackupService backupService;
    protected ThemeFactoryService themeFactoryService;
    
    public void setBackupService(BackupService backupService) {
        this.backupService = backupService;
    }
    
    public void setThemeFactoryService(ThemeFactoryService themeFactoryService) {
        this.themeFactoryService = themeFactoryService;
    }
    
    public void backupIncludePartThemeVariant(String path, String part, String theme) {
        backupService.backupInclude(getIncludePartThemeVariantDOM(path, part, theme));
    }
    
    public void deleteIncludePartThemeVariant(String path, String part, String theme) throws EditorParsingException, EditorSecurityException, EditorIOException {
        IncludePartThemeVariant v = getIncludePartThemeVariantDOM(path, part, theme);
        if (v != null) {
            v.getIncludePart().deleteThemeVariant(v);
        }
    }
    
    public Collection<String> getImageDependencies(IncludePartThemeVariantReferenceTO reference, String target, boolean recursive) throws EditorParsingException {
        return Collections.emptyList();
    }
    
    public Collection<IncludePartThemeVariantReferenceTO> getIncludeDependencies(IncludePartThemeVariantReferenceTO reference, String name, boolean recursive) throws EditorParsingException {
        return Collections.emptyList();
    }
    
    public IncludeFileTO getIncludeFile(String path) {
        IncludeFileTO to = new IncludeFileTO();
        IncludeFile file = getIncludeFileDOM(path);
        if (file == null) {
            return null;
        }
        to.path = path;
        to.serial = file.getSerial();
        for (IncludePart part : file.getParts()) {
            to.parts.add(part.getName());
        }
        return to;
    }
    
    public String getIncludeFileXML(String path, boolean forceUpdate) {
        IncludeFile file = getIncludeFileDOM(path);
        if (file == null) {
            return null;
        }
        Node node = file.getContentXML(forceUpdate);
        if (node != null) {
            return (new XMLSerializer()).serializeNode(node);
        } else {
            return null;
        }
    }
    
    public IncludePartTO getIncludePart(String path, String part) {
        IncludePartTO to = new IncludePartTO();
        IncludePart includePart = getIncludePartDOM(path, part);
        if (includePart == null) {
            return null;
        }
        to.path = path;
        to.part = part;
        for (IncludePartThemeVariant includePartThemeVariant : includePart.getThemeVariants()) {
            to.themes.add(includePartThemeVariant.getTheme().getName());
        }
        return to;
    }
    
    public Collection<String> getIncludePartThemeVariantBackupVersions(String path, String part, String theme) {
        IncludePartThemeVariant includePartThemeVariant = getIncludePartThemeVariantDOM(path, part, theme);
        if (includePartThemeVariant == null) {
            return null;
        }
        return includePartThemeVariant.getBackupVersions();
    }
    
    public IncludePartThemeVariantTO getIncludePartThemeVariantTO(IncludePartThemeVariantReferenceTO reference) {
        IncludePartThemeVariantTO to = new IncludePartThemeVariantTO();
        IncludePartThemeVariant includePartThemeVariant = getIncludePartThemeVariantDOM(reference.path, reference.part, reference.theme);
        if (includePartThemeVariant == null) {
            return null;
        }
        to.path = reference.path;
        to.part = reference.part;
        to.theme = reference.theme;
        to.md5 = includePartThemeVariant.getMD5();
        for (Page page : includePartThemeVariant.getAffectedPages()) {
            to.affectedPages.add(page.getFullName());
        }
        return to;
    }
    
    public String getIncludePartThemeVariantXML(String path, String part, String theme) {
        IncludePartThemeVariant includePartThemeVariant = getIncludePartThemeVariantDOM(path, part, theme);
        if (includePartThemeVariant == null) {
            return null;
        }
        Node node = includePartThemeVariant.getXML();
        if (node != null) {
            return (new XMLSerializer()).serializeNode(node);
        } else {
            return null;
        }
    }
    
    public String getIncludePartXML(String path, String part) {
        IncludePart includePart = getIncludePartDOM(path, part);
        if (includePart == null) {
            return null;
        }
        Node node = includePart.getContentXML();
        if (node != null) {
            return (new XMLSerializer()).serializeNode(node);
        } else {
            return null;
        }
    }
    
    public boolean restoreIncludePartThemeVariant(String path, String part, String theme, String version) throws EditorSecurityException {
        IncludePartThemeVariant includePartThemeVariant = getIncludePartThemeVariantDOM(path, part, theme);
        if (includePartThemeVariant == null) {
            return false;
        }
        return includePartThemeVariant.restore(version);
    }
    
    public void setIncludePartThemeVariantXML(String path, String part, String theme, String xml, boolean indent) throws EditorIOException, EditorParsingException, EditorSecurityException {
        IncludePartThemeVariant includePartThemeVariant = getIncludePartThemeVariantDOM(path, part, theme);
        Node node;
        if (xml == null) {
            node = null;
        } else {
            node = (new XMLSerializer()).deserializeNode(xml);
        }
        includePartThemeVariant.setXML(node, indent);
    }
    
    protected abstract IncludePartThemeVariant getIncludePartThemeVariantDOM(String path, String part, String theme);
    
    protected abstract IncludePart getIncludePartDOM(String path, String part);
    
    protected abstract IncludeFile getIncludeFileDOM(String path);
    
}
