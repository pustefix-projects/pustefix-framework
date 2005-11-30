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

import de.schlund.pfixxml.util.Path;
import de.schlund.pfixxml.util.RefCountingCollection;
import java.io.File;
import java.util.Iterator;
import java.util.TreeSet;
import org.apache.log4j.Category;

/**
 * Additional dependency besides XML and XSL source. 
 */
public class AuxDependency implements Comparable {
    
    private static Category      CAT = Category.getInstance(AuxDependency.class.getName());
    private final DependencyType type;
    private final Path           path;
    private final String         part;
    private final String         theme;
    private final int            hashCode;
   
    private long last_lastModTime = -1;
    
    public AuxDependency(DependencyType type, Path path, String part, String theme) {
        if (path == null) {
            throw new IllegalArgumentException("Need Path to construct AuxDependency");
        }
        this.type  = type;
        this.path  = path;
        this.part  = part;
        this.theme = theme;

        String key    = type.getTag() + "@" + path.getRelative() + "@" + part + "@" + theme;
        this.hashCode = key.hashCode();
    }

    public DependencyType getType() { return type; }

    public Path getPath() { return path; }

    public String getPart() { return part; }

    public String getTheme() { return theme; }
    
    public long getModTime() {
        File check = path.resolve();
        if (check.exists() && check.canRead() && check.isFile()) {
            if (last_lastModTime == 0) {
                // We change from the file being checked once to not exist to "it exists now".
                // so we need to make sure that all targets using it will be rebuild.
                TreeSet targets = TargetDependencyRelation.getInstance().getAffectedTargets(this);
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
                TreeSet targets = TargetDependencyRelation.getInstance().getAffectedTargets(this);
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
        return "[AUX/" + getType() + " " + getPath().getRelative() + "@" + getPart() + "@" + getTheme() + "]";
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
        return cmpOpt(theme, in.getTheme());
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
