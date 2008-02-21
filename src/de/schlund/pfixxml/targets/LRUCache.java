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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.oro.util.CacheLRU;

public class LRUCache<T1, T2> implements SPCache<T1, T2> {
    // private static final Logger   LOG          = Logger.getLogger(LRUCache.class);
    public  static final int DEFAULT_SIZE = 30;

    private CacheLRU cache;
    
    public LRUCache() {
        // nothing to do
        cache = new CacheLRU(DEFAULT_SIZE);
    }
    
    public void createCache(int capacity) {
        if (capacity <= 0 )
            capacity = DEFAULT_SIZE;
        cache = new CacheLRU(capacity);
    }
    
    @SuppressWarnings("unchecked")
    public Iterator<T1> getIterator() {
        Map<T1, T2> tmphash = new HashMap<T1, T2>();
        synchronized (cache) {
            for (Iterator<T1> iter = cache.keys(); iter.hasNext(); ) {
                T1 k = iter.next();
                tmphash.put(k, (T2) cache.getElement(k));
            }
        }
        return tmphash.keySet().iterator();
    }
    
    @SuppressWarnings("unchecked")
    public T2 getValue(T1 key) {
        T2 retval;
        synchronized (cache) {
            retval = (T2) cache.getElement(key);
        }
        return retval;
    }
    
    public void setValue(T1 key, T2 value) {
        // CAT.debug("*** LRU *** Setting " + key + " (free: " + cache.size() + "/" + cache.capacity() +")");
        synchronized (cache) { 
            cache.addElement(key, value);
        }
    }
    
    public int getCapacity() {
        return cache.capacity();
    }
    
    public int getSize() {
        return cache.size();
    }
}
