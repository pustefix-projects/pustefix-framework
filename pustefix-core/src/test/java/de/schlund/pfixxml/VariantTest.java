package de.schlund.pfixxml;

import junit.framework.TestCase;

public class VariantTest extends TestCase {

    public void test() {
        
        assertNull(new Variant(null).getVariantFallbackArray());
        assertNull(new Variant("").getVariantFallbackArray());
        assertEquals("foo:bar:baz", new Variant("foo:bar:baz").getVariantId());
        
        Variant variant = new Variant("foo:bar:baz");
        String[] fallbacks = variant.getVariantFallbackArray();
        assertEquals("foo:bar:baz", fallbacks[0]);
        assertEquals("foo:bar", fallbacks[1]);
        assertEquals("foo", fallbacks[2]);
        
        assertTrue(variant.matches("foo:bar:baz"));
        assertTrue(variant.matches("foo:bar"));
        assertTrue(variant.matches("foo"));
        
        assertTrue(variant.matches("foo:bar:*"));
        assertTrue(variant.matches("foo:*:baz"));
        assertTrue(variant.matches("foo:*:*"));
        assertTrue(variant.matches("*:bar:baz"));
        assertTrue(variant.matches("*:bar:*"));
        assertTrue(variant.matches("*:*:baz"));
        assertTrue(variant.matches("*:*:*"));
        
        assertTrue(variant.matches("foo:*"));
        assertTrue(variant.matches("*:bar"));
        assertTrue(variant.matches("*:*"));
        
        assertTrue(variant.matches("*"));
        
        assertFalse(variant.matches("foo:bar:xxx"));
        assertFalse(variant.matches("foo:xxx"));
        assertFalse(variant.matches("xxx"));
        
        assertFalse(variant.matches("foo:xxx:*"));
        assertFalse(variant.matches("foo:*:xxx"));
        assertFalse(variant.matches("xxx:*:*"));
        assertFalse(variant.matches("*:xxx:baz"));
        assertFalse(variant.matches("*:xxx:*"));
        assertFalse(variant.matches("*:*:xxx"));
        
        assertFalse(variant.matches("xxx:*"));
        assertFalse(variant.matches("*:xxx"));
        
        assertTrue(new Variant("foo:bar:baz").matches("**:baz"));
        assertTrue(new Variant("foo:bar:baz").matches("**:bar"));
        assertTrue(new Variant("foo:bar:baz").matches("**:bar:baz"));
        assertTrue(new Variant("foo:baz").matches("**:baz"));
        assertTrue(new Variant("foo:bar:hey:baz").matches("**:baz"));
        assertTrue(new Variant("foo:bar:baz:ho").matches("**:baz"));
        
        assertTrue(new Variant("foo:bar:baz:hey").matches("foo:**:hey"));
        assertTrue(new Variant("foo:bar:baz:hey").matches("foo:**:baz"));
        assertTrue(new Variant("foo:baz:baz:hey").matches("**:baz"));
        assertTrue(new Variant("foo:bar:baz:ho").matches("**:bar"));
        
        assertFalse(new Variant("foo:bar:baz").matches("**:xxx"));
        assertFalse(new Variant("foo:baz").matches("**:xxx"));
        assertFalse(new Variant("foo:bar:hey:baz").matches("**:xxx"));
        assertFalse(new Variant("foo:bar:baz:ho").matches("**:xxx"));
    }
    
}
