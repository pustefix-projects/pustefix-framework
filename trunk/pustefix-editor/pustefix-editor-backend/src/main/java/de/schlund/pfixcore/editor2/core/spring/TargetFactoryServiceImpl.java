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

package de.schlund.pfixcore.editor2.core.spring;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import org.pustefixframework.editor.common.dom.Project;
import org.pustefixframework.editor.common.dom.Target;

import de.schlund.pfixcore.editor2.core.spring.internal.TargetAuxDepImpl;
import de.schlund.pfixcore.editor2.core.spring.internal.TargetPfixImpl;
import de.schlund.pfixxml.targets.AuxDependency;
import de.schlund.pfixxml.targets.AuxDependencyFile;

/**
 * Implementation of TargetFactoryService
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class TargetFactoryServiceImpl implements TargetFactoryService {
    private Map<de.schlund.pfixxml.targets.Target, Target> cachePfixTarget;

    private Map<AuxDependency, Target> cacheAuxDepTarget;

    private ProjectFactoryService projectfactory;

    private VariantFactoryService variantfactory;

    private TargetFactoryService targetfactory;

    private ThemeFactoryService themefactory;

    private IncludeFactoryService includefactory;

    private ImageFactoryService imagefactory;

    private FileSystemService filesystem;

    private PathResolverService pathresolver;

    public void setProjectFactoryService(ProjectFactoryService projectfactory) {
        this.projectfactory = projectfactory;
    }

    public void setThemeFactoryService(ThemeFactoryService themefactory) {
        this.themefactory = themefactory;
    }

    public void setVariantFactoryService(VariantFactoryService variantfactory) {
        this.variantfactory = variantfactory;
    }

    public void setIncludeFactoryService(IncludeFactoryService includefactory) {
        this.includefactory = includefactory;
    }

    public void setImageFactoryService(ImageFactoryService imagefactory) {
        this.imagefactory = imagefactory;
    }

    public void setPathResolverService(PathResolverService pathresolver) {
        this.pathresolver = pathresolver;
    }

    public void setFileSystemService(FileSystemService filesystem) {
        this.filesystem = filesystem;
    }

    public void setTargetFactoryService(TargetFactoryService targetfactory) {
        this.targetfactory = targetfactory;
    }

    public TargetFactoryServiceImpl() {
        this.cachePfixTarget = new WeakHashMap<de.schlund.pfixxml.targets.Target, Target>();
        this.cacheAuxDepTarget = Collections
                .synchronizedMap(new HashMap<AuxDependency, Target>());
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.schlund.pfixcore.editor2.core.spring.TargetFactoryService#createTarget(de.schlund.pfixxml.targets.Target,
     *      de.schlund.pfixcore.editor2.core.dom.Project)
     */
    public Target getTargetFromPustefixTarget(
            de.schlund.pfixxml.targets.Target pfixTarget, Project project) {
        if (pfixTarget == null) {
            return null;
        }
        synchronized (this.cachePfixTarget) {
            if (!this.cachePfixTarget.containsKey(pfixTarget)) {
                this.cachePfixTarget.put(pfixTarget, new TargetPfixImpl(
                        targetfactory, projectfactory, variantfactory,
                        includefactory, themefactory, imagefactory,
                        pathresolver, filesystem, pfixTarget, project));
            }
            return (Target) this.cachePfixTarget.get(pfixTarget);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.schlund.pfixcore.editor2.core.spring.TargetFactoryService#createTarget(de.schlund.pfixxml.targets.AuxDependency)
     */
    public Target getLeafTargetFromPustefixAuxDependency(
            AuxDependencyFile auxdep) {
        // TODO Make sure Target object is unique within the whole installation
        if (this.cacheAuxDepTarget.containsKey(auxdep)) {
            return (Target) this.cacheAuxDepTarget.get(auxdep);
        }
        synchronized (this.cacheAuxDepTarget) {
            if (!this.cacheAuxDepTarget.containsKey(auxdep)) {
                this.cacheAuxDepTarget.put(auxdep, new TargetAuxDepImpl(
                        this.projectfactory, this.variantfactory,
                        this.pathresolver, this.filesystem, auxdep));
            }
        }
        return (Target) this.cacheAuxDepTarget.get(auxdep);
    }

}
