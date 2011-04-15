package de.schlund.pfixxml;

import java.net.URL;
import java.util.Set;

import junit.framework.TestCase;

import org.xml.sax.InputSource;

public class IncludePartsInfoParserTest extends TestCase {

    public void testParts() throws Exception {
        URL url = getClass().getClassLoader().getResource("de/schlund/pfixxml/parts.xml");
        InputSource source = new InputSource(url.openStream());
        source.setSystemId(url.toString());
        IncludePartsInfo info = IncludePartsInfoParser.parse(source);
        Set<String> parts = info.getParts();
        assertEquals(3, parts.size());
        assertTrue(parts.contains("aaa"));
        assertTrue(parts.contains("bbb"));
        assertFalse(parts.contains("ccc"));
        assertTrue(parts.contains("ddd"));
    }
    
}
