package de.schlund.pfixxml.util;

import java.util.Collection;
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



    
 

    
