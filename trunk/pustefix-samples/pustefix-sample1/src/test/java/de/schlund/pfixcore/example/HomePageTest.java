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

@ContextConfiguration(loader=PustefixWebApplicationContextLoader.class,locations={
    "docroot:/WEB-INF/project.xml", "docroot:/WEB-INF/spring.xml"})
public class HomePageTest extends AbstractJUnit4SpringContextTests {
    
    @Autowired
    private ServletContext servletContext;
    
    @Autowired
    private PustefixContextXMLRequestHandler requestHandler;
    
    @Test
    public void testPageRequest() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setPathInfo("/");
        req.setMethod("GET");
        MockHttpServletResponse res = new MockHttpServletResponse();
        MockHttpSession session = new MockHttpSession(servletContext);
        req.setSession(session);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(req));
        session.setAttribute(AbstractPustefixRequestHandler.VISIT_ID, "foo");
        
        requestHandler.handleRequest(req, res);
        Assert.assertTrue(res.getContentAsString(), res.getContentAsString().contains("<title>Pustefix Sample</title>"));
    }

}
