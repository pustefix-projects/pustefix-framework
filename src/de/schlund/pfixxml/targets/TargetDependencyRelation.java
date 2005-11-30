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
import de.schlund.pfixxml.util.RefCountingCollection;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeSet;
import org.apache.log4j.Category;

/**
 * TargetDependencyRelation
 *
 *
 * Created: Thu Nov 24 13:00:00 2005
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 *
 *
 */

public class TargetDependencyRelation {
    private static Category                 CAT      = Category.getInstance(TargetDependencyRelation.class.getName());
    private static TargetDependencyRelation instance = new TargetDependencyRelation();
    
    private HashMap<AuxDependency, TreeSet<Target>> allauxs                            = 
        new HashMap<AuxDependency, TreeSet<Target>>();
    private HashMap<Target, TreeSet<AuxDependency>> alltargets                         = 
        new HashMap<Target, TreeSet<AuxDependency>>();
    private HashMap<AuxDependency, RefCountingCollection<TargetGenerator>> auxtotgen   = 
        new HashMap<AuxDependency, RefCountingCollection<TargetGenerator>>(); 
    private HashMap<TargetGenerator, RefCountingCollection<AuxDependency>> tgentoaux   = 
        new HashMap<TargetGenerator, RefCountingCollection<AuxDependency>>();
    private HashMap<Target, HashMap<AuxDependency, HashSet<AuxDependency>>> targettoparentchild = 
        new HashMap<Target, HashMap<AuxDependency, HashSet<AuxDependency>>>();
    private HashMap<AuxDependency, RefCountingCollection<AuxDependency>> auxtochildaux = 
        new HashMap<AuxDependency, RefCountingCollection<AuxDependency>>();

    private TargetDependencyRelation() {}

    public static TargetDependencyRelation getInstance() {
        return instance;
    }

    // Mapping of aux    -> tgen[N]
    //            tgen   -> aux[N]
    //            aux    -> target
    //            target -> aux 
    //            target -> aux->auxhchild
    //            aux    -> auxchild[N]
    
    public synchronized TreeSet<AuxDependency> getAllDependencies() {
        if (allauxs.isEmpty()) {
            return null;
        } else {
            return new TreeSet<AuxDependency>(allauxs.keySet());
        }
    }

    public synchronized TreeSet<AuxDependency> getProjectDependencies(TargetGenerator tgen) {
        RefCountingCollection<AuxDependency> auxsForTgen = tgentoaux.get(tgen);
        if (auxsForTgen == null) {
            return null;
        } else {
            return new TreeSet<AuxDependency>(auxsForTgen);
        }
    }

    public synchronized TreeSet<AuxDependency> getAllDependenciesForType(DependencyType type) {
        if (allauxs.isEmpty()) {
            return null;
        } else {
            TreeSet<AuxDependency> tmp = new TreeSet();
            for (Iterator<AuxDependency> i = allauxs.keySet().iterator(); i.hasNext(); ) {
                AuxDependency aux = i.next();
                if (aux.getType().equals(type)) {
                    tmp.add(aux);
                }
            }
            return tmp;
        }
    }

    public synchronized TreeSet<AuxDependency> getProjectDependenciesForType(TargetGenerator tgen, DependencyType type) {
        RefCountingCollection<AuxDependency> auxsForTgen = tgentoaux.get(tgen);
        if (auxsForTgen == null) {
            return null;
        } else {
            TreeSet<AuxDependency> tmp = new TreeSet<AuxDependency>();
            for (Iterator<AuxDependency> i = auxsForTgen.iterator(); i.hasNext(); ) {
                AuxDependency aux = i.next();
                if (aux.getType().equals(type)) {
                    tmp.add(aux);
                }
            }
            return tmp;
        }
    }

    public synchronized TreeSet<AuxDependency> getDependenciesForTarget(Target target) {
        TreeSet<AuxDependency> auxsForTarget = alltargets.get(target);
        if (auxsForTarget == null) {
            return null;
        } else {
            return (TreeSet<AuxDependency>) auxsForTarget.clone();
        }
    }

    public synchronized TreeSet<Target> getAffectedTargets(AuxDependency aux) {
        TreeSet<Target> targetsForAux = allauxs.get(aux);
        if (targetsForAux == null) {
            return null;
        } else {
            return (TreeSet<Target>) targetsForAux.clone();
        }
    }

    public synchronized TreeSet<TargetGenerator> getAffectedTargetGenerators(AuxDependency aux) {
        RefCountingCollection<TargetGenerator> tgensForAux = auxtotgen.get(aux);
        if (tgensForAux == null) {
            return null;
        } else {
            return new TreeSet<TargetGenerator>(tgensForAux);
        }
    }

    synchronized HashMap<AuxDependency, HashSet<AuxDependency>> getParentChildMapForTarget(Target target) {
        HashMap<AuxDependency, HashSet<AuxDependency>> pcForTarget = targettoparentchild.get(target);
        if (pcForTarget == null) {
            return null;
        } else {
            HashMap<AuxDependency, HashSet<AuxDependency>> retval = new HashMap<AuxDependency, HashSet<AuxDependency>>();
            for (Iterator<AuxDependency> i = pcForTarget.keySet().iterator(); i.hasNext();) {
                AuxDependency parent = i.next();
                if (pcForTarget.get(parent) != null) {
                    HashSet<AuxDependency> children = (HashSet<AuxDependency>) pcForTarget.get(parent).clone();
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
        HashMap<AuxDependency, HashSet<AuxDependency>> pcForTarget = targettoparentchild.get(target);
        if (pcForTarget == null) {
            return null;
        } else {
            HashSet<AuxDependency> children = pcForTarget.get(parent);
            if (children == null) {
                return null;
            } else {
                return new TreeSet<AuxDependency>(children);
            }
        }
    }
    
        
    public synchronized void addRelation(AuxDependency parent, AuxDependency aux, Target target) {

        if (parent != AuxDependencyManager.root && !checkLoopFree(parent, aux, target)) {
            throw new RuntimeException("*** FATAL *** Adding " + aux + " to Parent " + parent
                                       + " for target " + target.getFullName() + " would result in a LOOP!");
        }

        CAT.debug("+++ Adding relations " + target.getFullName() + " <-> " + aux.toString() + " / " + parent.toString());

        TargetGenerator tgen = target.getTargetGenerator();
        if (allauxs.get(aux) == null) {
            allauxs.put(aux, new TreeSet<Target>());
        }
        if (alltargets.get(target) == null) {
            alltargets.put(target, new TreeSet<AuxDependency>());
        }
        if (auxtotgen.get(aux) == null) {
            auxtotgen.put(aux, new RefCountingCollection<TargetGenerator>());
        }
        if (tgentoaux.get(tgen) == null) {
            tgentoaux.put(tgen, new RefCountingCollection<AuxDependency>());
        }
        if (targettoparentchild.get(target) == null) {
            targettoparentchild.put(target, new HashMap<AuxDependency, HashSet<AuxDependency>>());
        }
        if (auxtochildaux.get(parent) == null) {
            auxtochildaux.put(parent, new RefCountingCollection<AuxDependency>());
        }

        TreeSet<Target>                        targetsForAux        = allauxs.get(aux);
        TreeSet<AuxDependency>                 auxsForTarget        = alltargets.get(target);
        RefCountingCollection<TargetGenerator> tgensForAux          = auxtotgen.get(aux);
        RefCountingCollection<AuxDependency>   auxsForTgen          = tgentoaux.get(tgen);
        HashMap<AuxDependency, HashSet<AuxDependency>>  pcForTarget = targettoparentchild.get(target);
        RefCountingCollection<AuxDependency>   auxsForParent        = auxtochildaux.get(parent);
        
        if (targetsForAux.add(target)) { // Make sure to ignore multiple target<->aux relations
            auxsForTarget.add(aux);
            tgensForAux.add(tgen);
            auxsForTgen.add(aux);
        }
        
        HashSet<AuxDependency> children = pcForTarget.get(parent);
        if (children == null) {
            children = new HashSet<AuxDependency>();
            pcForTarget.put(parent, children);
        }

        
        if (children.add(aux)) { // Make sure to ignore multiple parent<->child relations
                                 // Note: we must allow here multiple entries for the same
                                 // child per target, but not per parent!
            auxsForParent.add(aux);
        }
    }

    public synchronized void resetRelation(Target target) {
        CAT.debug("--- Removing all relations for " + target.getFullName());

        TargetGenerator tgen = target.getTargetGenerator();

        TreeSet<AuxDependency> auxsForTarget = alltargets.get(target);

        if (auxsForTarget == null) {
            // CAT.debug("*** Trying to reset relations for unknown target " + target.getFullName() + " ***");  
            return;
        }
        if (auxsForTarget.isEmpty()) {
            CAT.error("*** Known target " + target.getFullName() + " has empty relation set! ***");  
            return;
        }

        RefCountingCollection<AuxDependency>   auxsForTgen   = tgentoaux.get(tgen);

        for (Iterator<AuxDependency> i = auxsForTarget.iterator(); i.hasNext();) {
            AuxDependency aux = i.next();
            if (aux.getType().isDynamic()) {
                TreeSet<Target>                        targetsForAux = allauxs.get(aux);
                RefCountingCollection<TargetGenerator> tgensForAux   = auxtotgen.get(aux);
                
                targetsForAux.remove(target);
                if (targetsForAux.isEmpty()) {
                    allauxs.remove(aux);
                }
                tgensForAux.remove(tgen);
                if (tgensForAux.isEmpty()) {
                    auxtotgen.remove(aux);
                }
                
                auxsForTgen.remove(aux);
                i.remove();
            }
        }
        if (auxsForTgen.isEmpty()) {
            tgentoaux.remove(tgen);
        }
        if (auxsForTarget.isEmpty()) {
            alltargets.remove(target);
        }

        HashMap<AuxDependency, HashSet<AuxDependency>> pcForTarget = targettoparentchild.get(target);
        for (Iterator<AuxDependency> i = pcForTarget.keySet().iterator(); i.hasNext(); ) {
            AuxDependency                        parent      = i.next();
            // System.out.println("---> parent: " + parent);
            HashSet<AuxDependency>               children    = pcForTarget.get(parent);
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
        if (pcForTarget.isEmpty()) {
            targettoparentchild.remove(target);
        }
    }

    public synchronized void resetAllRelations(Collection<Target> targets) {
        for (Iterator<Target> i = targets.iterator(); i.hasNext();) {
            Target target = i.next();
            resetRelation(target);
        }
    }
    
    private synchronized boolean checkLoopFree(AuxDependency parent, AuxDependency aux, Target target) {
        // The simple loop
        if (parent == aux) {
            return false;
        }

        HashMap<AuxDependency, HashSet<AuxDependency>> parentchild = 
            targettoparentchild.get(target);

        if (parentchild != null) {
            // Now iterate over all children of aux recursively and check if any of them is parent.
            HashSet<AuxDependency> children = parentchild.get(aux);
            if (children != null) {
                for (Iterator<AuxDependency> i = children.iterator(); i.hasNext();) {
                    AuxDependency child = i.next();
                    if (child.getType() == DependencyType.TEXT) {
                        if (!checkLoopFree(parent, child, target)) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }
    
}// DependencyRefCounter
