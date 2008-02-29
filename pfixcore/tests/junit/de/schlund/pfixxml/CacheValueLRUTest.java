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
