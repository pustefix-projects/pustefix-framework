package de.schlund.pfixxml;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

import junit.framework.TestCase;

public class DefaultPathPrefixMappingTest extends TestCase {

    private Document doc;
    private PathPrefixMapping mapping;
    
    @Override
    protected void setUp() throws Exception {
        InputStream in = getClass().getResourceAsStream("pagealiases.xml");
        doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in);
        in.close();
        mapping = new DefaultPathPrefixMapping(doc);
    }
    
    public void testWithoutSelectors() {
        assertEquals("", mapping.getPrefix(null));
        Map<String, String> selectors = new HashMap<String, String>();
        assertEquals("", mapping.getPrefix(selectors));
    }
    
    public void testSingleSelector() {
        Map<String, String> selectors = new HashMap<String, String>();
        selectors.put("lang", "fr");
        assertEquals("fr", mapping.getPrefix(selectors));
        selectors.put("country", "CA");
        assertEquals("fr/ca", mapping.getPrefix(selectors));
    }
    
}
