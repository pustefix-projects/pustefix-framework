package org.pustefixframework.ide.eclipse.plugin.core.internal;

import java.util.Map;
import java.util.WeakHashMap;

import org.eclipse.core.resources.IProject;

public class PustefixCore {
    
    private static PustefixCore instance = new PustefixCore();
    private Map<IProject,IProject> changedClassPathProjects = new WeakHashMap<IProject,IProject>();
    
    public static PustefixCore getInstance() {
        return instance;
    }
    
    public void classPathChanged(IProject project) {
        changedClassPathProjects.put(project,project);
    }
    
    public void classPathChangeHandled(IProject project) {
        changedClassPathProjects.remove(project);
    }
    
    public boolean hasClassPathChanged(IProject project) {
        return changedClassPathProjects.containsKey(project);
    }
    
}
