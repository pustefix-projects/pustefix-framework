package org.pustefixframework.util.net;

import static org.junit.Assert.assertArrayEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import junit.framework.TestCase;

public class IPRangeTest extends TestCase {

    public void testInvalidIP() throws Exception {

        try {
            new IPRange("444.444.444.444");
            fail("expected exception");
        } catch(IllegalArgumentException x) {}

        try {
            new IPRange(null);
            fail("expected exception");
        } catch(IllegalArgumentException x) {}

        IPRange range = new IPRange("");
        assertTrue(range.contains("127.0.0.1"));

        try {
            new IPRange(null, "127.0.0.1");
            fail("expected exception");
        } catch(IllegalArgumentException x) {}

        try {
            new IPRange(null, "444.444.444.444");
            fail("expected exception");
        } catch(IllegalArgumentException x) {}
    }

    public void testIPv4() throws Exception {

        IPRange range = new IPRange("127.0.0.1");
        assertFalse(range.contains("127.0.0.0"));
        assertFalse(range.contains("127.0.0.2"));
        assertTrue(range.contains("127.0.0.1"));
        assertFalse(range.contains("invalid ip"));
        assertFalse(range.contains("www.pustefixframework.org"));
    }

    public void testIPv4Cidr() throws Exception {

        IPRange range = new IPRange("192.168.100.0/22");
        assertFalse(range.contains("127.0.0.1"));
        assertFalse(range.contains("192.168.099.255"));
        assertFalse(range.contains("192.168.104.0"));
        assertTrue(range.contains("192.168.100.0"));
        assertTrue(range.contains("192.168.103.255"));
        assertTrue(range.contains("192.168.102.111"));

        range = new IPRange("10.43.8.67/28");
        assertFalse(range.contains("127.0.0.1"));
        assertFalse(range.contains("10.43.8.63"));
        assertFalse(range.contains("10.43.8.80"));
        assertTrue(range.contains("10.43.8.64"));
        assertTrue(range.contains("10.43.8.70"));
        assertTrue(range.contains("10.43.8.79"));

        range = new IPRange("192.145.103.10", "192.145.103.20");
        assertFalse(range.contains("127.0.0.1"));
        assertFalse(range.contains("192.145.103.9"));
        assertFalse(range.contains("192.145.103.21"));
        assertTrue(range.contains("192.145.103.10"));
        assertTrue(range.contains("192.145.103.20"));
        assertTrue(range.contains("192.145.103.15"));

    }   

    public void testIPv6Cidr() throws Exception {

        IPRange range = new IPRange("2001:db8::/48");
        assertFalse(range.contains("127.0.0.1"));
        assertFalse(range.contains("2001:db7:ffff:ffff:ffff:ffff:ffff:ffff"));
        assertFalse(range.contains("2001:db8:1:0:0:0:0:0"));
        assertTrue(range.contains("2001:db8:0:0:0:0:0:0"));
        assertTrue(range.contains("2001:db8:0:ffff:ffff:ffff:ffff:ffff"));
        assertTrue(range.contains("2001:db8:0:0:ffff:0:0:0"));

        range = new IPRange("2001:0db8:85a3::8a2e:0370:7334/64");
        assertFalse(range.contains("127.0.0.1"));
        assertFalse(range.contains("2001:0DB8:85A2:ffff:ffff:ffff:ffff:ffff"));
        assertFalse(range.contains("2001:0DB8:85A3:0001:0000:0000:0000:0000"));
        assertTrue(range.contains("2001:0DB8:85A3:0000:0000:0000:0000:0000"));
        assertTrue(range.contains("2001:0DB8:85A3:0000:ffff:ffff:ffff:ffff"));
        assertTrue(range.contains("2001:0DB8:85A3:0000:1111:ffff:ffff:ffff"));
    }

    public void testComparison() {

        IPRange range1 = new IPRange("127.0.1.1");
        IPRange range2 = new IPRange("127.0.0.10", "127.0.0.20");
        IPRange range3 = new IPRange("127.0.0.100/30");
        IPRange range4 = new IPRange("127.0.0.100/29");
        IPRange range5 = new IPRange("127.0.0.100");
        IPRange range6 = new IPRange("127.0.0.0/30");
        IPRange range7 = new IPRange("2001:0db8:85a3::8a2e:0370:7334/120");
        IPRange range8 = new IPRange("2001:0db8:85a3::8a2e:0370:7334/122");
        IPRange range9 = new IPRange("2001:0DB8:85A3:0000:0000:8A2E:0370:7111");
        IPRange range10 = new IPRange("2001:0DB8:85A3:0000:0000:8A2E:0370:8111");

        List<IPRange> expected = new ArrayList<IPRange>();
        expected.add(range6);
        expected.add(range2);
        expected.add(range4);
        expected.add(range5);
        expected.add(range3);
        expected.add(range1);
        expected.add(range9);
        expected.add(range8);
        expected.add(range7);
        expected.add(range10);

        SortedSet<IPRange> set = new TreeSet<IPRange>();
        set.add(range1);
        set.add(range2);
        set.add(range3);
        set.add(range4);
        set.add(range5);
        set.add(range6);
        set.add(range7);
        set.add(range8);
        set.add(range9);
        set.add(range10);

        assertArrayEquals(set.toArray(new IPRange[set.size()]), expected.toArray(new IPRange[expected.size()]));
    }

}