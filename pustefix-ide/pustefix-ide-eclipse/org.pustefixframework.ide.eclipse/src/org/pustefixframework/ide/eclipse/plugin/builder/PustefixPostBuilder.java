package org.pustefixframework.ide.eclipse.plugin.builder;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jdt.core.JavaCore;

import org.pustefixframework.ide.eclipse.plugin.Environment;
import org.pustefixframework.ide.eclipse.plugin.core.ProjectModel;
import org.pustefixframework.ide.eclipse.plugin.core.PustefixModel;
import org.pustefixframework.ide.eclipse.plugin.util.ResourceUtils;

public class PustefixPostBuilder extends IncrementalProjectBuilder {

	public static final String BUILDER_ID = "org.pustefixframework.ide.eclipse.plugin.pustefixPostBuilder";
	
	private boolean initialized;
	
	private IPreferencesService prefService;
	private IScopeContext[] prefScopes;
	
	private Environment environment;
	private ServletConfigValidator servletConfigValidator;
	
	private PustefixModel model;
	
	public PustefixPostBuilder() {
		
	}
	
	private void init() {
		if(!initialized) {
			prefService=Platform.getPreferencesService();
			prefScopes=new IScopeContext[] {new ProjectScope(getProject()),new InstanceScope(),new DefaultScope()};
			environment=new Environment(prefService,prefScopes);
			servletConfigValidator=new ServletConfigValidator(environment,JavaCore.create(getProject()));
			model=PustefixModel.createModel(getProject());
			initialized=true;
		}
	}
	
	class SampleResourceVisitor implements IResourceVisitor {
		public boolean visit(IResource resource) {
			//iwrpGenerator.build(resource);
			//scodeGenerator.build(resource);
			//return true to continue visiting children.
			return true;
		}
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.internal.events.InternalBuilder#build(int,
	 *      java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
			throws CoreException {
	    /**
		init();
		if (kind == FULL_BUILD) {
			fullBuild(monitor);
		} else {
			IResourceDelta delta = getDelta(getProject());
			if (delta == null) {
				fullBuild(monitor);
			} else {
				incrementalBuild(delta, monitor);
			}
		}
		*/
		return null;
	}


	private void fullBuild(final IProgressMonitor monitor)
			throws CoreException {
		try {
			getProject().accept(new SampleResourceVisitor());
		} catch (CoreException e) {
		}
	}

	private void incrementalBuild(IResourceDelta delta,
			IProgressMonitor monitor) throws CoreException {

		Iterator<ProjectModel> projectModels=model.getProjectModels().values().iterator();
		while(projectModels.hasNext()) {
			ProjectModel projectModel=projectModels.next();
			List<IFile> configFiles=projectModel.getServletConfigFiles();
			for(IFile configFile:configFiles) {
				if(ResourceUtils.hasProblemMarker(configFile)) {
						servletConfigValidator.validate(configFile);
				}
			}	
		}
		
		servletConfigValidator.incrementalBuild(delta,monitor);
		
	}
	
	
	
}
