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
 */

package de.schlund.pfixxml.util;

import java.util.Iterator;
import java.util.Map;

/**
 * Describe class RefCountingCollectionIterator here.
 *
 *
 * Created: Wed Nov 16 00:18:45 2005
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 * @version 1.0
 */


public class RefCountingCollectionIterator<E> implements Iterator<E> {
    Iterator<E>              colliter;
    RefCountingCollection<E> coll;
    E                        current;
    Map<E, Integer>          map;
    
    RefCountingCollectionIterator(RefCountingCollection<E> coll, Map map) {
        this.map  = map;
        this.coll = coll;
        colliter = (Iterator<E>) map.keySet().iterator();
    }
    
    public boolean hasNext() {
        return colliter.hasNext();
    }
    
    public E next() {
        current = colliter.next();
        return current;
    }
    
    public void remove() {
        remove(map.get(current));
    }
    
    public void remove(int count) {
        int current_count = coll.getCardinality(current);
        if (count < current_count) {
            coll.remove(current, count);
        } else {
            colliter.remove();
            coll.fullsize = coll.fullsize - current_count;
        }
    }
}



    
 

    
