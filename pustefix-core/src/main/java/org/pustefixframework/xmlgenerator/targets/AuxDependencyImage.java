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

import org.pustefixframework.resource.Resource;



/**
 * Dependency referencing an image that is embedded into a target.
 * Usually not the images content itself, but a reference to it is included
 * in a target, but that reference may contain information retrieved from the
 * image file (like height and width attributes).
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class AuxDependencyImage extends AuxDependencyFile {

    public AuxDependencyImage(TargetDependencyRelation targetDependencyRelation, Resource path) {
        super(targetDependencyRelation, path);
        this.type = DependencyType.IMAGE;
    }

}
