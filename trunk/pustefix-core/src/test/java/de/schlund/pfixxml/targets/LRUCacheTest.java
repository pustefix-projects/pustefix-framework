package de.schlund.pfixxml.targets;

import junit.framework.TestCase;

public class LRUCacheTest extends TestCase {
    
    public void testRemoval() {
        
        LRUCache<String, String> cache = new LRUCache<String,String>();
        cache.createCache(3);
        cache.setValue("1", "a");
        cache.setValue("2", "b");
        cache.setValue("3", "c");
        cache.setValue("4", "d");
        assertNull(cache.getValue("1"));
        assertNotNull(cache.getValue("2"));
        cache.setValue("5", "e");
        assertNull(cache.getValue("3"));
        assertNotNull(cache.getValue("4"));
        assertNotNull(cache.getValue("2"));
        cache.setValue("6", "f");
        assertNull(cache.getValue("5"));
        assertNotNull(cache.getValue("2"));
        assertNotNull(cache.getValue("4"));
        assertNotNull(cache.getValue("6"));
        cache.setValue("1", "a");
        assertNull(cache.getValue("2"));
        cache.setValue("7", "g");
        cache.setValue("8", "h");
        assertNull(cache.getValue("4"));
        assertNotNull(cache.getValue("1"));
        
    }
    
    public void testSize() {
        
        LRUCache<String, String> cache = new LRUCache<String,String>();
        for(int i=0; i<31; i++) {
            cache.setValue("" + i, "x");
        }
        assertNull(cache.getValue("0"));
        assertNotNull(cache.getValue("1"));
        assertNotNull(cache.getValue("30"));
        assertNull(cache.getValue("31"));
        
        cache = new LRUCache<String,String>();
        cache.createCache(40);
        for(int i=0; i<41; i++) {
            cache.setValue("" + i, "x");
        }
        assertNull(cache.getValue("0"));
        assertNotNull(cache.getValue("1"));
        assertNotNull(cache.getValue("40"));
        assertNull(cache.getValue("41"));
        
        cache = new LRUCache<String,String>();
        cache.createCache(0);
        for(int i=0; i<31; i++) {
            cache.setValue("" + i, "x");
        }
        assertNull(cache.getValue("0"));
        assertNotNull(cache.getValue("1"));
        assertNotNull(cache.getValue("30"));
        assertNull(cache.getValue("31"));
        
        cache = new LRUCache<String,String>();
        cache.createCache(1);
        assertNull(cache.getValue("1"));
        cache.setValue("1", "x");
        cache.setValue("2", "y");
        assertNull(cache.getValue("1"));
        assertNotNull(cache.getValue("2"));
        
    }

}
