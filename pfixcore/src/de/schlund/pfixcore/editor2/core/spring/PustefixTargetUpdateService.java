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

import de.schlund.pfixxml.targets.Target;
import de.schlund.pfixxml.targets.TargetGenerator;

/**
 * Service running in background, updating pages when a dependency has changed.
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public interface PustefixTargetUpdateService {
    /**
     * Adds a target to the queue, so that it is updated.
     * 
     * @param target The target to do the update for
     */
    void registerTargetForUpdate(Target target);
    
    /**
     * Registers a TargetGenerator for periodical updates of its
     * top-level targets.
     * 
     * @param tgen TargetGenerator to do updates for
     */
    void registerTargetGeneratorForUpdateLoop(TargetGenerator tgen);
}
