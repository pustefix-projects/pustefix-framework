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

import org.pustefixframework.container.annotations.Inject;
import org.pustefixframework.editor.backend.config.EditorProjectInfo;
import org.pustefixframework.editor.common.dom.Project;
import org.pustefixframework.editor.common.exception.EditorInitializationException;

import de.schlund.pfixcore.editor2.core.spring.internal.ProjectImpl;
import de.schlund.pfixxml.targets.TargetGenerator;

/**
 * Implementation using the configured projects.xml file to retrieve a list fo
 * all projects.
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class ProjectFactoryServiceImpl implements ProjectFactoryService {
    
    private VariantFactoryService variantfactory;

    private PageFactoryService pagefactory;

    private ThemeFactoryService themefactory;

    private boolean initialized = false;

    private IncludeFactoryService includefactory;
    
    private DynIncludeFactoryService dynincludefactory;

    private ImageFactoryService imagefactory;

    private PustefixTargetUpdateService updater;

    private TargetFactoryService targetfactory;
    
    private ConfigurationService configuration;
    
    private EditorProjectInfo editorProjectInfo;
    
    private TargetGenerator targetGenerator;
    
    private ProjectImpl project;

    @Inject
    public void setEditorProjectInfo(EditorProjectInfo editorProjectInfo) {
        this.editorProjectInfo = editorProjectInfo;
    }
    
    @Inject
    public void setTargetGenerator(TargetGenerator targetGenerator) {
        this.targetGenerator = targetGenerator;
    }
    
    public void setThemeFactoryService(ThemeFactoryService themefactory) {
        this.themefactory = themefactory;
    }

    public void setVariantFactoryService(VariantFactoryService variantfactory) {
        this.variantfactory = variantfactory;
    }

    public void setPageFactoryService(PageFactoryService pagefactory) {
        this.pagefactory = pagefactory;
    }

    public void setIncludeFactoryService(IncludeFactoryService includefactory) {
        this.includefactory = includefactory;
    }

    public void setDynIncludeFactoryService(DynIncludeFactoryService dynincludefactory) {
        this.dynincludefactory = dynincludefactory;
    }

    public void setImageFactoryService(ImageFactoryService imagefactory) {
        this.imagefactory = imagefactory;
    }
    
    public void setTargetFactoryService(TargetFactoryService targetfactory) {
        this.targetfactory = targetfactory;
    }

    public void setConfigurationService(ConfigurationService configuration) {
        this.configuration = configuration;
    }
    
    public void setPustefixTargetUpdateService(
            PustefixTargetUpdateService updater) {
        this.updater = updater;
    }

    public void init() throws EditorInitializationException {
        String projectName = editorProjectInfo.getName();
        if (projectName == null) {
            throw new EditorInitializationException("Mandatory <name> is not set for <project>.");
        }
        String projectDescription = editorProjectInfo.getDescription();
        if (projectDescription == null) {
            throw new EditorInitializationException("Mandatory <description> is not set for <project>.");
        }
        
        this.project = new ProjectImpl(variantfactory, themefactory, pagefactory, includefactory, dynincludefactory, imagefactory, targetfactory, configuration, updater, projectName, projectDescription, editorProjectInfo.isIncludePartsEditableByDefault(), targetGenerator);
        
        this.initialized = true;
    }
    
    private void checkInitialized() {
        if (!this.initialized) {
            throw new RuntimeException(
                    "Service has to be initialized before use!");
        }
    }
    
    public Project getProject() {
        checkInitialized();
        return project;
    }

}
