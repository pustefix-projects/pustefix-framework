package org.pustefixframework.ide.eclipse.plugin.builder;

import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import org.pustefixframework.ide.eclipse.plugin.Environment;

public interface StatusCodeGenerator {

	public void incrementalBuild(Environment environment, IResourceDelta delta,IProgressMonitor monitor) throws CoreException;
	
}
