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
