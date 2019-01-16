package org.pustefixframework.web.filter;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.SessionTrackingMode;

import org.pustefixframework.web.AbstractSessionHandlingTest;


public class SessionTimeoutFilterTest extends AbstractSessionHandlingTest {

    @Override
    protected void configure(ServletContext context) {

        Set<SessionTrackingMode> modes = new HashSet<>();
        modes.add(SessionTrackingMode.COOKIE);
        context.setSessionTrackingModes(modes);

        SessionTimeoutFilter filter = new SessionTimeoutFilter();
        filter.setInitialRequestThreshold(1);
        filter.setInitialSessionTimeout(2);
        filter.setBotSessionTimeout(2);
        FilterRegistration reg = context.addFilter("SessionTimeoutFilter", filter);
        reg.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), false, "/*");
    }

    public void testInitialThresholdExceeded() throws Exception {

        Client client = new Client();

        ClientMethod method = client.doGet("http://localhost:"+HTTP_PORT);
        method.is200().isFirstVisit().setsSessionCookie();

        method = client.doGet("http://localhost:"+HTTP_PORT);
        method.is200().isSecondVisit().setsNoSessionCookie();

        try {
            Thread.sleep(3000);
        } catch(InterruptedException x) {}

        method = client.doGet("http://localhost:"+HTTP_PORT);
        method.is200().isThirdVisit().setsNoSessionCookie();
    }

    public void testInitialTimeoutReached() throws Exception {

        Client client = new Client();

        ClientMethod method = client.doGet("http://localhost:"+HTTP_PORT);
        method.is200().isFirstVisit().setsSessionCookie();

        try {
            Thread.sleep(3000);
        } catch(InterruptedException x) {}

        method = client.doGet("http://localhost:"+HTTP_PORT);
        method.is200().isFirstVisit().setsSessionCookie();
    }

    public void testBotTimeout() throws Exception {

        Client client = new Client().userAgent("Googlebot");

        ClientMethod method = client.doGet("http://localhost:"+HTTP_PORT);
        method.is200().isFirstVisit().setsSessionCookie();

        method = client.doGet("http://localhost:"+HTTP_PORT);
        method.is200().isSecondVisit().setsNoSessionCookie();

        try {
            Thread.sleep(3000);
        } catch(InterruptedException x) {}

        method = client.doGet("http://localhost:"+HTTP_PORT);
        method.is200().isFirstVisit().setsSessionCookie();
    }

}
