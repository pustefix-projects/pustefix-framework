package org.pustefixframework.ide.eclipse.plugin.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

public class ResourceUtils {
	
	public final static String MARKER_GENERIC="org.pustefixframework.ide.eclipse.plugin.marker";
	public final static String MARKER_PROBLEM="org.pustefixframework.ide.eclipse.plugin.problemmarker";
	
	public static boolean createParentFolder(IResource resource) throws CoreException {
		IContainer container=resource.getParent();
		if(container.getType()==IResource.FOLDER) {
			return createFolder((IFolder)container);
		}
		return false;
	}

	public static boolean createFolder(IFolder folder) throws CoreException {
		if(!folder.exists()) {
			IContainer container=folder.getParent();
			if(container.getType()==IResource.FOLDER) {
				createFolder((IFolder)container);
			}
			folder.create(IResource.NONE,true,null);
			return true;
		}
		return false;
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
	
	public static void deleteProblemMarkers(IResource resource) {
		try {
			IMarker[] markers=resource.findMarkers(MARKER_PROBLEM,false,IResource.DEPTH_ZERO);
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
