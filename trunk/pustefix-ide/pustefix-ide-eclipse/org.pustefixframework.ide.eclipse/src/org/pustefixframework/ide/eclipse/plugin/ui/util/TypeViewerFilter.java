package org.pustefixframework.ide.eclipse.plugin.ui.util;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

/**
 * Viewer filter which filters by type and non-object-equality
 */
public class TypeViewerFilter extends ViewerFilter {

	private Class<?>[] acceptedTypes;
	private Set<Object> rejectedObjects;

	public TypeViewerFilter(Class<?>[] acceptedTypes,Object[] rejectedObjects) {
		this.acceptedTypes=acceptedTypes;
		this.rejectedObjects=new HashSet<Object>();
		for(Object obj:rejectedObjects) this.rejectedObjects.add(obj);
	}
	
	private boolean acceptType(Object element) {
		for(Class<?> clazz:acceptedTypes) if(clazz.isInstance(element)) return true;
		return false;
	}
	
	private boolean acceptObject(Object element) {
		return !rejectedObjects.contains(element);
	}
	
	public boolean select(Viewer viewer,Object parentElement,Object element) {
		return acceptType(element) && acceptObject(element);
	}

}
