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

import java.util.TreeMap;
import java.util.TreeSet;

/**
 *
 *
 */

public interface Target {
    TargetType getType();
    String     getTargetKey();
    
    AuxDependencyManager getAuxDependencyManager();
    TargetGenerator      getTargetGenerator();

    Target  getXMLSource();
    Target  getXSLSource(); 

    TreeMap getParams();
    TreeSet getPageInfos();
   
    /**
     * Get the value of the target. Depending on the 
     * circumstances this will trigger a recursive 
     * generation of the target.</br> 
     * @return the value of this target.
     * @throws TargetGenerationException on 
     * known errors which can occur on target 
     * generation. 
     */
    Object  getValue() throws TargetGenerationException;
    
    boolean needsUpdate() throws Exception;
    long    getModTime();
    String  toString();
}
