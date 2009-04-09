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

import org.pustefixframework.editor.common.dom.AbstractIncludePartThemeVariant;
import org.pustefixframework.editor.common.dom.Image;
import org.pustefixframework.editor.common.dom.IncludePartThemeVariant;
import org.pustefixframework.editor.common.dom.Page;
import org.pustefixframework.editor.common.dom.Target;
import org.pustefixframework.editor.common.dom.Theme;
import org.pustefixframework.editor.common.exception.EditorIOException;
import org.pustefixframework.editor.common.exception.EditorParsingException;
import org.pustefixframework.editor.common.exception.EditorSecurityException;
import org.pustefixframework.editor.common.remote.service.RemoteCommonIncludeService;
import org.pustefixframework.editor.common.remote.transferobjects.IncludePartThemeVariantReferenceTO;
import org.pustefixframework.editor.common.remote.transferobjects.IncludePartThemeVariantTO;
import org.pustefixframework.editor.common.util.XMLSerializer;
import org.pustefixframework.editor.webui.remote.dom.util.RemoteServiceUtil;
import org.w3c.dom.Node;



public abstract class CommonIncludePartThemeVariantImpl extends AbstractIncludePartThemeVariant {
    
    protected RemoteServiceUtil remoteServiceUtil;
    protected String path;
    protected String part;
    private ThemeImpl theme;
    
    protected CommonIncludePartThemeVariantImpl(RemoteServiceUtil remoteServiceUtil, String path, String part, String theme) {
        this.remoteServiceUtil = remoteServiceUtil;
        this.path = path;
        this.part = part;
        this.theme = new ThemeImpl(theme);
    }
    
    public Collection<Page> getAffectedPages() {
        IncludePartThemeVariantTO includePartThemeVariantTO = getIncludePartThemeVariantTO();
        LinkedList<Page> pages = new LinkedList<Page>();
        for (String pageName : includePartThemeVariantTO.affectedPages) {
            pages.add(new PageImpl(remoteServiceUtil, pageName));
        }
        return pages;
    }
    
    public Collection<Image> getImageDependencies(boolean recursive) throws EditorParsingException {
        Collection<String> imagePaths = getRemoteService().getImageDependencies(getReference(), null, recursive);
        LinkedList<Image> images = new LinkedList<Image>();
        for (String imagePath : imagePaths) {
            images.add(new ImageImpl(remoteServiceUtil, imagePath));
        }
        return images;
    }
    
    public Collection<Image> getImageDependencies(Target target, boolean recursive) throws EditorParsingException {
        Collection<String> imagePaths = getRemoteService().getImageDependencies(getReference(), target.getName(), recursive);
        LinkedList<Image> images = new LinkedList<Image>();
        for (String imagePath : imagePaths) {
            images.add(new ImageImpl(remoteServiceUtil, imagePath));
        }
        return images;
    }
    
    public Collection<IncludePartThemeVariant> getIncludeDependencies(boolean recursive) throws EditorParsingException {
        Collection<IncludePartThemeVariantReferenceTO> references = getRemoteService().getIncludeDependencies(getReference(), null, recursive);
        LinkedList<IncludePartThemeVariant> includes = new LinkedList<IncludePartThemeVariant>();
        for (IncludePartThemeVariantReferenceTO reference : references) {
            includes.add(newInstance(reference));
        }
        return includes;
    }
    
    public Collection<IncludePartThemeVariant> getIncludeDependencies(Target target, boolean recursive) throws EditorParsingException {
        Collection<IncludePartThemeVariantReferenceTO> references = getRemoteService().getIncludeDependencies(getReference(), target.getName(), recursive);
        LinkedList<IncludePartThemeVariant> includes = new LinkedList<IncludePartThemeVariant>();
        for (IncludePartThemeVariantReferenceTO reference : references) {
            includes.add(newInstance(reference));
        }
        return includes;
    }
    
    public String getMD5() {
        return getIncludePartThemeVariantTO().md5;
    }
    
    public Theme getTheme() {
        return theme;
    }
    
    public Node getXML() {
        String xml = getRemoteService().getIncludePartThemeVariantXML(path, part, theme.getName());
        if (xml != null) {
            return (new XMLSerializer()).deserializeNode(xml);
        } else {
            return null;
        }
    }
    
    public void setXML(Node xml) throws EditorIOException, EditorParsingException, EditorSecurityException {
        this.setXML(xml, true);
    }
    
    public void setXML(Node xml, boolean indent) throws EditorIOException, EditorParsingException, EditorSecurityException {
        String xmlString = (new XMLSerializer()).serializeNode(xml);
        getRemoteService().setIncludePartThemeVariantXML(path, part, theme.getName(), xmlString, indent);
    }
    
    public Collection<String> getBackupVersions() {
        return getRemoteService().getIncludePartThemeVariantBackupVersions(path, part, theme.getName());
    }

    public boolean restore(String version) throws EditorSecurityException {
        return getRemoteService().restoreIncludePartThemeVariant(path, part, theme.getName(), version);
    }
    
    protected IncludePartThemeVariantReferenceTO getReference() {
        IncludePartThemeVariantReferenceTO reference = new IncludePartThemeVariantReferenceTO();
        reference.path = path;
        reference.part = part;
        reference.theme = theme.getName();
        return reference;
    }
    
    protected IncludePartThemeVariantTO getIncludePartThemeVariantTO() {
        return getRemoteService().getIncludePartThemeVariantTO(getReference());
    }
    
    @Override
    public int compareTo(IncludePartThemeVariant variant) {
        if (variant instanceof CommonIncludePartThemeVariantImpl) {
            CommonIncludePartThemeVariantImpl v = (CommonIncludePartThemeVariantImpl) variant;
            if (this.remoteServiceUtil.equals(v.remoteServiceUtil)) {
                return super.compareTo(variant);
            } else {
                return -1;
            }
        } else {
            return -1;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) {
            return false;
        }
        if (obj instanceof CommonIncludePartThemeVariantImpl) {
            CommonIncludePartThemeVariantImpl v = (CommonIncludePartThemeVariantImpl) obj;
            return this.remoteServiceUtil.equals(v.remoteServiceUtil);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return ("INCLUDEPARTTHEMEVARIANT: " + super.hashCode() + remoteServiceUtil.hashCode()).hashCode();
    }

    protected abstract IncludePartThemeVariant newInstance(IncludePartThemeVariantReferenceTO reference);
    
    protected abstract RemoteCommonIncludeService getRemoteService();
}
