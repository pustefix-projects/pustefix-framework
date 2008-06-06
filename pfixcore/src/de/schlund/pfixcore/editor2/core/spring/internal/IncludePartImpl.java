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
import java.util.LinkedHashSet;

import org.apache.log4j.Logger;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;

import de.schlund.pfixcore.editor2.core.dom.IncludeFile;
import de.schlund.pfixcore.editor2.core.dom.IncludePartThemeVariant;
import de.schlund.pfixcore.editor2.core.dom.Page;
import de.schlund.pfixcore.editor2.core.dom.Project;
import de.schlund.pfixcore.editor2.core.dom.Theme;
import de.schlund.pfixcore.editor2.core.exception.EditorIOException;
import de.schlund.pfixcore.editor2.core.exception.EditorParsingException;
import de.schlund.pfixcore.editor2.core.exception.EditorSecurityException;
import de.schlund.pfixcore.editor2.core.spring.BackupService;
import de.schlund.pfixcore.editor2.core.spring.FileSystemService;
import de.schlund.pfixcore.editor2.core.spring.IncludeFactoryService;
import de.schlund.pfixcore.editor2.core.spring.PathResolverService;
import de.schlund.pfixcore.editor2.core.spring.ProjectFactoryService;
import de.schlund.pfixcore.editor2.core.spring.SecurityManagerService;
import de.schlund.pfixcore.editor2.core.spring.ThemeFactoryService;
import de.schlund.pfixxml.resources.ResourceUtil;
import de.schlund.pfixxml.targets.AuxDependency;
import de.schlund.pfixxml.targets.AuxDependencyFactory;

/**
 * Implementation of IncludePart using a DOM tree
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class IncludePartImpl extends CommonIncludePartImpl {
    private IncludeFactoryService includefactory;

    private SecurityManagerService securitymanager;
    
    private ProjectFactoryService projectfactory;

    public IncludePartImpl(ThemeFactoryService themefactory,
            IncludeFactoryService includefactory, ProjectFactoryService projectfactory, FileSystemService filesystem,
            PathResolverService pathresolver, BackupService backup,
            SecurityManagerService securitymanager, String partName,
            IncludeFile file, Element el, long serial) {
        super(themefactory, filesystem, pathresolver, backup, partName, file, el, serial);
        this.includefactory = includefactory;
        this.securitymanager = securitymanager;
        this.projectfactory = projectfactory;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.schlund.pfixcore.editor2.core.dom.IncludePart#getPossibleThemes()
     */
    public Collection<Theme> getPossibleThemes() {
        HashSet<Page> pages = new HashSet<Page>();
        for (Iterator<IncludePartThemeVariant> i = this.getThemeVariants().iterator(); i.hasNext();) {
            IncludePartThemeVariant partVar = i.next();
            pages.addAll(partVar.getAffectedPages());
        }
        HashSet<Theme> themes = new HashSet<Theme>();
        for (Iterator<Page> i = pages.iterator(); i.hasNext();) {
            Page page = i.next();
            themes.addAll(page.getThemes().getThemes());
        }
        return themes;
    }

    protected void securityCheckDeleteIncludePartThemeVariant(
            IncludePartThemeVariant variant) throws EditorSecurityException {
        this.securitymanager.checkEditIncludePartThemeVariant(variant);
    }

    public IncludePartThemeVariant createThemeVariant(Theme theme) throws EditorIOException, EditorParsingException, EditorSecurityException, DOMException {
        // Make sure that returned instance is in auxdep map
        AuxDependency aux = AuxDependencyFactory.getInstance().getAuxDependencyInclude(
                ResourceUtil.getFileResourceFromDocroot(this.getIncludeFile().getPath()),
                this.getName(), theme.getName());
        
        return includefactory.getIncludePartThemeVariant(aux);
    }

    public IncludePartThemeVariant getThemeVariant(Theme theme) {
        for (Project project : this.projectfactory.getProjects()) {
            for (IncludePartThemeVariant part : project.getAllIncludeParts()) {
                if (part.getIncludePart().equals(this) && part.getTheme().equals(theme)) {
                    return part;
                }
            }
        }
        return null;
    }

    public Collection<IncludePartThemeVariant> getThemeVariants() {
        LinkedHashSet<IncludePartThemeVariant> parts = new LinkedHashSet<IncludePartThemeVariant>();
        for (Project project : this.projectfactory.getProjects()) {
            for (IncludePartThemeVariant part : project.getAllIncludeParts()) {
                if (part.getIncludePart().equals(this)) {
                    parts.add(part);
                }
            }
        }
        return parts;
    }

    public boolean hasThemeVariant(Theme theme) {
        for (Project project : this.projectfactory.getProjects()) {
            for (IncludePartThemeVariant part : project.getAllIncludeParts()) {
                if (part.getIncludePart().equals(this) && part.getTheme().equals(theme)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void deleteThemeVariant(IncludePartThemeVariant variant) throws EditorSecurityException, EditorIOException, EditorParsingException {
        super.deleteThemeVariant(variant);
        
        Logger.getLogger("LOGGER_EDITOR").warn(
                "TXT: " + this.securitymanager.getPrincipal().getName()
                        + ": " + variant.toString() + ": DELETED");
        
        // Register affected pages for regeneration
        Page affectedpage = null;
        for (Iterator<Page> i = variant.getAffectedPages().iterator(); i.hasNext();) {
            Page page = i.next();
            page.registerForUpdate();
            affectedpage = page;
        }

        // Regenerate exactly ONE affected page synchronously
        // to make sure changes in the dependency tree are
        // visible at once
        if (affectedpage != null) {
            affectedpage.update();
        }
    }
    
    
}
