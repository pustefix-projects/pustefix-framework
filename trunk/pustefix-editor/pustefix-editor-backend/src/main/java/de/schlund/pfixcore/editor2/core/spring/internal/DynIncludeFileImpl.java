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

package de.schlund.pfixcore.editor2.core.spring.internal;

import org.pustefixframework.editor.common.dom.IncludePart;
import org.w3c.dom.Element;

import de.schlund.pfixcore.editor2.core.spring.BackupService;
import de.schlund.pfixcore.editor2.core.spring.ConfigurationService;
import de.schlund.pfixcore.editor2.core.spring.FileSystemService;
import de.schlund.pfixcore.editor2.core.spring.PathResolverService;
import de.schlund.pfixcore.editor2.core.spring.ThemeFactoryService;

/**
 * Implementation of IncludeFile for DynIncludes.
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class DynIncludeFileImpl extends CommonIncludeFileImpl {

    private ThemeFactoryService themefactory;

    private ConfigurationService configuration;

    private BackupService backup;

    public DynIncludeFileImpl(ThemeFactoryService themefactory,
            ConfigurationService configuration, BackupService backup,
            FileSystemService filesystem, PathResolverService pathresolver,
            String path) {
        super(filesystem, pathresolver, path);
        this.themefactory = themefactory;
        this.configuration = configuration;
        this.backup = backup;
    }

    @Override
    protected IncludePart createIncludePartInstance(String name, Element el, long serial) {
        return new DynIncludePartImpl(themefactory, configuration, backup, this
                .getFileSystemService(), this.getPathResolverService(),
                name, this, el, serial);
    }
}
