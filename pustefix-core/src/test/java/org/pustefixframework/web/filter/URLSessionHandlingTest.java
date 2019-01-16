package org.pustefixframework.web.filter;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.SessionTrackingMode;

import org.pustefixframework.web.AbstractSessionHandlingTest;
import org.pustefixframework.web.filter.SessionTrackingFilter;


public class URLSessionHandlingTest extends AbstractSessionHandlingTest {

    @Override
    protected void configure(ServletContext context) {

        Set<SessionTrackingMode> modes = new HashSet<>();
        modes.add(SessionTrackingMode.URL);
        context.setSessionTrackingModes(modes);

        SessionTrackingFilter filter = new SessionTrackingFilter();
        FilterRegistration registration = context.addFilter("SessionTrackingFilter", filter);
        registration.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), false, "/*");
    }

    public void testNoSessionHttp() throws Exception {

        Client client = new Client();

        ClientMethod method = client.doGet("http://localhost:"+HTTP_PORT+"?foo=bar");
        method.is200().isInsecure().setsNoSessionCookie().isFirstVisit().hasContent().hasParam().hasEncodedLinks();
        client.hasNoSessionCookie();

        method = client.followLink(method);
        method.is200().isInsecure().setsNoSessionCookie().isSecondVisit().hasContent().hasNoParam().hasEncodedLinks();
        client.hasNoSessionCookie();
    }

    public void testNoSessionHttps() throws Exception {

        Client client = new Client();

        ClientMethod method = client.doGet("https://localhost:"+HTTPS_PORT+"?foo=bar");
        method.is200().isSecure().setsNoSessionCookie().isFirstVisit().hasContent().hasParam().hasEncodedLinks();
        client.hasNoSessionCookie();

        method = client.followLink(method);
        method.is200().isSecure().setsNoSessionCookie().isSecondVisit().hasContent().hasNoParam().hasEncodedLinks();
        client.hasNoSessionCookie();
    }

    public void testNoSessionHttpToHttps() throws Exception {

        Client client = new Client();

        ClientMethod method = client.doGet("http://localhost:"+HTTP_PORT+"/ssl?foo=bar");
        method.is307().redirectsToHttps().redirectsWithoutSessionParam().setsNoSessionCookie();

        method = client.followRedirect(method);
        method.is200().isSecure().setsNoSessionCookie().isFirstVisit().hasContent().hasParam().hasEncodedLinks();
        client.hasNoSessionCookie();

        method = client.followLink(method);
        method.is200().isSecure().setsNoSessionCookie().isSecondVisit().hasContent().hasNoParam().hasEncodedLinks();
        client.hasNoSessionCookie();
    }

    public void testInvalidSessionHttp() throws Exception {

        Client client = new Client();

        ClientMethod method = client.doGet("http://localhost:"+HTTP_PORT+"/;jsessionid=xyz?foo=bar");
        method.is200().isInsecure().setsNoSessionCookie().isFirstVisit().hasContent().hasParam().hasEncodedLinks();
        client.hasNoSessionCookie();

        method = client.followLink(method);
        method.is200().isInsecure().setsNoSessionCookie().isSecondVisit().hasContent().hasNoParam().hasEncodedLinks();
        client.hasNoSessionCookie();
    }

    public void testInvalidSessionHttps() throws Exception {

        Client client = new Client();

        ClientMethod method = client.doGet("https://localhost:"+HTTPS_PORT+"/;jsessionid=xyz?foo=bar");
        method.is200().isSecure().setsNoSessionCookie().isFirstVisit().hasContent().hasParam().hasEncodedLinks();
        client.hasNoSessionCookie();

        method = client.followLink(method);
        method.is200().isSecure().setsNoSessionCookie().isSecondVisit().hasContent().hasNoParam().hasEncodedLinks();
        client.hasNoSessionCookie();
    }

    public void testInvalidSessionHttpToHttps() throws Exception {

        Client client = new Client();

        ClientMethod method = client.doGet("http://localhost:"+HTTP_PORT+"/ssl;jsessionid=xyz?foo=bar");
        method.is307().redirectsToHttps().redirectsWithoutSessionParam().setsNoSessionCookie();

        method = client.followRedirect(method);
        method.is200().isSecure().setsNoSessionCookie().isFirstVisit().hasContent().hasParam().hasEncodedLinks();
        client.hasNoSessionCookie();
    }

    public void testHttpToHttpsBackButton() throws Exception {

        Client client = new Client();

        ClientMethod method = client.doGet("http://localhost:"+HTTP_PORT+"?foo=bar");
        method.is200().isInsecure().setsNoSessionCookie().isFirstVisit().hasContent().hasParam().hasEncodedLinks();
        client.hasNoSessionCookie();

        method = client.followLink(method, 1);
        method.is307().redirectsToHttps().redirectsWithSessionParam().setsNoSessionCookie();

        method = client.followRedirect(method);
        method.is200().isSecure().setsNoSessionCookie().isSecondVisit().hasContent().hasNoParam().hasEncodedLinks();
        client.hasNoSessionCookie();

        method = client.doGet("http://localhost:"+HTTP_PORT+"?foo=bar");
        method.is200().isInsecure().setsNoSessionCookie().isFirstVisit().hasContent().hasParam().hasEncodedLinks();
        client.hasNoSessionCookie();
    }

}
