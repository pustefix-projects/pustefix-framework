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
import java.util.Collections;

import org.pustefixframework.editor.common.dom.IncludeFile;
import org.pustefixframework.editor.common.dom.IncludePart;
import org.pustefixframework.editor.common.dom.IncludePartThemeVariant;
import org.pustefixframework.editor.common.remote.service.RemoteDynIncludeService;
import org.pustefixframework.editor.common.remote.transferobjects.IncludePartThemeVariantReferenceTO;

import de.schlund.pfixcore.editor2.core.spring.DynIncludeFactoryService;


public class RemoteDynIncludeServiceImpl extends RemoteCommonIncludeServiceImpl implements RemoteDynIncludeService {
    
    private DynIncludeFactoryService dynIncludeFactoryService;
    
    public void setDynIncludeFactoryService(DynIncludeFactoryService dynIncludeFactoryService) {
        this.dynIncludeFactoryService = dynIncludeFactoryService;
    }

    public Collection<String> getImageDependencies(IncludePartThemeVariantReferenceTO reference, boolean recursive) {
        return Collections.emptyList();
    }
    
    @Override
    public Collection<String> getImageDependencies(IncludePartThemeVariantReferenceTO reference, String target, boolean recursive) {
        return Collections.emptyList();
    }
    
    public Collection<IncludePartThemeVariantReferenceTO> getIncludeDependencies(IncludePartThemeVariantReferenceTO reference, boolean recursive) {
        return Collections.emptyList();
    }
    
    @Override
    public Collection<IncludePartThemeVariantReferenceTO> getIncludeDependencies(IncludePartThemeVariantReferenceTO reference, String name, boolean recursive) {
        return Collections.emptyList();
    }
    
    @Override
    protected IncludePartThemeVariant getIncludePartThemeVariantDOM(String path, String part, String theme) {
        IncludePart iPart = getIncludePartDOM(path, part);
        if (iPart == null) {
            return null;
        }
        return iPart.getThemeVariant(themeFactoryService.getTheme(theme));
    }
    
    @Override
    protected IncludePart getIncludePartDOM(String path, String part) {
        IncludeFile file = getIncludeFileDOM(path);
        if (file == null) {
            return null;
        }
        return  file.getPart(part);
    }
    
    @Override
    protected IncludeFile getIncludeFileDOM(String path) {
        return dynIncludeFactoryService.getIncludeFile(path);
    }
}
