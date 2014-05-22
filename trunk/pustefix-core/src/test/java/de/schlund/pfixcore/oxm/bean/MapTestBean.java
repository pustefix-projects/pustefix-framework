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
package de.schlund.pfixcore.oxm.bean;

import java.util.HashMap;
import java.util.Map;

import de.schlund.pfixcore.oxm.impl.annotation.MapSerializer;

/**
 * Test bean for MapSerializer
 * 
 * @author Stephan Schmidt <schst@stubbles.net>
 */
public class MapTestBean {
    
    public Map<String, String> myMap = new HashMap<String, String>();
    
    @MapSerializer(elementName = "element")
    public Map<String, String> annoMap = new HashMap<String, String>();
    
    public MapTestBean() {
        this.myMap.put("one", "foo");
        this.myMap.put("two", "bar");

        this.annoMap.put("one", "foo");
        this.annoMap.put("two", "bar");
    }
}