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
import java.util.*;
import org.apache.log4j.Category;

/**
 * Additional dependency besides XML and XSL source. 
 */
public class AuxDependency implements Comparable {
    
    private static Category                              CAT = Category.getInstance(AuxDependency.class.getName());
    private final DependencyType                         type;
    private final Path                                   path;
    private final String                                 part;
    private final String                                 product;
    private final int                                    hashCode;
    private final TreeSet<Target>                        affectedtargets;
    private final RefCountingCollection<TargetGenerator> affectedtargetgenerators;
    private final HashMap                                themes_children;
   
    private long last_lastModTime = -1;
    
    public AuxDependency(DependencyType type, Path path, String part, String product) {
        if (path == null) {
            throw new IllegalArgumentException("Need Path to construct AuxDependency");
        }
        this.type                     = type;
        this.path                     = path;
        this.part                     = part;
        this.product                  = product;
        this.affectedtargets          = new TreeSet<Target>();
        this.affectedtargetgenerators = new RefCountingCollection<TargetGenerator>();
        this.themes_children          = new HashMap();

        String key    = type.getTag() + "@" + path.getRelative() + "@" + part + "@" + product;
        this.hashCode = key.hashCode();
    }

    public boolean addChild(AuxDependency aux, VirtualTarget target) {
        synchronized (themes_children) {
            aux.addTargetDependency(target);
            Themes themes = target.getThemes();
            if (themes_children.get(themes) == null) {
                themes_children.put(themes, new TreeSet());
            }
            TreeSet children = (TreeSet) themes_children.get(themes);
            //CAT.warn("\n#####> ADD [" + id + "] " + aux);
            return children.add(aux);
        }
    }

    public void removeChildren(VirtualTarget target) {
        synchronized (themes_children) {
            Themes themes = target.getThemes();
            if (themes_children.get(themes) == null) {
                themes_children.put(themes, new TreeSet());
            }
            TreeSet tmp_children = (TreeSet) themes_children.get(themes);
            for (Iterator i = tmp_children.iterator(); i.hasNext(); ) {
                AuxDependency aux  = (AuxDependency) i.next();
                if (aux.getType().isDynamic()) {
                    //CAT.warn("\n==>REMOVE: " + aux);
                    i.remove();
                } else {
                    //CAT.warn("\n==>REMAIN: " + aux);
                }
            }
        }
    }
    
    public boolean addTargetDependency(VirtualTarget target) {
        synchronized (affectedtargets) {
            TargetGenerator tgen    = target.getTargetGenerator();
            boolean         changed = affectedtargets.add(target);
            if (changed) {
                affectedtargetgenerators.add(tgen);
            }
            return changed;
        }
    }

    public void resetTargetDependency(VirtualTarget target) { 
        synchronized (affectedtargets) {
            affectedtargets.remove(target);
            TargetGenerator tgen  = target.getTargetGenerator();
            affectedtargetgenerators.remove(tgen);
        }
    }

    public TreeSet getChildren(VirtualTarget target) {
        Themes themes = target.getThemes();
        return getChildrenForThemes(themes);
    }

    public TreeSet getChildrenForThemes(Themes themes) {
        synchronized (themes_children) {
            if (themes_children.get(themes) == null) {
                themes_children.put(themes, new TreeSet());
            }
            return (TreeSet) ((TreeSet) themes_children.get(themes)).clone();
        }
    }
    
    public TreeSet getChildrenForAllThemes() {
        synchronized (themes_children) {
            TreeSet retval = new TreeSet();
            for (Iterator i = themes_children.values().iterator(); i.hasNext();) {
                TreeSet children = (TreeSet) i.next();
                retval.addAll(children);
            }
            return retval;
        }
    }

    public HashSet getThemesList() {
        synchronized (themes_children) {
            return new HashSet(themes_children.keySet());
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
        return "[AUX/" + getType() + " "  + getPath().getRelative() + "@" + getPart() + "@" + getProduct() + "]";
    }
    
    public String fullDescription() {
        StringBuffer retval = new StringBuffer(toString() + "\n");
        if (!themes_children.isEmpty()) {
            retval.append("        -------------- Children: --------------\n");
            for (Iterator i = themes_children.keySet().iterator(); i.hasNext();) {
                Themes  themes = (Themes) i.next();
                TreeSet set    = (TreeSet) themes_children.get(themes);
                if (set != null && !set.isEmpty()) {
                    retval.append("           ==> ThemeID: " + themes.getId() + "\n");
                    for (Iterator j = set.iterator(); j.hasNext(); ) {
                        AuxDependency child = (AuxDependency) j.next();
                        retval.append("        " + "[AUXDEP: " + child.getType() + " " +
                                      child.getPath() + "@" + child.getPart() + "@" + child.getProduct() + "]\n");
                    }
                }
            }
        }
        retval.append("        -------------- Targets:  --------------\n");
        TreeSet targets = getAffectedTargets();
        for (Iterator i = targets.iterator(); i.hasNext(); ) {
            Target target = (Target) i.next();
            retval.append("        " + target + "\n");
        }
        return retval.toString();
    }
    
    public TreeSet<Target> getAffectedTargets() {
        synchronized (affectedtargets) {
            return (TreeSet<Target>) affectedtargets.clone();
        }
    }

    public TreeSet<TargetGenerator> getAffectedTargetGenerators() {
        synchronized (affectedtargets) { // Note: this is right, we only change
                                         // affectedtargetgenerators when in synchronized blocks of
                                         // affectedtargets.
            return new TreeSet<TargetGenerator>(affectedtargetgenerators);
        }
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
