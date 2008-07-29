package org.pustefixframework.webservices.spring;

import org.pustefixframework.webservices.BaseTestCase;
import org.pustefixframework.webservices.test.MockServiceProcessor;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.support.GenericWebApplicationContext;

public class HandlerTest extends BaseTestCase {

    public void test() throws Exception {
        /**
        GenericWebApplicationContext ctx=new GenericWebApplicationContext();
        MockServletContext servletContext = new MockServletContext();
        ctx.setServletContext(servletContext);
        ctx.refresh();
        
        XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader(ctx);
        xmlReader.loadBeanDefinitions(new FileSystemResource("src/test/resources/conf/spring-beans.xml"));
        
        WebServiceHttpRequestHandler handler = (WebServiceHttpRequestHandler)ctx.getBean("org.pustefixframework.webservices.spring.WebServiceHttpRequestHandler");
        System.out.println("'''''''''''' "+handler.getServletContext());
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setContentType("text/plain");
        req.setCharacterEncoding("UTF-8");
        req.setContent("test".getBytes("UTF-8"));
        req.setPathInfo("/Test");
        req.setMethod("POST");
        
        MockHttpSession session = new MockHttpSession();
        req.setSession(session);
        
        MockHttpServletResponse res = new MockHttpServletResponse();
        
        MockServiceProcessor proc = new MockServiceProcessor();
        proc.setContent("dfadfasd");
        
        handler.getServiceRuntime().addServiceProcessor("TEST", proc);
        
        handler.handleRequest(req, res);
      
        System.out.println("RESULT: "+res.getContentAsString());
    */
    }
    
}
