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
 *
 */

package org.pustefixframework.webservices.spring;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.pustefixframework.webservices.BaseTestCase;
import org.pustefixframework.webservices.TestServiceProcessor;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.support.GenericWebApplicationContext;

/**
 * 
 * @author mleidig
 *
 */
public class WebServiceHttpRequestHandlerTest extends BaseTestCase {
    
    private WebServiceHttpRequestHandler handler;
    
    @Before 
    @Override
    public void setUp() {
        super.setUp();        
        GenericWebApplicationContext ctx=new GenericWebApplicationContext();
        MockServletContext servletContext = new MockServletContext();
        ctx.setServletContext(servletContext);
        ctx.refresh();
        XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader(ctx);
        xmlReader.loadBeanDefinitions(new FileSystemResource("src/test/resources/WEB-INF/spring.xml"));
        
        handler = (WebServiceHttpRequestHandler)ctx.getBean("org.pustefixframework.webservices.spring.WebServiceHttpRequestHandler");
        TestServiceProcessor proc = new TestServiceProcessor();
        proc.setServiceMethod("echo");
        handler.getServiceRuntime().addServiceProcessor("TEST", proc);
    }

    @Test
    public void withoutSession() throws Exception {
        
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setContentType("text/plain");
        req.setCharacterEncoding("UTF-8");
        String content="xyz";
        req.setContent(content.getBytes("UTF-8"));
        req.setPathInfo("/TestNestedRef");
        req.setMethod("POST");
        MockHttpServletResponse res = new MockHttpServletResponse();
        handler.handleRequest(req, res);
        
        assertEquals(content,res.getContentAsString());
    }

    @Test
    public void withSession() throws Exception {
        
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setContentType("text/plain");
        req.setCharacterEncoding("UTF-8");
        String content="xyz";
        req.setContent(content.getBytes("UTF-8"));
        req.setPathInfo("/TestNested");
        req.setMethod("POST");
        
        MockHttpSession session = new MockHttpSession();
        req.setSession(session);
        //TODO: mock Context
        
        //MockHttpServletResponse res = new MockHttpServletResponse();
        
        //TODO: uncomment when Context can be mocked
        //handler.handleRequest(req, res);
            
        //assertEquals(content,res.getContentAsString());
    }
    
}
