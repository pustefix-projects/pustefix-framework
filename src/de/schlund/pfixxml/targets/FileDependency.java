/*
* This file is part of PFIXCORE.
*
* PFIXCORE is free software; you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation; either version 2 of the License, or
* (at your option) any later version.
*
* PFIXCORE is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with PFIXCORE; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*
*/

package de.schlund.pfixxml.targets;
import java.util.*;

/**
 * FileDependency.java
 *
 *
 * Created: Tue Jul 17 14:27:10 2001
 *
 * @author <a href="mailto: jtl@schlund.de">Jens Lautenbacher</a>
 *
 *
 */

public class FileDependency extends AbstractDependency {
    private HashSet parents = new HashSet();
    
    public boolean addDependencyParent(DependencyParent v) {
        synchronized (parents) {
            return parents.add(v);
        }
    }

    public boolean removeDependencyParent(DependencyParent v) { 
        synchronized (parents) {
            return parents.remove(v);
        }
    }

    public TreeSet getAffectedTargets() {
        TreeSet tmp = new TreeSet();
        synchronized (parents) {
            for (Iterator i = parents.iterator(); i.hasNext(); ) {
                DependencyParent parent = (DependencyParent) i.next();
                tmp.addAll(parent.getAffectedTargets());
            }
        }
        return tmp;
    }

    public boolean addChild(AuxDependency v) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Can't add child to " + this.getClass().getName());
    }  

    public TreeSet getChildren() {return null;}

    public void reset() {/* NOP */}

    public String toString() {
        String retval = "[AUXDEP: " + getType() + " " + getPath() + "@" + getPart() + "@" + getProduct() + "]\n";
        retval += "        -------------- Targets:  --------------\n";
        TreeSet targets = getAffectedTargets();
        for (Iterator i = targets.iterator(); i.hasNext(); ) {
            Target target = (Target) i.next();
            retval += "        " + target + "\n";
        }
        return retval;
    }

    public FileDependency() {}

    public FileDependency (DependencyType type, Path path, String dummy, String dummy2) {
        this.type    = type;
        this.path    = path;
        this.part    = null;
        this.product = null;
        if (path == null) {
            throw new RuntimeException("Need Path to construct FileDependency");
        }
        dir = path.getDir();
    }

}// FileDependency
