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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import de.schlund.pfixcore.editor2.core.dom.IncludeFile;
import de.schlund.pfixcore.editor2.core.dom.IncludePart;
import de.schlund.pfixcore.editor2.core.dom.IncludePartThemeVariant;
import de.schlund.pfixcore.editor2.core.dom.Theme;
import de.schlund.pfixcore.editor2.core.exception.EditorParsingException;
import de.schlund.pfixcore.editor2.core.spring.internal.IncludeFileImpl;
import de.schlund.pfixcore.editor2.core.spring.internal.IncludePartThemeVariantImpl;
import de.schlund.pfixxml.targets.AuxDependency;
import de.schlund.pfixxml.targets.AuxDependencyInclude;
import de.schlund.pfixxml.targets.DependencyType;

/**
 * Implementation of IncludeFactoryService using Pustefix IncludeDocumentFactory
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class IncludeFactoryServiceImpl implements IncludeFactoryService {
    private Map<String, IncludeFile> cache;
    
    private Map<AuxDependency, IncludePartThemeVariant> auxdepMap;

    private ThemeFactoryService themefactory;

    private ProjectFactoryService projectfactory;

    private VariantFactoryService variantfactory;

    private IncludeFactoryService includefactory;

    private ImageFactoryService imagefactory;

    private PathResolverService pathresolver;

    private FileSystemService filesystem;

    private ConfigurationService configuration;

    private SecurityManagerService securitymanager;

    private BackupService backup;

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

    public void setConfigurationService(ConfigurationService configuration) {
        this.configuration = configuration;
    }

    public void setSecurityManagerService(SecurityManagerService securitymanager) {
        this.securitymanager = securitymanager;
    }

    public void setBackupService(BackupService backup) {
        this.backup = backup;
    }

    public IncludeFactoryServiceImpl() {
        this.cache = Collections
                .synchronizedMap(new HashMap<String, IncludeFile>());
        this.auxdepMap = new HashMap<AuxDependency, IncludePartThemeVariant>();
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.schlund.pfixcore.editor2.core.spring.IncludeFactoryService#createIncludeFile(java.lang.String)
     */
    public IncludeFile getIncludeFile(String filename)
            throws EditorParsingException {
        if (cache.containsKey(filename)) {
            return (IncludeFile) cache.get(filename);
        }

        synchronized (cache) {
            if (!cache.containsKey(filename)) {
                IncludeFile incfile = new IncludeFileImpl(themefactory,
                        includefactory, this.projectfactory, this.filesystem, this.pathresolver,
                        this.backup, this.securitymanager, filename);
                cache.put(filename, incfile);
            }
        }
        return (IncludeFile) cache.get(filename);
    }

    private IncludePartThemeVariant getIncludePartThemeVariant(Theme theme,
            IncludePart part) {
        return new IncludePartThemeVariantImpl(this.projectfactory,
                this.variantfactory, this.includefactory, this.themefactory,
                this.imagefactory, this.filesystem, this.pathresolver,
                this.configuration, this.securitymanager, this.backup, theme,
                part);
    }

    public IncludePartThemeVariant getIncludePartThemeVariant(
            AuxDependency auxdep) throws EditorParsingException {
        if (auxdep.getType() == DependencyType.TEXT) {
            AuxDependencyInclude aux = (AuxDependencyInclude) auxdep;
            synchronized (this.auxdepMap) {
                IncludePartThemeVariant variant = this.auxdepMap.get(aux);
                if (variant != null) {
                    return variant;
                }
                variant = getIncludePartThemeVariant(this.themefactory.getTheme(aux.getTheme()), this.includefactory.getIncludeFile(aux.getPath().toURI().toString()).createPart(aux.getPart()));
                this.auxdepMap.put(aux, variant);
                return variant;
            }
        } else {
            String err = "Supplied AuxDependency is not of type DependencyType.TEXT!";
            Logger.getLogger(this.getClass()).error(err);
            throw new RuntimeException(err);
        }
    }

}
