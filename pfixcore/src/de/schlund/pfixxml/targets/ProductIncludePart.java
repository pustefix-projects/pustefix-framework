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
 * Describe class <code>ProductIncludePart</code> here.
 *
 * @author jtl
 *
 *
 */

public class ProductIncludePart extends AbstractDependency {
    private TreeSet children = new TreeSet();
    private HashSet parents  = new HashSet(); 
    
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
                if (aux.isDynamic()) {
                    i.remove();
                }
            }
        }
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
    
    public ProductIncludePart(DependencyType type, Path path, String part, String product) {
        this.type    = type;
        this.path    = path;
        this.part    = part;
        this.product = product;
        if (product == null || part == null || path == null) {
            throw new RuntimeException("Need all of Path/Part/Product: "
                                       + path + "/"
                                       + part + "/"
                                       + product);
        }
        dir = path.getDir();
    }

    public String toString() {
        String retval = "[AUXDEP: " + getType() + " " + getPath() + "@" + getPart() + "@" + getProduct() + "]\n";
        retval += "        -------------- Children: --------------\n";
        TreeSet set = getChildren();
        for (Iterator i = set.iterator(); i.hasNext(); ) {
            AuxDependency child = (AuxDependency) i.next();
            retval += "        " + "[AUXDEP: " + child.getType() + " " +
                child.getPath() + "@" + child.getPart() + "@" + child.getProduct() + "]\n";
        }
        retval += "        -------------- Targets:  --------------\n";
        TreeSet targets = getAffectedTargets();
        for (Iterator i = targets.iterator(); i.hasNext(); ) {
            Target target = (Target) i.next();
            retval += "        " + target + "\n";
        }
        return retval;
    }
    
    public int compareTo(Object inobj) {
        int val = super.compareTo(inobj);
        if (val == 0) {
            if (inobj instanceof ProductIncludePart) {
                ProductIncludePart in = (ProductIncludePart) inobj;
                if (part.compareTo(in.getPart()) != 0) {
                    return part.compareTo(in.getPart());
                } else { 
                    return product.compareTo(in.getProduct());
                }
            }
        }
        return val;
    }
}
