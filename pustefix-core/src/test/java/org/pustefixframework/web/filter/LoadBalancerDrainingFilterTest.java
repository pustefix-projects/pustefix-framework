package org.pustefixframework.web.filter;

import java.io.IOException;
import java.net.InetAddress;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.SessionTrackingMode;

import org.pustefixframework.web.AbstractSessionHandlingTest;
import org.pustefixframework.web.ServletUtils;

public class LoadBalancerDrainingFilterTest extends AbstractSessionHandlingTest {

    private static LoadBalancerTestFilter testFilter;
    private static LoadBalancerDrainingFilter drainingFilter;

    @Override
    protected void configure(ServletContext context) {

        Set<SessionTrackingMode> modes = new HashSet<>();
        modes.add(SessionTrackingMode.COOKIE);
        modes.add(SessionTrackingMode.URL);
        context.setSessionTrackingModes(modes);

        testFilter = new LoadBalancerTestFilter();
        FilterRegistration reg = context.addFilter("LoadBalancerTestFilter", testFilter);
        reg.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), false, "/*");

        drainingFilter = new LoadBalancerDrainingFilter();
        reg = context.addFilter("LoadBalancerDrainingFilter", drainingFilter);
        reg.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), false, "/*");
    }

    public void testInvalidSessionFromCookie() throws Exception {

        testFilter.setDisabled(true);

        Client client = new Client();
        client.addCookie(ServletUtils.DEFAULT_SESSION_COOKIE_NAME, "xyz");

        ClientMethod method = client.doGet("http://localhost:"+HTTP_PORT+"/test?foo=bar");
        method.dump();
        method.is307().clearsSessionCookie();
        client.hasNoSessionCookie();

        testFilter.setDisabled(false);

        method = client.followRedirect(method);
        method.is200().setsSessionCookie();
        client.hasSessionCookie();
    }

    public void testInvalidSessionFromParam() throws Exception {

        testFilter.setDisabled(true);

        Client client = new Client();

        ClientMethod method = client.doGet("http://localhost:"+HTTP_PORT+"/test;jsessionid=xyz?foo=bar");
        method.is307().redirectsWithoutSessionParam();
        client.hasNoSessionCookie();

        testFilter.setDisabled(false);

        method = client.followRedirect(method);
        method.is200().setsSessionCookie();
        client.hasSessionCookie();
    }

    public void testForceLocalParam() throws Exception {

        testFilter.setDisabled(true);
        drainingFilter.setIgnoreAllowFrom(InetAddress.getLoopbackAddress().getHostAddress());

        Client client = new Client();

        ClientMethod method = client.doGet("http://localhost:"+HTTP_PORT+"/test;jsessionid=xyz?foo=bar&__forcelocal=1");
        method.is200().setsSessionCookie();
        client.hasSessionCookie();
    }

    public void testForceLocalParamNotAllowed() throws Exception {

        testFilter.setDisabled(true);
        drainingFilter.setIgnoreAllowFrom("1.1.1.1");

        Client client = new Client();
        client.addCookie(ServletUtils.DEFAULT_SESSION_COOKIE_NAME, "xyz");

        ClientMethod method = client.doGet("http://localhost:"+HTTP_PORT+"/test;jsessionid=xyz?foo=bar&__forcelocal=1");
        method.is307().clearsSessionCookie().redirectsWithoutSessionParam();
        client.hasNoSessionCookie();

        testFilter.setDisabled(false);

        method = client.followRedirect(method);
        method.is200().setsSessionCookie();
        client.hasSessionCookie();
    }

    public void testForceLocalCookie() throws Exception {

        testFilter.setDisabled(true);
        drainingFilter.setIgnoreCookieName("__forcelocal");
        drainingFilter.setIgnoreCookieValue("1");
        drainingFilter.setIgnoreAllowFrom();

        Client client = new Client();
        client.addCookie(ServletUtils.DEFAULT_SESSION_COOKIE_NAME, "xyz");
        client.addCookie("__forcelocal", "1");

        ClientMethod method = client.doGet("http://localhost:"+HTTP_PORT+"/test?foo=bar");
        method.is200().setsSessionCookie();
        client.hasSessionCookie();
    }

    public void testForceLocalParamPattern() throws Exception {

        Pattern pattern = LoadBalancerDrainingFilter.FORCELOCAL_PARAM_PATTERN;

        assertTrue(pattern.matcher("__forcelocal=1").find());
        assertTrue(pattern.matcher("__forcelocal=true&foo=bar").find());
        assertTrue(pattern.matcher("foo=bar&__forcelocal=1").find());
        assertTrue(pattern.matcher("foo=bar&__forcelocal=yes&").find());

        assertFalse(pattern.matcher("___forcelocal=1").find());
        assertFalse(pattern.matcher("__forcelocal=11").find());
        assertFalse(pattern.matcher("__forcelocal=").find());
    }


    public class LoadBalancerTestFilter implements Filter {

        boolean disabled;

        @Override
        public void init(FilterConfig filterConfig) throws ServletException {
        }

        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                throws IOException, ServletException {

            String state = disabled ? "DIS" : "ACT";
            request.setAttribute(LoadBalancerDrainingFilter.REQUEST_ATTR_JK_LB_ACTIVATION, state);
            chain.doFilter(request, response);
        }

        @Override
        public void destroy() {
        }

        public void setDisabled(boolean disabled) {
            this.disabled = disabled;
        }

    }

}
