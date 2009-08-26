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
import java.util.Iterator;
import java.util.TreeMap;
import java.util.TreeSet;

import org.pustefixframework.resource.Resource;
import org.pustefixframework.resource.ResourceLoader;


/**
 * AuxDependencyFactory.java
 *
 *
 * Created: Fri Jul 13 13:31:32 2001
 *
 *
 */

public class AuxDependencyFactory {
    
    private TreeMap<String, AuxDependencyInclude> includeparts = new TreeMap<String, AuxDependencyInclude>();
    private TreeMap<String, AuxDependencyImage> images = new TreeMap<String, AuxDependencyImage>();
    private TreeMap<String, AuxDependencyFile> files = new TreeMap<String, AuxDependencyFile>();
    private TreeMap<String, AuxDependencyTarget> targets = new TreeMap<String, AuxDependencyTarget>();
    
    private TargetDependencyRelation targetDependencyRelation;
    
    public AuxDependencyFactory(TargetDependencyRelation targetDependencyRelation) {
    	this.targetDependencyRelation = targetDependencyRelation;
    }
    
    private AuxDependency root = new AbstractAuxDependency(null) {
        
        @Override
        public DependencyType getType() {
            return DependencyType.ROOT;
        }
        
        @Override
        public String toString() {
            return "[AUX/" + getType() + "]";
        }

        public long getModTime() {
            return 0;
        }
        
    };
    
    public TargetDependencyRelation getTargetDependencyRelation() {
    	return targetDependencyRelation;
    }
    
    public synchronized AuxDependency getAuxDependencyRoot() {
        return root;
    }
    
    public synchronized AuxDependencyInclude getAuxDependencyInclude(Resource path, ResourceLoader resourceLoader, String part, String theme) {
        String key = DependencyType.TEXT.getTag() + "@" + path.toString() + "@" + part + "@" + theme;
        AuxDependencyInclude ret = includeparts.get(key);
        if (ret == null) {
            ret = new AuxDependencyInclude(targetDependencyRelation, path, resourceLoader, part, theme);
            includeparts.put(key, ret);
        }
        return ret;
    }
    
    public synchronized AuxDependencyImage getAuxDependencyImage(Resource path, ResourceLoader resourceLoader) {
        String key = path.toString();
        AuxDependencyImage ret = (AuxDependencyImage) images.get(key);
        if (ret == null) {
            ret = new AuxDependencyImage(targetDependencyRelation, path, resourceLoader);
            images.put(key, ret);
        }
        return ret;
    }
    
    public synchronized AuxDependencyFile getAuxDependencyFile(Resource path, ResourceLoader resourceLoader) {
        String key = path.toString();
        AuxDependencyFile ret = (AuxDependencyFile) files.get(key);
        if (ret == null) {
            ret = new AuxDependencyFile(targetDependencyRelation, path, resourceLoader);
            files.put(key, ret);
        }
        return ret;
    }
    
    public synchronized AuxDependencyTarget getAuxDependencyTarget(TargetGenerator tgen, String targetkey) {
        String key = targetkey;
        AuxDependencyTarget ret = (AuxDependencyTarget) targets.get(key);
        if (ret == null) {
            ret = new AuxDependencyTarget(tgen, targetkey);
            targets.put(key, ret);
        }
        return ret;
    }

    public synchronized TreeSet<AuxDependency> getAllAuxDependencies() {
        TreeSet<AuxDependency> retval =  new TreeSet<AuxDependency>();

        for (Iterator<AuxDependencyInclude> i = includeparts.values().iterator(); i.hasNext();) {
            AuxDependency aux = i.next();
            retval.add(aux);
        }

        for (Iterator<AuxDependencyImage> i = images.values().iterator(); i.hasNext();) {
            AuxDependency aux = i.next();
            retval.add(aux);
        }
        
        for (Iterator<AuxDependencyFile> i = files.values().iterator(); i.hasNext();) {
            AuxDependency aux = i.next();
            retval.add(aux);
        }

        for (Iterator<AuxDependencyTarget> i = targets.values().iterator(); i.hasNext();) {
            AuxDependency aux = i.next();
            retval.add(aux);
        }

        return retval;
    }
    
    public void reset() {
        includeparts = new TreeMap<String, AuxDependencyInclude>();
        images = new TreeMap<String, AuxDependencyImage>();
        files = new TreeMap<String, AuxDependencyFile>();
        targets = new TreeMap<String, AuxDependencyTarget>();
    }
    
    
}// AuxDependencyFactory
