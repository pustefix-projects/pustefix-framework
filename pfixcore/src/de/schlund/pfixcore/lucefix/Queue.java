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

package de.schlund.pfixcore.lucefix;

/**
 * @author schuppi
 * @date Jun 14, 2005
 */
public class Queue {

    private QueuedTripel head  = null;
    private QueuedTripel tail  = null;
    private Object       mutex = new Object();
    private int          size  = 0;

    public void add(Tripel newtripel) {
        synchronized (mutex) {
            if (tail == null) {
                head = new QueuedTripel(newtripel, null);
                tail = head;
            } else {
                tail.setNext(new QueuedTripel(newtripel, null));
                tail = tail.nextTripel();
            }
            size++;
        }
    }

    public void addPrio(Tripel newtripel) {
        synchronized (mutex) {
            if (head == null) {
                head = new QueuedTripel(newtripel, null);
                tail = head;
            } else {
                QueuedTripel newelem = new QueuedTripel(newtripel, head);
                head = newelem;
            }
            size++;
        }
    }

    public Tripel next() {
        synchronized (mutex) {
            if (head == null) return null;
            
            size--;
            Tripel retval = head;
            if (head.next() == null) {
                head = null;
                tail = null;
                return retval;
            } else {
                head = head.nextTripel();
                return retval;
            }
        }
    }
    public int getSize(){
        return size;
    }
}