package org.pustefixframework.ide.eclipse.plugin.ui.util;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.BackingStoreException;
import org.pustefixframework.ide.eclipse.plugin.Activator;
import org.pustefixframework.ide.eclipse.plugin.Logger;


public class SettingsStore {

	private static Logger LOG=Activator.getLogger();
	
	private String qualifier;
	private IProject project;
	private IPreferencesService service;
	private IScopeContext[] contexts;
	private IScopeContext[] defaultContexts;
	
	private IEclipsePreferences defaultNode = null;
	private IEclipsePreferences instanceNode = null;
	private IEclipsePreferences projectNode = null;
	
	public SettingsStore(String qualifier,IProject project) {
		this.qualifier=qualifier;
		this.project=project;
		service=Platform.getPreferencesService();
		IScopeContext context=null;
		if(project!=null) context=new ProjectScope(project);
		else context=new InstanceScope();
		contexts=new IScopeContext[] {context};
		defaultContexts=new IScopeContext[] {new DefaultScope()};
	}
	
	public String getStringValue(String key) {
		return service.getString(qualifier,key,"",contexts);
	}
	
	public String getStringDefaultValue(String key) {
		return service.getString(qualifier,key,"",defaultContexts);
	}
	
	public boolean getBooleanValue(String key) {
		return service.getBoolean(qualifier,key,false,contexts);
	}
	
	public boolean getBooleanDefaultValue(String key) {
		return service.getBoolean(qualifier,key,false,defaultContexts);
	}
	
	
	
	//DEFAULT PREFERENCES
	
	public String getDefaultValue(String key,String defaultValue) {
		String value=null;
		if(defaultNode==null) {	
			IScopeContext defaultContext=new DefaultScope();
			defaultNode=defaultContext.getNode(qualifier);
		}
		if(defaultNode!=null) value=defaultNode.get(key,defaultValue);
		return value;
	}
	
	public boolean hasDefaultValue(String key) {
		return getDefaultValue(key,null)!=null;
	}
	
	public boolean getDefaultValue(String key,boolean defaultValue) {
		String strValue=getDefaultValue(key,null);
		if(strValue==null) return defaultValue;
		else return Boolean.valueOf(strValue);
	}
	
	//INSTANCE PREFERENCES
	
	public String getInstanceValue(String key,String defaultValue) {
		String value=null;
		if(instanceNode==null) {	
			IScopeContext defaultContext=new InstanceScope();
			instanceNode=defaultContext.getNode(qualifier);
		}
		if(instanceNode!=null) value=instanceNode.get(key,defaultValue);
		return value;
	}
	
	public boolean hasInstanceValue(String key) {
		return getInstanceValue(key,null)!=null;
	}
	
	public boolean getInstanceValue(String key,boolean defaultValue) {
		String strValue=getInstanceValue(key,null);
		if(strValue==null) return defaultValue;
		else return Boolean.valueOf(strValue);
	}
	
	public void setInstanceValue(String key,String value) {
		if(instanceNode==null) {	
			IScopeContext defaultContext=new InstanceScope();
			instanceNode=defaultContext.getNode(qualifier);
		}
		if(instanceNode!=null) {
			String defaultValue=getDefaultValue(key,null);
			String oldValue=getInstanceValue(key,null);
			try {
				if(value==null) {
					if(oldValue!=null) {
						instanceNode.remove(key);
						instanceNode.flush();
					}
				} else {
					if(defaultValue!=null && defaultValue.equals(value) && oldValue!=null) {
						instanceNode.remove(key);
						instanceNode.flush();
					} else if(oldValue==null || !oldValue.equals(value)) {
						instanceNode.put(key,value);
						instanceNode.flush();
					}
				}
			} catch(BackingStoreException x) {
				LOG.error(x);
			}
		}
	}
	
	public void setInstanceValue(String key,boolean value) {
		setInstanceValue(key,Boolean.toString(value));
	}
	
	//PROJECT PREFERENCES
	
	public String getProjectValue(String key,String defaultValue) {
		String value=null;
		if(projectNode==null) {	
			IScopeContext defaultContext=new ProjectScope(project);
			projectNode=defaultContext.getNode(qualifier);
		}
		if(projectNode!=null) value=projectNode.get(key,defaultValue);
		return value;
	}
	
	public boolean hasProjectValue(String key) {
		return getProjectValue(key,null)!=null;
	}
	
	public boolean getProjectValue(String key,boolean defaultValue) {
		String strValue=getProjectValue(key,null);
		if(strValue==null) return defaultValue;
		else return Boolean.valueOf(strValue);
	}
	
	public void setProjectValue(String key,String value) {
		if(projectNode==null) {
			IScopeContext projectContext=new ProjectScope(project);
			projectNode=projectContext.getNode(qualifier);
		}
		try {	
			if(projectNode!=null && projectNode.nodeExists("")) {
				String oldValue=getProjectValue(key,null);
				if(value==null) {
					if(oldValue!=null) {
						projectNode.remove(key);
						projectNode.flush();
					}
				} else {
					if(oldValue==null || !oldValue.equals(value)) {
						projectNode.put(key,value);
						projectNode.flush();
					}
				}
			}
		} catch(BackingStoreException x) {
			LOG.error(x);
		}
	}
	
	public void removeProjectValue(String key) {
		setProjectValue(key,null);
	}
	
	public void setProjectValue(String key,boolean value) {
		setProjectValue(key,Boolean.toString(value));
	}
	
	//CASCADED INSTANCE PREFERENCES
	
	public String getCascadedInstanceValue(String key,String defaultValue) {
		String value=getInstanceValue(key,null);
		if(value==null) value=getDefaultValue(key,defaultValue);
		return value;
	}
	
	public boolean getCascadedInstanceValue(String key,boolean defaultValue) {
		boolean value=false;
		if(hasInstanceValue(key)) value=getInstanceValue(key,defaultValue);
		else value=getDefaultValue(key,defaultValue);
		return value;
	}
	
	//CASCADED PROJECT PREFERENCES
	
	public String getCascadedProjectValue(String key,String defaultValue) {
		String value=getProjectValue(key,null);
		if(value==null) value=getCascadedInstanceValue(key,defaultValue);
		return value;
	}
	
	public boolean getCascadedProjectValue(String key,boolean defaultValue) {
		String value=getCascadedProjectValue(key,null);
		if(value==null) return defaultValue;
		else return Boolean.valueOf(value);
	}
	
}
