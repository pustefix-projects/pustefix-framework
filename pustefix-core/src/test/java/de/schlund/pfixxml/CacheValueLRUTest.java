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
package de.schlund.pfixxml;

import de.schlund.pfixxml.util.CacheValueLRU;
import junit.framework.TestCase;

public class CacheValueLRUTest extends TestCase {
    
    public void testLRU() throws Exception {
        System.out.println("-----testLRU-----");
        CacheValueLRU<String,String> map = new CacheValueLRU<String, String>(3);
        map.put("Key_A", "Val_1");
        map.put("Key_B", "Val_2");        
        map.put("Key_C", "Val_3");
        System.out.println(map);
        assertEquals(3, map.size());
        assertEquals(3, map.sizeOfUniqueValueEntries());
        map.put("Key_X", "Val_1");
        System.out.println(map);
        assertEquals(4, map.size());
        assertEquals(3, map.sizeOfUniqueValueEntries());
        assertEquals(map.get("Key_X"), "Val_1");
        assertEquals(map.get("Key_A"), "Val_1");
        assertEquals(map.get("Key_B"), "Val_2");
        System.out.println(map);
        map.put("Key_D", "Val_4");
        assertNull(map.get("Key_C"));
        System.out.println(map);
        map.put("Key_E", "Val_5");
        System.out.println(map);
        assertNull(map.get("Key_A"));
        assertNull(map.get("Key_X"));       
    }
    
    
    public void testMultipleKeys() throws Exception {
        System.out.println("-----testMultipleKeys-----");
        CacheValueLRU<String,String> map = new CacheValueLRU<String, String>(3);
        map.put("Key_A", "Val_1");
        map.put("Key_B", "Val_2");        
        map.put("Key_C", "Val_3");
        System.out.println(map);
        map.put("Key_X", "Val_1");
        assertEquals(2, map.sizeOfKeyEntriesForValue("Val_1"));
        System.out.println(map);
        map.put("Key_X", "Val_2");
        assertEquals(1, map.sizeOfKeyEntriesForValue("Val_1"));
        assertEquals(2, map.sizeOfKeyEntriesForValue("Val_2"));
        System.out.println(map);
        map.put("Key_X", "Val_3");
        System.out.println(map);
        map.remove("Key_X");
        assertEquals(1, map.sizeOfKeyEntriesForValue("Val_3"));
        System.out.println(map);
        map.remove("Key_C");
        assertEquals(2, map.size());
        System.out.println(map);
    }
}
