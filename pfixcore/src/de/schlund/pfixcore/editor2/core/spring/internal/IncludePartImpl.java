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

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.w3c.dom.Element;

import de.schlund.pfixcore.editor2.core.dom.IncludeFile;
import de.schlund.pfixcore.editor2.core.dom.IncludePartThemeVariant;
import de.schlund.pfixcore.editor2.core.dom.Page;
import de.schlund.pfixcore.editor2.core.dom.Theme;
import de.schlund.pfixcore.editor2.core.exception.EditorSecurityException;
import de.schlund.pfixcore.editor2.core.spring.BackupService;
import de.schlund.pfixcore.editor2.core.spring.FileSystemService;
import de.schlund.pfixcore.editor2.core.spring.IncludeFactoryService;
import de.schlund.pfixcore.editor2.core.spring.PathResolverService;
import de.schlund.pfixcore.editor2.core.spring.SecurityManagerService;
import de.schlund.pfixcore.editor2.core.spring.ThemeFactoryService;

/**
 * Implementation of IncludePart using a DOM tree
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class IncludePartImpl extends CommonIncludePartImpl {
    private IncludeFactoryService includefactory;

    private SecurityManagerService securitymanager;

    public IncludePartImpl(ThemeFactoryService themefactory,
            IncludeFactoryService includefactory, FileSystemService filesystem,
            PathResolverService pathresolver, BackupService backup,
            SecurityManagerService securitymanager, String partName,
            IncludeFile file, Element el, long serial) {
        super(themefactory, filesystem, pathresolver, backup, partName, file, el, serial);
        this.includefactory = includefactory;
        this.securitymanager = securitymanager;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.schlund.pfixcore.editor2.core.dom.IncludePart#getPossibleThemes()
     */
    public Collection getPossibleThemes() {
        HashSet pages = new HashSet();
        for (Iterator i = this.getThemeVariants().iterator(); i.hasNext();) {
            IncludePartThemeVariant partVar = (IncludePartThemeVariant) i
                    .next();
            pages.addAll(partVar.getAffectedPages());
        }
        HashSet themes = new HashSet();
        for (Iterator i = pages.iterator(); i.hasNext();) {
            Page page = (Page) i.next();
            themes.addAll(page.getThemes().getThemes());
        }
        return themes;
    }

    protected IncludePartThemeVariant createIncludePartThemeVariant(Theme theme) {
        return includefactory.getIncludePartThemeVariant(theme, this);
    }

    protected void securityCheckDeleteIncludePartThemeVariant(
            IncludePartThemeVariant variant) throws EditorSecurityException {
        this.securitymanager.checkEditIncludePartThemeVariant(variant);
    }
}
