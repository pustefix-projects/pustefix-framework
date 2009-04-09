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

import java.util.Iterator;
import java.util.TreeMap;

/**
 *
 *
 */

public class NullCache<T1, T2> implements SPCache<T1, T2> {

    public NullCache() {
        // ???
        super();
    }
    
    public void createCache(int capacity) {
        // nothing to do 
    }
    
    public Iterator<T1> getIterator() {
        TreeMap<T1, T2> tmphash = new TreeMap<T1, T2>();
        return tmphash.keySet().iterator();
    }
    
    public T2 getValue(T1 key) {
        return null;
    }
    
    public void setValue(T1 key, T2 value) {
        // nothing here...
    }
    
    public int getCapacity() {
        return 0;
    }
    
    public int getSize() {
        return 0;
    }
}
