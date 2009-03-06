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
 */

package de.schlund.pfixcore.editor2.core.spring;

import de.schlund.pfixcore.editor2.core.dom.Project;
import de.schlund.pfixcore.editor2.core.dom.Target;
import de.schlund.pfixxml.targets.AuxDependencyFile;

/**
 * Service providing methods to create Target objects
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public interface TargetFactoryService {
    /**
     * Creates Target using a Target object from the Pustefix generator
     * 
     * @param pfixTarget Pustefix generator Target object
     * @param project Project target belongs to
     * @return New Target object
     */
    Target getTargetFromPustefixTarget(de.schlund.pfixxml.targets.Target pfixTarget, Project project);
    
    /**
     * Creates Target using an AuxDependency object from the Pustefix generator.
     * Only intended to be used for auxilliary leaf targets.
     * 
     * @param auxdep AuxDependency object to use
     * @return New Target object
     */
    Target getLeafTargetFromPustefixAuxDependency(AuxDependencyFile auxdep);
}
