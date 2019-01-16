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

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.SessionTrackingMode;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.pustefixframework.web.ServletUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servlet filter implementing some useful session tracking extensions when
 * using URL based session tracking or switching from HTTP to HTTPS within
 * the application workflow.
 *
 * Be aware that this filter is only intended for usage by legacy applications.
 * Modern state-of-the-art applications neither should use URL parameters for
 * session tracking (but cookies), nor they should switch from HTTP to HTTPS
 * within the workflow (but use HTTPS only).
 */
public class SessionTrackingFilter implements Filter {

    private static Logger LOG = LoggerFactory.getLogger(SessionTrackingFilter.class);

    private static final String SESSION_ATTR_INSECURE_SESSION = "__PFX_INSECURE_SESSION__";
    private static final String SESSION_ATTR_USER_AGENT = "__PFX_USER_AGENT__";
    private static final String SESSION_ATTR_REMOTE_IP = "__PFX_REMOTE_IP__";

    public static final String COOKIE_SWITCHED_TO_SSL = "_PFXSSL_";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if(request instanceof HttpServletRequest) {
            HttpServletRequest req = (HttpServletRequest)request;
            HttpServletResponse res = (HttpServletResponse)response;

            if(!req.isSecure()) {
                if(ServletUtils.hasCookie(req, SessionTrackingFilter.COOKIE_SWITCHED_TO_SSL, "true")) {
                    //Redirect back to https, because client already had switched from http to https
                    ServletUtils.redirectToSSL(req, res, HttpServletResponse.SC_TEMPORARY_REDIRECT);
                    return;
                }
            }

            if(req.isRequestedSessionIdValid()) {
                HttpSession session = req.getSession(false);
                if(session != null) {

                    if(req.isRequestedSessionIdFromURL() && !checkClientIdentity(req)) {
                        //invalidate session if it was requested via URL parameter and the identity
                        //of the client differs from the one which initially created the session
                        session.invalidate();
                        ServletUtils.redirect(res, HttpServletResponse.SC_TEMPORARY_REDIRECT, ServletUtils.getRedirectURL(req, true));
                        return;
                    }

                    if(req.isSecure() && session.getAttribute(SESSION_ATTR_INSECURE_SESSION) != null) {
                        //Switch to new session, if request is secure, but session was created within insecure request
                        req.changeSessionId();
                        session.removeAttribute(SESSION_ATTR_INSECURE_SESSION);
                        if(req.isRequestedSessionIdFromURL()) {
                            ServletUtils.redirect(req, res, HttpServletResponse.SC_TEMPORARY_REDIRECT);
                            return;
                        } else if(req.isRequestedSessionIdFromCookie()) {
                            ServletUtils.addCookie(req, res, COOKIE_SWITCHED_TO_SSL, "true");
                        }
                    }
                }
            }

            try {
                chain.doFilter(request, response);
            } finally {

                HttpSession session = req.getSession(false);
                if(session != null && session.isNew()) {
                    if(req.getServletContext().getEffectiveSessionTrackingModes().contains(SessionTrackingMode.URL)) {
                        storeClientIdentity(req);
                    }
                    if(!req.isSecure()) {
                        session.setAttribute(SESSION_ATTR_INSECURE_SESSION, true);
                    }
                }
            }

        } else {
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {
    }

    private boolean checkClientIdentity(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if(session != null) {
            String storedIp = (String)session.getAttribute(SESSION_ATTR_REMOTE_IP);
            if(storedIp != null) {
                String ip = ServletUtils.getRemoteAddr(req);
                if(!ip.equals(storedIp)) {
                    LOG.warn("Differing client IP: " + storedIp + " => " + ip);
                    return false;
                }
            }
            String storedUserAgent = (String)session.getAttribute(SESSION_ATTR_USER_AGENT);
            if(storedUserAgent != null) {
                String userAgent = req.getHeader("User-Agent");
                if(userAgent == null) userAgent = "-";
                if(!userAgent.equals(storedUserAgent)) {
                    LOG.warn("Differing client useragent: " + storedUserAgent + " => " + userAgent);
                    return false;
                }
            }
        }
        return true;
    }

    private void storeClientIdentity(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if(session != null) {
            String ip = ServletUtils.getRemoteAddr(req);
            session.setAttribute(SESSION_ATTR_REMOTE_IP, ip);
            String userAgent = req.getHeader("User-Agent");
            if(userAgent == null) {
                userAgent = "-";
            }
            session.setAttribute(SESSION_ATTR_USER_AGENT, userAgent);
        }
    }

}
