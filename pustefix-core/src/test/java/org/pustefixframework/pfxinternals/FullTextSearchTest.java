package org.pustefixframework.pfxinternals.search;

import junit.framework.TestCase;

public class FullTextSearchTest extends TestCase {

	public void testTextCutting() {
		
		String str="abcXYZabc";
		assertEquals(str, FullTextSearch.cutOff(str, 3, 5, 10));
		assertEquals(str, FullTextSearch.cutOff(str, 3, 5, 9));
		assertEquals("abcXYZab", FullTextSearch.cutOff(str, 3, 5, 8));
		assertEquals("abcXYZa", FullTextSearch.cutOff(str, 3, 5, 7));
		assertEquals("abcXYZ", FullTextSearch.cutOff(str, 3, 5, 6));
		assertEquals("bcXYZ", FullTextSearch.cutOff(str, 3, 5, 5));
    	assertEquals("cXYZ", FullTextSearch.cutOff(str, 3, 5, 4));
    	assertEquals("XYZ", FullTextSearch.cutOff(str, 3, 5, 3));
    	assertEquals("XY", FullTextSearch.cutOff(str, 3, 5, 2));
    	assertEquals("X", FullTextSearch.cutOff(str, 3, 5, 1));
    	assertEquals("X", FullTextSearch.cutOff(str, 3, 5, 1));
    	assertEquals("", FullTextSearch.cutOff(str, 3, 5, 0));
    	assertEquals("", FullTextSearch.cutOff(str, 3, 5, -1));
    	
    	str="XYZabcabc";
		assertEquals(str, FullTextSearch.cutOff(str, 0, 2, 10));
		assertEquals(str, FullTextSearch.cutOff(str, 0, 2, 9));
		assertEquals("XYZabcab", FullTextSearch.cutOff(str, 0, 2, 8));
		assertEquals("XYZabca", FullTextSearch.cutOff(str, 0, 2, 7));
		assertEquals("XYZabc", FullTextSearch.cutOff(str, 0, 2, 6));
		assertEquals("XYZab", FullTextSearch.cutOff(str, 0, 2, 5));
    	assertEquals("XYZa", FullTextSearch.cutOff(str, 0, 2, 4));
    	assertEquals("XYZ", FullTextSearch.cutOff(str, 0, 2, 3));
    	assertEquals("XY", FullTextSearch.cutOff(str, 0, 2, 2));
    	assertEquals("X", FullTextSearch.cutOff(str, 0, 2, 1));
    	assertEquals("X", FullTextSearch.cutOff(str, 0, 2, 1));
    	assertEquals("", FullTextSearch.cutOff(str, 0, 2, 0));
    	assertEquals("", FullTextSearch.cutOff(str, 0, 2, -1));
    	
    	str="abcabcXYZ";
		assertEquals(str, FullTextSearch.cutOff(str, 6, 8, 10));
		assertEquals(str, FullTextSearch.cutOff(str, 6, 8, 9));
		assertEquals("bcabcXYZ", FullTextSearch.cutOff(str, 6, 8, 8));
		assertEquals("cabcXYZ", FullTextSearch.cutOff(str, 6, 8, 7));
		assertEquals("abcXYZ", FullTextSearch.cutOff(str, 6, 8, 6));
		assertEquals("bcXYZ", FullTextSearch.cutOff(str, 6, 8, 5));
    	assertEquals("cXYZ", FullTextSearch.cutOff(str, 6, 8, 4));
    	assertEquals("XYZ", FullTextSearch.cutOff(str, 6, 8, 3));
    	assertEquals("XY", FullTextSearch.cutOff(str, 6, 8, 2));
    	assertEquals("X", FullTextSearch.cutOff(str, 6, 8, 1));
    	assertEquals("X", FullTextSearch.cutOff(str, 6, 8, 1));
    	assertEquals("", FullTextSearch.cutOff(str, 6, 8, 0));
    	assertEquals("", FullTextSearch.cutOff(str, 6, 8, -1));
	
    	str="a";
		assertEquals(str, FullTextSearch.cutOff(str, 0, 0, 2));
		assertEquals(str, FullTextSearch.cutOff(str, 0, 0, 1));
		assertEquals("", FullTextSearch.cutOff(str, 0, 0, 0));
		assertEquals("", FullTextSearch.cutOff(str, 0, 0, -1));
		
		str="abc";
		assertEquals(str, FullTextSearch.cutOff(str, 1, 1, 4));
		assertEquals(str, FullTextSearch.cutOff(str, 1, 1, 3));
		assertEquals("ab", FullTextSearch.cutOff(str, 1, 1, 2));
		assertEquals("b", FullTextSearch.cutOff(str, 1, 1, 1));
		assertEquals("", FullTextSearch.cutOff(str, 1, 1, 0));
		assertEquals("", FullTextSearch.cutOff(str, 1, 1, -1));
		
		str="abc";
		assertEquals(str, FullTextSearch.cutOff(str, 1, 2, 4));
		assertEquals(str, FullTextSearch.cutOff(str, 1, 2, 3));
		assertEquals("bc", FullTextSearch.cutOff(str, 1, 2, 2));
		assertEquals("b", FullTextSearch.cutOff(str, 1, 2, 1));
		assertEquals("", FullTextSearch.cutOff(str, 1, 2, 0));
		assertEquals("", FullTextSearch.cutOff(str, 1, 2, -1));
		
		str="abc";
		assertEquals(str, FullTextSearch.cutOff(str, 0, 1, 4));
		assertEquals(str, FullTextSearch.cutOff(str, 0, 1, 3));
		assertEquals("ab", FullTextSearch.cutOff(str, 0, 1, 2));
		assertEquals("a", FullTextSearch.cutOff(str, 0, 1, 1));
		assertEquals("", FullTextSearch.cutOff(str, 0, 1, 0));
		assertEquals("", FullTextSearch.cutOff(str, 0, 1, -1));
		
		str="abc";
		assertEquals(str, FullTextSearch.cutOff(str, 0, 0, 4));
		assertEquals(str, FullTextSearch.cutOff(str, 0, 0, 3));
		assertEquals("ab", FullTextSearch.cutOff(str, 0, 0, 2));
		assertEquals("a", FullTextSearch.cutOff(str, 0, 0, 1));
		assertEquals("", FullTextSearch.cutOff(str, 0, 0, 0));
		assertEquals("", FullTextSearch.cutOff(str, 0, 0, -1));
		
		str="abc";
		assertEquals(str, FullTextSearch.cutOff(str, 0, 2, 4));
		assertEquals(str, FullTextSearch.cutOff(str, 0, 2, 3));
		assertEquals("ab", FullTextSearch.cutOff(str, 0, 2, 2));
		assertEquals("a", FullTextSearch.cutOff(str, 0, 2, 1));
		assertEquals("", FullTextSearch.cutOff(str, 0, 2, 0));
		assertEquals("", FullTextSearch.cutOff(str, 0, 2, -1));
		
	}
	
}
