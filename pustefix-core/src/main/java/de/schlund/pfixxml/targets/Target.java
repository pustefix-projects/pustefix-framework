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

import java.util.TreeMap;
import java.util.TreeSet;

import org.w3c.dom.Document;

/**
 *
 *
 */

public interface Target extends Comparable<Target> {
    TargetType      getType();
    String          getTargetKey();
    TargetGenerator getTargetGenerator();
    Target          getXMLSource();
    Target          getXSLSource();
    Themes          getThemes();
    TreeMap<String, Object> getParams();
    TreeSet<PageInfo> getPageInfos();
    long            getModTime();
    String          toString();
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
    Document getDOM() throws TargetGenerationException;
}
