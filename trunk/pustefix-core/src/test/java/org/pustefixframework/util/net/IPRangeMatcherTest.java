package org.pustefixframework.util.net;

import junit.framework.TestCase;

public class IPRangeMatcherTest extends TestCase {

    public void testMatchingIPs() {

        IPRangeMatcher matcher = new IPRangeMatcher("127.0.0.110", "127.0.0.100/30", "2001:0db8:85a3::8a2e:0370:7330/122");
        assertTrue(matcher.matches("127.0.0.100"));
        assertTrue(matcher.matches("127.0.0.103"));
        assertFalse(matcher.matches("127.0.0.99"));
        assertFalse(matcher.matches("127.0.0.104"));
        assertTrue(matcher.matches("127.0.0.110"));
        assertTrue(matcher.matches("2001:0DB8:85A3:0000:0000:8A2E:0370:7330"));
        assertTrue(matcher.matches("2001:0DB8:85A3:0000:0000:8A2E:0370:733F"));
        assertFalse(matcher.matches("2001:0DB8:85A3:0000:0000:8A2E:0370:7299"));
        assertFalse(matcher.matches("2001:0DB8:85A3:0000:0000:8A2E:0370:7340"));
        assertTrue(matcher.matches("127.0.0.100"));
        assertTrue(matcher.matches("127.0.0.103"));
        assertFalse(matcher.matches("127.0.0.99"));
        assertFalse(matcher.matches("127.0.0.104"));
        assertTrue(matcher.matches("127.0.0.110"));
        assertTrue(matcher.matches("2001:0DB8:85A3:0000:0000:8A2E:0370:7330"));
        assertTrue(matcher.matches("2001:0DB8:85A3:0000:0000:8A2E:0370:733F"));
        assertFalse(matcher.matches("2001:0DB8:85A3:0000:0000:8A2E:0370:7299"));
        assertFalse(matcher.matches("2001:0DB8:85A3:0000:0000:8A2E:0370:7340"));
    }

    public void testPrivateNetwork() {

        IPRangeMatcher matcher = new IPRangeMatcher("10.0.0.0/8", "169.254.0.0/16", "172.16.0.0/12", 
                "192.168.0.0/16", "fc00::/7");
        assertTrue(matcher.matches("10.0.0.0"));
        assertTrue(matcher.matches("10.255.255.255"));
        assertTrue(matcher.matches("169.254.0.0"));
        assertTrue(matcher.matches("169.254.255.255"));
        assertTrue(matcher.matches("172.16.0.0"));
        assertTrue(matcher.matches("172.31.255.255"));
        assertTrue(matcher.matches("192.168.0.0"));
        assertTrue(matcher.matches("192.168.255.255"));
        assertTrue(matcher.matches("FC00:0000:0000:0000:0000:0000:0000:0000"));
        assertTrue(matcher.matches("FDFF:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF"));
        assertFalse(matcher.matches("9.255.255.255"));
        assertFalse(matcher.matches("11.0.0.0"));
        assertFalse(matcher.matches("169.253.255.255"));
        assertFalse(matcher.matches("169.255.0.0"));
        assertFalse(matcher.matches("172.15.255.255"));
        assertFalse(matcher.matches("172.32.0.0"));
        assertFalse(matcher.matches("192.167.255.255"));
        assertFalse(matcher.matches("192.169.0.0"));
        assertFalse(matcher.matches("FBFF:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF"));
        assertFalse(matcher.matches("FE00:0000:0000:0000:0000:0000:0000:0000"));
    }

}
