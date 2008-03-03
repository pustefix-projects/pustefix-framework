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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * A collection that stores a counter for each object. adding the same Object more than once will
 * increase the counter, removing it will decrease the counter until it reaches 0, in that case the
 * whole entry will be removed from the map. This implementation is backed by a HashMap, use
 * SortedRefCountingCollection for an implementation that uses a TreeMap.
 *
 * Note that the iterator that is given will iterate over the distinct elements, regardless of their
 * cardinality. The Iterator is implemented by the class RefCountingCollectionIterator, which
 * features the additional method remove(int count) which works like removing from the collection
 * itself, while the usual Iterator.remove() will remove the whole element, regardles of cardinality
 * to be consistent with next() and hasNext().
 *
 * Created: Wed Nov 16 00:18:45 2005
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 * @version 1.0
 */
public class RefCountingCollection<E> implements Collection<E> {
    private Map<E, Integer> map; 
    int                     fullsize;
    
    public RefCountingCollection() {
        init(false);
    }

    public RefCountingCollection(Collection<? extends E> collection) {
        init(false);
        addAll(collection);
    }

    protected final void init(boolean sorted) {
        fullsize = 0;
        if (sorted) {
            map = new TreeMap<E, Integer>();
        } else {
            map = new HashMap<E, Integer>();
        }
    }

    // Special methods
    
    public final boolean add(final E object, final int cardinality) {
        if (cardinality < 0) {
            throw new RuntimeException("Can't add an element with a negative cardinality");
        }

        if (cardinality == 0) {
            return false;
        }

        int newcount = cardinality;
        if (map.containsKey(object)) {
            newcount = newcount + map.get(object);
        }
        
        map.put(object, newcount);
        fullsize = fullsize + cardinality;
        
        return true;
    }

    public final boolean removeElement(final Object object) {
        if (!contains(object)) {
            return false;
        }
        return remove(object, map.get(object));
    }

    @SuppressWarnings("unchecked")
    public final boolean remove(final Object object, int cardinality) {
        if (cardinality < 0) {
            throw new RuntimeException("Can't remove an element with a negative cardinality");
        }

        if (cardinality == 0 || !map.containsKey(object)) {
            return false;
        }
        int count = map.get(object);
        if (cardinality >= count) {
            map.remove(object);
            fullsize = fullsize - count;
        } else {
            // This cast is OK, as we already know that object is in the map. 
            map.put((E) object, count - cardinality);
            fullsize = fullsize - cardinality;
        }
        return true;
    }
    
    public final int getCardinality(final Object object) {
        int retval = 0;
        if (map.containsKey(object)) {
            retval = map.get(object);
        }
        return retval;
    }

    public final String toString() {
        StringBuffer buff = new StringBuffer("[");
        for (Iterator<E> i = iterator(); i.hasNext();) {
            E   key   = i.next();
            int count = getCardinality(key);
            buff.append(key + " [" + count + "] ");
        }
        buff.append(" FS: " + fullsize + "]");
        return buff.toString();
    }

    protected final boolean isInternalMapEqualToMap(Map<?, ?> map) {
        return this.map.equals(map);
    }
    
    // Implementation of java.util.Collection

    public final boolean add(final E object) {
        return add(object, 1);
    }

    public final void clear() {
        map.clear();
    }

    public final boolean contains(final Object object) {
        if (object == null) {
            throw new NullPointerException("Object to test must be null");
        }
        return map.containsKey(object);
    }

    @SuppressWarnings("unchecked")
    public final boolean addAll(final Collection<? extends E> collection) {
        for (Iterator<? extends E> i = collection.iterator(); i.hasNext(); ) {
            E item = i.next();

            int cardinality = 1;
            if (collection instanceof RefCountingCollection) {
                cardinality = ((RefCountingCollection<E>) collection).getCardinality(item);
            }
            add(item, cardinality);
        }
        return true;
    }

    public final int size() {
        return map.keySet().size();
    }

    public final boolean remove(final Object object) {
        return remove(object, 1);
    }

    public final boolean isEmpty() {
        return map.isEmpty();
    }

    public final Iterator<E> iterator() {
        return new RefCountingCollectionIterator<E>(this, map);
    }

    public final int hashCode() {
        return map.hashCode();
    }

    public final boolean equals(final Object object) {
        RefCountingCollection<?> incoll;
        try {
            incoll = (RefCountingCollection<?>) object;
        } catch (ClassCastException e) {
            return false;
        }
        return incoll.isInternalMapEqualToMap(map);
    }

    public final boolean containsAll(final Collection<?> collection) {
        for (Iterator<?> i  = collection.iterator(); i.hasNext();) {
            if (!contains(i.next())) {
                return false;
            }
        }
        return true;
    }

    public final boolean removeAll(final Collection<?> collection) {
        boolean retval = false;
        boolean is_rcc = collection instanceof RefCountingCollection;
        
        for (Iterator<?> i  = collection.iterator(); i.hasNext();) {
            if (is_rcc) {
                Object obj   = i.next();
                int count = ((RefCountingCollection<?>) collection).getCardinality(obj);
                if (remove(obj, count)) {
                    retval = true;
                }
            } else {
                if (remove(i.next())) {
                    retval = true;
                }
            }
        }
        return retval;
    }

    public final boolean retainAll(final Collection<?> collection) {
        boolean retval = false;
        boolean is_rcc = collection instanceof RefCountingCollection;
        for (Iterator<E> i = iterator(); i.hasNext();) {
            E obj = i.next();
            if (!collection.contains(obj)) {
                i.remove();
                retval = true;
            } else if (is_rcc) {
                int count = ((RefCountingCollection<?>) collection).getCardinality(obj);
                if (count < getCardinality(obj)) {
                    map.put(obj, count);
                    retval = true;
                }
            }
        }
        return retval;
    }

    public Object[] toArray() {
        Object[] retval = new Object[fullsize];
        if (fullsize == 0) {
            return retval;
        }
        int index = 0;
        for (Iterator<E> i = iterator(); i.hasNext();) {
            E   obj   = i.next();
            int count = getCardinality(obj);
            for (int j = 0; j < count ; j++) {
                retval[index++] = obj;
            }
        }
        return retval;
    }

    @SuppressWarnings("unchecked")
    public final <T> T[] toArray(T[] array) {
        if (array.length < fullsize) {
            array = (T[]) java.lang.reflect.Array.newInstance(array.getClass().getComponentType(), fullsize);
        }
        int index = 0;
        for (Iterator<E> i = iterator(); i.hasNext();) {
            E   obj   = i.next();
            int count = getCardinality(obj);
            for (int j = 0; j < count ; j++) {
                array[index++] = (T) obj;
            }
        }
        if (array.length > fullsize) {
            array[fullsize] = null;
        }
        return array;
    }


    // ============================================== END of class =================================

    
    public static void main(String[] args) {
        RefCountingCollection<String> coll = new SortedRefCountingCollection<String>(); 
        coll.add("Hallo",5);
        coll.add("Foo",2);
        coll.add("Bar");
        coll.add("Baz",3);
        coll.add("Baz");
        
        System.out.println(coll);
        System.out.println("-------");

        coll.remove("Foo");
        System.out.println("Removing Foo:\n" + coll);
        System.out.println("-------");

        coll.remove("Baz", 4);
        System.out.println("Remove 4 x Baz:\n" + coll);
        System.out.println("-------");

        Collection<String> testcol = new LinkedList<String>();
        testcol.add("XXX");
        testcol.add("YYY");
        testcol.add("XXX");
        testcol.add("ZZZ");
        coll.addAll(testcol);
        System.out.println("AddAll (XXX YYY XXX ZZZ): ");
        System.out.println(coll);
        System.out.println("-------");
        System.out.println("Size: " + coll.size());
        System.out.println("Contains ZZZ: " + coll.contains("ZZZ"));
        System.out.println("Contains Baz: " + coll.contains("Baz"));
        System.out.println("-------");

        testcol.clear();
        testcol.add("ZZZ");
        testcol.add("Baz");
        System.out.println("ContainsAll (ZZZ Baz): " + coll.containsAll(testcol));
        System.out.println("-------");

        testcol.clear();
        testcol.add("XXX");
        testcol.add("Foo");
        System.out.println("ContainsAll (XXX Foo): " + coll.containsAll(testcol));
        System.out.println("-------");

        coll.removeAll(testcol);
        System.out.println("RemoveAll (XXX Foo):");
        System.out.println(coll);
        System.out.println("-------");

        RefCountingCollection<String> test = new RefCountingCollection<String>();
        test.add("Hallo", 2);
        test.add("Bar");
        test.add("TTT", 10);
        coll.removeAll(test);
        System.out.println("RemoveAll " + test);
        System.out.println(coll);
        System.out.println("-------");

        Object[] array = coll.toArray();
        System.out.print("[ ");
        for (int i = 0; i < array.length ; i++) {
            System.out.print(array[i] + " ");
        }
        System.out.println("]");
        System.out.println("-------");

        String[] str_array = coll.toArray(new String[]{});
        System.out.print("[ ");
        for (int i = 0; i < str_array.length ; i++) {
            System.out.print(str_array[i] + " ");
        }
        System.out.println("]");
        System.out.println("-------");

        for (Iterator<String> i = coll.iterator(); i.hasNext();) {
            String obj = i.next();
            System.out.println("Objekt:" + obj);
            System.out.println("     K:" + coll.getCardinality(obj));
            RefCountingCollectionIterator<String> iter = (RefCountingCollectionIterator<String>) i;
            iter.remove(1);
            System.out.println("           -> removing via iterator.remove(1)");
        }
        System.out.println("-------");
        System.out.println("After remove(1) on iterator:\n" + coll);
        System.out.println("-------");
        
        RefCountingCollection<String> blah = new RefCountingCollection<String>();
        blah.add("A");
        blah.add("A");
        blah.add("B");
        List<Integer> fasel = new ArrayList<Integer>();
        fasel.add(1);
        fasel.add(1);
        fasel.add(10);
        
        blah.removeAll(fasel);
        System.out.println("-------");
        System.out.println("After remove(fasel):\n" + blah);
        System.out.println("-------");
        
    }
}
