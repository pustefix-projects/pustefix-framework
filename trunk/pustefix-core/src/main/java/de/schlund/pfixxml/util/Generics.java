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

package de.schlund.pfixxml.util;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;

/**
 * Utility class that provides methods for converting collections.  
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class Generics {
    private Generics() {};
    
    @SuppressWarnings("unchecked")
    public static <T> Collection<T> convertCollection(Collection<?> collection) {
        return (Collection<T>) collection;
    }
    
    @SuppressWarnings("unchecked")
    public static <T> Set<T> convertSet(Set<?> set) {
        return (Set<T>) set;
    }
    
    @SuppressWarnings("unchecked")
    public static <T> SortedSet<T> convertSortedSet(SortedSet<?> set) {
        return (SortedSet<T>) set;
    }
    
    @SuppressWarnings("unchecked")
    public static <T> List<T> convertList(List<?> list) {
        return (List<T>) list;
    }
    
    @SuppressWarnings("unchecked")
    public static <T1, T2> Map<T1, T2> convertMap(Map<?, ?> map) {
        return (Map<T1, T2>) map;
    }

    @SuppressWarnings("unchecked")
    public static <T1, T2> SortedMap<T1, T2> convertSortedMap(SortedMap<?, ?> map) {
        return (SortedMap<T1, T2>) map;
    }
}
