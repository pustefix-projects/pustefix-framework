package org.pustefixframework.ide.eclipse.plugin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.pustefixframework.ide.eclipse.plugin.builder.PustefixBuilder;
import org.pustefixframework.ide.eclipse.plugin.builder.PustefixPostBuilder;

public class PustefixNature implements IProjectNature {

	public static final String NATURE_ID = "org.pustefixframework.ide.eclipse.plugin.pustefixnature";

	private IProject project;

	public PustefixNature() {

	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.resources.IProjectNature#configure()
	 */
	public void configure() throws CoreException {
		IProjectDescription desc = project.getDescription();
		ICommand[] commands = desc.getBuildSpec();

		for (int i = 0; i < commands.length; ++i) {
			if (commands[i].getBuilderName().equals(PustefixBuilder.BUILDER_ID)) {
				return;
			}
		}

		ICommand[] newCommands = new ICommand[commands.length + 2];
		System.arraycopy(commands, 0, newCommands, 1, commands.length);
		//Prebuilder
		ICommand command = desc.newCommand();
		command.setBuilderName(PustefixBuilder.BUILDER_ID);
		newCommands[0] = command;
		//Postbuilder
		command = desc.newCommand();
		command.setBuilderName(PustefixPostBuilder.BUILDER_ID);
		newCommands[newCommands.length-1] = command;
		desc.setBuildSpec(newCommands);
		project.setDescription(desc, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.resources.IProjectNature#deconfigure()
	 */
	public void deconfigure() throws CoreException {
		
		removeOldMarkers();
		removeBuilder();
		
	}
	
	
	/**
	 * Remove builders from project description
	 * 
	 * @throws CoreException
	 */
	private void removeBuilder() throws CoreException {
		IProjectDescription description = getProject().getDescription();
		ICommand[] commands = description.getBuildSpec();
		List<ICommand> cmds=new ArrayList<ICommand>();
		for (int i = 0; i < commands.length; ++i) {
			if (!(commands[i].getBuilderName().equals(PustefixBuilder.BUILDER_ID)||
					commands[i].getBuilderName().equals(PustefixPostBuilder.BUILDER_ID))) {
				cmds.add(commands[i]);
			}
		}
		ICommand[] newCommands = new ICommand[cmds.size()];
		cmds.toArray(newCommands);
		description.setBuildSpec(newCommands);
		project.setDescription(description, null);
	}
	
	/**
	 * Remove markers from plugin version < 0.3
	 */
	private void removeOldMarkers() {
		Set<String> markerSubTypes=new HashSet<String>();
		markerSubTypes.add("iwrapper");
		markerSubTypes.add("config");
		markerSubTypes.add("statuscode");
		try {
			IMarker[] markers=getProject().findMarkers(IMarker.PROBLEM,false,IResource.DEPTH_INFINITE);
			for(IMarker marker:markers) {
				String value=(String)marker.getAttribute("subtype");
				if(value!=null && markerSubTypes.contains(value)) marker.delete();
			}
		} catch (CoreException e) {}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.resources.IProjectNature#getProject()
	 */
	public IProject getProject() {
		return project;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.resources.IProjectNature#setProject(org.eclipse.core.resources.IProject)
	 */
	public void setProject(IProject project) {
		this.project = project;
	}
	
	
	

}
