/*
 * This file is part of Pustefix.
 *
 * Pustefix is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Pustefix is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Pustefix; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package de.schlund.pfixcore.example;

import java.net.URL;

import javax.servlet.ServletContext;

import org.junit.Assert;
import org.junit.Test;
import org.pustefixframework.http.AbstractPustefixRequestHandler;
import org.pustefixframework.http.PustefixContextXMLRequestHandler;
import org.pustefixframework.test.PustefixWebApplicationContextLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Integration test of the scripted flows from the sample application.
 */
@ContextConfiguration(loader = PustefixWebApplicationContextLoader.class,
        locations = {"docroot:/WEB-INF/project.xml", "docroot:/WEB-INF/spring.xml", "(mode=test)"})
public class ScriptedFlowTest extends AbstractJUnit4SpringContextTests {
    
    @Autowired
    private ServletContext servletContext;
    
    @Autowired
    private PustefixContextXMLRequestHandler requestHandler;
    
    @Test
    public void testFlow() throws Exception {
        
        MockHttpSession session = new MockHttpSession(servletContext);
        session.setAttribute(AbstractPustefixRequestHandler.VISIT_ID, "foo");
        
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setPathInfo("/");
        req.addParameter("__scriptedflow", "forceadult");
        req.setSession(session);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(req));
        MockHttpServletResponse res = new MockHttpServletResponse();
        
        requestHandler.handleRequest(req, res);
        Assert.assertEquals(301, res.getStatus());
        Assert.assertTrue(res.getHeader("Location").toString().contains("/order"));
       
        req = new MockHttpServletRequest();
        req.setPathInfo("/order");
        URL url = new URL(res.getHeader("Location").toString());
        req.setQueryString(url.getQuery());
        req.setSession(session);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(req));
        res = new MockHttpServletResponse();
        
        requestHandler.handleRequest(req, res);
        Assert.assertEquals(200, res.getStatus());
        Assert.assertTrue(res.getContentAsString(), res.getContentAsString().contains("<title>Pustefix Sample</title>"));
        
    }
    
    @Test
    public void testInteractiveFlow() throws Exception {
        
        MockHttpSession session = new MockHttpSession(servletContext);
        session.setAttribute(AbstractPustefixRequestHandler.VISIT_ID, "bar");
        
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setPathInfo("/");
        req.addParameter("__scriptedflow", "ordershirt");
        req.addParameter("color", "4");
        req.setSession(session);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(req));
        MockHttpServletResponse res = new MockHttpServletResponse();
        
        requestHandler.handleRequest(req, res);
        Assert.assertEquals(200, res.getStatus());
        Assert.assertTrue(res.getContentAsString(), res.getContentAsString().contains("<title>Pustefix Sample</title>"));
        
        req = new MockHttpServletRequest();
        req.setPathInfo("/");
        req.addParameter("__lf", "OrderFlow");
        req.addParameter("__sendingdata", "1");
        req.addParameter("info.Adult", "false");
        req.setSession(session);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(req));
        res = new MockHttpServletResponse();
        
        requestHandler.handleRequest(req, res);
        Assert.assertEquals(301, res.getStatus());
        Assert.assertTrue(res.getHeader("Location").toString().contains("/overview"));
       
        req = new MockHttpServletRequest();
        req.setPathInfo("/overview");
        URL url = new URL(res.getHeader("Location").toString());
        req.setQueryString(url.getQuery());
        req.setSession(session);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(req));
        res = new MockHttpServletResponse();
        
        requestHandler.handleRequest(req, res);
        Assert.assertEquals(200, res.getStatus());
        Assert.assertTrue(res.getContentAsString(), res.getContentAsString().contains("<title>Pustefix Sample</title>"));
        
    }

}
