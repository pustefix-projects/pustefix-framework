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

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.pustefixframework.config.Constants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import de.schlund.pfixcore.editor2.core.dom.Project;
import de.schlund.pfixcore.editor2.core.exception.EditorInitializationException;
import de.schlund.pfixcore.editor2.core.spring.internal.ProjectImpl;
import de.schlund.pfixxml.targets.TargetGenerator;

/**
 * Implementation using the configured projects.xml file to retrieve a list fo
 * all projects.
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class ProjectFactoryServiceImpl implements ProjectFactoryService {
    private final static Logger LOG = Logger.getLogger(ProjectFactoryServiceImpl.class);
    
    private PathResolverService pathresolver;

    private VariantFactoryService variantfactory;

    private PageFactoryService pagefactory;

    private HashMap<String, Project> projects;

    private FileSystemService filesystem;

    private ThemeFactoryService themefactory;

    private boolean initialized;

    private HashMap<String, String> generatorToProjectNameMap;

    private IncludeFactoryService includefactory;

    private ImageFactoryService imagefactory;

    private PustefixTargetUpdateService updater;

    private TargetFactoryService targetfactory;

    public void setPathResolverService(PathResolverService pathresolver) {
        this.pathresolver = pathresolver;
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

    public void setFileSystemService(FileSystemService filesystem) {
        this.filesystem = filesystem;
    }

    public void setIncludeFactoryService(IncludeFactoryService includefactory) {
        this.includefactory = includefactory;
    }

    public void setImageFactoryService(ImageFactoryService imagefactory) {
        this.imagefactory = imagefactory;
    }
    
    public void setTargetFactoryService(TargetFactoryService targetfactory) {
        this.targetfactory = targetfactory;
    }

    public void setPustefixTargetUpdateService(
            PustefixTargetUpdateService updater) {
        this.updater = updater;
    }

    public ProjectFactoryServiceImpl() {
        this.projects = new HashMap<String, Project>();
        this.generatorToProjectNameMap = new HashMap<String, String>();
        this.initialized = false;
    }

    public void init() throws SAXException, IOException,
            ParserConfigurationException, FactoryConfigurationError,
            TransformerException, EditorInitializationException {

        /*
         * Document doc = DocumentBuilderFactory.newInstance()
         * .newDocumentBuilder().parse( new
         * File(pathresolver.resolve(projectsFile)));
         */

        Collection<File> projectFiles = findProjectFiles();
        for (File projectFile : projectFiles) {
            Document doc;
            synchronized (filesystem.getLock(projectFile)) {
                doc = filesystem.readCustomizedXMLDocumentFromFile(projectFile, Constants.NS_PROJECT);
            }
            Element rootElement = doc.getDocumentElement();
            Element projectElement = (Element) rootElement.getElementsByTagNameNS(Constants.NS_PROJECT, "project").item(0);
            Element nameElement = null;
            Element descriptionElement = null;
            if (projectElement != null) {
                nameElement = (Element) projectElement.getElementsByTagNameNS(Constants.NS_PROJECT, "name").item(0);
                descriptionElement = (Element) projectElement.getElementsByTagNameNS(Constants.NS_PROJECT, "description").item(0);
            }
            Element editorElement = (Element) rootElement.getElementsByTagNameNS(Constants.NS_PROJECT, "editor").item(0);
            Element editorEnabledElement = null;
            if (editorElement != null) {
                editorEnabledElement = (Element) editorElement.getElementsByTagNameNS(Constants.NS_PROJECT, "enabled").item(0);
            }
            Element generatorElement = (Element) rootElement.getElementsByTagNameNS(Constants.NS_PROJECT, "xml-generator").item(0);
            Element generatorConfigElement = null;
            if (generatorElement != null) {
                generatorConfigElement = (Element) generatorElement.getElementsByTagNameNS(Constants.NS_PROJECT, "config-file").item(0);
            }
            String projectName = "<unknown>";
            try {
                if (nameElement == null) {
                    String err = "<name>-tag missing in file " + projectFile + "";
                    Logger.getLogger(this.getClass()).error(err);
                    throw new EditorInitializationException(err);
                }
                projectName = nameElement.getTextContent().trim();
                if (descriptionElement == null) {
                    String err = "Project " + projectName
                            + " does not have mandatory <description> element!";
                    Logger.getLogger(this.getClass()).error(err);
                    throw new EditorInitializationException(err);
                }
                String projectComment = descriptionElement.getTextContent().trim();
                if (generatorConfigElement == null) {
                    String err = "Project " + projectName
                        + " does not have mandatory <xml-generator>/<config-file> element!";
                    Logger.getLogger(this.getClass()).error(err);
                    throw new EditorInitializationException(err);
                }
                if (editorEnabledElement != null) {
                    if (editorEnabledElement.getTextContent().trim().equals("false")) {
                        // Do not load this project
                        continue;
                    }
                }
                String projectDependFile = generatorConfigElement.getTextContent().trim();
                
                ProjectImpl project = new ProjectImpl(variantfactory, themefactory, pagefactory, includefactory, imagefactory, targetfactory, updater, projectName, projectComment, projectDependFile);
                this.projects.put(projectName, project);
                this.generatorToProjectNameMap.put(project.getTargetGenerator().getName(), projectName);
            } catch (Throwable e) {
                LOG.error("Initialization of project " + projectName + " failed!", e);
            }
        }
        this.initialized = true;
    }

    private Collection<File> findProjectFiles() {
        File docroot = new File(pathresolver.resolve(""));
        return findProjectFiles(docroot);
    }
    
    private Collection<File> findProjectFiles(File directory) {
        HashSet<File> files = new HashSet<File>();
        
        FileFilter directoryFilter = new FileFilter() {

            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
            
        };
        
        File[] directories = directory.listFiles(directoryFilter);
        for (File subDirectory : directories) {
            files.addAll(findProjectFiles(subDirectory));
        }
        
        if (directory.getName().equals("conf")) {
            File projectFile = new File(directory, "project.xml");
            if (projectFile.exists()) {
                files.add(projectFile);
            }
        }
        
        return files;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.schlund.pfixcore.editor2.core.spring.ProjectFactoryService#getProjectByName(java.lang.String)
     */
    public Project getProjectByName(String projectName) {
        checkInitialized();
        return (Project) this.projects.get(projectName);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.schlund.pfixcore.editor2.core.spring.ProjectFactoryService#getProjects()
     */
    public Collection<Project> getProjects() {
        checkInitialized();
        return new HashSet<Project>(this.projects.values());
    }

    private void checkInitialized() {
        if (!this.initialized) {
            throw new RuntimeException(
                    "Service has to be initialized before use!");
        }
    }

    public Project getProjectByPustefixTargetGenerator(TargetGenerator tgen) {
        this.checkInitialized();
        return this.getProjectByName((String) this.generatorToProjectNameMap
                .get(tgen.getName()));
    }

    public Project getProjectByPustefixTargetGeneratorName(
            String targetGenerator) {
        this.checkInitialized();
        return this.getProjectByName((String) this.generatorToProjectNameMap
                .get(targetGenerator));
    }

}
