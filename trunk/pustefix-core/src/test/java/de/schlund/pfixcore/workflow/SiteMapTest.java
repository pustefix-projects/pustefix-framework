package de.schlund.pfixcore.workflow;

import java.io.File;

import junit.framework.TestCase;

public class SiteMapTest extends TestCase {

    public void test() throws Exception {
        
        SiteMap siteMap = new SiteMap(
            new File(getClass().getResource("sitemap.xml").toURI()), new File[] {
            new File(getClass().getResource("sitemap-aliases-de.xml").toURI()),
            new File(getClass().getResource("sitemap-aliases-en.xml").toURI()),
            new File(getClass().getResource("sitemap-aliases-fr.xml").toURI())
        });
        
        assertEquals("Start", siteMap.getAlias("Home", "de", null, null));
        assertEquals("Home", siteMap.getPageName("Start", "de").pageName);
        
        assertEquals("Home", siteMap.getAlias("Home", "en", null, null));
        assertEquals("Home", siteMap.getPageName("Home", "en").pageName);
        
        assertEquals("Depart", siteMap.getAlias("Home", "fr", null, null));
        assertEquals("Home", siteMap.getPageName("Depart", "fr").pageName);
        
        assertEquals("Home", siteMap.getAlias("Home", "xx", null, null));
        assertEquals("Home", siteMap.getPageName("Home", "xx").pageName);
        
        assertEquals("Home", siteMap.getAlias("Home", "", null, null));
        assertEquals("Home", siteMap.getPageName("Home", "").pageName);
        
        assertEquals("Home", siteMap.getAlias("Home", null, null, null));
        assertEquals("Home", siteMap.getPageName("Home", null).pageName);
        
        assertEquals("Inhalt", siteMap.getAlias("Overview", "de", null, null));
        assertEquals("Overview", siteMap.getPageName("Inhalt", "de").pageName);
        
        assertEquals("TOC", siteMap.getAlias("Overview", "en", null, null));
        assertEquals("Overview", siteMap.getPageName("TOC", "en").pageName);
        
        assertEquals("TOC", siteMap.getAlias("Overview", "fr", null, null));
        assertEquals("Overview", siteMap.getPageName("TOC", "fr").pageName);

        assertEquals("Staedte", siteMap.getAlias("Info", "de", "cities", null));
        assertEquals("Info", siteMap.getPageName("Staedte", "de").pageName);
        assertEquals("cities", siteMap.getPageName("Staedte", "de").pageAlternativeKey);
        
        assertEquals("Cities", siteMap.getAlias("Info", "en", "cities", null));
        assertEquals("Info", siteMap.getPageName("Cities", "en").pageName);
        assertEquals("cities", siteMap.getPageName("Cities", "en").pageAlternativeKey);
        
        assertEquals("Cites", siteMap.getAlias("Info", "fr", "cities", null));
        assertEquals("Info", siteMap.getPageName("Cites", "fr").pageName);
        assertEquals("cities", siteMap.getPageName("Cites", "fr").pageAlternativeKey);
        
        assertEquals("Parks", siteMap.getAlias("Info", "en", "nationalparks", null));
        assertEquals("Info", siteMap.getPageName("Parks", "en").pageName);
        assertEquals("nationalparks", siteMap.getPageName("Parks", "en").pageAlternativeKey);
        
        //test duplicate logical page/alias duplicate
        assertEquals("Test", siteMap.getAlias("Test", "en", null, null));
        assertEquals("Test", siteMap.getPageName("Test", "en").pageName);
        assertEquals("Test", siteMap.getAlias("Info", "de", "nationalparks", null));        
        assertEquals("Info", siteMap.getPageName("Test", "de").pageName);
       
        //test default page alternative
        assertEquals("baz", siteMap.getAlias("foo", "en", "2", null));
        assertEquals("foo", siteMap.getPageName("foo", "en").pageName);
        assertEquals("2", siteMap.getPageName("foo", "en").pageAlternativeKey);
        assertEquals("bar", siteMap.getAlias("foo", "en", "1", null));
        assertEquals("1", siteMap.getPageName("bar", "en").pageAlternativeKey);
        assertEquals("2", siteMap.getPageName("baz", "en").pageAlternativeKey);
        
    }
    
}
