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

package org.pustefixframework.xmlgenerator.targets;
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
 * of a target via the addRelation(parent, child, target) method, and reset before gneration of the
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
	
    private static final Logger                   LOG      = Logger.getLogger(TargetDependencyRelation.class);
    
    private HashMap<AuxDependency, TreeSet<Target>> allauxs; 
    private HashMap<Target, TreeSet<AuxDependency>> alltargets;
    private HashMap<AuxDependency, RefCountingCollection<TargetGenerator>> auxtotgen; 
    private HashMap<TargetGenerator, RefCountingCollection<AuxDependency>> tgentoaux;
    private HashMap<Target, HashMap<AuxDependency, HashSet<AuxDependency>>> targettoparentchild;
    private HashMap<AuxDependency, RefCountingCollection<AuxDependency>> auxtochildaux;
//     private HashMap<Target, HashMap<AuxDependency, HashSet<AuxDependency>>> targettochildparent;
//     private HashMap<AuxDependency, RefCountingCollection<AuxDependency>> auxtoparentaux;
    
    public TargetDependencyRelation() {
        reset();
    }

    public void reset() {
        allauxs = new HashMap<AuxDependency, TreeSet<Target>>();
        alltargets = new HashMap<Target, TreeSet<AuxDependency>>();
        auxtotgen = new HashMap<AuxDependency, RefCountingCollection<TargetGenerator>>();
        tgentoaux = new HashMap<TargetGenerator, RefCountingCollection<AuxDependency>>();
        targettoparentchild = new HashMap<Target, HashMap<AuxDependency, HashSet<AuxDependency>>>();
        auxtochildaux = new HashMap<AuxDependency, RefCountingCollection<AuxDependency>>();
//        targettochildparent = new HashMap<Target, HashMap<AuxDependency, HashSet<AuxDependency>>>();
//        auxtoparentaux = new HashMap<AuxDependency, RefCountingCollection<AuxDependency>>();
    }
    
    
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

    public synchronized TreeSet<TargetGenerator> getAffectedTargetGenerators(AuxDependency aux) {
        RefCountingCollection<TargetGenerator> tgensForAux = auxtotgen.get(aux);
        if (tgensForAux == null) {
            return null;
        } else {
            return new TreeSet<TargetGenerator>(tgensForAux);
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

//     public synchronized HashMap<AuxDependency, HashSet<AuxDependency>> getChildParentMapForTarget(Target target) {
//         HashMap<AuxDependency, HashSet<AuxDependency>> childparentForTarget = targettochildparent.get(target);
//         if (childparentForTarget == null) {
//             return null;
//         } else {
//             HashMap<AuxDependency, HashSet<AuxDependency>> retval = new HashMap<AuxDependency, HashSet<AuxDependency>>();
//             for (Iterator<AuxDependency> i = childparentForTarget.keySet().iterator(); i.hasNext();) {
//                 AuxDependency child = i.next();
//                 if (childparentForTarget.get(child) != null) {
//                     HashSet<AuxDependency> parents = (HashSet<AuxDependency>) childparentForTarget.get(child).clone();
//                     retval.put(child, parents);
//                 }
//             }
//             return retval;
//         }
//     }
        
//     public synchronized TreeSet<AuxDependency> getParentsOverallForAuxDependency(AuxDependency child) {
//         RefCountingCollection<AuxDependency> parents = auxtoparentaux.get(child);
//         if (parents == null) {
//             return null;
//         } else {
//             return new TreeSet<AuxDependency>(parents);
//         }
//     }

//     public synchronized TreeSet<AuxDependency> getParentsForTargetForAuxDependency(Target target, AuxDependency child) {
//         HashMap<AuxDependency, HashSet<AuxDependency>> childparentForTarget = targettochildparent.get(target);
//         if (childparentForTarget == null) {
//             return null;
//         } else {
//             HashSet<AuxDependency> parents = childparentForTarget.get(child);
//             if (parents == null) {
//                 return null;
//             } else {
//                 return new TreeSet<AuxDependency>(parents);
//             }
//         }
//     }

    public synchronized void addRelation(AuxDependency parent, AuxDependency aux, Target target) {

    	TargetGenerator tgen = target.getTargetGenerator();
    	
    	if (parent != tgen.getAuxDependencyFactory().getAuxDependencyRoot() && !checkLoopFree(parent, aux, target)) {
            throw new RuntimeException("*** FATAL *** Adding " + aux + " to Parent " + parent
                                       + " for target " + target.getFullName() + " would result in a LOOP!");
        }

        LOG.debug("+++ Adding relations " + target.getFullName() + " <-> " + aux.toString() + " / " + parent.toString());

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
//         if (targettochildparent.get(target) == null) {
//             targettochildparent.put(target, new HashMap<AuxDependency, HashSet<AuxDependency>>());
//         }
//         if (auxtoparentaux.get(aux) == null) {
//             auxtoparentaux.put(aux, new RefCountingCollection<AuxDependency>());
//         }


        TreeSet<Target>                        targetsForAux       = allauxs.get(aux);
        TreeSet<AuxDependency>                 auxsForTarget       = alltargets.get(target);
        RefCountingCollection<TargetGenerator> tgensForAux         = auxtotgen.get(aux);
        RefCountingCollection<AuxDependency>   auxsForTgen         = tgentoaux.get(tgen);
        HashMap<AuxDependency, HashSet<AuxDependency>> parentchildForTarget = targettoparentchild.get(target);
        RefCountingCollection<AuxDependency>   auxsForParent       = auxtochildaux.get(parent);
//         HashMap<AuxDependency, HashSet<AuxDependency>> childparentForTarget = targettochildparent.get(target);
//         RefCountingCollection<AuxDependency>   auxsForChild        = auxtoparentaux.get(aux);
        
        if (targetsForAux.add(target)) { // Make sure to ignore multiple target<->aux relations
            auxsForTarget.add(aux);
            tgensForAux.add(tgen);
            auxsForTgen.add(aux);
        }
        
        HashSet<AuxDependency> children = parentchildForTarget.get(parent);
        if (children == null) {
            children = new HashSet<AuxDependency>();
            parentchildForTarget.put(parent, children);
        }
//         HashSet<AuxDependency> parents = childparentForTarget.get(aux);
//         if (parents == null) {
//             parents = new HashSet<AuxDependency>();
//             childparentForTarget.put(aux, parents);
//         }
        
        if (children.add(aux)) { // Make sure to ignore multiple parent<->child relations
                                 // Note: we must allow here multiple entries for the same
                                 // child per target, but not per parent!
            auxsForParent.add(aux);
//             parents.add(parent);
//             auxsForChild.add(parent);
        }
    }

    public synchronized void resetRelation(Target target) {
        LOG.debug("--- Removing all relations for " + target.getFullName());

        TargetGenerator tgen = target.getTargetGenerator();

        TreeSet<AuxDependency> auxsForTarget = alltargets.get(target);

        if (auxsForTarget == null) {
            // CAT.debug("*** Trying to reset relations for unknown target " + target.getFullName() + " ***");  
            return;
        }
        if (auxsForTarget.isEmpty()) {
            LOG.error("*** Known target " + target.getFullName() + " has empty relation set! ***");  
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

//         HashMap<AuxDependency, HashSet<AuxDependency>> childparentForTarget = targettochildparent.get(target);
//         for (Iterator<AuxDependency> i = childparentForTarget.keySet().iterator(); i.hasNext(); ) {
//             AuxDependency                        child = i.next();
//             if (!child.getType().isDynamic()) { // YES! child.getType() here, not parent.getType() in the inner loop is correct!
//                 continue;
//             }
//             HashSet<AuxDependency>               parents    = childparentForTarget.get(child);
//             RefCountingCollection<AuxDependency> allparents = auxtoparentaux.get(child);
//             for (Iterator<AuxDependency> j = parents.iterator(); j.hasNext();) {
//                 AuxDependency parent = j.next();
//                 allparents.remove(parent);
//                 j.remove();
//             }
//             if (parents.isEmpty()) {
//                 i.remove();
//             }
//             if (allparents.isEmpty()) {
//                 auxtoparentaux.remove(child);
//             }
//         }
//         if (childparentForTarget.isEmpty()) {
//             targettochildparent.remove(target);
//         }
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
