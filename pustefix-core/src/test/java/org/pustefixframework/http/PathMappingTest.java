package org.pustefixframework.http;

import java.io.File;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import de.schlund.pfixcore.workflow.SiteMap;
import de.schlund.pfixcore.workflow.SiteMap.PageLookupResult;
import junit.framework.TestCase;

public class PathMappingTest extends TestCase {

    public void test() throws Exception {
        
        URL url = getClass().getResource("sitemap.xml");
        File file = new File(url.toURI());
        SiteMap siteMap = new SiteMap(file, new File[0]);
        
        Set<String> pageNames = siteMap.getPageNames(true);
        assertEquals(10, pageNames.size());
        
        Set<String> allAltKeys = siteMap.getAllPageAlternativeKeys("x");
        Set<String> expSet = new HashSet<String>();
        expSet.add("k1");
        expSet.add("k2");
        expSet.add("k3");
        expSet.add("k4");
        assertEquals(expSet, allAltKeys);
       
        Set<String> allAliases = siteMap.getAllPageAliases("x", "en_GB");
        expSet = new HashSet<String>();
        expSet.add("main/one/x-k1");
        expSet.add("main/one/x-k2");
        expSet.add("main/two/x-k3");
        expSet.add("main/two/x");
        expSet.add("x-k4");
        expSet.add("x");
        assertEquals(expSet, allAliases);
        
        allAliases = siteMap.getAllPageAliases("page_s", "en_GB");
        expSet = new HashSet<String>();
        expSet.add("group_s/page_s");
        assertEquals(expSet, allAliases);
        
        allAliases = siteMap.getAllPageAliases("page_m", "en_GB");
        expSet = new HashSet<String>();
        expSet.add("group_m/group_m1/page_m");
        expSet.add("group_m/group_m2/page_m");
        expSet.add("group_m/page_m");
        assertEquals(expSet, allAliases);
        
        PageLookupResult res =  siteMap.getPageName("main/one/x-k1", "en_GB");
        assertEquals("one", res.getPageGroup());
        assertEquals("x", res.getPageName());
        assertEquals("k1", res.getPageAlternativeKey());
        res =  siteMap.getPageName("main/one/x-k2", "en_GB");
        assertEquals("one", res.getPageGroup());
        assertEquals("x", res.getPageName());
        assertEquals("k2", res.getPageAlternativeKey());
        res =  siteMap.getPageName("main/two/x-k3", "en_GB");
        assertEquals("two", res.getPageGroup());
        assertEquals("x", res.getPageName());
        assertEquals("k3", res.getPageAlternativeKey());
        res =  siteMap.getPageName("x-k4", "en_GB");
        assertNull(res.getPageGroup());
        assertEquals("x", res.getPageName());
        assertEquals("k4", res.getPageAlternativeKey());
        
        assertEquals("", PathMapping.getURLPath("home", null, null, null, "en_GB", "home", null, siteMap));
        assertEquals("", PathMapping.getURLPath(null, null, null, null, "en_GB", "home", null, siteMap));
        assertEquals("foo", PathMapping.getURLPath("foo", null, null, null, "en_GB", "home", null, siteMap));
        assertEquals("baz", PathMapping.getURLPath("bar", null, null, null, "en_GB", "home", null, siteMap));
        assertEquals("baz", PathMapping.getURLPath("baz", null, null, null, "en_GB", "home", null, siteMap));
        assertEquals("xyz", PathMapping.getURLPath("xyz", null, null, null, "en_GB", "home", null, siteMap));
        
        assertEquals("myflow", PathMapping.getURLPath("home", null, null, "flow", "en_GB", "home", null, siteMap));
        assertEquals("myflow", PathMapping.getURLPath(null, null, null, "flow", "en_GB", "home", null, siteMap));
        assertEquals("myflow/foo", PathMapping.getURLPath("foo", null, null, "flow", "en_GB", "home", null, siteMap));
        assertEquals("myflow/baz", PathMapping.getURLPath("bar", null, null, "flow", "en_GB", "home", null, siteMap));
        assertEquals("myflow/baz", PathMapping.getURLPath("baz", null, null, "flow", "en_GB", "home", null, siteMap));
        assertEquals("myflow/xyz", PathMapping.getURLPath("xyz", null, null, "flow", "en_GB", "home", null, siteMap));
        assertEquals("otherflow", PathMapping.getURLPath("home", null, null, "otherflow", "en_GB", "home", null, siteMap));
        
        assertEquals("hey", PathMapping.getURLPath("hey", null, null, null, "en_GB", "home", null, siteMap));
        assertEquals("heyho", PathMapping.getURLPath("hey", "ho", null, null, "en_GB", "home", null, siteMap));
        assertEquals("hihey", PathMapping.getURLPath("hey", "hi", null, null, "en_GB", "home", null, siteMap));
        assertEquals("hidefhey", PathMapping.getURLPath("defhey", null, null, null, "en_GB", "home", null, siteMap));
        assertEquals("myflow/hihey", PathMapping.getURLPath("hey", "hi", null, "flow", "en_GB", "home", null, siteMap));
        assertEquals("otherflow/heyho", PathMapping.getURLPath("hey", "ho", null, "otherflow", "en_GB", "home", null, siteMap));
        
        assertEquals("fr", PathMapping.getURLPath(null, null, null, null, "fr_CA", "home", "en_CA", siteMap));
        assertEquals("fr", PathMapping.getURLPath("home", null, null, null, "fr_CA", "home", "en_CA", siteMap));
        assertEquals("fr/foo", PathMapping.getURLPath("foo", null, null, null, "fr_CA", "home", "en_CA", siteMap));
        assertEquals("fr/myflow/foo", PathMapping.getURLPath("foo", null, null, "flow", "fr_CA", "home", "en_CA", siteMap));
        assertEquals("myflow/foo", PathMapping.getURLPath("foo", null, null, "flow", "en_CA", "home", "en_CA", siteMap));
        assertEquals("fr/myflow/hihey", PathMapping.getURLPath("hey", "hi", null, "flow", "fr_CA", "home", "en_CA", siteMap));
        
        assertEquals("main/one/y", PathMapping.getURLPath("y", null, "one", null, "en_GB", "home", null, siteMap));
        assertEquals("main/one/x-k2", PathMapping.getURLPath("x", "k2", "one", null, "en_GB", "home", null, siteMap));
        assertEquals("main/z", PathMapping.getURLPath("z", null, "main", null, "en_GB", "home", null, siteMap));
        assertEquals("main/z", PathMapping.getURLPath("z", null, "one", null, "en_GB", "home", null, siteMap));
        
    }
    
}
