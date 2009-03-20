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

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import org.pustefixframework.editor.common.dom.Project;
import org.pustefixframework.editor.webui.remote.dom.ProjectImpl;
import org.pustefixframework.editor.webui.remote.dom.util.RemoteServiceUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.schlund.pfixxml.resources.FileResource;
import de.schlund.pfixxml.resources.ResourceUtil;
import de.schlund.pfixxml.util.MD5Utils;
import de.schlund.pfixxml.util.Xml;


public class ProjectPoolImpl implements ProjectPool {
    private LinkedHashMap<String, Project> locationToProject = new LinkedHashMap<String, Project>();
    private LinkedHashMap<String, Project> idToProject = new LinkedHashMap<String, Project>();
    private LinkedHashMap<Project, String> projectToId = new LinkedHashMap<Project, String>();
    private LinkedHashMap<Project, RemoteServiceUtil> projectToRemoteServiceUtil = new LinkedHashMap<Project, RemoteServiceUtil>();
    private Object mapsLock = new Object();
    
    public ProjectPoolImpl() {
        loadFromFile();
    }
    
    public Project getProjectById(String projectId) {
        synchronized (mapsLock) {
            return idToProject.get(projectId);
        }
    }
    
    public Project getProjectByURI(String uri) {
        if (!uri.endsWith("/")) {
            uri = uri + "/";
        }
        synchronized (mapsLock) {
            return locationToProject.get(uri);
        }
    }
    
    public Collection<Project> getProjects() {
        synchronized (mapsLock) {
            return new LinkedList<Project>(projectToId.keySet());
        }
    }
    
    public RemoteServiceUtil getRemoteServiceUtil(Project project) {
        synchronized (mapsLock) {
            return projectToRemoteServiceUtil.get(project);
        }
    }
    
    public void reloadConfiguration() {
        synchronized (mapsLock) {
            locationToProject.clear();
            idToProject.clear();
            projectToId.clear();
            projectToRemoteServiceUtil.clear();
            loadFromFile();
        }
    }
    
    private void loadFromFile() {
        FileResource file = ResourceUtil.getFileResourceFromDocroot("conf/editor-locations.xml");
        Document doc;
        try {
            doc = Xml.parseMutable(file);
        } catch (IOException e) {
            throw new RuntimeException("Could not read project list from " + file, e);
        } catch (SAXException e) {
            throw new RuntimeException("Could not read project list from " + file, e);
        }
        Element docElement = doc.getDocumentElement();
        NodeList projectElements = docElement.getElementsByTagName("project");
        for (int i = 0; i < projectElements.getLength(); i++) {
            Element projectElement = (Element) projectElements.item(i);
            Element locationElement = (Element) projectElement.getElementsByTagName("location").item(0);
            if (locationElement == null) {
                throw new RuntimeException("Could not find location element within project element in file " + file);
            }
            String location = locationElement.getTextContent();
            Element secretElement = (Element) projectElement.getElementsByTagName("secret").item(0);
            if (secretElement == null) {
                throw new RuntimeException("Could not find secret element within project element in file " + file);
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
        String id = generateId(location);
        Project project = new ProjectImpl(remoteServiceUtil);
        locationToProject.put(location, project);
        for (String aliasLocation : aliasLocations) {
            locationToProject.put(aliasLocation, project);
        }
        idToProject.put(id, project);
        projectToId.put(project, id);
        projectToRemoteServiceUtil.put(project, remoteServiceUtil);
    }
    
    private String generateId(String location) {
        return MD5Utils.hex_md5(location);
    }


    
}
