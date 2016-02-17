package org.pustefixframework.util;

import junit.framework.TestCase;

public class URLUtilsTest extends TestCase {

    public void testPathParameterRemoval() {
        
        assertEquals("/", URLUtils.removePathAttributes("/"));
        assertEquals("/foo", URLUtils.removePathAttributes("/foo"));
        assertEquals("/foo", URLUtils.removePathAttributes("/foo;jsessionid=123"));
        assertEquals("/foo/bar", URLUtils.removePathAttributes("/foo/bar;jsessionid=123"));
        assertEquals("/foo/bar/", URLUtils.removePathAttributes("/foo/bar/;jsessionid=123"));
        assertEquals("/foo/bar", URLUtils.removePathAttributes("/foo;hey/bar;ho=hi"));
        assertEquals("/foo/bar", URLUtils.removePathAttributes("/foo;hey/bar;ho=hi"));
        assertEquals("/foo/bar", URLUtils.removePathAttributes("/foo;a=b;b=c/bar;d"));
    }
    
}
