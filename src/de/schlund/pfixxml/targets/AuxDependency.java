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

import java.io.File;
import java.util.*;
import de.schlund.pfixxml.util.Path;

/**
 * Additional dependency besides XML and XSL source. 
 */
public class AuxDependency implements DependencyParent, Comparable {

    private final DependencyType type;
    private final Path           path;
    private final String         part;
    private final String         product;
    private final int            hashCode;
    private final HashSet        parents; 
    private final TreeSet        children;

    private long last_lastModTime = -1;
    
    public AuxDependency(DependencyType type, Path path, String part, String product) {
        if (path == null) {
            throw new IllegalArgumentException("Need Path to construct AbstractDependency");
        }
        this.type = type;
        this.path = path;
        this.part = part;
        this.product = product;
        this.parents = new HashSet();
        this.children = new TreeSet();

        String key = type.getTag() + "@" + path.getRelative() + "@" + part + "@" + product;
        this.hashCode = key.hashCode();
    }

    public boolean addChild(AuxDependency aux) {
        synchronized (children) {
            aux.addDependencyParent(this);
            return children.add(aux);
        }
    }

    public TreeSet getChildren() {
        synchronized (children) {
            return (TreeSet) children.clone();
        }
    }
    
    public void reset() {
        synchronized (children) {
            for (Iterator i = children.iterator(); i.hasNext(); ) {
                AuxDependency aux  = (AuxDependency) i.next();
                aux.removeDependencyParent(this);
                if (aux.getType().isDynamic()) {
                    i.remove();
                }
            }
        }
    }
    
    public DependencyType getType() {
        return type;
    }
    
    public Path getPath() {
        return path;
    }
    
    public String getPart() {
        return part;
    }
    
    public String getProduct() {
        return product;
    }
    
    public long getModTime() {
        File check = path.resolve();
        if (check.exists() && check.canRead() && check.isFile()) {
            if (last_lastModTime == 0) {
                // We change from the file being checked once to not exist to "it exists now".
                // so we need to make sure that all targets using it will be rebuild.
                TreeSet targets = getAffectedTargets();
                for (Iterator i = targets.iterator(); i.hasNext(); ) {
                    VirtualTarget target = (VirtualTarget) i.next();
                    target.setForceUpdate();
                }
            }
            last_lastModTime = check.lastModified();
            return last_lastModTime;
        } else {
            if (last_lastModTime > 0) {
                // The file existed when last check has been made,
                // so make sure each target using it is being rebuild
                TreeSet targets = getAffectedTargets();
                for (Iterator i = targets.iterator(); i.hasNext(); ) {
                    VirtualTarget target = (VirtualTarget) i.next();
                    target.setForceUpdate();
                }
            }
            last_lastModTime = 0;
            return 0;
        }
    }

    public String toString() {
        String retval = "[AUXDEP: " + getType() + " " + getPath() + "@" + getPart() + "@" + getProduct() + "]\n";
        if (!children.isEmpty()) {
            retval += "        -------------- Children: --------------\n";
            TreeSet set = getChildren();
            for (Iterator i = set.iterator(); i.hasNext(); ) {
                AuxDependency child = (AuxDependency) i.next();
                retval += "        " + "[AUXDEP: " + child.getType() + " " +
                    child.getPath() + "@" + child.getPart() + "@" + child.getProduct() + "]\n";
            }
        }
        retval += "        -------------- Targets:  --------------\n";
        TreeSet targets = getAffectedTargets();
        for (Iterator i = targets.iterator(); i.hasNext(); ) {
            Target target = (Target) i.next();
            retval += "        " + target + "\n";
        }
        return retval;
    }
    
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
    
    public int compareTo(Object inobj) {
        AuxDependency in = (AuxDependency) inobj;

        if (getType().getTag().compareTo(in.getType().getTag()) != 0) {
            return getType().getTag().compareTo(in.getType().getTag());
        }
        if (path.compareTo(in.getPath()) != 0) { 
            return path.compareTo(in.getPath());
        }
        if (cmpOpt(part, in.getPart()) != 0) {
            return cmpOpt(part, in.getPart());
        }
        return cmpOpt(product, in.getProduct());
    }
    
    private int cmpOpt(String left, String right) {
        if (left == null) {
            return right == null? 0 : 1;
        }
        if (right == null) {
            return -1;
        }
        return left.compareTo(right);
    }
    
    public int hashCode() {
        return hashCode;
    }

}// AuxDependency
