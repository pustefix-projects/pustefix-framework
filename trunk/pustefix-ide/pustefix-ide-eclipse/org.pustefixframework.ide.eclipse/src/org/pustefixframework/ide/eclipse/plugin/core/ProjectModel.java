package org.pustefixframework.ide.eclipse.plugin.core;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import org.pustefixframework.ide.eclipse.plugin.Activator;
import org.pustefixframework.ide.eclipse.plugin.Logger;

public class ProjectModel {

	private static Logger LOG=Activator.getLogger();
	
	private String projectName;
	private IFolder projectFolder;
	private List<IFile> servletConfigFiles;
	
	public ProjectModel(String projectName,IFolder projectFolder) {
		this.projectName=projectName;
		this.projectFolder=projectFolder;
	}
	
	public String getProjectName() {
		return projectName;
	}
	
	public IFolder getProjectFolder() {
		return projectFolder;
	}
	
	public List<IFile> getServletConfigFiles() {
		if(servletConfigFiles==null) {
			servletConfigFiles=new ArrayList<IFile>();
			try {
				IFolder configFolder=projectFolder.getFolder("conf");
				if(configFolder.exists()) {
					IResource[] resources=configFolder.members();
					for(IResource resource:resources) {
						if(resource.getType()==IResource.FILE && 
								resource.getName().endsWith(".conf.xml")) {
							servletConfigFiles.add((IFile)resource);
						}
					}
				}
			} catch(CoreException x) {
				LOG.error(x);
			}
		}
		return servletConfigFiles;
	}
	
}
