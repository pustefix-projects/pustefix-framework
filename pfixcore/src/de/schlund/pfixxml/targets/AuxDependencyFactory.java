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
import java.util.*;
import de.schlund.pfixxml.util.Path;

/**
 * AuxDependencyFactory.java
 *
 *
 * Created: Fri Jul 13 13:31:32 2001
 *
 *
 */

public class AuxDependencyFactory {
    private static AuxDependencyFactory instance     = new AuxDependencyFactory();
    private TreeMap              includeparts = new TreeMap();
    
    private AuxDependencyFactory() {}
    
    public static AuxDependencyFactory getInstance() {
        return instance;
    }

    public synchronized AuxDependency getAuxDependency(DependencyType type, Path path, String part, String product) {
        String        key = type.getTag() + "@" + path.getRelative() + "@" + part + "@" + product;
        AuxDependency ret = (AuxDependency) includeparts.get(key);
        if (ret == null) {
            ret = type.newInstance(path, part, product);
            includeparts.put(key, ret);
        }
        return ret;
    }

    public TreeSet getAllAuxDependencies() {
        TreeSet retval =  new TreeSet();
        synchronized (includeparts) {
            for (Iterator i = includeparts.values().iterator(); i.hasNext();) {
                AuxDependency aux = (AuxDependency) i.next();
                retval.add(aux);
            }
        }
        return retval;
    }
    
    public void reset() {
        includeparts = new TreeMap();
    }
    
    
}// AuxDependencyFactory
