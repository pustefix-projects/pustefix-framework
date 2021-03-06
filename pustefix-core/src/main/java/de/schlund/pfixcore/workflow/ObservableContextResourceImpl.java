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

package de.schlund.pfixcore.workflow;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements an observable ContextResource. Observers will be
 * notified, by calling update(this), if the method notifiyObservers()
 * is called.
 * 
 * @author <a href="mailto:stieler@schlund.de">Thomas Stieler</a>
 *
 *
 */
public abstract class ObservableContextResourceImpl implements ObservableContextResource {

    private List<ContextResourceObserver> observers = new CopyOnWriteArrayList<ContextResourceObserver>();
    private Logger LOG = LoggerFactory.getLogger(this.getClass());

    /**
     * Adds an observer to the list of registered observers.
     *
     * @param obj an <code>ContextResourceObserver</code> value
     */
    public void addObserver(ContextResourceObserver obj) {
        LOG.debug("Adding observer " + obj.getClass().getName());
        observers.add(obj);
    }

    /**
     * Notifies all registered observers by calling update(this).
     *
     */
    public void notifyObservers() throws Exception {
        Iterator<ContextResourceObserver> iter = observers.iterator();
        while (iter.hasNext()) {
            ContextResourceObserver obj = iter.next();
            LOG.debug("Notify observer" + obj.getClass().getName());
            obj.update(this);
            LOG.debug("notify done..." + obj.getClass().getName());
        }
    }
}
