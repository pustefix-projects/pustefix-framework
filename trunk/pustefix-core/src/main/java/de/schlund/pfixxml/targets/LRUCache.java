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
import java.util.LinkedHashMap;
import java.util.Map;


public class LRUCache<T1, T2> extends LinkedHashMap<T1, T2> implements SPCache<T1, T2> {
    
    private static final long serialVersionUID = 598221489774574020L;

    private static int DEFAULT_MAX_ENTRIES = 30;

    private int maxEntries;
    
    public LRUCache() {
        super(DEFAULT_MAX_ENTRIES, 0.75f, true);
        this.maxEntries = DEFAULT_MAX_ENTRIES;
    }
    
    public void createCache(final int maxEntries) {
        if(maxEntries > 0) {
            this.maxEntries = maxEntries;
        }
        clear();
    }
    
    @SuppressWarnings("unchecked")
    public synchronized Iterator<T1> getIterator() {
        Map<T1, T2> clonedMap = (Map<T1, T2>)clone();
        return clonedMap.keySet().iterator();
    }
    
    public synchronized T2 getValue(T1 key) {
        return (T2)get(key);
    }
    
    public synchronized void setValue(T1 key, T2 value) {
        put(key, value);
    }
    
    public int getCapacity() {
        return maxEntries;
    }
    
    public synchronized int getSize() {
        return size();
    }

    @Override
    protected boolean removeEldestEntry(java.util.Map.Entry<T1, T2> eldest) {
        return size() > maxEntries;
    }

}
