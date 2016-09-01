package de.schlund.pfixxml.util.xsltimpl;

import junit.framework.TestCase;

public class Xslt2BackPortFunctionsTest extends TestCase {
    
    public void testMatches() {
        
        String text = "foobarbaz";
        
        assertTrue(Xslt2BackPortFunctions.matches(text, "bar"));
        assertTrue(Xslt2BackPortFunctions.matches(text, "^foobarbaz$"));
        assertFalse(Xslt2BackPortFunctions.matches(text, "^bar$"));
        assertFalse(Xslt2BackPortFunctions.matches(text, "xxx"));
    }
    
    public void testMatchesFromSpec() {
        
        //Examples from XPath 2.0 functions specification,
        //see https://www.w3.org/TR/xpath-functions/#func-matches
        
        assertTrue(Xslt2BackPortFunctions.matches("abracadabra", "bra"));
        assertTrue(Xslt2BackPortFunctions.matches("abracadabra", "^a.*a$"));
        assertFalse(Xslt2BackPortFunctions.matches("abracadabra", "^bra"));
        
        String input = 
                "<poem author=\"Wilhelm Busch\">\n" + 
                "Kaum hat dies der Hahn gesehen,\n" +
                "Fängt er auch schon an zu krähen:\n" +
                "«Kikeriki! Kikikerikih!!»\n" +
                "Tak, tak, tak! - da kommen sie.\n" + 
                "</poem>\n";
        
        assertFalse(Xslt2BackPortFunctions.matches(input, "Kaum.*krähen"));
        assertTrue(Xslt2BackPortFunctions.matches(input, "Kaum.*krähen", "s"));
        assertTrue(Xslt2BackPortFunctions.matches(input, "^Kaum.*gesehen,$", "m"));
        assertFalse(Xslt2BackPortFunctions.matches(input, "^Kaum.*gesehen,$"));
        assertTrue(Xslt2BackPortFunctions.matches(input, "kiki", "i"));
    }
    
    public void testMatchesWithFlags() {
        
        String text = "foo\nbar\nbaz";
        
        assertTrue(Xslt2BackPortFunctions.matches(text, "bar.baz", "s"));
        assertTrue(Xslt2BackPortFunctions.matches(text, "foo.*baz", "s"));
        assertFalse(Xslt2BackPortFunctions.matches(text, "bar.baz"));
        
        assertTrue(Xslt2BackPortFunctions.matches(text, "^bar$", "m"));
        assertFalse(Xslt2BackPortFunctions.matches(text, "^bar$"));
        
        assertTrue(Xslt2BackPortFunctions.matches(text, "BAR", "i"));
        assertFalse(Xslt2BackPortFunctions.matches(text, "BAR"));
        
        assertTrue(Xslt2BackPortFunctions.matches(text, "^BAR$", "im"));
        assertFalse(Xslt2BackPortFunctions.matches(text, "^BAR$", "i"));
        
        assertTrue(Xslt2BackPortFunctions.matches(text, " bar ", "x"));
        assertFalse(Xslt2BackPortFunctions.matches(text, " bar "));
    }

    public void testReplace() {
        
        String text = "foobarbaz";
        
        assertEquals("fooxxxbaz", Xslt2BackPortFunctions.replace(text, "bar", "xxx"));
        assertEquals("fooXarXaz", Xslt2BackPortFunctions.replace(text, "b", "X"));
    }
    
    public void testReplaceFromSpec() {
        
        //Examples from XPath 2.0 functions specification,
        //see https://www.w3.org/TR/xpath-functions/#func-replace
        
        assertEquals("a*cada*", Xslt2BackPortFunctions.replace("abracadabra", "bra", "*"));
        assertEquals("*", Xslt2BackPortFunctions.replace("abracadabra", "a.*a", "*"));
        assertEquals("*c*bra", Xslt2BackPortFunctions.replace("abracadabra", "a.*?a", "*"));
        assertEquals("brcdbr", Xslt2BackPortFunctions.replace("abracadabra", "a", ""));
        assertEquals("abbraccaddabbra", Xslt2BackPortFunctions.replace("abracadabra", "a(.)", "a$1$1"));
        try {
            Xslt2BackPortFunctions.replace("abracadabra", ".*?", "$1");
            fail("should raise an error, because the pattern matches the zero-length string");
        } catch(Exception x) {}
        assertEquals("b", Xslt2BackPortFunctions.replace("AAAA", "A+", "b"));
        assertEquals("bbbb", Xslt2BackPortFunctions.replace("AAAA", "A+?", "b"));
        assertEquals("carted", Xslt2BackPortFunctions.replace("darted", "^(.*?)d(.*)$", "$1c$2"));        
    }
    
    public void testReplaceWithFlags() {
        
        String text = "foo\nbar\nbaz";
        
        assertEquals("foo\nxxx\nbaz", Xslt2BackPortFunctions.replace(text, "BAR", "xxx", "i"));
        assertEquals("foo\nXar\nXaz", Xslt2BackPortFunctions.replace(text, " B ", "X", "ix"));
    }

    public void testEncodeForUri() {

        //Examples from XPath 2.0 functions specification,
        //see https://www.w3.org/TR/xpath-functions/#func-encode-for-uri

        assertEquals("http%3A%2F%2Fwww.example.com%2F00%2FWeather%2FCA%2FLos%2520Angeles%23ocean",
                Xslt2BackPortFunctions.encodeForUri("http://www.example.com/00/Weather/CA/Los%20Angeles#ocean"));
        assertEquals("~b%C3%A9b%C3%A9", Xslt2BackPortFunctions.encodeForUri("~bébé"));
        assertEquals("100%25%20organic", Xslt2BackPortFunctions.encodeForUri("100% organic"));

        assertEquals("%23-_.%21~%2A%27%28%29%20%2F%3A", Xslt2BackPortFunctions.encodeForUri("#-_.!~*'() /:"));
    }

    public void testSubstring() {

        //Examples from XPath 2.0 functions specification,
        //see https://www.w3.org/TR/xpath-functions/#func-substring

        assertEquals(" car", Xslt2BackPortFunctions.substring("motor car", 6));
        assertEquals("ada", Xslt2BackPortFunctions.substring("metadata", 4, 3));
        assertEquals("234", Xslt2BackPortFunctions.substring("12345", 1.5, 2.6));
        assertEquals("12", Xslt2BackPortFunctions.substring("12345", 0, 3));
        assertEquals("", Xslt2BackPortFunctions.substring("12345", 5, -3));
        assertEquals("1", Xslt2BackPortFunctions.substring("12345", -3, 5));

        assertEquals("motor car", Xslt2BackPortFunctions.substring("motor car", 0));
        assertEquals("motor car", Xslt2BackPortFunctions.substring("motor car", -1));
        assertEquals("", Xslt2BackPortFunctions.substring("motor car", 10));
        assertEquals("", Xslt2BackPortFunctions.substring("motor car", 11));
        assertEquals("", Xslt2BackPortFunctions.substring("12345", -3, 4));
        assertEquals("12345", Xslt2BackPortFunctions.substring("12345", 1, 5));
        assertEquals("12345", Xslt2BackPortFunctions.substring("12345", 1, 6));
        assertEquals("1", Xslt2BackPortFunctions.substring("12345", 1, 1));
        assertEquals("5", Xslt2BackPortFunctions.substring("12345", 5, 1));
        assertEquals("", Xslt2BackPortFunctions.substring("12345", 6, 1));
    }

    public void testStringLength() {

        assertEquals(3, Xslt2BackPortFunctions.stringLength("foo"));
        assertEquals(0, Xslt2BackPortFunctions.stringLength(""));
    }

}
