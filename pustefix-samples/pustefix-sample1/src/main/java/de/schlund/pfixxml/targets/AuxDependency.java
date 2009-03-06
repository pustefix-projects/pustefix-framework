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

/**
 * Defines method common to all types of dependencies being registered during
 * the generation of a target in the Pustefix target generator system.
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 * @see de.schlund.pfixxml.targets.TargetGenerator
 * @see de.schlund.pfixxml.targets.Target
 * @see de.schlund.pfixxml.targets.DependencyType
 */
public interface AuxDependency extends Comparable<AuxDependency> {
    /**
     * Returns the type of the dependency.
     * The type is important as it gives information about to which sub-type
     * a given instance of AuxDependency can be casted.
     * 
     * @return type of this dependency
     */
    DependencyType getType();
    
    /**
     * Returns a timestamp indicating when this dependency was last changed.
     * Note that, depending of the type of this dependency, it might have
     * changed later without being regenerated. Then the time returned will denote
     * the last time the dependency has been generated.
     *  
     * @return Timestamp indicating the last modification
     */
    long getModTime();
}
