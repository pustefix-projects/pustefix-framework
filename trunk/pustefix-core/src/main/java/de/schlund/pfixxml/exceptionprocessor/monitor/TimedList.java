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

package de.schlund.pfixxml.exceptionprocessor.monitor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Data structure storing entries in a list limited by the
 * number of entries and the age.
 * Entries can be retrieved by either specifying a time range
 * or the number of (newest) entries.
 *
 */
public class TimedList<T> {

    private int maxSize;
    private long maxAge;
    private LinkedList<Entry<T>> entries = new LinkedList<Entry<T>>();

    public TimedList(int maxSize, long maxAge) {

        this.maxSize = maxSize;
        this.maxAge = maxAge;
    }

    public void add(T value) {

        synchronized(entries) {

            long now = System.currentTimeMillis();

            Entry<T> newEntry = new Entry<T>();
            newEntry.time = now;
            newEntry.value = value;
            entries.add(newEntry);

            if(entries.size() > maxSize) {
                entries.removeFirst();
            }

            long minTime = now - maxAge;
            Iterator<Entry<T>> it = entries.iterator();
            while(it.hasNext()) {
                Entry<T> entry = it.next();
                if(entry.time < minTime) {
                    it.remove();
                } else {
                    break;
                }
            }
        }
    }

    public List<T> get(long start, long end) {

        List<T> result = new ArrayList<T>();
        synchronized(entries) {
            Iterator<Entry<T>> it = entries.descendingIterator();
            while(it.hasNext()) {
                Entry<T> entry = it.next();
                if(entry.time < start) {
                    break;
                } else if(entry.time < end) {
                    result.add(0, entry.value);
                }
            }
        }
        return result;

    }

    public List<T> get(int no) {

        List<T> result = new ArrayList<T>();
        synchronized(entries) {
            Iterator<Entry<T>> it = entries.descendingIterator();
            while(it.hasNext() && no > 0) {
                Entry<T> entry = it.next();
                result.add(entry.value);
                no--;
            }
        }
        return result;
    }

    private static final class Entry<T> {

        private Entry() {}

        private long time;
        private T value;

    }

}
