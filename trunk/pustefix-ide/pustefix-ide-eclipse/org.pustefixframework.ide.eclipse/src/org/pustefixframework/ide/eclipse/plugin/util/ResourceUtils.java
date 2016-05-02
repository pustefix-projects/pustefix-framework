package org.pustefixframework.ide.eclipse.plugin.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public class ResourceUtils {
	
	public final static String MARKER_GENERIC="org.pustefixframework.ide.eclipse.plugin.marker";
	public final static String MARKER_PROBLEM="org.pustefixframework.ide.eclipse.plugin.problemmarker";
	
	public static boolean createParentFolder(IResource resource, IProgressMonitor monitor) throws CoreException {
		IContainer container=resource.getParent();
		if(container.getType()==IResource.FOLDER) {
			return createFolder((IFolder)container, monitor);
		}
		return false;
	}

	public static boolean createFolder(IFolder folder, IProgressMonitor monitor) throws CoreException {
		if(!folder.exists()) {
			IContainer container=folder.getParent();
			if(container.getType()==IResource.FOLDER) {
				createFolder((IFolder)container, monitor);
			}
			folder.create(IResource.NONE, true, monitor);
			folder.setDerived(true, monitor);
			return true;
		}
		return false;
	}
	
	public static void removeDerivedResources(IResource resource, final IProgressMonitor monitor) throws CoreException {
	    IResourceVisitor visitor = new IResourceVisitor() {
            @Override
            public boolean visit(IResource resource) throws CoreException {
                if(resource.isDerived()) {
                    resource.delete(false, monitor);
                    return false;
                }
                return true;
            }
        };
        resource.accept(visitor);
	}
	
	public static void addProblemMarker(IResource resource,String message,int line) {
		try {
			IMarker marker=resource.createMarker(MARKER_PROBLEM);
			Map<String,Object> atts=new HashMap<String,Object>();
			atts.put(IMarker.MESSAGE,message);
			atts.put(IMarker.SEVERITY,IMarker.SEVERITY_ERROR);
			atts.put(IMarker.LINE_NUMBER,line);
			marker.setAttributes(atts);
		} catch (CoreException e) {}
	}
	
	public static void deleteProblemMarkers(IResource resource, boolean deep) {
		try {
			IMarker[] markers=resource.findMarkers(MARKER_PROBLEM,false, (deep ? IResource.DEPTH_INFINITE : IResource.DEPTH_ZERO));
			for(IMarker marker:markers) marker.delete();
		} catch (CoreException e) {}
	}
	
	public static List<IMarker> getProblemMarkers(IResource resource) {
		List<IMarker> list=new ArrayList<IMarker>();
		try {
			IMarker[] markers=resource.findMarkers(MARKER_PROBLEM,false,IResource.DEPTH_ZERO);
			for(IMarker marker:markers) list.add(marker);
		} catch (CoreException e) {}
		return list;
	}
	
	public static boolean hasProblemMarker(IResource resource) {
		try {
			IMarker[] markers=resource.findMarkers(MARKER_PROBLEM,false,IResource.DEPTH_ZERO);
			if(markers.length>0) return true;
		} catch (CoreException e) {}
		return false;
	}

}
