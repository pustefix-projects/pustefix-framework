package org.pustefixframework.samples.mvctest;

import javax.servlet.ServletContext;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pustefixframework.http.AbstractPustefixRequestHandler;
import org.pustefixframework.http.PustefixContextXMLRequestHandler;
import org.pustefixframework.web.ServletUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SpringConfig.class})
@WebAppConfiguration
public class HomePageTest {

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
        session.setAttribute(ServletUtils.SESSION_ATTR_VISIT_ID, "foo");

        requestHandler.handleRequest(req, res);
        Assert.assertTrue(res.getContentAsString(), res.getContentAsString().contains("<title>Pustefix MVC Test</title>"));
    }

}
