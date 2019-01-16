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
 */
package org.pustefixframework.web.filter;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.pustefixframework.util.net.IPRangeMatcher;
import org.pustefixframework.web.ServletUtils;

/**
 * Servlet filter which redirects requests containing an invalid session, if the
 * current node/worker is in disabled state. Therefor the session cookie and/or
 * session URL parameter is removed, which lets the loadbalancer rebalance to
 * an other, active node.
 * The rebalancing can be prevented by adding a special cookie, e.g. for testing
 * purposes. A special URL parameter is supported too, but it's not recommended
 * and only exists for backwards compatibility.
 */
public class LoadBalancerDrainingFilter implements Filter {

    final static String REQUEST_ATTR_JK_LB_ACTIVATION = "JK_LB_ACTIVATION";
    final static Pattern FORCELOCAL_PARAM_PATTERN =
            Pattern.compile("(\\A|&)__forcelocal=(1|true|yes)(\\Z|&)");

    private String ignoreCookieName;
    private String ignoreCookieValue;

    private IPRangeMatcher ignoreAllowFrom;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

        String value = filterConfig.getInitParameter("ignoreAllowFrom");
        if(value != null) {
            String[] tokens = value.trim().split("\\s+");
            if(tokens.length > 0) {
                ignoreAllowFrom = new IPRangeMatcher(tokens);
            }
        }
        value = filterConfig.getInitParameter("ignoreCookieName");
        if(value != null) {
            ignoreCookieName = value;
        }
        value = filterConfig.getInitParameter("ignoreCookieValue");
        if(value != null) {
            ignoreCookieValue = value;
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if(request instanceof HttpServletRequest && response instanceof HttpServletResponse) {

            HttpServletRequest req = (HttpServletRequest)request;
            HttpServletResponse res = (HttpServletResponse)response;

            if("DIS".equals(req.getAttribute(REQUEST_ATTR_JK_LB_ACTIVATION)) &&
                    !req.isRequestedSessionIdValid()) {

                if(!isIgnored(req)) {
                    if(ServletUtils.getSessionCookie(req) != null) {
                        ServletUtils.resetSessionCookie(req, res);
                    }
                    res.setStatus(307);
                    res.setHeader("Location", ServletUtils.getRedirectURL(req, true));
                    res.setHeader("Cache-Control", "private");
                    return;
                }
            }
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }

    private boolean isIgnored(HttpServletRequest req) {
        return ((ignoreCookieName != null && ignoreCookieValue != null
                && ServletUtils.hasCookie(req, ignoreCookieName, ignoreCookieValue))
                || containsForceLocalParam(req)) && (ignoreAllowFrom == null ||
                ignoreAllowFrom.matches(ServletUtils.getRemoteAddr(req)));
    }

    /**
     * Set a whitespace separated list of IPs or IP ranges
     * from where it is allowed to prevent potential rebalancing
     * using an ignore cookie or parameter.
     *
     * @param ips - list of IPs or IP ranges
     */
    public void setIgnoreAllowFrom(String... ips) {
        if(ips == null || ips.length == 0) {
            ignoreAllowFrom = null;
        } else {
            ignoreAllowFrom = new IPRangeMatcher(ips);
        }
    }

    /**
     * Sets the name of the cookie that prevents potential rebalancing.
     * @param ignoreCookieName - cookie name
     */
    public void setIgnoreCookieName(String ignoreCookieName) {
        this.ignoreCookieName = ignoreCookieName;
    }

    /**
     * Sets the expected value of the cookie that prevents potential rebalancing.
     * @param ignoreCookieValue - cookie value
     */
    public void setIgnoreCookieValue(String ignoreCookieValue) {
        this.ignoreCookieValue = ignoreCookieValue;
    }

    /**
     * Checks if the HttpServletRequest contains a __forcelocal parameter.
     * Directly checks the undecoded query string to prevent early decoding
     * of the request parameters using a possibly wrong encoding (e.g. might
     * be set in a subsequent servlet filter or servlet).
     *
     * @param req - the HttpServletRequest
     * @return if HttpServletRequest contains __forcelocal parameter
     */
    boolean containsForceLocalParam(HttpServletRequest req) {
        String queryString = req.getQueryString();
        if(queryString != null) {
            Matcher matcher = FORCELOCAL_PARAM_PATTERN.matcher(queryString);
            return matcher.find();
        }
        return false;
    }

}
