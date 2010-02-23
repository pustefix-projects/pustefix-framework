package de.schlund.pfixcore.example;

import java.io.File;

import junit.framework.TestCase;

import org.pustefixframework.container.spring.beans.PustefixWebApplicationContext;
import org.pustefixframework.test.PustefixWebApplicationContextLoader;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class SessionMockTest extends TestCase {

    public void test() {
        
        PustefixWebApplicationContextLoader loader = new PustefixWebApplicationContextLoader(new File("src/main/webapp"));
        String[] locations = {"docroot:/WEB-INF/project.xml", "docroot:/WEB-INF/spring.xml"};
        PustefixWebApplicationContext appContext = (PustefixWebApplicationContext) loader.loadContext(locations);
        
        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpSession session = new MockHttpSession(appContext.getServletContext());
        req.setSession(session);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(req));

        TestData data = (TestData)appContext.getBean("testdata");
        data.getText();
    }
    
}
