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

import org.w3c.dom.Element;

import de.schlund.pfixcore.editor2.core.dom.IncludeFile;
import de.schlund.pfixcore.editor2.core.dom.IncludePartThemeVariant;
import de.schlund.pfixcore.editor2.core.dom.Theme;
import de.schlund.pfixcore.editor2.core.exception.EditorSecurityException;
import de.schlund.pfixcore.editor2.core.spring.BackupService;
import de.schlund.pfixcore.editor2.core.spring.ConfigurationService;
import de.schlund.pfixcore.editor2.core.spring.FileSystemService;
import de.schlund.pfixcore.editor2.core.spring.PathResolverService;
import de.schlund.pfixcore.editor2.core.spring.SecurityManagerService;
import de.schlund.pfixcore.editor2.core.spring.ThemeFactoryService;

/**
 * Implementation of IncludePart for DynIncludes.
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class DynIncludePartImpl extends CommonIncludePartImpl {
    private ConfigurationService configuration;

    private BackupService backup;

    private FileSystemService filesystem;

    private PathResolverService pathresolver;

    private SecurityManagerService securitymanager;

    public DynIncludePartImpl(ThemeFactoryService themefactory,
            ConfigurationService configuration, BackupService backup,
            FileSystemService filesystem, PathResolverService pathresolver,
            SecurityManagerService securitymanager, String partName,
            IncludeFile file, Element el, long serial) {
        super(themefactory, filesystem, pathresolver, backup, partName, file,
                el, serial);
        this.configuration = configuration;
        this.backup = backup;
        this.filesystem = filesystem;
        this.pathresolver = pathresolver;
        this.securitymanager = securitymanager;
    }

    protected IncludePartThemeVariant createIncludePartThemeVariant(Theme theme) {
        return new DynIncludePartThemeVariantImpl(configuration, backup,
                filesystem, pathresolver, securitymanager, theme, this);
    }

    protected void securityCheckDeleteIncludePartThemeVariant(
            IncludePartThemeVariant variant) throws EditorSecurityException {
        this.securitymanager.checkEditDynInclude();
    }

    public Collection<Theme> getPossibleThemes() {
        // For DynIncludes all thinkable themes are possible,
        // so return an empty list
        return new ArrayList<Theme>();
    }

}
