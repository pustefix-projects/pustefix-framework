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
public class QueuedTripel extends Tripel implements QueueElement {

    private QueuedTripel next;
    
    /**
     * @param product
     * @param part
     * @param filename
     * @param next
     */
    public QueuedTripel(String product, String part, String filename, QueuedTripel next, Tripel.Type type) {
        super(product, part, filename, type);
        this.next = next;
    }
    public QueuedTripel(Tripel tripel, QueuedTripel next){
        super(tripel.getProduct(),tripel.getPart(),tripel.getFilename(),tripel.getType());
        this.next = next;
    }
    public QueueElement next() {
        return next;
    }
    
    public QueuedTripel nextTripel(){
        return next;
    }

    public void setNext(QueuedTripel next) {
        this.next = next;
    }

    public void setNext(QueueElement next) {
        if (next instanceof QueuedTripel)
            setNext((QueuedTripel)next);
        throw new RuntimeException(next + " is not a valid QueuedTripel");
    }

}
