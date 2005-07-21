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

package de.schlund.pfixcore.editor2.core.spring;

import java.util.Hashtable;

import de.schlund.pfixcore.editor2.core.dom.Project;
import de.schlund.pfixcore.editor2.core.dom.Target;
import de.schlund.pfixcore.editor2.core.spring.internal.TargetAuxDepImpl;
import de.schlund.pfixcore.editor2.core.spring.internal.TargetPfixImpl;
import de.schlund.pfixxml.targets.AuxDependency;

/**
 * Implementation of TargetFactoryService
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class TargetFactoryServiceImpl implements TargetFactoryService {
    private Hashtable cachePfixTarget;

    private Hashtable cacheAuxDepTarget;

    private ProjectFactoryService projectfactory;

    private PageFactoryService pagefactory;

    private VariantFactoryService variantfactory;

    private TargetFactoryService targetfactory;

    private ThemeFactoryService themefactory;

    private IncludeFactoryService includefactory;

    private ImageFactoryService imagefactory;

    private FileSystemService filesystem;

    private PathResolverService pathresolver;

    public TargetFactoryServiceImpl(ProjectFactoryService projectfactory,
            PageFactoryService pagefactory,
            VariantFactoryService variantfactory,
            TargetFactoryService targetfactory,
            ThemeFactoryService themefactory,
            IncludeFactoryService includefactory,
            ImageFactoryService imagefactory,
            FileSystemService filesystem,
            PathResolverService pathresolver) {
        this.projectfactory = projectfactory;
        this.pagefactory = pagefactory;
        this.targetfactory = targetfactory;
        this.variantfactory = variantfactory;
        this.themefactory = themefactory;
        this.includefactory = includefactory;
        this.imagefactory = imagefactory;
        this.pathresolver = pathresolver;
        this.filesystem = filesystem;
        this.cachePfixTarget = new Hashtable();
        this.cacheAuxDepTarget = new Hashtable();
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.schlund.pfixcore.editor2.core.spring.TargetFactoryService#createTarget(de.schlund.pfixxml.targets.Target,
     *      de.schlund.pfixcore.editor2.core.dom.Project)
     */
    public Target getTargetFromPustefixTarget(de.schlund.pfixxml.targets.Target pfixTarget,
            Project project) {
        // TODO Make sure Target object is unique within the whole installation
        if (this.cachePfixTarget.containsKey(pfixTarget)) {
            return (Target) this.cachePfixTarget.get(pfixTarget);
        }
        synchronized (this.cachePfixTarget) {
            if (!this.cachePfixTarget.containsKey(pfixTarget)) {
                this.cachePfixTarget.put(pfixTarget, new TargetPfixImpl(
                        targetfactory, projectfactory, variantfactory,
                        includefactory, themefactory, imagefactory, pathresolver, filesystem, pfixTarget,
                        project));
            }
        }
        return (Target) this.cachePfixTarget.get(pfixTarget);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.schlund.pfixcore.editor2.core.spring.TargetFactoryService#createTarget(de.schlund.pfixxml.targets.AuxDependency)
     */
    public Target getLeafTargetFromPustefixAuxDependency(AuxDependency auxdep) {
        // TODO Make sure Target object is unique within the whole installation
        if (this.cacheAuxDepTarget.containsKey(auxdep)) {
            return (Target) this.cacheAuxDepTarget.get(auxdep);
        }
        synchronized (this.cacheAuxDepTarget) {
            if (!this.cacheAuxDepTarget.containsKey(auxdep)) {
                this.cacheAuxDepTarget.put(auxdep, new TargetAuxDepImpl(
                        this.projectfactory, this.pagefactory,
                        this.variantfactory, this.pathresolver, this.filesystem, auxdep));
            }
        }
        return (Target) this.cacheAuxDepTarget.get(auxdep);
    }

}
