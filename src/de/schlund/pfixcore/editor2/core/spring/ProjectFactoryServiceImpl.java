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
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import de.schlund.pfixcore.editor2.core.dom.Project;
import de.schlund.pfixcore.editor2.core.exception.EditorInitializationException;
import de.schlund.pfixcore.editor2.core.spring.internal.ProjectImpl;
import de.schlund.pfixxml.targets.TargetGenerator;
import de.schlund.pfixxml.util.XPath;

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

    private String projectsFile;

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

    public void setProjectsFilePath(String path) {
        this.projectsFile = path;
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

        File prjFile = new File(pathresolver.resolve(projectsFile));
        Document doc;
        synchronized (filesystem.getLock(prjFile)) {
            doc = filesystem.readXMLDocumentFromFile(new File(pathresolver
                    .resolve(projectsFile)));
        }
        List projectNodes = XPath.select(doc.getDocumentElement(),
                "project[servlet/@useineditor='true']");
        for (Iterator i = projectNodes.iterator(); i.hasNext();) {
            Element projectElement = (Element) i.next();
            String projectName = "<unknown>";
            try {
                if (!projectElement.hasAttribute("name")) {
                    String err = "<project>-tag needs name attribute!";
                    Logger.getLogger(this.getClass()).error(err);
                    throw new EditorInitializationException(err);
                }
                projectName = projectElement.getAttribute("name");
                Node tempNode;
                tempNode = XPath.selectNode(projectElement, "comment/text()");
                if (tempNode == null) {
                    String err = "Project " + projectName
                            + " does not have mandatory <comment> element!";
                    Logger.getLogger(this.getClass()).error(err);
                    throw new EditorInitializationException(err);
                }
                String projectComment = tempNode.getNodeValue();
                tempNode = XPath.selectNode(projectElement, "depend/text()");
                if (tempNode == null) {
                    String err = "Project " + projectName
                        + " does not have mandatory <depend> element!";
                    Logger.getLogger(this.getClass()).error(err);
                    throw new EditorInitializationException(err);
                }
                String projectDependFile = tempNode.getNodeValue();
                
                ProjectImpl project = new ProjectImpl(variantfactory, themefactory, pagefactory, includefactory, imagefactory, targetfactory, updater, projectName, projectComment, projectDependFile);
                this.projects.put(projectName, project);
                this.generatorToProjectNameMap.put(project.getTargetGenerator().getName(), projectName);
            } catch (Throwable e) {
                LOG.error("Initialization of project " + projectName + " failed!", e);
            }
        }
        this.initialized = true;
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
