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
import java.util.Collections;

import org.pustefixframework.editor.common.dom.IncludeFile;
import org.pustefixframework.editor.common.dom.IncludePartThemeVariant;
import org.pustefixframework.editor.common.dom.Theme;
import org.pustefixframework.editor.common.remote.service.RemoteCommonIncludeService;
import org.pustefixframework.editor.webui.remote.dom.util.RemoteServiceUtil;



public class DynIncludePartImpl extends CommonIncludePartImpl {
    
    public DynIncludePartImpl(RemoteServiceUtil remoteServiceUtil, String path, String part) {
        super(remoteServiceUtil, path, part);
    }
    
    public IncludeFile getIncludeFile() {
        return new DynIncludeFileImpl(remoteServiceUtil, path);
    }
    
    public Collection<Theme> getPossibleThemes() {
        return Collections.emptyList();
    }

    @Override
    protected RemoteCommonIncludeService getRemoteService() {
        return remoteServiceUtil.getRemoteDynIncludeService();
    }

    @Override
    protected IncludePartThemeVariant newThemeVariantInstance(String theme) {
        return new DynIncludePartThemeVariantImpl(remoteServiceUtil, path, part, theme);
    }
}
