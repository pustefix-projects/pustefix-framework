package org.pustefixframework.http;

import org.springframework.mock.web.MockHttpServletResponse;

import junit.framework.TestCase;

public class VirtualHttpServletResponseTest extends TestCase {

    public void testAgainstSpringMock() {
        
        MockHttpServletResponse mockRes = new MockHttpServletResponse();
        mockRes.setContentType("text/html");
        mockRes.setCharacterEncoding("utf8");
        System.out.println(mockRes.getContentType());
        
        new VirtualHttpServletResponse();
    }

}
