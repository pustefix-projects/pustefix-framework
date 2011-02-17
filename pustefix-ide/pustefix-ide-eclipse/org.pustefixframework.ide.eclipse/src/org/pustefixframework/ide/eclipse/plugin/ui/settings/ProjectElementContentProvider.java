package org.pustefixframework.ide.eclipse.plugin.ui.settings;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.ui.StandardJavaElementContentProvider;

import org.pustefixframework.ide.eclipse.plugin.builder.PustefixNature;

public class ProjectElementContentProvider extends StandardJavaElementContentProvider {

	@Override
	public Object[] getChildren(Object element) {
		List<Object> list=new ArrayList<Object>();
		Object[] objs=super.getChildren(element);
		for(Object obj:objs) {
			if(obj instanceof IJavaProject) {
				if(isPustefixProject((IJavaProject)obj)) list.add(obj);
			}
		}
		return list.toArray();
	}
	
	private boolean isPustefixProject(IJavaProject project) {
		try {
			IProjectDescription desc=project.getProject().getDescription();
			return desc.hasNature(PustefixNature.NATURE_ID);
		} catch(CoreException x) {
			return false;
		}
	}
	
}
