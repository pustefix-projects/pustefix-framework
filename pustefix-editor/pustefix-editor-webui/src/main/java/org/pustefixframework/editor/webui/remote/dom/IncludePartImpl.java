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

package org.pustefixframework.editor.webui.remote.dom;

import java.util.Collection;
import java.util.LinkedList;

import org.pustefixframework.editor.common.dom.IncludeFile;
import org.pustefixframework.editor.common.dom.IncludePartThemeVariant;
import org.pustefixframework.editor.common.dom.Theme;
import org.pustefixframework.editor.common.remote.service.RemoteCommonIncludeService;
import org.pustefixframework.editor.webui.remote.dom.util.RemoteServiceUtil;



public class IncludePartImpl extends CommonIncludePartImpl {

    public IncludePartImpl(RemoteServiceUtil remoteServiceUtil, String path, String part) {
        super(remoteServiceUtil, path, part);
    }

    public IncludeFile getIncludeFile() {
        return new IncludeFileImpl(remoteServiceUtil, path);
    }
    
    public Collection<Theme> getPossibleThemes() {
        Collection<String> themeNames = remoteServiceUtil.getRemoteIncludeService().getPossibleThemes(path, part);
        LinkedList<Theme> themes = new LinkedList<Theme>();
        for (String themeName : themeNames) {
            themes.add(new ThemeImpl(themeName));
        }
        return themes;
    }

    @Override
    protected RemoteCommonIncludeService getRemoteService() {
        return remoteServiceUtil.getRemoteIncludeService();
    }

    @Override
    protected IncludePartThemeVariant newThemeVariantInstance(String theme) {
        return new IncludePartThemeVariantImpl(remoteServiceUtil, path, part, theme);
    }
}
