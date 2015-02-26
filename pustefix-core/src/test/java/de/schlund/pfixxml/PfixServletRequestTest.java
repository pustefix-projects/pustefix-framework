package de.schlund.pfixxml;

import java.util.Properties;

import junit.framework.TestCase;

import org.springframework.mock.web.MockHttpServletRequest;

public class PfixServletRequestTest extends TestCase {

    public void testRemoteAddr() {

        Properties props = new Properties();

        MockHttpServletRequest httpReq = new MockHttpServletRequest();
        httpReq.setRemoteAddr("1.2.3.4");
        PfixServletRequest req = new PfixServletRequestImpl(httpReq, props);
        assertEquals("1.2.3.4", req.getRemoteAddr());

        httpReq = new MockHttpServletRequest();
        httpReq.setRemoteAddr("10.10.10.10");
        req = new PfixServletRequestImpl(httpReq, props);
        assertEquals("10.10.10.10", req.getRemoteAddr());

        httpReq = new MockHttpServletRequest();
        httpReq.setRemoteAddr("invalid ip");
        req = new PfixServletRequestImpl(httpReq, props);
        assertEquals("invalid ip", req.getRemoteAddr());

        httpReq = new MockHttpServletRequest();
        httpReq.setRemoteAddr("10.10.10.10");
        httpReq.addHeader("X-Forwarded-For", "1.2.3.4");
        req = new PfixServletRequestImpl(httpReq, props);
        assertEquals("1.2.3.4", req.getRemoteAddr());

        httpReq = new MockHttpServletRequest();
        httpReq.setRemoteAddr("2.3.4.5");
        httpReq.addHeader("X-Forwarded-For", "1.2.3.4");
        req = new PfixServletRequestImpl(httpReq, props);
        assertEquals("2.3.4.5", req.getRemoteAddr());

        httpReq = new MockHttpServletRequest();
        httpReq.setRemoteAddr("1.2.3.4");
        httpReq.addHeader("X-Forwarded-For", "10.10.10.10");
        req = new PfixServletRequestImpl(httpReq, props);
        assertEquals("1.2.3.4", req.getRemoteAddr());

        httpReq = new MockHttpServletRequest();
        httpReq.setRemoteAddr("1.2.3.4");
        httpReq.addHeader("X-Forwarded-For", "unknown");
        req = new PfixServletRequestImpl(httpReq, props);
        assertEquals("1.2.3.4", req.getRemoteAddr());

        httpReq = new MockHttpServletRequest();
        httpReq.setRemoteAddr("10.10.10.10");
        httpReq.addHeader("X-Forwarded-For", "unknown");
        req = new PfixServletRequestImpl(httpReq, props);
        assertEquals("10.10.10.10", req.getRemoteAddr());

        httpReq = new MockHttpServletRequest();
        httpReq.setRemoteAddr("10.10.10.10");
        httpReq.addHeader("X-Forwarded-For", "1.2.3.4, unknown");
        req = new PfixServletRequestImpl(httpReq, props);
        assertEquals("1.2.3.4", req.getRemoteAddr());

        httpReq = new MockHttpServletRequest();
        httpReq.setRemoteAddr("10.10.10.10");
        httpReq.addHeader("X-Forwarded-For", "unknown, 1.2.3.4");
        req = new PfixServletRequestImpl(httpReq, props);
        assertEquals("1.2.3.4", req.getRemoteAddr());

        httpReq = new MockHttpServletRequest();
        httpReq.setRemoteAddr("10.10.10.10");
        httpReq.addHeader("X-Forwarded-For", "1.2.3.4, 10.11.11.11");
        req = new PfixServletRequestImpl(httpReq, props);
        assertEquals("1.2.3.4", req.getRemoteAddr());

        httpReq = new MockHttpServletRequest();
        httpReq.setRemoteAddr("10.10.10.10");
        httpReq.addHeader("X-Forwarded-For", "10.11.11.11, 1.2.3.4");
        req = new PfixServletRequestImpl(httpReq, props);
        assertEquals("1.2.3.4", req.getRemoteAddr());

        httpReq = new MockHttpServletRequest();
        httpReq.setRemoteAddr("1.2.3.4");
        httpReq.addHeader("X-Forwarded-For", "");
        req = new PfixServletRequestImpl(httpReq, props);
        assertEquals("1.2.3.4", req.getRemoteAddr());

        httpReq = new MockHttpServletRequest();
        httpReq.setRemoteAddr("FC00:0000:0000:0000:0000:0000:0000:FFFF");
        httpReq.addHeader("X-Forwarded-For", "2001:cdba:0:0:0:0:3257:9652");
        req = new PfixServletRequestImpl(httpReq, props);
        assertEquals("2001:cdba:0:0:0:0:3257:9652", req.getRemoteAddr());
    }

}
