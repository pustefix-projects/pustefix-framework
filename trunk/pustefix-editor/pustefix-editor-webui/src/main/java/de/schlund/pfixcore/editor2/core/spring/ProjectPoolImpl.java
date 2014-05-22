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

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import javax.servlet.ServletContext;

import org.pustefixframework.editor.common.dom.Project;
import org.pustefixframework.editor.webui.remote.dom.ProjectImpl;
import org.pustefixframework.editor.webui.remote.dom.util.RemoteServiceUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;
import org.springframework.web.context.ServletContextAware;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.schlund.pfixxml.util.Xml;


public class ProjectPoolImpl implements ProjectPool, ApplicationContextAware, ServletContextAware, InitializingBean {
    
    private final static String CONTEXT_PARAM_EDITOR_LOCATIONS = "editor.locations";
    private final static String DEFAULT_EDITOR_LOCATIONS = "WEB-INF/editor-locations.xml";
    
    private LinkedHashMap<String, Project> locationToProject = new LinkedHashMap<String, Project>();
    private LinkedHashMap<Project, String> projectToLocation = new LinkedHashMap<Project, String>();
    private LinkedHashMap<Project, RemoteServiceUtil> projectToRemoteServiceUtil = new LinkedHashMap<Project, RemoteServiceUtil>();
    private Object mapsLock = new Object();
    private ApplicationContext applicationContext;
    private ServletContext servletContext;
    
    public ProjectPoolImpl() {
    }
    
    public void afterPropertiesSet() throws Exception {
        String editorLocations = servletContext.getInitParameter(CONTEXT_PARAM_EDITOR_LOCATIONS);
        Resource resource;
        if(editorLocations == null) {
            resource = applicationContext.getResource(DEFAULT_EDITOR_LOCATIONS);
        } else {
            resource = applicationContext.getResource(editorLocations);
        }
        loadFromResource(resource);
    }
    
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
    
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }
    
    public Project getProjectForURI(String uri) {
        if (!uri.endsWith("/")) {
            uri = uri + "/";
        }
        synchronized (mapsLock) {
            return locationToProject.get(uri);
        }
    }
    
    public String getURIForProject(Project project) {
        synchronized (mapsLock) {
            return projectToLocation.get(project);
        }
    }
    
    public Collection<Project> getProjects() {
        synchronized (mapsLock) {
            return new LinkedList<Project>(projectToLocation.keySet());
        }
    }
    
    public RemoteServiceUtil getRemoteServiceUtil(Project project) {
        synchronized (mapsLock) {
            return projectToRemoteServiceUtil.get(project);
        }
    }
    
    /**
    public void reloadConfiguration() {
        synchronized (mapsLock) {
            locationToProject.clear();
            projectToLocation.clear();
            projectToRemoteServiceUtil.clear();
            loadFromFile();
        }
    }
    */
    
    private void loadFromResource(Resource resource) {
        Document doc;
        try {
            doc = Xml.parseMutable(resource.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException("Could not read project list from " + resource, e);
        } catch (SAXException e) {
            throw new RuntimeException("Could not read project list from " + resource, e);
        }
        Element docElement = doc.getDocumentElement();
        NodeList projectElements = docElement.getElementsByTagName("project");
        for (int i = 0; i < projectElements.getLength(); i++) {
            Element projectElement = (Element) projectElements.item(i);
            Element locationElement = (Element) projectElement.getElementsByTagName("location").item(0);
            if (locationElement == null) {
                throw new RuntimeException("Could not find location element within project element in file " + resource);
            }
            String location = locationElement.getTextContent();
            Element secretElement = (Element) projectElement.getElementsByTagName("secret").item(0);
            if (secretElement == null) {
                throw new RuntimeException("Could not find secret element within project element in file " + resource);
            }
            String secret = secretElement.getTextContent();
            LinkedList<String> aliasLocations = new LinkedList<String>();
            NodeList aliasElements = projectElement.getElementsByTagName("aliasLocation");
            for (int j= 0; j < aliasElements.getLength(); j++) {
                Element aliasElement = (Element) aliasElements.item(j);
                String aliasLocation = aliasElement.getTextContent();
                aliasLocations.add(aliasLocation);
            }
            registerProject(location, aliasLocations, secret);
        }
    }
    
    private void registerProject(String location, LinkedList<String> aliasLocations, String password) {
        if (!location.endsWith("/")) {
            location = location + "/";
        }
        RemoteServiceUtil remoteServiceUtil = new RemoteServiceUtil(location, password);
        Project project = new ProjectImpl(remoteServiceUtil);
        locationToProject.put(location, project);
        for (String aliasLocation : aliasLocations) {
            locationToProject.put(aliasLocation, project);
        }
        projectToLocation.put(project, location);
        projectToRemoteServiceUtil.put(project, remoteServiceUtil);
    }
    
}
