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
 * StaticFileDependency.java
 *
 *
 * Created: Thu Jul 19 13:25:21 2001
 *
 * @author <a href="mailto: jtl@schlund.de">Jens Lautenbacher</a>
 *
 *
 */

public class StaticFileDependency extends FileDependency {
    
    public boolean isDynamic() { return false; }

    public boolean removeDependencyParent(DependencyParent v) {
        return false; 
    }

    public StaticFileDependency (DependencyType type, String path) {
        this.type    = type;
        this.path    = path;
        this.part    = null;
        this.product = null;
        if (path == null) {
            throw new RuntimeException("Need Path to construct StaticFileDependency");
        }
        dir = path.substring(0,path.lastIndexOf("/"));
    }

}// StaticFileDependency
