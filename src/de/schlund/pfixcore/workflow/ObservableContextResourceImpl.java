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

package de.schlund.pfixcore.workflow;

import java.util.Iterator;
import java.util.Vector;

import org.apache.log4j.Logger;

/**
 * Implements an observable ContextResource. Observers will be
 * notified, by calling update(this), if the method notifiyObservers()
 * is called.
 * 
 * @author <a href="mailto:stieler@schlund.de">Thomas Stieler</a>
 *
 *
 */

public abstract class ObservableContextResourceImpl implements ContextResource, ObservableContextResource {
    private Vector<ContextResourceObserver> observers = new Vector<ContextResourceObserver>();
    private Logger LOG = Logger.getLogger(this.getClass());

    /**
     * Adds an observer to the list of registered observers.
     *
     * @param obj an <code>ContextResourceObserver</code> value
     */
    public synchronized void addObserver(ContextResourceObserver obj) {
        LOG.debug("Adding observer " + obj.getClass().getName());
        observers.add(obj);
    }

    /**
     * Notifies all registered observers by calling update(this).
     *
     */
    public synchronized void notifyObservers() throws Exception {
        Iterator<ContextResourceObserver> iter = observers.iterator();
        while (iter.hasNext()) {
            ContextResourceObserver obj = iter.next();
            LOG.debug("Notify observer" + obj.getClass().getName());
            obj.update(this);
            LOG.debug("notify done..." + obj.getClass().getName());
        }
    }
}
