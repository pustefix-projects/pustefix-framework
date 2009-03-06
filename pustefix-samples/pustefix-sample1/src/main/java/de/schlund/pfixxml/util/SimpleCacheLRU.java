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

    protected boolean removeEldestEntry(Entry<K,V> eldest) {
        return size() > maxsize;
    }
}
