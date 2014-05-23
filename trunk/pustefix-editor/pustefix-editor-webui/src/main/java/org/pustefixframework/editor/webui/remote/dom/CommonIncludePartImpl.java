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

import org.pustefixframework.editor.common.dom.AbstractIncludePart;
import org.pustefixframework.editor.common.dom.IncludePart;
import org.pustefixframework.editor.common.dom.IncludePartThemeVariant;
import org.pustefixframework.editor.common.dom.Theme;
import org.pustefixframework.editor.common.exception.EditorIOException;
import org.pustefixframework.editor.common.exception.EditorParsingException;
import org.pustefixframework.editor.common.exception.EditorSecurityException;
import org.pustefixframework.editor.common.remote.service.RemoteCommonIncludeService;
import org.pustefixframework.editor.common.remote.transferobjects.IncludePartTO;
import org.pustefixframework.editor.common.util.XMLSerializer;
import org.pustefixframework.editor.webui.remote.dom.util.RemoteServiceUtil;
import org.w3c.dom.Node;



public abstract class CommonIncludePartImpl extends AbstractIncludePart {
    
    protected RemoteServiceUtil remoteServiceUtil;
    protected String path;
    protected String part;

    public CommonIncludePartImpl(RemoteServiceUtil remoteServiceUtil, String path, String part) {
        this.remoteServiceUtil = remoteServiceUtil;
        this.path = path;
        this.part = part;
    }
    
    public Node getContentXML() {
        String xml = getRemoteService().getIncludePartXML(path, part);
        if (xml != null) {
            return (new XMLSerializer()).deserializeNode(xml);
        } else {
            return null;
        }
    }
    
    public String getName() {
        return part;
    }
    
    public IncludePartThemeVariant createThemeVariant(Theme theme) throws EditorIOException, EditorParsingException, EditorSecurityException {
        return newThemeVariantInstance(theme.getName());
    }
    
    public IncludePartThemeVariant getThemeVariant(Theme theme) {
        if (hasThemeVariant(theme)) {
            return newThemeVariantInstance(theme.getName());
        }
        return null;
    }

    public Collection<IncludePartThemeVariant> getThemeVariants() {
        LinkedList<IncludePartThemeVariant> themeVariants = new LinkedList<IncludePartThemeVariant>();
        for (String themeName : getIncludePartTO().themes) {
            themeVariants.add(newThemeVariantInstance(themeName));
        }
        return themeVariants;
    }
    
    public boolean hasThemeVariant(Theme theme) {
        return getIncludePartTO().themes.contains(theme.getName());
    }
    
    public void deleteThemeVariant(IncludePartThemeVariant variant) throws EditorSecurityException, EditorIOException, EditorParsingException {
        getRemoteService().deleteIncludePartThemeVariant(path, part, variant.getTheme().getName());
    }
    
    protected IncludePartTO getIncludePartTO() {
        return getRemoteService().getIncludePart(path, part);
    }
    
    @Override
    public int compareTo(IncludePart part) {
        if (part instanceof CommonIncludePartImpl) {
            CommonIncludePartImpl p = (CommonIncludePartImpl) part;
            if (this.remoteServiceUtil.equals(p.remoteServiceUtil)) {
                return super.compareTo(part);
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
        if (obj instanceof CommonIncludePartImpl) {
            CommonIncludePartImpl p = (CommonIncludePartImpl) obj;
            return this.remoteServiceUtil.equals(p.remoteServiceUtil);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return ("INCLUDEPART: " + super.hashCode() + remoteServiceUtil.hashCode()).hashCode();
    }

    protected abstract RemoteCommonIncludeService getRemoteService();
    
    protected abstract IncludePartThemeVariant newThemeVariantInstance(String theme);
    
}
