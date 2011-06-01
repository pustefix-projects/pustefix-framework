package de.schlund.pfixxml;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import junit.framework.TestCase;

import org.w3c.dom.Document;

public class DefaultPageAliasMappingTest extends TestCase {

    private Document doc;
    private PageAliasMapping mapping;
    
    @Override
    protected void setUp() throws Exception {
        InputStream in = getClass().getResourceAsStream("pagealiases.xml");
        doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in);
        in.close();
        mapping = new DefaultPageAliasMapping(doc);
    }
    
    public void testWithoutSelectors() {
        assertEquals("unknown", mapping.getAlias("unknown", null));
        assertEquals("test", mapping.getAlias("test", null));
        assertEquals("foo", mapping.getAlias("foo", null));
        Map<String, String> selectors = new HashMap<String, String>();
        assertEquals("unknown", mapping.getAlias("unknown", selectors));
        assertEquals("test", mapping.getAlias("test", selectors));
        assertEquals("foo", mapping.getAlias("foo", selectors));
    }
    
    public void testReverseWithoutSelectors() {
        assertEquals("unknown", mapping.getPage("unknown", null));
        assertEquals("test", mapping.getPage("test", null));
        assertEquals("foo", mapping.getPage("foo", null));
        Map<String, String> selectors = new HashMap<String, String>();
        assertEquals("unknown", mapping.getPage("unknown", selectors));
        assertEquals("test", mapping.getPage("test", selectors));
        assertEquals("foo", mapping.getPage("foo", selectors));
    }
    
    public void testSingleSelector() {
        Map<String, String> selectors = new HashMap<String, String>();
        selectors.put("lang", "de");
        assertEquals("unknown", mapping.getAlias("unknown", selectors));
        assertEquals("test", mapping.getAlias("test", selectors));
        assertEquals("bar", mapping.getAlias("foo", selectors));
        selectors.put("lang", "en");
        assertEquals("foo", mapping.getAlias("foo", selectors));
        assertEquals("home", mapping.getAlias("Home", selectors));
        assertEquals("goodbye", mapping.getAlias("Bye", selectors));
        selectors.put("lang", "fr");
        assertEquals("depart", mapping.getAlias("Home", selectors));
    }
    
    public void testReverseSingleSelector() {
        Map<String, String> selectors = new HashMap<String, String>();
        selectors.put("lang", "de");
        assertEquals("unknown", mapping.getPage("unknown", selectors));
        assertEquals("test", mapping.getPage("test", selectors));
        assertEquals("foo", mapping.getPage("bar", selectors));
        selectors.put("lang", "en");
        assertEquals("foo", mapping.getPage("foo", selectors));
        assertEquals("Home", mapping.getPage("home", selectors));
        assertEquals("Bye", mapping.getPage("goodbye", selectors));
        selectors.put("lang", "fr");
        assertEquals("Home", mapping.getPage("depart", selectors));
    }
    
    public void testMultipleSelectors() {
        Map<String, String> selectors = new HashMap<String, String>();
        selectors.put("lang", "en");
        selectors.put("country", "US");
        assertEquals("bye", mapping.getAlias("Bye", selectors));
        selectors.put("country", "CA");
        assertEquals("go", mapping.getAlias("Bye", selectors));
        selectors.put("branch", "hey");
        assertEquals("ho", mapping.getAlias("Bye", selectors));
        selectors.put("branch", "xyz");
        assertEquals("go", mapping.getAlias("Bye", selectors));
        selectors.clear();
        selectors.put("country", "US");
        assertEquals("bye", mapping.getAlias("Bye", selectors));
        selectors.put("lang", "en");
        selectors.put("branch", "none");
        assertEquals("LandingPage", mapping.getAlias("LandingPage", selectors));
        selectors.put("branch", "living");
        assertEquals("living", mapping.getAlias("LandingPage", selectors));
        selectors.put("lang", "fr");
        assertEquals("habiter", mapping.getAlias("LandingPage", selectors));
        selectors.put("country", "CA");
        assertEquals("habiter", mapping.getAlias("LandingPage", selectors));
    }
    
    public void testReverseMultipleSelectors() {
        Map<String, String> selectors = new HashMap<String, String>();
        selectors.put("lang", "en");
        selectors.put("country", "US");
        assertEquals("Bye", mapping.getPage("bye", selectors));
        selectors.put("country", "CA");
        assertEquals("Bye", mapping.getPage("go", selectors));
        selectors.put("branch", "hey");
        assertEquals("Bye", mapping.getPage("ho", selectors));
        selectors.put("branch", "xyz");
        assertEquals("Bye", mapping.getPage("go", selectors));
        selectors.clear();
        selectors.put("country", "US");
        assertEquals("Bye", mapping.getPage("Bye", selectors));
        selectors.put("lang", "en");
        assertEquals("LandingPage", mapping.getPage("LandingPage", selectors));
        selectors.put("branch", "living");
        assertEquals("LandingPage", mapping.getPage("living", selectors));
        selectors.put("lang", "fr");
        assertEquals("LandingPage", mapping.getPage("habiter", selectors));
    }
 
    public void testReverse() {
        Map<String, String> selectors = new HashMap<String, String>();
        selectors.put("lang", "en");
        selectors.put("country", "US");
        assertEquals("Duplicate", mapping.getPage("go", selectors));
        selectors.put("lang", "fr");
        selectors.put("country", "CA");
        assertEquals("LandingPage", mapping.getPage("habiter", selectors));
    }
    
}
