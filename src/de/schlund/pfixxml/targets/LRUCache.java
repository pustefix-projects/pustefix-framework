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

import java.util.Enumeration;
import java.util.Iterator;
import java.util.TreeMap;

import org.apache.log4j.Category;
import org.apache.oro.util.CacheLRU;

/**
 *
 *
 */

public class LRUCache implements SPCache {
    private static       Category CAT          = Category.getInstance(LRUCache.class.getName());
    public  static final int      DEFAULT_SIZE = 30;

    private CacheLRU cache;
    
    public LRUCache() {
        // nothing to do
        cache= new CacheLRU(DEFAULT_SIZE);
    }
    
    public void createCache(int capacity) {
        if(capacity <= 0 )
            capacity= DEFAULT_SIZE;
        cache= new CacheLRU(capacity);
    }
    
    public Iterator getIterator() {
        TreeMap tmphash = new TreeMap();
        synchronized (cache) {
            for (Iterator iter = cache.keys(); iter.hasNext(); ) {
                String k = (String) iter.next();
                tmphash.put(k, cache.getElement(k));
            }
        }
        return tmphash.keySet().iterator();
    }
    
    public Object getValue(Object key) {
        Object retval;
        synchronized (cache) {
            retval = cache.getElement(key);
        }
        return retval;
    }
    
    public void setValue(Object key, Object value) {
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
