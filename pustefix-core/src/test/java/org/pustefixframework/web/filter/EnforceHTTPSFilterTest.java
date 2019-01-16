package org.pustefixframework.web.filter;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.SessionTrackingMode;

import org.pustefixframework.web.AbstractSessionHandlingTest;


public class EnforceHTTPSFilterTest extends AbstractSessionHandlingTest {

    @Override
    protected void configure(ServletContext context) {

        Set<SessionTrackingMode> modes = new HashSet<>();
        modes.add(SessionTrackingMode.COOKIE);
        modes.add(SessionTrackingMode.URL);
        context.setSessionTrackingModes(modes);

        FilterRegistration reg = context.addFilter("EnforceHTTPSFilter", EnforceHTTPSFilter.class);
        reg.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), false, "/*");
    }

    public void testRedirect() throws Exception {

        Client client = new Client();

        ClientMethod method = client.doGet("http://localhost:"+HTTP_PORT+"/test?foo=bar");
        method.is307().redirectsToHttps();

        method = client.followRedirect(method);
        method.is200();
    }

}
