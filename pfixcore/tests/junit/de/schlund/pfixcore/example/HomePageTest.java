package de.schlund.pfixcore.example;

import javax.servlet.ServletContext;

import org.junit.Assert;
import org.pustefixframework.http.PustefixContextXMLRequestHandler;
import org.pustefixframework.test.PustefixWebApplicationContextLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit38.AbstractJUnit38SpringContextTests;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import de.schlund.pfixxml.serverutil.SessionHelper;

@ContextConfiguration(loader=PustefixWebApplicationContextLoader.class,locations={"file:projects/sample1/conf/project.xml","file:projects/sample1/conf/spring.xml"})
public class HomePageTest extends AbstractJUnit38SpringContextTests {
    
    @Autowired
    private ServletContext servletContext;
    
    @Autowired
    private PustefixContextXMLRequestHandler requestHandler;
    
    public void testPageRequest() throws Exception {
   
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setPathInfo("/home");
        req.setMethod("GET");
        MockHttpServletResponse res = new MockHttpServletResponse();
        MockHttpSession session = new MockHttpSession(servletContext);
        req.setSession(session);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(req));
        session.setAttribute(SessionHelper.SESSION_ID_URL, SessionHelper.getURLSessionId(req));
        
        requestHandler.handleRequest(req, res);
        Assert.assertTrue(res.getContentAsString().contains("<title>PFIXCORE Sample</title>"));
        
    }

}
