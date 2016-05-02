package org.pustefixframework.ide.eclipse.plugin;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;

import org.pustefixframework.ide.eclipse.plugin.core.internal.PustefixCore;

public class ClassPathChangeDetector implements IElementChangedListener {
    
    private Logger LOG = Activator.getLogger();
    
    public ClassPathChangeDetector() {
        
    }
    
    public void elementChanged(ElementChangedEvent event) {
        IJavaElementDelta delta = event.getDelta();
        IJavaElement element = delta.getElement();
        if(element instanceof IJavaModel) {
            IJavaElementDelta[] subDeltas = delta.getAffectedChildren();
            for(IJavaElementDelta subDelta:subDeltas) {
                IJavaElement subElement = subDelta.getElement();
                if(subElement instanceof IJavaProject) {
                    IProject project = ((IJavaProject)subElement).getProject();
                    boolean isPustefixProject = false;
                    try {
                        isPustefixProject = project.hasNature(PustefixNature.NATURE_ID);
                    } catch(CoreException x) {
                        LOG.error("Checking for Pustefix nature failed", x);
                    }
                    if(isPustefixProject) {
                        if((subDelta.getFlags() & IJavaElementDelta.F_CLASSPATH_CHANGED)!=0) {
                            PustefixCore.getInstance().classPathChanged(project);
                        }
                        //looks like checking the IPackageFragmentRoot deltas isn't necessary
                        //because IJavaProject delta flag CLASSPATH_CHANGED is set too
                        /**
                        IJavaElementDelta[] pkgDeltas = subDelta.getAffectedChildren();
                        for(IJavaElementDelta pkgDelta:pkgDeltas) {
                            IJavaElement pkgElement = pkgDelta.getElement();
                            if(pkgElement instanceof IPackageFragmentRoot) {
                                if( ( pkgDelta.getFlags() & ( 
                                        IJavaElementDelta.F_ADDED_TO_CLASSPATH |
                                        IJavaElementDelta.F_CLASSPATH_CHANGED |
                                        IJavaElementDelta.F_REMOVED_FROM_CLASSPATH ) ) != 0) {
                                    PustefixCore.getInstance().classPathChanged(project);
                                    return;
                                }
                            }
                        }
                        */
                    }
                    
                }
            }
            
        }
        
    }
    
    private String getDeltaAsString(IJavaElementDelta delta) {
        StringBuilder sb = new StringBuilder();
        IJavaElement element = delta.getElement();
        sb.append(element.getClass().getName());
        sb.append("[kind="+getKindAsString(delta)+"]");
        sb.append("[flags="+getFlagsAsString(delta)+"]");
        return sb.toString();
    }
    
    private String getKindAsString(IJavaElementDelta delta) {
        int kind = delta.getKind();
        if(kind==IJavaElementDelta.ADDED) return "ADDED";
        else if(kind==IJavaElementDelta.CHANGED) return "CHANGED";
        else if(kind==IJavaElementDelta.REMOVED) return "REMOVED";
        return "";
    }
    
    private String getFlagsAsString(IJavaElementDelta delta) {
        StringBuilder sb = new StringBuilder();
        int flags = delta.getFlags();
        if((flags & IJavaElementDelta.F_ADDED_TO_CLASSPATH)!=0) sb.append("ADDED_TO_CLASSPATH|");
        if((flags & IJavaElementDelta.F_CLASSPATH_CHANGED)!=0) sb.append("CLASSPATH_CHANGED|");
        if((flags & IJavaElementDelta.F_REMOVED_FROM_CLASSPATH)!=0) sb.append("REMOVED_FROM_CLASSPATH");
        if(sb.length()>0 && sb.charAt(sb.length()-1)=='|') sb.deleteCharAt(sb.length()-1);
        return sb.toString();
    }

}
