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

package org.pustefixframework.editor.common.remote.service;

import java.util.Collection;

import org.pustefixframework.editor.common.exception.EditorIOException;
import org.pustefixframework.editor.common.exception.EditorParsingException;
import org.pustefixframework.editor.common.exception.EditorSecurityException;
import org.pustefixframework.editor.common.remote.transferobjects.IncludeFileTO;
import org.pustefixframework.editor.common.remote.transferobjects.IncludePartTO;
import org.pustefixframework.editor.common.remote.transferobjects.IncludePartThemeVariantReferenceTO;
import org.pustefixframework.editor.common.remote.transferobjects.IncludePartThemeVariantTO;



public interface RemoteCommonIncludeService {
    
    Collection<String> getImageDependencies(IncludePartThemeVariantReferenceTO reference, boolean recursive) throws EditorParsingException;

    Collection<String> getImageDependencies(IncludePartThemeVariantReferenceTO reference, String target, boolean recursive) throws EditorParsingException;

    Collection<IncludePartThemeVariantReferenceTO> getIncludeDependencies(IncludePartThemeVariantReferenceTO reference, boolean recursive) throws EditorParsingException;

    Collection<IncludePartThemeVariantReferenceTO> getIncludeDependencies(IncludePartThemeVariantReferenceTO reference, String name, boolean recursive) throws EditorParsingException;

    IncludePartThemeVariantTO getIncludePartThemeVariantTO(IncludePartThemeVariantReferenceTO reference);

    IncludePartTO getIncludePart(String path, String part);

    void deleteIncludePartThemeVariant(String path, String part, String name) throws EditorParsingException, EditorSecurityException, EditorIOException;

    IncludeFileTO getIncludeFile(String path);

    String getIncludeFileXML(String path, boolean forceUpdate);
    
    String getIncludePartXML(String path, String part);
    
    String getIncludePartThemeVariantXML(String path, String part, String theme);
    
    void setIncludePartThemeVariantXML(String path, String part, String theme, String xml, boolean indent) throws EditorIOException, EditorParsingException, EditorSecurityException;

    Collection<String> getIncludePartThemeVariantBackupVersions(String path, String part, String name);
    
    void backupIncludePartThemeVariant(String path, String part, String theme);
    boolean restoreIncludePartThemeVariant(String path, String part, String theme, String version) throws EditorSecurityException;
}
