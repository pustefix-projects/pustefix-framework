package org.pustefixframework.web.filter;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.SessionTrackingMode;

import org.pustefixframework.web.AbstractSessionHandlingTest;
import org.pustefixframework.web.ServletUtils;
import org.pustefixframework.web.filter.SessionTrackingFilter;


public class CookieSessionHandlingTest extends AbstractSessionHandlingTest {

    @Override
    protected void configure(ServletContext context) {

        Set<SessionTrackingMode> modes = new HashSet<>();
        modes.add(SessionTrackingMode.COOKIE);
        modes.add(SessionTrackingMode.URL);
        context.setSessionTrackingModes(modes);

        SessionTrackingFilter filter = new SessionTrackingFilter();
        FilterRegistration registration = context.addFilter("SessionTrackingFilter", filter);
        registration.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), false, "/*");
    }

    public void testNoSessionHttp() throws Exception {

        Client client = new Client();

        ClientMethod method = client.doGet("http://localhost:"+HTTP_PORT+"?foo=bar");
        method.is200().isInsecure().setsSessionCookie().isFirstVisit().hasContent().hasParam().hasEncodedLinks();
        client.hasSessionCookie();

        method = client.followLink(method);
        method.is200().isInsecure().setsNoSessionCookie().isSecondVisit().hasContent().hasNoParam().hasNoEncodedLinks();
        client.hasSessionCookie();

        client = new Client().disableCookies();

        method = client.doGet("http://localhost:"+HTTP_PORT+"?foo=bar");
        method.is200().isInsecure().setsSessionCookie().isFirstVisit().hasContent().hasParam().hasEncodedLinks();
        client.hasNoSessionCookie();

        method = client.followLink(method);
        method.is200().isInsecure().setsNoSessionCookie().isSecondVisit().hasContent().hasNoParam().hasEncodedLinks();
        client.hasNoSessionCookie();
    }

    public void testNoSessionHttps() throws Exception {

        Client client = new Client();

        ClientMethod method = client.doGet("https://localhost:"+HTTPS_PORT+"?foo=bar");
        method.is200().isSecure().setsSessionCookie().isFirstVisit().hasContent().hasParam().hasEncodedLinks();
        client.hasSecureSessionCookie();

        method = client.followLink(method);
        method.is200().isSecure().setsNoSessionCookie().isSecondVisit().hasContent().hasNoParam().hasNoEncodedLinks();
        client.hasSecureSessionCookie();

        client = new Client().disableCookies();

        method = client.doGet("https://localhost:"+HTTPS_PORT+"?foo=bar");
        method.is200().isSecure().setsSessionCookie().isFirstVisit().hasContent().hasParam().hasEncodedLinks();
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
        method.is200().isSecure().setsSessionCookie().isFirstVisit().hasContent().hasParam().hasEncodedLinks();
        client.hasSecureSessionCookie();

        method = client.followLink(method);
        method.is200().isSecure().setsNoSessionCookie().isSecondVisit().hasContent().hasNoParam().hasNoEncodedLinks();
        client.hasSecureSessionCookie();

        client = new Client().disableCookies();

        method = client.doGet("http://localhost:"+HTTP_PORT+"/ssl?foo=bar");
        method.is307().redirectsToHttps().redirectsWithoutSessionParam().setsNoSessionCookie();

        method = client.followRedirect(method);
        method.is200().isSecure().setsSessionCookie().isFirstVisit().hasContent().hasParam().hasEncodedLinks();
        client.hasNoSessionCookie();

        method = client.followLink(method);
        method.is200().isSecure().setsNoSessionCookie().isSecondVisit().hasContent().hasNoParam().hasEncodedLinks();
        client.hasNoSessionCookie();
    }    

    public void testInvalidSessionHttp() throws Exception {

        Client client = new Client();
        client.addCookie(ServletUtils.DEFAULT_SESSION_COOKIE_NAME, "xyz");

        ClientMethod method = client.doGet("http://localhost:"+HTTP_PORT+"?foo=bar");
        method.is200().isInsecure().setsSessionCookie().isFirstVisit().hasContent().hasParam().hasNoEncodedLinks();
        client.hasSessionCookie();

        method = client.followLink(method);
        method.is200().isInsecure().setsNoSessionCookie().isSecondVisit().hasContent().hasNoParam().hasNoEncodedLinks();
        client.hasSessionCookie();

        client = new Client().disableCookies();

        method = client.doGet("http://localhost:"+HTTP_PORT+"/;jsessionid=xyz?foo=bar");
        method.is200().isInsecure().setsSessionCookie().isFirstVisit().hasContent().hasParam().hasEncodedLinks();
        client.hasNoSessionCookie();

        method = client.followLink(method);
        method.is200().isInsecure().setsNoSessionCookie().isSecondVisit().hasContent().hasNoParam().hasEncodedLinks();
        client.hasNoSessionCookie();
    }

    public void testInvalidSessionHttps() throws Exception {

        Client client = new Client();
        client.addCookie(ServletUtils.DEFAULT_SESSION_COOKIE_NAME, "xyz", true);

        ClientMethod method = client.doGet("https://localhost:"+HTTPS_PORT+"?foo=bar");
        method.is200().isSecure().setsSessionCookie().isFirstVisit().hasContent().hasParam().hasNoEncodedLinks();
        client.hasSecureSessionCookie();

        method = client.followLink(method);
        method.is200().isSecure().setsNoSessionCookie().isSecondVisit().hasContent().hasNoParam().hasNoEncodedLinks();
        client.hasSecureSessionCookie();

        client = new Client().disableCookies();

        method = client.doGet("https://localhost:"+HTTPS_PORT+"/;jsessionid=xyz?foo=bar");
        method.is200().isSecure().setsSessionCookie().isFirstVisit().hasContent().hasParam().hasEncodedLinks();
        client.hasNoSessionCookie();

        method = client.followLink(method);
        method.is200().isSecure().setsNoSessionCookie().isSecondVisit().hasContent().hasNoParam().hasEncodedLinks();
        client.hasNoSessionCookie();
    }

    public void testInvalidSessionHttpToHttps() throws Exception {

        Client client = new Client();
        client.addCookie(ServletUtils.DEFAULT_SESSION_COOKIE_NAME, "xyz");

        ClientMethod method = client.doGet("http://localhost:"+HTTP_PORT+"/ssl?foo=bar");
        method.is307().redirectsToHttps().redirectsWithoutSessionParam().setsNoSessionCookie();

        method = client.followRedirect(method);
        method.is200().isSecure().setsSessionCookie().isFirstVisit().hasContent().hasParam().hasNoEncodedLinks();
        client.hasSecureSessionCookie();

        client = new Client().disableCookies();

        method = client.doGet("http://localhost:"+HTTP_PORT+"/ssl;jsessionid=xyz?foo=bar");
        method.is307().redirectsToHttps().redirectsWithoutSessionParam().setsNoSessionCookie();

        method = client.followRedirect(method);
        method.is200().isSecure().setsSessionCookie().isFirstVisit().hasContent().hasParam().hasEncodedLinks();
        client.hasNoSessionCookie();
    }

    public void testHttpToHttpsBackButton() throws Exception {

        Client client = new Client();

        ClientMethod method = client.doGet("http://localhost:"+HTTP_PORT+"?foo=bar");
        method.is200().isInsecure().setsSessionCookie().isFirstVisit().hasContent().hasParam().hasEncodedLinks();
        client.hasSessionCookie();

        method = client.followLink(method, 1);
        method.is200().isSecure().setsSessionCookie().isSecondVisit().hasContent().hasNoParam().hasNoEncodedLinks();
        client.hasSecureSessionCookie();

        method = client.doGet("http://localhost:"+HTTP_PORT+"?foo=bar");
        method.is307().redirectsToHttps().redirectsWithoutSessionParam().setsNoSessionCookie();
        client.hasSecureSessionCookie();

        method = client.followRedirect(method);
        method.is200().isSecure().setsNoSessionCookie().isThirdVisit().hasContent().hasParam().hasNoEncodedLinks();
        client.hasSecureSessionCookie();

        client = new Client().disableCookies();

        method = client.doGet("http://localhost:"+HTTP_PORT+"?foo=bar");
        method.is200().isInsecure().setsSessionCookie().isFirstVisit().hasContent().hasParam().hasEncodedLinks();
        client.hasNoSessionCookie();

        method = client.followLink(method, 1);
        method.is307().redirectsToHttps().redirectsWithSessionParam().setsSessionCookie();

        method = client.followRedirect(method);
        method.is200().isSecure().setsNoSessionCookie().isSecondVisit().hasContent().hasNoParam().hasEncodedLinks();
        client.hasNoSessionCookie();

        method = client.doGet("http://localhost:"+HTTP_PORT+"?foo=bar");
        method.is200().isInsecure().setsSessionCookie().isFirstVisit().hasContent().hasParam().hasEncodedLinks();
        client.hasNoSessionCookie();
    }

    public void testHttpToHttpsBackButtonExpired() throws Exception {

        Client client = new Client();
        client.addCookie(SessionTrackingFilter.COOKIE_SWITCHED_TO_SSL, "true");
        client.addCookie(ServletUtils.DEFAULT_SESSION_COOKIE_NAME, "xyz", true);

        ClientMethod method = client.doGet("http://localhost:"+HTTP_PORT+"?foo=bar");
        method.is307().redirectsToHttps().redirectsWithoutSessionParam().setsNoSessionCookie();
        client.hasSessionCookie();

        method = client.followRedirect(method);
        method.is200().isSecure().setsSessionCookie().isFirstVisit().hasContent().hasParam().hasNoEncodedLinks();
        client.hasSecureSessionCookie();
    }

}
