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
import java.util.Iterator;
import java.util.TreeMap;
import java.util.TreeSet;

import de.schlund.pfixxml.resources.DocrootResource;

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
    private TreeMap                     includeparts = new TreeMap();
    private TreeMap                     images       = new TreeMap();
    private TreeMap                     files        = new TreeMap();
    private TreeMap                     targets      = new TreeMap();
    
    private AuxDependencyFactory() {}
    
    private AuxDependency root = new AbstractAuxDependency() {
        
        public DependencyType getType() {
            return DependencyType.ROOT;
        }
        
        public String toString() {
            return "[AUX/" + getType() + "]";
        }

        public long getModTime() {
            return 0;
        }
        
    };
    
    public static AuxDependencyFactory getInstance() {
        return instance;
    }
    
    public synchronized AuxDependency getAuxDependencyRoot() {
        return root;
    }
    
    public synchronized AuxDependencyInclude getAuxDependencyInclude(DocrootResource path, String part, String theme) {
        String key = DependencyType.TEXT.getTag() + "@" + path.toString() + "@" + part + "@" + theme;
        AuxDependencyInclude ret = (AuxDependencyInclude) includeparts.get(key);
        if (ret == null) {
            ret = new AuxDependencyInclude(path, part, theme);
            includeparts.put(key, ret);
        }
        return ret;
    }
    
    public synchronized AuxDependencyImage getAuxDependencyImage(DocrootResource path) {
        String key = path.toString();
        AuxDependencyImage ret = (AuxDependencyImage) images.get(key);
        if (ret == null) {
            ret = new AuxDependencyImage(path);
            images.put(key, ret);
        }
        return ret;
    }
    
    public synchronized AuxDependencyFile getAuxDependencyFile(DocrootResource path) {
        String key = path.toString();
        AuxDependencyFile ret = (AuxDependencyFile) files.get(key);
        if (ret == null) {
            ret = new AuxDependencyFile(path);
            files.put(key, ret);
        }
        return ret;
    }
    
    public synchronized AuxDependencyTarget getAuxDependencyTarget(TargetGenerator tgen, String targetkey) {
        String key = tgen.getConfigPath().toString() + ":" + targetkey;
        AuxDependencyTarget ret = (AuxDependencyTarget) targets.get(key);
        if (ret == null) {
            ret = new AuxDependencyTarget(tgen, targetkey);
            targets.put(key, ret);
        }
        return ret;
    }

    public synchronized TreeSet getAllAuxDependencies() {
        TreeSet retval =  new TreeSet();

        for (Iterator i = includeparts.values().iterator(); i.hasNext();) {
            AuxDependency aux = (AuxDependency) i.next();
            retval.add(aux);
        }

        for (Iterator i = images.values().iterator(); i.hasNext();) {
            AuxDependency aux = (AuxDependency) i.next();
            retval.add(aux);
        }
        
        for (Iterator i = files.values().iterator(); i.hasNext();) {
            AuxDependency aux = (AuxDependency) i.next();
            retval.add(aux);
        }

        for (Iterator i = targets.values().iterator(); i.hasNext();) {
            AuxDependency aux = (AuxDependency) i.next();
            retval.add(aux);
        }

        return retval;
    }
    
    public void reset() {
        includeparts = new TreeMap();
        images = new TreeMap();
        files = new TreeMap();
        targets = new TreeMap();
    }
    
    
}// AuxDependencyFactory
