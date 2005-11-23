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
import java.util.HashSet;
import java.util.Iterator;

import org.apache.log4j.Logger;

import de.schlund.pfixcore.editor2.core.dom.Image;
import de.schlund.pfixcore.editor2.core.dom.IncludePart;
import de.schlund.pfixcore.editor2.core.dom.IncludePartThemeVariant;
import de.schlund.pfixcore.editor2.core.dom.Page;
import de.schlund.pfixcore.editor2.core.dom.Project;
import de.schlund.pfixcore.editor2.core.dom.Theme;
import de.schlund.pfixcore.editor2.core.dom.ThemeList;
import de.schlund.pfixcore.editor2.core.dom.Variant;
import de.schlund.pfixcore.editor2.core.exception.EditorParsingException;
import de.schlund.pfixcore.editor2.core.exception.EditorSecurityException;
import de.schlund.pfixcore.editor2.core.spring.BackupService;
import de.schlund.pfixcore.editor2.core.spring.ConfigurationService;
import de.schlund.pfixcore.editor2.core.spring.FileSystemService;
import de.schlund.pfixcore.editor2.core.spring.ImageFactoryService;
import de.schlund.pfixcore.editor2.core.spring.IncludeFactoryService;
import de.schlund.pfixcore.editor2.core.spring.PathResolverService;
import de.schlund.pfixcore.editor2.core.spring.ProjectFactoryService;
import de.schlund.pfixcore.editor2.core.spring.SecurityManagerService;
import de.schlund.pfixcore.editor2.core.spring.ThemeFactoryService;
import de.schlund.pfixcore.editor2.core.spring.VariantFactoryService;
import de.schlund.pfixxml.PathFactory;
import de.schlund.pfixxml.targets.AuxDependency;
import de.schlund.pfixxml.targets.AuxDependencyFactory;
import de.schlund.pfixxml.targets.DependencyType;
import de.schlund.pfixxml.targets.PageInfo;
import de.schlund.pfixxml.targets.TargetGenerator;
import de.schlund.pfixxml.targets.Themes;

/**
 * Implementation of IncludePartThemeVariant using a DOM tree
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class IncludePartThemeVariantImpl extends
        CommonIncludePartThemeVariantImpl {

    private ProjectFactoryService projectfactory;

    private VariantFactoryService variantfactory;

    private IncludeFactoryService includefactory;

    private ThemeFactoryService themefactory;

    private ImageFactoryService imagefactory;

    private SecurityManagerService securitymanager;

    public IncludePartThemeVariantImpl(ProjectFactoryService projectfactory,
            VariantFactoryService variantfactory,
            IncludeFactoryService includefactory,
            ThemeFactoryService themefactory, ImageFactoryService imagefactory,
            FileSystemService filesystem, PathResolverService pathresolver,
            ConfigurationService configuration,
            SecurityManagerService securitymanager, BackupService backup,
            Theme theme, IncludePart part) {
        super(filesystem, pathresolver, configuration, backup, theme, part);
        this.projectfactory = projectfactory;
        this.variantfactory = variantfactory;
        this.includefactory = includefactory;
        this.themefactory = themefactory;
        this.imagefactory = imagefactory;
        this.securitymanager = securitymanager;
    }

    private AuxDependency getAuxDependency() {
        return AuxDependencyFactory.getInstance().getAuxDependency(
                DependencyType.TEXT,
                PathFactory.getInstance().createPath(
                        this.getIncludePart().getIncludeFile().getPath()),
                this.getIncludePart().getName(), this.getTheme().getName());
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.schlund.pfixcore.editor2.core.dom.IncludePartThemeVariant#getIncludeDependencies(boolean)
     */
    public Collection getIncludeDependencies(boolean recursive)
            throws EditorParsingException {
        HashSet includes = new HashSet();
        Collection childs = this.getAuxDependency().getChildrenForAllThemes();
        for (Iterator i = childs.iterator(); i.hasNext();) {
            AuxDependency child = (AuxDependency) i.next();
            if (child.getType() == DependencyType.TEXT) {
                IncludePartThemeVariant variant = this.includefactory
                        .getIncludePartThemeVariant(child);
                includes.add(variant);
                if (recursive) {
                    includes.addAll(variant.getIncludeDependencies(true));
                }
            }
        }
        return includes;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.schlund.pfixcore.editor2.core.dom.IncludePartThemeVariant#getImageDependencies(boolean)
     */
    public Collection getImageDependencies(boolean recursive)
            throws EditorParsingException {
        HashSet images = new HashSet();
        Collection childs = this.getAuxDependency().getChildrenForAllThemes();
        for (Iterator i = childs.iterator(); i.hasNext();) {
            AuxDependency child = (AuxDependency) i.next();
            if (child.getType() == DependencyType.IMAGE) {
                Image image = this.imagefactory.getImage(child.getPath()
                        .getRelative());
                images.add(image);
            } else if ((child.getType() == DependencyType.TEXT) && recursive) {
                IncludePartThemeVariant variant = this.includefactory
                        .getIncludeFile(child.getPath().getRelative())
                        .createPart(child.getPart()).createThemeVariant(
                                themefactory.getTheme(child.getProduct()));
                images.addAll(variant.getImageDependencies(true));
            }
        }
        return images;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.schlund.pfixcore.editor2.core.dom.IncludePartThemeVariant#getAffectedPages()
     */
    public Collection getAffectedPages() {
        HashSet pageinfos = new HashSet();
        for (Iterator i = this.getAuxDependency().getAffectedTargets()
                .iterator(); i.hasNext();) {
            de.schlund.pfixxml.targets.Target pfixTarget = (de.schlund.pfixxml.targets.Target) i
                    .next();
            pageinfos.addAll(pfixTarget.getPageInfos());
        }

        HashSet pages = new HashSet();
        for (Iterator i2 = pageinfos.iterator(); i2.hasNext();) {
            PageInfo pageinfo = (PageInfo) i2.next();
            Project project = projectfactory
                    .getProjectByPustefixTargetGenerator(pageinfo
                            .getTargetGenerator());
            Variant variant = null;
            if (pageinfo.getVariant() != null) {
                variant = variantfactory.getVariant(pageinfo.getVariant());
            }
            Page page = project.getPage(pageinfo.getName(), variant);
            if (page != null) {
                pages.add(page);
            }
        }

        return pages;
    }

    public Collection getIncludeDependencies(ThemeList themes, boolean recursive)
            throws EditorParsingException {
        HashSet includes = new HashSet();

        ArrayList themesArray = new ArrayList();
        for (Iterator i = themes.getThemes().iterator(); i.hasNext();) {
            Theme theme = (Theme) i.next();
            themesArray.add(theme.getName());
        }

        Collection childs = this.getAuxDependency().getChildrenForThemes(
                new Themes((String[]) themesArray.toArray(new String[0])));
        for (Iterator i = childs.iterator(); i.hasNext();) {
            AuxDependency child = (AuxDependency) i.next();
            if (child.getType() == DependencyType.TEXT) {
                IncludePartThemeVariant variant = this.includefactory
                        .getIncludePartThemeVariant(child);
                includes.add(variant);
                if (recursive) {
                    includes.addAll(variant.getIncludeDependencies(true));
                }
            }
        }
        return includes;
    }

    public Collection getImageDependencies(ThemeList themes, boolean recursive)
            throws EditorParsingException {
        HashSet images = new HashSet();

        ArrayList themesArray = new ArrayList();
        for (Iterator i = themes.getThemes().iterator(); i.hasNext();) {
            Theme theme = (Theme) i.next();
            themesArray.add(theme.getName());
        }

        Collection childs = this.getAuxDependency().getChildrenForThemes(
                new Themes((String[]) themesArray.toArray(new String[0])));
        for (Iterator i = childs.iterator(); i.hasNext();) {
            AuxDependency child = (AuxDependency) i.next();
            if (child.getType() == DependencyType.IMAGE) {
                Image image = this.imagefactory.getImage(child.getPath()
                        .getRelative());
                images.add(image);
            } else if ((child.getType() == DependencyType.TEXT) && recursive) {
                IncludePartThemeVariant variant = this.includefactory
                        .getIncludeFile(child.getPath().getRelative())
                        .createPart(child.getPart()).createThemeVariant(
                                themefactory.getTheme(child.getProduct()));
                images.addAll(variant.getImageDependencies(true));
            }
        }
        return images;
    }

    protected void securityCheckCreateIncludePartThemeVariant()
            throws EditorSecurityException {
        this.securitymanager.checkCreateIncludePartThemeVariant(this
                .getIncludePart(), this.getTheme());
    }

    protected void securityCheckEditIncludePartThemeVariant()
            throws EditorSecurityException {
        this.securitymanager.checkEditIncludePartThemeVariant(this);
    }

    public Collection getAffectedProjects() {
        HashSet projects = new HashSet();
        for (Iterator i = this.getAuxDependency().getAffectedTargetGenerators()
                .iterator(); i.hasNext();) {
            TargetGenerator tgen = (TargetGenerator) i.next();
            Project project = this.projectfactory
                    .getProjectByPustefixTargetGenerator(tgen);
            if (project != null) {
                projects.add(project);
            }
        }
        return projects;
    }

    protected void writeChangeLog() {
        Logger.getLogger("LOGGER_EDITOR").warn(
                "TXT: " + this.securitymanager.getPrincipal().getName() + ": "
                        + this.toString());
    }
}
