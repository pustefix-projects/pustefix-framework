package org.pustefixframework.ide.eclipse.plugin.builder;

import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public interface BuilderDelegate {

    public void clean(IProgressMonitor monitor) throws CoreException;
    public void incrementalBuild(IResourceDelta delta, IProgressMonitor monitor) throws CoreException;
    public void fullBuild(IProgressMonitor monitor) throws CoreException;
    
}
