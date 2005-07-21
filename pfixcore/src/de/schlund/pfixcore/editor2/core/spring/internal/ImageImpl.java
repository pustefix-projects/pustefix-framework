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

import de.schlund.pfixcore.editor2.core.dom.AbstractImage;
import de.schlund.pfixcore.editor2.core.dom.Page;
import de.schlund.pfixcore.editor2.core.dom.Project;
import de.schlund.pfixcore.editor2.core.dom.Variant;
import de.schlund.pfixcore.editor2.core.spring.ProjectFactoryService;
import de.schlund.pfixcore.editor2.core.spring.VariantFactoryService;
import de.schlund.pfixxml.PathFactory;
import de.schlund.pfixxml.targets.AuxDependency;
import de.schlund.pfixxml.targets.AuxDependencyFactory;
import de.schlund.pfixxml.targets.DependencyType;
import de.schlund.pfixxml.targets.PageInfo;

public class ImageImpl extends AbstractImage {

    private String path;

    private Project project;

    private AuxDependency auxdep;

    private ProjectFactoryService projectfactory;

    private VariantFactoryService variantfactory;

    public ImageImpl(ProjectFactoryService projectfactory,
            VariantFactoryService variantfactory, String path) {
        this.projectfactory = projectfactory;
        this.variantfactory = variantfactory;
        this.path = path;
        String projectName = path.substring(0, path.indexOf("/"));
        this.project = this.projectfactory.getProjectByName(projectName);
        this.auxdep = AuxDependencyFactory.getInstance().getAuxDependency(
                DependencyType.IMAGE,
                PathFactory.getInstance().createPath(path), null, null);
    }

    public String getPath() {
        return this.path;
    }

    public Collection getAffectedPages() {
        HashSet pageinfos = new HashSet();
        for (Iterator i = this.auxdep.getAffectedTargets().iterator(); i
                .hasNext();) {
            de.schlund.pfixxml.targets.Target pfixTarget = (de.schlund.pfixxml.targets.Target) i
                    .next();
            pageinfos.addAll(pfixTarget.getPageInfos());
        }

        HashSet pages = new HashSet();
        for (Iterator i2 = pageinfos.iterator(); i2.hasNext();) {
            PageInfo pageinfo = (PageInfo) i2.next();
            String projectName = pageinfo.getTargetGenerator().getName();
            Project project = projectfactory.getProjectByName(projectName);
            Variant variant = null;
            if (pageinfo.getVariant() != null) {
                variant = this.variantfactory.getVariant(pageinfo
                        .getVariant());
            }
            Page page = project.getPage(pageinfo.getName(), variant);
            pages.add(page);
        }

        return pages;
    }

    public Project getProject() {
        return this.project;
    }

}
