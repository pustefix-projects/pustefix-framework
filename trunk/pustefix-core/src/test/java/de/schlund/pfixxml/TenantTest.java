package de.schlund.pfixxml;

import junit.framework.TestCase;
import org.springframework.mock.web.MockHttpServletRequest;

public class TenantTest extends TestCase {

    public void testRegexps() {
        Tenant variant = new Tenant("test");
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

        variant.setHostPattern("(int\\.)?(login|account).1and1.mx");
        req.setServerName("account.1and1.mx");
        assertTrue(variant.matches(req));
        req.setServerName("login.1and1.mx");
        assertTrue(variant.matches(req));
        req.setServerName("int.account.1and1.mx");
        assertTrue(variant.matches(req));
        req.setServerName("int.login.1and1.mx");
        assertTrue(variant.matches(req));
        req.setServerName("mx.int.login.1and1.mx");
        assertFalse(variant.matches(req));
    }

}
