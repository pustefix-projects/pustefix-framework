package org.pustefixframework.ide.eclipse.plugin.core;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;

import org.pustefixframework.ide.eclipse.plugin.Activator;
import org.pustefixframework.ide.eclipse.plugin.Logger;
import org.pustefixframework.ide.eclipse.plugin.ui.preferences.PreferenceConstants;
import org.pustefixframework.ide.eclipse.plugin.ui.util.SettingsStore;

public class PustefixModel {

	private static Logger LOG=Activator.getLogger();
	
	private static Map<String,PustefixModel> models=new HashMap<String,PustefixModel>();
	
	public static PustefixModel createModel(IProject project) {
		PustefixModel model=models.get(project.getName());
		if(model==null) {
			model=new PustefixModel(project);
			models.put(project.getName(),model);
		}
		return model;
	}
	
	private IFolder projectFolder;
	private Map<String,ProjectModel> projectModels;
	
	private PustefixModel(IProject project) {
		SettingsStore store=new SettingsStore(Activator.PLUGIN_ID,project);
//		String str=store.getCascadedProjectValue(PreferenceConstants.PREF_PROJECTDIR,"");
//		projectFolder=project.getFolder(new Path(str));
//		projectModels=new HashMap<String,ProjectModel>();
//		if(projectFolder.exists()) {
//			try {
//				IResource[] members=projectFolder.members(false);
//				for(IResource member:members) {
//					if(member.getType()==IResource.FOLDER) {
//						IFolder folder=(IFolder)member;
//						IFile file=folder.getFile("conf/project.xml.in");
//						if(file.exists()) {
//							String name=folder.getName();
//							ProjectModel model=new ProjectModel(name,folder);
//							projectModels.put(name,model);
//						}
//					}
//				}
//			} catch(CoreException x) {
//				LOG.error(x);
//			}
//		}
	}
	
	public Map<String,ProjectModel> getProjectModels() {
		return projectModels;
	}
			
}
