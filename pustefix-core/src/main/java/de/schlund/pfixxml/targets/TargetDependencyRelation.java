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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import de.schlund.pfixxml.util.RefCountingCollection;

/**
 * TargetDependencyRelation This is a helper singleton class that holds all the "interesting"
 * relations between targets and AuxDependencies. These relations are build up during the generation
 * of a target via the addRelation(parent, child, target) method, and reset before generation of the
 * target via the resetRelation(target) method.
 *
 * The relations currently handled are:
 * 
 *            Target          -> Set(AuxDependency)         
 *            AuxDependency   -> Set(Target)
 *            TargetGenerator -> RefCountingCollection(AuxDependency)
 *            AuxDependency   -> RefCountingCollection(TargetGenerator)
 *            Target          -> Map("parent"-AuxDependency->Set("child"-AuxDependency))
 *            AuxDependency   -> RefCountingCollection("child"-AuxDependency)
 *            Target          -> Map("child"-AuxDependency->Set("parent"-AuxDependency)) // Commented out currently!
 *            AuxDependency   -> RefCountingCollection("parent"-AuxDependency) // Commented out currently!
 *
 * The last two mappings are not use anywhere and are thus commented out in the code to save memory.
 *
 * Created: Thu Nov 24 13:00:00 2005
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 *
 */

public class TargetDependencyRelation {
    
    private static final Logger LOG = Logger.getLogger(TargetDependencyRelation.class);
    
    private HashMap<AuxDependency, TreeSet<Target>> allauxs; 
    private HashMap<Target, TreeSet<AuxDependency>> alltargets;
    private RefCountingCollection<AuxDependency> auxDepRefCount;
    private HashMap<Target, HashMap<AuxDependency, HashSet<AuxDependency>>> targettoparentchild;
    private HashMap<AuxDependency, RefCountingCollection<AuxDependency>> auxtochildaux;
    
    public TargetDependencyRelation() {
        reset();
    }

    public void reset() {
        allauxs = new HashMap<AuxDependency, TreeSet<Target>>();
        alltargets = new HashMap<Target, TreeSet<AuxDependency>>();
        auxDepRefCount = new RefCountingCollection<AuxDependency>();
        targettoparentchild = new HashMap<Target, HashMap<AuxDependency, HashSet<AuxDependency>>>();
        auxtochildaux = new HashMap<AuxDependency, RefCountingCollection<AuxDependency>>();
    }

    public synchronized TreeSet<AuxDependency> getAllDependencies() {
        if (allauxs.isEmpty()) {
            return null;
        } else {
            return new TreeSet<AuxDependency>(allauxs.keySet());
        }
    }

    public synchronized TreeSet<AuxDependency> getProjectDependencies() {
        return new TreeSet<AuxDependency>(auxDepRefCount);
    }

    public synchronized TreeSet<AuxDependency> getAllDependenciesForType(DependencyType type) {
        if (allauxs.isEmpty()) {
            return null;
        } else {
            TreeSet<AuxDependency> tmp = new TreeSet<AuxDependency>();
            for (Iterator<AuxDependency> i = allauxs.keySet().iterator(); i.hasNext(); ) {
                AuxDependency aux = i.next();
                if (aux.getType().equals(type)) {
                    tmp.add(aux);
                }
            }
            return tmp;
        }
    }

    public synchronized TreeSet<AuxDependency> getProjectDependenciesForType(DependencyType type) {
        TreeSet<AuxDependency> tmp = new TreeSet<AuxDependency>();
        for (Iterator<AuxDependency> i = auxDepRefCount.iterator(); i.hasNext(); ) {
            AuxDependency aux = i.next();
            if (aux.getType().equals(type)) {
                tmp.add(aux);
            }
        }
        return tmp;
    }

    public synchronized TreeSet<AuxDependency> getDependenciesForTarget(Target target) {
        TreeSet<AuxDependency> auxsForTarget = alltargets.get(target);
        if (auxsForTarget == null) {
            return null;
        } else {
            return new TreeSet<AuxDependency>(auxsForTarget);
        }
    }

    public synchronized TreeSet<Target> getAffectedTargets(AuxDependency aux) {
        TreeSet<Target> targetsForAux = allauxs.get(aux);
        if (targetsForAux == null) {
            return null;
        } else {
            return new TreeSet<Target>(targetsForAux);
        }
    }

    synchronized HashMap<AuxDependency, HashSet<AuxDependency>> getParentChildMapForTarget(Target target) {
        HashMap<AuxDependency, HashSet<AuxDependency>> parentchildForTarget = targettoparentchild.get(target);
        if (parentchildForTarget == null) {
            return null;
        } else {
            HashMap<AuxDependency, HashSet<AuxDependency>> retval = new HashMap<AuxDependency, HashSet<AuxDependency>>();
            for (Iterator<AuxDependency> i = parentchildForTarget.keySet().iterator(); i.hasNext();) {
                AuxDependency parent = i.next();
                if (parentchildForTarget.get(parent) != null) {
                    HashSet<AuxDependency> children = new HashSet<AuxDependency>(parentchildForTarget.get(parent));
                    retval.put(parent, children);
                }
            }
            return retval;
        }
    }

    public synchronized TreeSet<AuxDependency> getChildrenOverallForAuxDependency(AuxDependency parent) {
        RefCountingCollection<AuxDependency> children = auxtochildaux.get(parent);
        if (children == null) {
            return null;
        } else {
            return new TreeSet<AuxDependency>(children);
        }
    }

    public synchronized TreeSet<AuxDependency> getChildrenForTargetForAuxDependency(Target target, AuxDependency parent) {
        HashMap<AuxDependency, HashSet<AuxDependency>> parentchildForTarget = targettoparentchild.get(target);
        if (parentchildForTarget == null) {
            return null;
        } else {
            HashSet<AuxDependency> children = parentchildForTarget.get(parent);
            if (children == null) {
                return null;
            } else {
                return new TreeSet<AuxDependency>(children);
            }
        }
    }

    public synchronized void addRelation(AuxDependency parent, AuxDependency aux, Target target) {

        if(LOG.isDebugEnabled()) {
            LOG.debug("+++ Adding relations " + target.getTargetKey() + " <-> " + aux.toString() + " / " + parent.toString());
        }
        if (allauxs.get(aux) == null) {
            allauxs.put(aux, new TreeSet<Target>());
        }
        if (alltargets.get(target) == null) {
            alltargets.put(target, new TreeSet<AuxDependency>());
        }
        if (targettoparentchild.get(target) == null) {
            targettoparentchild.put(target, new HashMap<AuxDependency, HashSet<AuxDependency>>());
        }
        if (auxtochildaux.get(parent) == null) {
            auxtochildaux.put(parent, new RefCountingCollection<AuxDependency>());
        }

        TreeSet<Target>                        targetsForAux       = allauxs.get(aux);
        TreeSet<AuxDependency>                 auxsForTarget       = alltargets.get(target);
        HashMap<AuxDependency, HashSet<AuxDependency>> parentchildForTarget = targettoparentchild.get(target);
        RefCountingCollection<AuxDependency>   auxsForParent       = auxtochildaux.get(parent);
     
        if (targetsForAux.add(target)) { // Make sure to ignore multiple target<->aux relations
            auxsForTarget.add(aux);
            auxDepRefCount.add(aux);
        }
        
        HashSet<AuxDependency> children = parentchildForTarget.get(parent);
        if (children == null) {
            children = new HashSet<AuxDependency>();
            parentchildForTarget.put(parent, children);
        }
        
        if (children.add(aux)) { // Make sure to ignore multiple parent<->child relations
                                 // Note: we must allow here multiple entries for the same
                                 // child per target, but not per parent!
            auxsForParent.add(aux);

        }
    }

    public synchronized void resetRelation(Target target) {
        if(LOG.isDebugEnabled()) {
            LOG.debug("--- Removing all relations for " + target.getTargetKey());
        }
        TreeSet<AuxDependency> auxsForTarget = alltargets.get(target);

        if (auxsForTarget == null) {
            // CAT.debug("*** Trying to reset relations for unknown target " + target.getFullName() + " ***");  
            return;
        }
        if (auxsForTarget.isEmpty()) {
            LOG.error("*** Known target " + target.getTargetKey() + " has empty relation set! ***");  
            return;
        }

        for (Iterator<AuxDependency> i = auxsForTarget.iterator(); i.hasNext();) {
            AuxDependency aux = i.next();
            if (aux.getType().isDynamic()) {
                TreeSet<Target>                        targetsForAux = allauxs.get(aux);
                
                targetsForAux.remove(target);
                if (targetsForAux.isEmpty()) {
                    allauxs.remove(aux);
                }
                
                auxDepRefCount.remove(aux);
                i.remove();
            }
        }
        if (auxsForTarget.isEmpty()) {
            alltargets.remove(target);
        }

        HashMap<AuxDependency, HashSet<AuxDependency>> parentchildForTarget = targettoparentchild.get(target);
        for (Iterator<AuxDependency> i = parentchildForTarget.keySet().iterator(); i.hasNext(); ) {
            AuxDependency                        parent      = i.next();
            // System.out.println("---> parent: " + parent);
            HashSet<AuxDependency>               children    = parentchildForTarget.get(parent);
            RefCountingCollection<AuxDependency> allchildren = auxtochildaux.get(parent);
            // System.out.println("     allc: " + allchildren);
            for (Iterator<AuxDependency> j = children.iterator(); j.hasNext();) {
                AuxDependency child = j.next();
                // System.out.println("     child: " + child);
                if (child.getType().isDynamic()) {
                    allchildren.remove(child);
                    j.remove();
                }
            }
            if (children.isEmpty()) {
                i.remove();
            }
            if (allchildren.isEmpty()) {
                auxtochildaux.remove(parent);
            }
        }
        if (parentchildForTarget.isEmpty()) {
            targettoparentchild.remove(target);
        }

    }

    public synchronized void resetAllRelations(Collection<Target> targets) {
        for (Iterator<Target> i = targets.iterator(); i.hasNext();) {
            Target target = i.next();
            resetRelation(target);
        }
    }

}// DependencyRefCounter
