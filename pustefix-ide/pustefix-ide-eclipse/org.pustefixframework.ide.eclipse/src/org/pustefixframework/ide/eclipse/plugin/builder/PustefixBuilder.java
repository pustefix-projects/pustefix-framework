package org.pustefixframework.ide.eclipse.plugin.builder;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

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
import org.pustefixframework.ide.eclipse.plugin.Activator;
import org.pustefixframework.ide.eclipse.plugin.Environment;
import org.pustefixframework.ide.eclipse.plugin.Logger;
import org.pustefixframework.ide.eclipse.plugin.core.internal.PustefixCore;
import org.pustefixframework.ide.eclipse.plugin.ui.preferences.PreferenceConstants;
import org.pustefixframework.ide.eclipse.plugin.util.PustefixVersion;
import org.pustefixframework.ide.eclipse.plugin.util.VersionCheck;

public class PustefixBuilder extends IncrementalProjectBuilder {

	public static final String BUILDER_ID = "org.pustefixframework.ide.eclipse.plugin.builder.pustefixbuilder";
	
	private static Logger LOG=Activator.getLogger();
	
	private IPreferencesService prefService;
	private IScopeContext[] prefScopes;
	
	private Environment environment;
	private IWrapperGenerator iwrpGenerator;
	private StatusCodeGenerator scodeGenerator;
	private VersionLoader versionLoader;
	
	private boolean initialized;

	private PustefixVersion pustefixVersion;
	
	public PustefixBuilder() {
		
	}
	
	private void init() {
		if(!initialized) {
			prefService=Platform.getPreferencesService();
			prefScopes=new IScopeContext[] {new ProjectScope(getProject()),new InstanceScope(),new DefaultScope()};
			environment=new Environment(prefService,prefScopes);
			try {
                versionLoader = new VersionLoader();
            } catch (IOException e) {
                throw new RuntimeException("Can't get VersionLoader", e);
            }
			checkVersion();
			initialized = true;
		}
	}
	
	public void checkVersion() {
	    PustefixVersion oldVersion = pustefixVersion;
	    pustefixVersion = VersionCheck.getPustefixVersion(getProject());
	    if(pustefixVersion==null) {
	        pustefixVersion = new PustefixVersion();
	        pustefixVersion.setMajorVersion(0);
	        pustefixVersion.setMinorVersion(16);
	        pustefixVersion.setMicroVersion(0);
	    }
	    System.out.println(">>> OLD VERSION: " + oldVersion);
	    System.out.println(">>> NEW VERSION: " + pustefixVersion);
	    if(oldVersion==null || oldVersion.compareTo(pustefixVersion) != 0) {
	        Class<?> clazz = versionLoader.loadClass("StatusCodeGeneratorImpl", pustefixVersion);
	        try {
                scodeGenerator = (StatusCodeGenerator)clazz.newInstance();
            } catch(Exception e) {
                throw new RuntimeException(e);
            }
            URL url = versionLoader.loadResource("iwrapper.xsl", pustefixVersion);
            iwrpGenerator = new IWrapperGenerator(environment, url);
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
		
		try {
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
		} catch(Exception x) {
			//avoid that exceptions in Pustefix build influence main build
			LOG.error(x);
		}
		return null;
		
	}


	private void fullBuild(final IProgressMonitor monitor)
			throws CoreException {
		checkVersion();
		try {
			getProject().accept(new SampleResourceVisitor());
		} catch (CoreException e) {
		}
	}

	private void incrementalBuild(IResourceDelta delta,
			IProgressMonitor monitor) throws CoreException {
		
		if(PustefixCore.getInstance().hasClassPathChanged(getProject())) {
		    checkVersion();
		    PustefixCore.getInstance().classPathChangeHandled(getProject());
		}
		
		boolean iwrpGen=prefService.getBoolean(Activator.PLUGIN_ID,PreferenceConstants.PREF_GENERATEIWRAPPERS,false,prefScopes);
		if(iwrpGen) iwrpGenerator.incrementalBuild(delta,monitor);
		boolean scodeGen=prefService.getBoolean(Activator.PLUGIN_ID,PreferenceConstants.PREF_GENERATESTATUSCODES,false,prefScopes);
		if(scodeGen) scodeGenerator.incrementalBuild(environment, delta, monitor);
		
	}
	
}
