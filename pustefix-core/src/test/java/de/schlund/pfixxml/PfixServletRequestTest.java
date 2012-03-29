package de.schlund.pfixxml;

import java.util.Properties;

import junit.framework.TestCase;

import org.springframework.mock.web.MockHttpServletRequest;


public class PfixServletRequestTest extends TestCase {
    
    public void testRemoteAddr() {
        
        Properties props = new Properties();
        
        MockHttpServletRequest httpReq = new MockHttpServletRequest();
        httpReq.setRemoteAddr("1.2.3.4");
        httpReq.addHeader("X-Forwarded-For", "127.0.0.1");
        PfixServletRequest req = new PfixServletRequestImpl(httpReq, props);
        assertEquals("127.0.0.1", req.getRemoteAddr());
        
        httpReq = new MockHttpServletRequest();
        httpReq.setRemoteAddr("1.2.3.4");
        httpReq.addHeader("X-Forwarded-For", "unknown");
        req = new PfixServletRequestImpl(httpReq, props);
        assertEquals("1.2.3.4", req.getRemoteAddr());
        
        httpReq = new MockHttpServletRequest();
        httpReq.setRemoteAddr("1.2.3.4");
        httpReq.addHeader("X-Forwarded-For", "unknown, 127.0.0.1");
        req = new PfixServletRequestImpl(httpReq, props);
        assertEquals("127.0.0.1", req.getRemoteAddr());
        
        httpReq = new MockHttpServletRequest();
        httpReq.setRemoteAddr("1.2.3.4");
        httpReq.addHeader("X-Forwarded-For", "");
        req = new PfixServletRequestImpl(httpReq, props);
        assertEquals("1.2.3.4", req.getRemoteAddr());
        
        httpReq = new MockHttpServletRequest();
        httpReq.setRemoteAddr("1.2.3.4");
        httpReq.addHeader("X-Forwarded-For", "1.2.3.4, 2001:cdba:0:0:0:0:3257:9652");
        req = new PfixServletRequestImpl(httpReq, props);
        assertEquals("2001:cdba:0:0:0:0:3257:9652", req.getRemoteAddr());
        
    }

}
