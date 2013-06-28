package org.pustefixframework.util;


import junit.framework.TestCase;

public class NetUtilsTest extends TestCase {

    public void testIPCheck() {
        
        assertTrue(NetUtils.checkIP("127.0.0.1"));
        assertTrue(NetUtils.checkIP("::1"));
        assertTrue(NetUtils.checkIP("2001:0db8:85a3:0000:0000:8a2e:0370:7334"));
        assertTrue(NetUtils.checkIP("2001:0db8:85a3:0:0:8a2e:0370:7334"));
        assertTrue(NetUtils.checkIP("2001:0db8:85a3::8a2e:0370:7334"));
        assertTrue(NetUtils.checkIP("2001::"));
        assertTrue(NetUtils.checkIP("1::1"));
        assertTrue(NetUtils.checkIP("2001:cdba:0000:0000:0000:0000:3257:9652"));
        assertTrue(NetUtils.checkIP("2001:cdba:0:0:0:0:3257:9652"));
        assertTrue(NetUtils.checkIP("2001:cdba::3257:9652"));
        assertTrue(NetUtils.checkIP("2001:0db8:0:0:8d3:0:0:0"));
        assertTrue(NetUtils.checkIP("2001:db8:0:0:8d3::"));
        assertTrue(NetUtils.checkIP("2001:db8::8d3:0:0:0"));
        
        assertFalse(NetUtils.checkIP(""));
        assertFalse(NetUtils.checkIP("127"));
        assertFalse(NetUtils.checkIP("127.0.0"));
        assertFalse(NetUtils.checkIP("127.0.0.256"));
        assertFalse(NetUtils.checkIP(":2001:0db8:85a3:0000:0000:8a2e:0370"));
        assertFalse(NetUtils.checkIP("1::1::1"));
        assertFalse(NetUtils.checkIP("::2001:0db8:85a3:0000:0000:8a2e:0370:7334"));
        assertFalse(NetUtils.checkIP("2001:0db8:85a3:0000:0000:8a2e:0370:733X"));
        assertFalse(NetUtils.checkIP("2001:0db8:85a3:0000:0000:8a2e:0370:7334:"));
        assertFalse(NetUtils.checkIP("2001:0db8:85a3:0000:0000:8a2e:0370:7334:1"));
        assertFalse(NetUtils.checkIP("2001:db8::8d3::"));
        
    }
    
}
