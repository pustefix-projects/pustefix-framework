package org.pustefixframework.http;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import de.schlund.pfixcore.workflow.SiteMap;
import de.schlund.pfixcore.workflow.SiteMap.PageLookupResult;
import junit.framework.TestCase;

public class PathMappingTest extends TestCase {

    public void test() throws Exception {
        
        File siteMapFile = new File(getClass().getResource("sitemap.xml").toURI());
        File siteMapAliasFile = new File(getClass().getResource("sitemap-aliases.xml").toURI());
        SiteMap siteMap = new SiteMap(siteMapFile, new File[] {siteMapAliasFile});
        
        Set<String> pageNames = siteMap.getPageNames(true);
        assertEquals(20, pageNames.size());
        
        Set<String> allAltKeys = siteMap.getAllPageAlternativeKeys("x");
        Set<String> expSet = new HashSet<String>();
        expSet.add("k1");
        expSet.add("k2");
        expSet.add("k3");
        expSet.add("k4");
        assertEquals(expSet, allAltKeys);

        Set<String> allAliases = siteMap.getAllPageAliases("x", "en_GB", false);
        expSet = new HashSet<String>();
        expSet.add("main/one/x-k1");
        expSet.add("main/one/");
        expSet.add("main/two/x-k3");
        expSet.add("main/two/x");
        expSet.add("x-k4");
        expSet.add("x");
        assertEquals(expSet, allAliases);
        
        allAliases = siteMap.getAllPageAliases("x", "en_GB", true);
        expSet = new HashSet<String>();
        expSet.add("main/one");
        expSet.add("main/one/");
        expSet.add("main/one/x-k1");
        expSet.add("main/one/x-k2");
        expSet.add("main/two/x-k3");
        expSet.add("main/two/x");
        expSet.add("x-k4");
        expSet.add("x");
        assertEquals(expSet, allAliases);
        
        allAliases = siteMap.getAllPageAliases("u", "en_GB", false);
        expSet = new HashSet<String>();
        expSet.add("main/g3/u");
        assertEquals(expSet, allAliases);
        
        allAliases = siteMap.getAllPageAliases("u", "en_GB", true);
        expSet = new HashSet<String>();
        expSet.add("main/g3/u");
        assertEquals(expSet, allAliases);
        
        allAliases = siteMap.getAllPageAliases("v", "en_GB", false);
        expSet = new HashSet<String>();
        expSet.add("main/g3/");
        assertEquals(expSet, allAliases);
        
        allAliases = siteMap.getAllPageAliases("v", "en_GB", true);
        expSet = new HashSet<String>();
        expSet.add("main/g3");
        expSet.add("main/g3/");
        expSet.add("main/g3/v");
        assertEquals(expSet, allAliases);
        
        allAliases = siteMap.getAllPageAliases("t", "en_GB", false);
        expSet = new HashSet<String>();
        expSet.add("main/");
        assertEquals(expSet, allAliases);
        
        allAliases = siteMap.getAllPageAliases("t", "en_GB", true);
        expSet = new HashSet<String>();
        expSet.add("main/t");
        expSet.add("main");
        expSet.add("main/");
        assertEquals(expSet, allAliases);
        
        allAliases = siteMap.getAllPageAliases("z", "en_GB", false);
        expSet = new HashSet<String>();
        expSet.add("main/zz");
        assertEquals(expSet, allAliases);
        
        allAliases = siteMap.getAllPageAliases("z", "en_GB", true);
        expSet = new HashSet<String>();
        expSet.add("main/z");
        expSet.add("main/zz");
        assertEquals(expSet, allAliases);
        
        allAliases = siteMap.getAllPageAliases("page_s", "en_GB", false);
        expSet = new HashSet<String>();
        expSet.add("group_s/page_s");
        assertEquals(expSet, allAliases);
        
        allAliases = siteMap.getAllPageAliases("page_m", "en_GB", false);
        expSet = new HashSet<String>();
        expSet.add("group_m/group_m1/page_m");
        expSet.add("group_m/group_m2/page_m");
        expSet.add("group_m/page_m");
        assertEquals(expSet, allAliases);
        
        allAliases = siteMap.getAllPageAliases("merged", "en_GB", false);
        expSet = new HashSet<String>();
        expSet.add("main/merged");
        assertEquals(expSet, allAliases);
        
        allAliases = siteMap.getAllPageAliases("encoding", "en_GB", false);
        expSet = new HashSet<String>();
        expSet.add("xencodingpage");
        assertEquals(expSet, allAliases);
        
        allAliases = siteMap.getAllPageAliases("encoding", "en_GB", true);
        expSet = new HashSet<String>();
        expSet.add("encodingpage");
        expSet.add("xencodingpage");
        assertEquals(expSet, allAliases);
        
        allAliases = siteMap.getAllPageAliases("TA", "en_GB", false);
        expSet = new HashSet<String>();
        expSet.add("TA");
        expSet.add("TB");
        expSet.add("TG/TA");
        expSet.add("TG/TC");
        assertEquals(expSet, allAliases);
        
        allAliases = siteMap.getAllPageAliases("TA", "en_GB", true);
        expSet = new HashSet<String>();
        expSet.add("TA");
        expSet.add("TB");
        expSet.add("TG/TA");
        expSet.add("TG/TC");
        assertEquals(expSet, allAliases);
        
        PageLookupResult res =  siteMap.getPageName("main/one/x-k1", "en_GB");
        assertEquals("one", res.getPageGroup());
        assertEquals("x", res.getPageName());
        assertEquals("k1", res.getPageAlternativeKey());
        
        res =  siteMap.getPageName("main/one/x-k2", "en_GB");
        assertEquals("one", res.getPageGroup());
        assertEquals("x", res.getPageName());
        assertEquals("k2", res.getPageAlternativeKey());
        
        res =  siteMap.getPageName("main/one", "en_GB");
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
        
        res = siteMap.getPageName("x/page_x", "en_GB");
        assertEquals("x", res.getPageGroup());
        assertEquals("page_x", res.getPageName());
        
        //Page group with same name as an existing page
        res = siteMap.getPageName("x", "en_GB");
        assertNull("x", res.getPageGroup());
        assertEquals("x", res.getPageName());
        
        res =  siteMap.getPageName("main/zz", "en_GB");
        assertEquals("main", res.getPageGroup());
        assertEquals("z", res.getPageName());
        
        res =  siteMap.getPageName("main/z", "en_GB");
        assertEquals("main", res.getPageGroup());
        assertEquals("z", res.getPageName());
        
        res =  siteMap.getPageName("main/g3/u", "en_GB");
        assertEquals("three", res.getPageGroup());
        assertEquals("u", res.getPageName());
        
        res =  siteMap.getPageName("main/g3", "en_GB");
        assertEquals("three", res.getPageGroup());
        assertEquals("v", res.getPageName());
        
        res =  siteMap.getPageName("main/t", "en_GB");
        assertEquals("main", res.getPageGroup());
        assertEquals("t", res.getPageName());
        
        res =  siteMap.getPageName("main", "en_GB");
        assertEquals("main", res.getPageGroup());
        assertEquals("t", res.getPageName());
        
        res =  siteMap.getPageName("AN100V", "en_GB");
        assertNull(res.getPageGroup());
        assertEquals("an100v", res.getPageName());
        assertNull(res.getPageAlternativeKey());
        
        res =  siteMap.getPageName("xencodingpage", "en_GB");
        assertNull(res.getPageGroup());
        assertEquals("encoding", res.getPageName());
        assertEquals("xencoding", res.getPageAlternativeKey());
        
        res =  siteMap.getPageName("encodingpage", "en_GB");
        assertNull(res.getPageGroup());
        assertEquals("encoding", res.getPageName());
        assertEquals("xencoding", res.getPageAlternativeKey());
        
        res =  siteMap.getPageName("encoding", "en_GB");
        assertNull(res.getPageGroup());
        assertEquals("encoding", res.getPageName());
        assertEquals("xencoding", res.getPageAlternativeKey());

        res =  siteMap.getPageName("foo", null);
        assertEquals("foo", res.getPageName());
        assertNull(res.getPageGroup());
        assertNull(res.getPageAlternativeKey());

        res =  siteMap.getPageName("Fu", "de_DE");
        assertEquals("foo", res.getPageName());
        assertNull(res.getPageGroup());
        assertNull(res.getPageAlternativeKey());

        res =  siteMap.getPageName("Fu", "de");
        assertEquals("foo", res.getPageName());
        assertNull(res.getPageGroup());
        assertNull(res.getPageAlternativeKey());

        res =  siteMap.getPageName("nonexisting", "de_DE");
        assertEquals("nonexisting", res.getPageName());
        assertNull(res.getPageGroup());
        assertNull(res.getPageAlternativeKey());

        res =  siteMap.getPageName("nonexisting", "de");
        assertEquals("nonexisting", res.getPageName());
        assertNull(res.getPageGroup());
        assertNull(res.getPageAlternativeKey());

        res =  siteMap.getPageName("main/one/YYY", "de_DE");
        assertEquals("y", res.getPageName());
        assertEquals("one", res.getPageGroup());
        assertNull(res.getPageAlternativeKey());

        res =  siteMap.getPageName("main/ZZZ", "de_DE");
        assertEquals("z", res.getPageName());
        assertEquals("main", res.getPageGroup());
        assertNull(res.getPageAlternativeKey());

        res =  siteMap.getPageName("foo/123", "en_GB");
        assertEquals("foo", res.getPageName());
        assertNull(res.getPageGroup());
        assertNull(res.getPageAlternativeKey());

        assertEquals("", PathMapping.getURLPath("home", null, null, null, "en_GB", "home", null, siteMap));
        assertEquals("", PathMapping.getURLPath(null, null, null, null, "en_GB", "home", null, siteMap));
        assertEquals("foo", PathMapping.getURLPath("foo", null, null, null, "en_GB", "home", null, siteMap));
        assertEquals("foo", PathMapping.getURLPath("foo", null, "nonexisting", null, "en_GB", "home", null, siteMap));
        assertEquals("foo", PathMapping.getURLPath("foo", null, "main", null, "en_GB", "home", null, siteMap));
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
        assertEquals("main/one/", PathMapping.getURLPath("x", "k2", "one", null, "en_GB", "home", null, siteMap));
        assertEquals("main/one/x-k1", PathMapping.getURLPath("x", "k1", "one", null, "en_GB", "home", null, siteMap));
        assertEquals("main/zz", PathMapping.getURLPath("z", null, "main", null, "en_GB", "home", null, siteMap));
        assertEquals("main/zz", PathMapping.getURLPath("z", null, "one", null, "en_GB", "home", null, siteMap));
        assertEquals("main/g3/", PathMapping.getURLPath("v", null, "three", null, "en_GB", "home", null, siteMap));
        assertEquals("main/one/y", PathMapping.getURLPath("y", null, null, null, "en_GB", "home", null, siteMap));
        assertEquals("x", PathMapping.getURLPath("x", "k2", "nonexisting", null, "en_GB", "home", null, siteMap));
        assertEquals("x", PathMapping.getURLPath("x", null, "nonexisting", null, "en_GB", "home", null, siteMap));
        
        assertEquals("d", PathMapping.getURLPath("ptn", "d", "h", null, "en_GB", "home", null, siteMap));
        assertEquals("h/", PathMapping.getURLPath("ptn", null, "h", null, "en_GB", "home", null, siteMap));
        assertEquals("h/btia/", PathMapping.getURLPath("ptn", "h/btia", "h", null, "en_GB", "home", null, siteMap));

        assertEquals("AN100V", PathMapping.getURLPath("an100v", null, null, null, "en_GB", "home", null, siteMap));
        assertEquals("anfb/an100v", PathMapping.getURLPath("ban100v", "anfb/an100v", "anfb", null, "en_GB", "home", null, siteMap));

        assertEquals("AN100V", PathMapping.getURLPath("an100v", null, null, null, "en_GB", "home", null, siteMap));
        assertEquals("anfb/an100v", PathMapping.getURLPath("ban100v", "anfb/an100v", "anfb", null, "en_GB", "home", null, siteMap));
        
        assertEquals("xencodingpage", PathMapping.getURLPath("encoding", null, null, null, "en_GB", "home", null, siteMap));
        assertEquals("xencodingpage", PathMapping.getURLPath("encoding", "xencoding", null, null, "en_GB", "home", null, siteMap));
        
        assertEquals("nonexisting", PathMapping.getURLPath("nonexisting", null, null, null, "en_GB", "home", null, siteMap));
        assertEquals("nonexisting", PathMapping.getURLPath("nonexisting", "", null, null, "en_GB", "home", null, siteMap));
        
        assertEquals("TA", PathMapping.getURLPath("TA", null, null, null, "en_GB", "home", null, siteMap));
        assertEquals("TB", PathMapping.getURLPath("TA", "TB", null, null, "en_GB", "home", null, siteMap));
        assertEquals("TG/TA", PathMapping.getURLPath("TA", null, "TG", null, "en_GB", "home", null, siteMap));
        assertEquals("TG/TC", PathMapping.getURLPath("TA", "TC", "TG", null, "en_GB", "home", null, siteMap));
        assertEquals("TB", PathMapping.getURLPath("TA", "TB", "TG", null, "en_GB", "home", null, siteMap));
        
        assertEquals("Fuh", PathMapping.getURLPath("foo", null, null, null, "de_DE", "home", null, siteMap));
        assertEquals("Fu", PathMapping.getURLPath("foo", null, null, null, "de_AT", "home", null, siteMap));
        assertEquals("baz", PathMapping.getURLPath("bar", null, null, null, "de_DE", "home", null, siteMap));

    }
    
}
