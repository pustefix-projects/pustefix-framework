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
import org.apache.log4j.*;
import java.util.*;

/**
 * DependencyRefCounter.java
 *
 *
 * Created: Thu Jul 19 21:48:57 2001
 *
 * @author <a href="mailto: "Jens Lautenbacher</a>
 *
 *
 */

public class DependencyRefCounter {
    private static Category CAT = Category.getInstance(DependencyRefCounter.class.getName());
    private HashMap alldeps    = new HashMap();
    private HashMap alltargets = new HashMap();
    
    public TreeSet getAllDependencies() {
        synchronized (alldeps) {
            return new TreeSet(alldeps.keySet());
        }
    }

    public TreeSet getDependenciesOfType(DependencyType type) {
        TreeSet tmp = new TreeSet();
        synchronized (alldeps) {
            for (Iterator i = alldeps.keySet().iterator(); i.hasNext(); ) {
                AuxDependency aux = (AuxDependency) i.next();
                if (aux.getType().equals(type)) {
                    tmp.add(aux);
                }
            }
            return tmp;
        }
    }


    public void ref(AuxDependency aux, Target target) {
        synchronized (alldeps) {
            if (alldeps.get(aux) == null) {
                alldeps.put(aux, new HashSet());
            }
            if (alltargets.get(target) == null) {
                alltargets.put(target, new HashSet());
            }
            HashSet tmp  = (HashSet) alldeps.get(aux);
            CAT.debug("+++ Refing for target " + target.getTargetKey() + " aux " +
                     aux.getType() + " " + aux.getPath() + "@" + aux.getPart());
            tmp.add(target);
            HashSet tmp2 = (HashSet) alltargets.get(target);
            tmp2.add(aux);
        }
    }

    public void unref(Target target) {
        synchronized (alldeps) {
            HashSet affaux = (HashSet) alltargets.get(target);
            if (affaux != null) {
                for (Iterator i = affaux.iterator(); i.hasNext(); ) {
                    AuxDependency aux = (AuxDependency) i.next();
                    if (aux.isDynamic()) {
                        CAT.debug("--- Unrefing aux " + aux.getType() + " " + aux.getPath() + "@" + aux.getPart());
                        HashSet targets = (HashSet) alldeps.get(aux);
                        targets.remove(target);
                        if (targets.size() == 0) {
                            alldeps.remove(aux);
                        }
                        i.remove();
                    }
                }
            } else {
                CAT.debug("*** Trying unref for Target " + target.getTargetKey() +
                          " where no mapping T->(A1...An) exists!");
            }
        }
    }

    public String toString() {
        StringBuffer retval = new StringBuffer();
        synchronized (alldeps) {
            for (Iterator i = getAllDependencies().iterator(); i.hasNext(); ) {
                AuxDependency aux = (AuxDependency) i.next();
                if (aux.getType() == DependencyType.TEXT) {
                    retval.append("A->(T): " + aux.getPart() + "@" + aux.getPath() + "\n");
                } else {
                    retval.append("A->(T): " + aux.getPath() + "\n");
                }
                HashSet targets = (HashSet) alldeps.get(aux);
                for (Iterator j = targets.iterator(); j.hasNext(); ) {
                    Target tmp = (Target) j.next();
                    retval.append("        " + tmp.getTargetKey() + "\n");
                }
            }
            for (Iterator i = alltargets.keySet().iterator(); i.hasNext(); ) {
                Target tmp = (Target) i.next();
                retval.append("T->(A): " + tmp.getTargetKey() + "\n");
                HashSet auxs = (HashSet) alltargets.get(tmp);
                for (Iterator j = auxs.iterator(); j.hasNext(); ) {
                    AuxDependency aux = (AuxDependency) j.next();
                    if (aux.getType() == DependencyType.FILE) {
                        retval.append("        " + aux.getPart() + "@" + aux.getPath() + "\n");
                    } else {
                        retval.append("        " + aux.getPath() + "\n");
                    }
                }
            }
        }
        
        return retval.toString();
    }
    
}// DependencyRefCounter
