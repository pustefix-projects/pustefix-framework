package de.schlund.pfixxml.util;

import java.util.Collection;

/**
 * Describe class SortedRefCountingCollection here.
 *
 *
 * Created: Wed Nov 16 08:51:21 2005
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 * @version 1.0
 */
public class SortedRefCountingCollection<E> extends RefCountingCollection<E> {

    /**
     * Creates a new <code>SortedRefCountingCollection</code> instance.
     *
     */
    public SortedRefCountingCollection() {
        init(true);
    }
    
    public SortedRefCountingCollection(Collection<? extends E> collection) {
        init(true);
        addAll(collection);
    }

}
