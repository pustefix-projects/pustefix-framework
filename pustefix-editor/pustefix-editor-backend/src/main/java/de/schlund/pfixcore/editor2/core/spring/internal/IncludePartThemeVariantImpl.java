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
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.pustefixframework.editor.common.dom.Image;
import org.pustefixframework.editor.common.dom.IncludePart;
import org.pustefixframework.editor.common.dom.IncludePartThemeVariant;
import org.pustefixframework.editor.common.dom.Page;
import org.pustefixframework.editor.common.dom.Project;
import org.pustefixframework.editor.common.dom.Theme;
import org.pustefixframework.editor.common.dom.Variant;
import org.pustefixframework.editor.common.exception.EditorParsingException;

import de.schlund.pfixcore.editor2.core.spring.BackupService;
import de.schlund.pfixcore.editor2.core.spring.ConfigurationService;
import de.schlund.pfixcore.editor2.core.spring.FileSystemService;
import de.schlund.pfixcore.editor2.core.spring.ImageFactoryService;
import de.schlund.pfixcore.editor2.core.spring.IncludeFactoryService;
import de.schlund.pfixcore.editor2.core.spring.PathResolverService;
import de.schlund.pfixcore.editor2.core.spring.ProjectFactoryService;
import de.schlund.pfixcore.editor2.core.spring.ThemeFactoryService;
import de.schlund.pfixcore.editor2.core.spring.VariantFactoryService;
import de.schlund.pfixxml.resources.ResourceUtil;
import de.schlund.pfixxml.targets.AuxDependency;
import de.schlund.pfixxml.targets.AuxDependencyFactory;
import de.schlund.pfixxml.targets.AuxDependencyImage;
import de.schlund.pfixxml.targets.AuxDependencyInclude;
import de.schlund.pfixxml.targets.DependencyType;
import de.schlund.pfixxml.targets.PageInfo;
import de.schlund.pfixxml.targets.Target;
import de.schlund.pfixxml.targets.TargetDependencyRelation;

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

    // private ThemeFactoryService themefactory;

    private ImageFactoryService imagefactory;

    public IncludePartThemeVariantImpl(ProjectFactoryService projectfactory,
            VariantFactoryService variantfactory,
            IncludeFactoryService includefactory,
            ThemeFactoryService themefactory, ImageFactoryService imagefactory,
            FileSystemService filesystem, PathResolverService pathresolver,
            ConfigurationService configuration,
            BackupService backup,
            Theme theme, IncludePart part) {
        super(filesystem, pathresolver, configuration, backup, theme, part);
        this.projectfactory = projectfactory;
        this.variantfactory = variantfactory;
        this.includefactory = includefactory;
        this.imagefactory = imagefactory;
    }

    private AuxDependency getAuxDependency() {
        return AuxDependencyFactory.getInstance().getAuxDependencyInclude(
                ResourceUtil.getResource(
                        this.getIncludePart().getIncludeFile().getPath()),
                this.getIncludePart().getName(), this.getTheme().getName());
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.schlund.pfixcore.editor2.core.dom.IncludePartThemeVariant#getIncludeDependencies(boolean)
     */
    public Collection<IncludePartThemeVariant> getIncludeDependencies(
            boolean recursive) throws EditorParsingException {
        HashSet<IncludePartThemeVariant> includes = new HashSet<IncludePartThemeVariant>();
        Collection<AuxDependency> childs = TargetDependencyRelation.getInstance()
                .getChildrenOverallForAuxDependency(this.getAuxDependency());
        if (childs == null) {
            return includes;
        }
        for (Iterator<AuxDependency> i = childs.iterator(); i.hasNext();) {
            AuxDependency child = i.next();
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
    public Collection<Image> getImageDependencies(boolean recursive)
            throws EditorParsingException {
        HashSet<Image> images = new HashSet<Image>();
        Collection<AuxDependency> childs = TargetDependencyRelation.getInstance()
                .getChildrenOverallForAuxDependency(this.getAuxDependency());
        if (childs == null) {
            return images;
        }
        for (Iterator<AuxDependency> i = childs.iterator(); i.hasNext();) {
            AuxDependency child = i.next();
            if (child.getType() == DependencyType.IMAGE) {
                Image image = this.imagefactory.getImage(((AuxDependencyImage) child).getPath().toURI().toString());
                images.add(image);
            } else if ((child.getType() == DependencyType.TEXT) && recursive) {
                AuxDependencyInclude aux = (AuxDependencyInclude) child;
                IncludePartThemeVariant variant = this.includefactory
                        .getIncludePartThemeVariant(aux);
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
    public Collection<Page> getAffectedPages() {
        HashSet<PageInfo> pageinfos = new HashSet<PageInfo>();
        HashSet<Page> pages = new HashSet<Page>();
        Set<Target> afftargets = TargetDependencyRelation.getInstance()
                .getAffectedTargets(this.getAuxDependency());
        if (afftargets == null) {
            return pages;
        }

        for (Iterator<Target> i = afftargets.iterator(); i.hasNext();) {
            Target pfixTarget = i.next();
            pageinfos.addAll(pfixTarget.getPageInfos());
        }

        for (Iterator<PageInfo> i2 = pageinfos.iterator(); i2.hasNext();) {
            PageInfo pageinfo = i2.next();
            Project project = projectfactory.getProject();
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

    public Collection<IncludePartThemeVariant> getIncludeDependencies(
            org.pustefixframework.editor.common.dom.Target target,
            boolean recursive) throws EditorParsingException {
        HashSet<IncludePartThemeVariant> includes = new HashSet<IncludePartThemeVariant>();

        Collection<AuxDependency> childs = getChildrenForTarget(this.getAuxDependency(),
                target);
        for (Iterator<AuxDependency> i = childs.iterator(); i.hasNext();) {
            AuxDependency child = i.next();
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

    public Collection<Image> getImageDependencies(
            org.pustefixframework.editor.common.dom.Target target,
            boolean recursive) throws EditorParsingException {
        HashSet<Image> images = new HashSet<Image>();

        Collection<AuxDependency> childs = getChildrenForTarget(this.getAuxDependency(),
                target);
        for (Iterator<AuxDependency> i = childs.iterator(); i.hasNext();) {
            AuxDependency child = i.next();
            if (child.getType() == DependencyType.IMAGE) {
                Image image = this.imagefactory.getImage(((AuxDependencyImage) child).getPath().toURI().toString());
                images.add(image);
            } else if ((child.getType() == DependencyType.TEXT) && recursive) {
                AuxDependencyInclude aux = (AuxDependencyInclude) child;
                IncludePartThemeVariant variant = this.includefactory
                        .getIncludePartThemeVariant(aux);
                images.addAll(variant.getImageDependencies(true));
            }
        }
        return images;
    }


    protected void writeChangeLog() {
        Logger.getLogger("LOGGER_EDITOR").warn(
                "TXT: remote_access: "
                        + this.toString());
    }

    private Set<AuxDependency> getChildrenForTarget(AuxDependency parent,
            org.pustefixframework.editor.common.dom.Target target) {
        Target pfixTarget;
        if (target instanceof TargetPfixImpl) {
            pfixTarget = ((TargetPfixImpl) target).getPfixTarget();
        } else {
            return new TreeSet<AuxDependency>();
        }
        TreeSet<AuxDependency> retval = TargetDependencyRelation.getInstance()
                .getChildrenForTargetForAuxDependency(pfixTarget, parent);
        if (retval == null) {
            return new TreeSet<AuxDependency>();
        } else {
            return retval;
        }
    }

}
