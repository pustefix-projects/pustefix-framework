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
import java.io.File;

/**
 * AbstractDependency.java
 *
 *
 * Created: Tue Jul 17 14:27:10 2001
 *
 * @author <a href="mailto: ">Jens Lautenbacher</a>
 *
 *
 */

public abstract class AbstractDependency implements Comparable, AuxDependency {
    protected String         dir;
    protected DependencyType type;

    protected Path  path;
    protected String  part;
    protected String  product;

    public boolean isDynamic() {
        return true;
    }
    
    public DependencyType getType() {
        return type;
    }
    
    public String getDir() {
        return dir;
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
            return check.lastModified();
        } else {
            return 0l;
        }
    }

    public abstract String  toString();
    public abstract boolean addDependencyParent(DependencyParent v);
    public abstract boolean removeDependencyParent(DependencyParent v);
    public abstract TreeSet getAffectedTargets();
    public abstract boolean addChild(AuxDependency v);
    public abstract TreeSet getChildren();
    public abstract void    reset();
    
    public int compareTo(Object inobj) {
        AuxDependency in = (AuxDependency) inobj;

        if (getType().getTag().compareTo(in.getType().getTag()) != 0) {
            return getType().getTag().compareTo(in.getType().getTag());
        } else if (dir.compareTo(in.getDir()) != 0) {  
            return dir.compareTo(in.getDir());
        } else { 
            return path.compareTo(in.getPath());
        }
    }

    public int hashCode() {
        String key = type.getTag() + "@" + path.getRelative() + "@" + part + "@" + product;
        return key.hashCode();
    }

}// AbstractDependency
