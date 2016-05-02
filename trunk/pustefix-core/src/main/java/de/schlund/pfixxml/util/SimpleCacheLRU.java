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
package de.schlund.pfixxml.util;

import java.util.LinkedHashMap;
import java.util.Map.Entry;

/**
 * Describe class CacheLRU here.
 *
 *
 * Created: Tue Jul  4 10:35:21 2006
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 * @version 1.0
 */
public class SimpleCacheLRU<K,V> extends LinkedHashMap<K,V> {
    /**
     * 
     */
    private static final long serialVersionUID = 2578526678043152277L;
    int maxsize = 1;
    
    /**
     * Creates a new <code>SimpleCacheLRU</code> instance.
     *
     */

    public SimpleCacheLRU(int maxsize) {
        super(8, 0.75f, true);
        if (maxsize > 0) {
            this.maxsize = maxsize;
        }
    }

    @Override
    protected boolean removeEldestEntry(Entry<K,V> eldest) {
        return size() > maxsize;
    }
}
