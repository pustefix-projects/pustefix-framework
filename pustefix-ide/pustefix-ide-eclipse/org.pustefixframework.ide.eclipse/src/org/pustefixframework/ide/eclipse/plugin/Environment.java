package org.pustefixframework.ide.eclipse.plugin;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;

import org.pustefixframework.ide.eclipse.plugin.ui.preferences.PreferenceConstants;

public class Environment implements IPreferenceChangeListener {

	private IPreferencesService service;
	private IScopeContext[] scopes;
	
	private IPath iwrpSrcDir;
	private IPath iwrpTargetDir;
	
	private IPath scodeSrcDirWebapp;
	private IPath scodeSrcDirModule;
	private IPath scodeTargetDir;
	
	public Environment(IPreferencesService service,IScopeContext[] scopes) {
		this.service=service;
		this.scopes=scopes;
		loadPreferences();
		for(IScopeContext context:scopes) {
			IEclipsePreferences node=context.getNode(Activator.PLUGIN_ID);
			if(node!=null) node.addPreferenceChangeListener(this);
		}
	}
	
	public void preferenceChange(PreferenceChangeEvent event) {
		loadPreferences();
	}
	
	private void loadPreferences() {
		
	    iwrpSrcDir = new Path(service.getString(Activator.PLUGIN_ID, PreferenceConstants.PREF_IWRPSRCDIR, null, scopes));
	    iwrpTargetDir = new Path(service.getString(Activator.PLUGIN_ID, PreferenceConstants.PREF_IWRPTARGETDIR, null, scopes));
	      
	    scodeSrcDirWebapp = new Path(service.getString(Activator.PLUGIN_ID, PreferenceConstants.PREF_SCODESRCDIR_WEBAPP, null, scopes));
	    scodeSrcDirModule = new Path(service.getString(Activator.PLUGIN_ID, PreferenceConstants.PREF_SCODESRCDIR_MODULE, null, scopes));
	    scodeTargetDir = new Path(service.getString(Activator.PLUGIN_ID, PreferenceConstants.PREF_SCODETARGETDIR, null, scopes));
	    
	}
	
	public IPath getIWrapperSourceDir() {
		return iwrpSrcDir;
	}
	
	public IPath getIWrapperTargetDir() {
	    return iwrpTargetDir;
	}
	
	public IPath getStatusCodeSourceDirForWebapp() {
		return scodeSrcDirWebapp;
	}
	
	public IPath getStatusCodeSourceDirForModule() {
	    return scodeSrcDirModule;
	}
	
	public IPath getStatusCodeTargetDir() {
	    return scodeTargetDir;
	}

}
