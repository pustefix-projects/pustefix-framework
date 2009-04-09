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


/**
 * Dependency referencing a target that is supplied by a target generator.
 * The target generator is always the same the target referencing this aux
 * dependency is using.
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class AuxDependencyTarget extends AbstractAuxDependency {
    private TargetGenerator tgen;

    private String targetkey;

    private int hashCode;

    public AuxDependencyTarget(TargetGenerator tgen, String targetkey) {
        this.type = DependencyType.TARGET;
        this.tgen = tgen;
        this.targetkey = targetkey;
        this.hashCode = (tgen.getConfigPath().toString() + ":" + targetkey)
                .hashCode();
    }

    /**
     * Returns the referenced target object. This might be a virtual target
     * as well as a leaf target.
     * 
     * @return referenced target
     */
    public Target getTarget() {
        Target target = tgen.getTarget(targetkey);
        if (target == null) {
            target = tgen.createXMLLeafTarget(targetkey);
        }
        return target;
    }

    public long getModTime() {
        return this.getTarget().getModTime();
    }

    @Override
    public int compareTo(AuxDependency o) {
        int comp;

        comp = super.compareTo(o);
        if (comp != 0) {
            return comp;
        }

        AuxDependencyTarget a = (AuxDependencyTarget) o;
        
        comp = tgen.getConfigPath().compareTo(a.tgen.getConfigPath());
        if (comp != 0) {
            return comp;
        }
        
        return targetkey.compareTo(a.targetkey);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AuxDependencyTarget) {
            return this.compareTo((AuxDependency) obj) == 0;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return this.hashCode;
    }

    @Override
    public String toString() {
        String path = tgen.getConfigPath().toURI().toString();
        return "[AUX/" + getType() + " " + path + ": " + targetkey + "]";
    }

}
