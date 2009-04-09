/*
 * This file is part of Pustefix.
 *
 * Pustefix is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Pustefix is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Pustefix; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package de.schlund.pfixxml.targets;

import java.util.Iterator;
import java.util.TreeSet;

import de.schlund.pfixxml.resources.Resource;
import de.schlund.pfixxml.resources.ResourceUtil;

/**
 * Dependency referencing a static file on the filesystem
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class AuxDependencyFile extends AbstractAuxDependency {
    private Resource path;

    private long last_lastModTime = -1;
    
    protected int hashCode;
    
    public AuxDependencyFile(Resource path) {
        this.type = DependencyType.FILE;
        this.path = path;
        this.hashCode = (type.getTag() + ":" + path.toString()).hashCode();
    }
    
    /**
     * Returns path to the file containing the referenced include part
     * 
     * @return path to the include file
     */
    public Resource getPath() {
        return path;
    }
    
    public long getModTime() {
        if("dynamic".equals(path.getOriginatingURI().getScheme())) {
            Resource res = ResourceUtil.getResource(path.getOriginatingURI());
            if(!res.toURI().equals(path.toURI())) {
                TreeSet<Target> targets = TargetDependencyRelation.getInstance()
                .getAffectedTargets(this);
                for (Iterator<Target> i = targets.iterator(); i.hasNext();) {
                    VirtualTarget target = (VirtualTarget) i.next();
                    target.setForceUpdate();
                }
            }
        }
        if (path.exists() && path.canRead() && path.isFile()) {
            if (last_lastModTime == 0) {
                // We change from the file being checked once to not exist to "it exists now".
                // so we need to make sure that all targets using it will be rebuild.
                TreeSet<Target> targets = TargetDependencyRelation.getInstance()
                        .getAffectedTargets(this);
                for (Iterator<Target> i = targets.iterator(); i.hasNext();) {
                    VirtualTarget target = (VirtualTarget) i.next();
                    target.setForceUpdate();
                }
            }
            last_lastModTime = path.lastModified();
            return last_lastModTime;
        } else {
            if (last_lastModTime > 0) {
                // The file existed when last check has been made,
                // so make sure each target using it is being rebuild
                TreeSet<Target> targets = TargetDependencyRelation.getInstance()
                        .getAffectedTargets(this);
                for (Iterator<Target> i = targets.iterator(); i.hasNext();) {
                    VirtualTarget target = (VirtualTarget) i.next();
                    target.setForceUpdate();
                }
            }
            last_lastModTime = 0;
            return 0;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AuxDependencyFile) {
            return (this.compareTo((AuxDependency) obj) == 0);
        } else {
            return false;
        }
    }

    @Override
    public int compareTo(AuxDependency o) {
        int comp;
        
        comp = super.compareTo(o);
        if (comp != 0) {
            return comp;
        }
        
        AuxDependencyFile a = (AuxDependencyFile) o;
        return path.compareTo(a.path);
    }

    @Override
    public int hashCode() {
        return this.hashCode;
    }

    @Override
    public String toString() {
        return "[AUX/" + getType() + " " + getPath().toURI().toString() + "]";
    }

}
