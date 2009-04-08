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

package de.schlund.pfixcore.editor2.core.spring.internal;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.pustefixframework.editor.common.dom.Image;
import org.pustefixframework.editor.common.dom.IncludePart;
import org.pustefixframework.editor.common.dom.IncludePartThemeVariant;
import org.pustefixframework.editor.common.dom.Page;
import org.pustefixframework.editor.common.dom.Project;
import org.pustefixframework.editor.common.dom.Target;
import org.pustefixframework.editor.common.dom.Theme;
import org.pustefixframework.editor.common.exception.EditorParsingException;

import de.schlund.pfixcore.editor2.core.spring.BackupService;
import de.schlund.pfixcore.editor2.core.spring.ConfigurationService;
import de.schlund.pfixcore.editor2.core.spring.FileSystemService;
import de.schlund.pfixcore.editor2.core.spring.PathResolverService;

/**
 * Implementation of IncludePartThemeVariant for DynIncludes
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class DynIncludePartThemeVariantImpl extends
        CommonIncludePartThemeVariantImpl {

    public DynIncludePartThemeVariantImpl(ConfigurationService configuration,
            BackupService backup, FileSystemService filesystem,
            PathResolverService pathresolver,
            Theme theme,
            IncludePart part) {
        super(filesystem, pathresolver, configuration, backup, theme, part);
    }

    public Collection<IncludePartThemeVariant> getIncludeDependencies(boolean recursive)
            throws EditorParsingException {
        return new ArrayList<IncludePartThemeVariant>();
    }

    public Collection<Image> getImageDependencies(boolean recursive)
            throws EditorParsingException {
        return new ArrayList<Image>();
    }

    public Collection<IncludePartThemeVariant> getIncludeDependencies(Target target, boolean recursive)
            throws EditorParsingException {
        return new ArrayList<IncludePartThemeVariant>();
    }

    public Collection<Image> getImageDependencies(Target target, boolean recursive)
            throws EditorParsingException {
        return new ArrayList<Image>();
    }

    public Collection<Page> getAffectedPages() {
        return new ArrayList<Page>();
    }

    public Collection<Project> getAffectedProjects() {
        return new ArrayList<Project>();
    }

    @Override
    protected void writeChangeLog() {
        Logger.getLogger("LOGGER_EDITOR").warn(
                "DYNTXT: remote_access: " + this.toString());
    }

}
