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
package de.schlund.pfixxml.perflogging;

/**
 * @author jh
 *
 */
public class Interval {
    private long from;
    private long until;
    
    Interval(long from, long until) {
        this.from = from;
        this.until = until;
    }
    
    boolean contains(long value) {
        boolean ret = (value >= from && value < until);
        //System.out.println("Contains: "+value+":"+ret+"-->"+toString());
        return ret;
    }
    
    boolean isGreater(long value) {
        boolean ret = value >= until;
        //System.out.println("greater: "+value+":"+ret+"-->"+toString());
        return ret;
    }
    
    boolean isLess(long value) {
        boolean ret = value < from;
        //System.out.println("less: "+value+":"+ret+"-->"+toString());
        return ret;
    }
    
    public long getFrom() {
        return from;
    }
    
    public long getUntil() {
        return until;
    }
    
    @Override
    public String toString() {
        return "["+from+"-"+until+"]";
    }

}
