package de.schlund.pfixxml;

import junit.framework.TestCase;

import org.springframework.mock.web.MockHttpServletRequest;

import de.schlund.pfixxml.AppVariant;

public class AppVariantTest extends TestCase {

    public void testRegexps() {
        AppVariant variant = new AppVariant("test");
        variant.setHostPattern("pustefixframework.org");
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setServerName("pustefixframework.org");
        assertTrue(variant.matches(req));
        req.setServerName("www.pustefixframework.org");
        assertFalse(variant.matches(req));
        variant.setHostPattern("^.*\\.org$");
        req.setServerName("pustefixframework.org");
        assertTrue(variant.matches(req));
        req.setServerName("www.pustefixframework.org");
        assertTrue(variant.matches(req));
        variant.setHostPattern("^(.*\\.us)|(us\\..*)|(us-.*)$");
        req.setServerName("localhost.us");
        assertTrue(variant.matches(req));
        req.setServerName("us.localhost");
        assertTrue(variant.matches(req));
        req.setServerName("us-localhost");
        assertTrue(variant.matches(req));
        req.setServerName("localhost-us");
        assertFalse(variant.matches(req));
    }
    
}
