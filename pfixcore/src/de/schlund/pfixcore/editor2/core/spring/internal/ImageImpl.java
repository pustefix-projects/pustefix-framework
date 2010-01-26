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

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;

import de.schlund.pfixcore.editor2.core.dom.AbstractImage;
import de.schlund.pfixcore.editor2.core.dom.Page;
import de.schlund.pfixcore.editor2.core.dom.Project;
import de.schlund.pfixcore.editor2.core.dom.Variant;
import de.schlund.pfixcore.editor2.core.exception.EditorIOException;
import de.schlund.pfixcore.editor2.core.exception.EditorSecurityException;
import de.schlund.pfixcore.editor2.core.spring.BackupService;
import de.schlund.pfixcore.editor2.core.spring.FileSystemService;
import de.schlund.pfixcore.editor2.core.spring.PathResolverService;
import de.schlund.pfixcore.editor2.core.spring.ProjectFactoryService;
import de.schlund.pfixcore.editor2.core.spring.SecurityManagerService;
import de.schlund.pfixcore.editor2.core.spring.VariantFactoryService;
import de.schlund.pfixxml.resources.ResourceUtil;
import de.schlund.pfixxml.targets.AuxDependency;
import de.schlund.pfixxml.targets.AuxDependencyFactory;
import de.schlund.pfixxml.targets.PageInfo;
import de.schlund.pfixxml.targets.Target;
import de.schlund.pfixxml.targets.TargetDependencyRelation;
import de.schlund.pfixxml.targets.TargetGenerator;

public class ImageImpl extends AbstractImage {

    private String path;

    private AuxDependency auxdep;

    private ProjectFactoryService projectfactory;

    private VariantFactoryService variantfactory;

    private PathResolverService pathresolver;

    private FileSystemService filesystem;

    private SecurityManagerService securitymanager;

    private BackupService backup;

    public ImageImpl(VariantFactoryService variantfactory,
            ProjectFactoryService projectfactory,
            PathResolverService pathresolver, FileSystemService filesystem,
            SecurityManagerService securitymanager, BackupService backup,
            String path) {
        this.variantfactory = variantfactory;
        this.projectfactory = projectfactory;
        this.pathresolver = pathresolver;
        this.securitymanager = securitymanager;
        this.filesystem = filesystem;
        this.backup = backup;
        this.path = path;
        this.auxdep = AuxDependencyFactory.getInstance().getAuxDependencyImage(ResourceUtil.getResource(path));
    }

    public String getPath() {
        return this.path;
    }

    public Collection<Page> getAffectedPages() {
        HashSet<PageInfo> pageinfos = new HashSet<PageInfo>();
        HashSet<Page> pages = new HashSet<Page>();
        Set<Target> afftargets = TargetDependencyRelation.getInstance()
                .getAffectedTargets(this.auxdep);
        if (afftargets == null) {
            return pages;
        }

        for (Iterator<Target> i = afftargets.iterator(); i.hasNext();) {
            Target pfixTarget = i.next();
            pageinfos.addAll(pfixTarget.getPageInfos());
        }

        for (Iterator<PageInfo> i2 = pageinfos.iterator(); i2.hasNext();) {
            PageInfo pageinfo = i2.next();
            TargetGenerator tgen = pageinfo.getTargetGenerator();
            Project project = projectfactory
                    .getProjectByPustefixTargetGenerator(tgen);
            Variant variant = null;
            if (pageinfo.getVariant() != null) {
                variant = this.variantfactory.getVariant(pageinfo.getVariant());
            }
            Page page = project.getPage(pageinfo.getName(), variant);
            if (page != null) {
                pages.add(page);
            }
        }

        return pages;
    }

    public void replaceFile(File newFile) throws EditorIOException,
            EditorSecurityException {
        this.securitymanager.checkEditImage(this);
        File imageFile = new File(this.pathresolver.resolve(this.getPath()));

        // Log change
        this.writeChangeLog();

        synchronized (this.filesystem.getLock(imageFile)) {
            File directory = imageFile.getParentFile();
            if (!directory.exists()) {
                this.filesystem.makeDirectory(directory, true);
            }
            if (imageFile.exists()) {
                this.backup.backupImage(this);
            }
            filesystem.copy(newFile, imageFile);
        }

    }

    private void writeChangeLog() {
        Logger.getLogger("LOGGER_EDITOR").warn(
                "IMG: " + this.securitymanager.getPrincipal().getName() + ": "
                        + this.getPath());
    }

    public long getLastModTime() {
    	return ResourceUtil.getResource(getPath()).lastModified();
    }
}
