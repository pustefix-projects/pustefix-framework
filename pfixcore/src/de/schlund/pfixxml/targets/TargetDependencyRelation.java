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
import java.util.HashMap;
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
    private static Category                 CAT      = Category.getInstance(DependencyRefCounter.class.getName());
    private static TargetDependencyRelation instance = new TargetDependencyRelation();
    
    private HashMap<AuxDependency, TreeSet<Target>> allauxs                          = 
        new HashMap<AuxDependency, TreeSet<Target>>();
    private HashMap<Target, TreeSet<AuxDependency>> alltargets                       = 
        new HashMap<Target, TreeSet<AuxDependency>>();
    private HashMap<AuxDependency, RefCountingCollection<TargetGenerator>> auxtotgen = 
        new HashMap<AuxDependency, RefCountingCollection<TargetGenerator>>(); 
    private HashMap<TargetGenerator, RefCountingCollection<AuxDependency>> tgentoaux = 
        new HashMap<TargetGenerator, RefCountingCollection<AuxDependency>>();
    

    private TargetDependencyRelation() {}

    public static TargetDependencyRelation getInstance() {
        return instance;
    }

    // Mapping of aux    -> tgen
    //            tgen   -> aux
    //            aux    -> target
    //            target -> aux
    
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
            return new TreeSet(auxsForTgen);
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
            TreeSet<AuxDependency> tmp = new TreeSet();
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
    
    public synchronized void addRelation(AuxDependency aux, Target target) {
        CAT.debug("+++ Adding relations " + target.getFullName() + " <-> " + aux.toString());

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
        TreeSet<Target>                        targetsForAux = allauxs.get(aux);
        TreeSet<AuxDependency>                 auxsForTarget = alltargets.get(target);
        RefCountingCollection<TargetGenerator> tgensForAux   = auxtotgen.get(aux);
        RefCountingCollection<AuxDependency>   auxsForTgen   = tgentoaux.get(tgen);
        
        if (auxsForTarget.add(aux)) { // Make sure to ignore multiple target<->aux relations
            targetsForAux.add(target);
            tgensForAux.add(tgen);
            auxsForTgen.add(aux);
        }
    }

    public synchronized void resetRelation(Target target) {
        CAT.debug("--- Removing all relations for " + target.getFullName());

        TargetGenerator tgen = target.getTargetGenerator();

        TreeSet<AuxDependency> auxsForTarget = alltargets.get(target);
        if (auxsForTarget == null) {
            CAT.error("*** Trying to reset relations for unknown target " + target.getFullName() + " ***");  
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
    }

    
}// DependencyRefCounter
