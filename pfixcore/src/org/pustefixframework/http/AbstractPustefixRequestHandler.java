/*
 * This file is part of PFIXCORE.
 *
 * PFIXCORE is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * PFIXCORE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with PFIXCORE; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */

package org.pustefixframework.http;

import java.io.IOException;
import java.nio.charset.Charset;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.TreeSet;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.pustefixframework.admin.mbeans.WebappAdmin;
import org.pustefixframework.config.contextxmlservice.ServletManagerConfig;
import org.pustefixframework.container.spring.http.UriProvidingHttpRequestHandler;
import org.pustefixframework.http.internal.FactoryInitWorker;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.context.ServletContextAware;

import de.schlund.pfixxml.FactoryInitException;
import de.schlund.pfixxml.FactoryInitUtil;
import de.schlund.pfixxml.PfixServletRequest;
import de.schlund.pfixxml.PfixServletRequestImpl;
import de.schlund.pfixxml.exceptionprocessor.ExceptionConfig;
import de.schlund.pfixxml.exceptionprocessor.ExceptionProcessor;
import de.schlund.pfixxml.serverutil.SessionAdmin;
import de.schlund.pfixxml.serverutil.SessionHelper;
import de.schlund.pfixxml.serverutil.SessionInfoStruct;
import de.schlund.pfixxml.serverutil.SessionInfoStruct.TrailElement;
import de.schlund.pfixxml.util.CookieUtils;
import de.schlund.pfixxml.util.MD5Utils;

/**
 * ServletManager.java
 *
 *
 * Created: Wed May  8 16:39:06 2002
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 */

public abstract class AbstractPustefixRequestHandler implements UriProvidingHttpRequestHandler, ServletContextAware, InitializingBean {
    public static final String           VISIT_ID                      = "__VISIT_ID__";
    public static final String           PROP_LOADINDEX                = "__PROPERTIES_LOAD_INDEX";
    private static final String          STORED_REQUEST                = "__STORED_PFIXSERVLETREQUEST__";
    private static final String          SECURE_SESS_COOKIE            = "__PFIX_SSC_";
    private static final String          SECURE_SESS_COOKIE_OLD        = "__PFIX_SEC_";
    private static final String          COOKIE_VALUE_SEPARATOR        = "_";
    private static final String          COOKIE_VALUE_SEPARATOR_OLD    = ":";
    private static final String          TEST_COOKIE                   = "__PFIX_TST_";
    private static final String          SESSION_COOKIES_MARKER        = "__COOKIES_USED_DURING_SESSION__";
    private static final String          REFUSE_COOKIES                = "__REFUSE_COOKIES__";
    private static final String          RAND_SESS_COOKIE_VALUE        = "__RAND_SESS_COOKIE_VALUE__";
    private static final String          CHECK_FOR_RUNNING_SSL_SESSION = "__CHECK_FOR_RUNNING_SSL_SESSION__";
    private static final String          PARAM_FORCELOCAL              = "__forcelocal";
    public static final String           PROP_COOKIE_SEC_NOT_ENFORCED  = "servletmanager.cookie_security_not_enforced";
    public static final String           PROP_P3PHEADER                = "servletmanager.p3p";
    private static final String          PROP_EXCEPTION                = "exception";
    public static final String           PROP_SSL_REDIRECT_PORT        = "pfixcore.ssl_redirect_port.for.";
    protected static final String        DEF_CONTENT_TYPE              = "text/html";
    private static final String          DEFAULT_ENCODING              = "UTF-8";
    private static final String          SERVLET_ENCODING              = "servlet.encoding";
    private static final int             MAX_PARALLEL_SEC_SESSIONS     = 10;
    private static String                TIMESTAMP_ID                  = "";
    private static int                   INC_ID                        = 0;
    private boolean                      cookie_security_not_enforced  = false;
    private Logger                       LOGGER_VISIT                  = Logger.getLogger("LOGGER_VISIT");
    private static Logger                       LOG                           = Logger.getLogger(AbstractPustefixRequestHandler.class);
    private Map<Class<? extends Throwable>, ExceptionConfig> exceptionConfigs = new HashMap<Class<? extends Throwable>, ExceptionConfig>();
    private String                       servletEncoding;
    private ServletContext servletContext;
    private String handlerURI;
    private SessionAdmin sessionAdmin;
    private WebappAdmin webappAdmin;

    protected abstract ServletManagerConfig getServletManagerConfig();

    protected boolean runningUnderSSL(HttpServletRequest req) {
        return req.isSecure();
    }

    protected boolean needsSSL(PfixServletRequest preq) throws ServletException {
        return this.getServletManagerConfig().isSSL();
    }

    abstract protected boolean needsSession();

    abstract protected boolean allowSessionCreate();

    public static String getServerName(HttpServletRequest req) {
        String forward = req.getHeader("X-Forwarded-Server");
        if (forward != null && !forward.equals("")) {
            return forward;
        } else {
            return req.getServerName();
        }
    }
    
    protected void relocate(HttpServletResponse res, String reloc_url) {
        relocate(res, HttpServletResponse.SC_MOVED_TEMPORARILY, reloc_url);
    }

    protected void relocate(HttpServletResponse res, int type, String reloc_url) {
        LOG.debug("\n\n        ======> relocating to " + reloc_url + "\n");
        res.setHeader("Expires", "Mon, 26 Jul 1997 05:00:00 GMT");
        res.setHeader("Pragma", "no-cache");
        res.setHeader("Cache-Control", "no-cache, no-store, private, must-revalidate");
        res.setStatus(type);
        res.setHeader("Location", reloc_url);
    }
    
    public void setHandlerURI(String uri) {
        this.handlerURI = uri;
    }
    
    public String[] getRegisteredURIs() {
        return new String[] { handlerURI };
    }

    public void handleRequest(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        
        if(webappAdmin!=null) webappAdmin.refreshIfTriggered();
        
        req.setCharacterEncoding(servletEncoding);
        res.setCharacterEncoding(servletEncoding);
        if (LOG.isDebugEnabled()) {
            LOG.debug("\n ------------------- Start of new Request ---------------");
            LOG.debug("====> Scheme://Server:Port " + req.getScheme() + "://" + getServerName(req) + ":" + req.getServerPort());
            LOG.debug("====> URI:   " + req.getRequestURI());
            LOG.debug("====> Query: " + req.getQueryString());
            LOG.debug("----> needsSession=" + needsSession() + " allowSessionCreate=" + allowSessionCreate());
            LOG.debug("====> Sessions: " + sessionAdmin.toString());
            LOG.debug("\n");

            Enumeration<?> headers = req.getHeaderNames();

            while (headers.hasMoreElements()) {
                String header = (String) headers.nextElement();
                String headerval = req.getHeader(header);
                LOG.debug("+++ Header: " + header + " -> " + headerval);
            }

        }

        HttpSession session = null;
        boolean has_session = false;
        boolean has_ssl_session_insecure = false;
        boolean has_ssl_session_secure = false;
        boolean force_jump_back_to_ssl = false;
        boolean force_reuse_visit_id = false;
        String mark_session_as_no_cookies = null;
        boolean does_cookies = false;
        boolean refuse_cookies = false;
        
        // Set P3P-Header if needed to make sure it is 
        // set for every response (even redirects).
        String p3pHeader = getServletManagerConfig().getProperties().getProperty(PROP_P3PHEADER);
        if (p3pHeader != null && p3pHeader.length() > 0) {
            res.addHeader("P3P", p3pHeader);
        }
        
        // Delete JSESSIONID cookie
        // Otherwise a redirect loop will be caused when a request with an
        // invalid JSESSIONID cookie is made
        Cookie[] cookies = CookieUtils.getCookies(req);
        if (cookies != null) {
            for (int i = 0; i < cookies.length; i++) {
                Cookie cookie = cookies[i];
                if (cookie.getName().equalsIgnoreCase("JSESSIONID")) {
                    cookie.setMaxAge(0);
                    cookie.setPath("/");
System.out.println("/");
                    res.addCookie(cookie);
                    String path = req.getContextPath() + req.getServletPath();
                    if(req.getPathInfo() != null) path += req.getPathInfo();
                    int ind = 0;
                    while((ind = path.indexOf('/', ind + 1)) > -1) {
                      cookie = (Cookie)cookie.clone();
                      cookie.setPath(path.substring(0, ind));
System.out.println(path.substring(0,ind));
                      res.addCookie(cookie);
                    }
                    if(path.length() > 0) {
                      cookie = (Cookie)cookie.clone();
                      cookie.setPath(path);
System.out.println(path);
                      res.addCookie(cookie);
                    }
                   
                }
            }
        }
        
        if (req.isRequestedSessionIdValid()) {
            session = req.getSession(false);
            has_session = true;
            LOG.debug("*** Found valid session with ID " + session.getId());

            // Much of the advanced security depends on having cookies enabled.  We need to make
            // sure that this isn't defeated by just disabling cookies.  So we mark every session
            // whenever the client has cookies enabled, and don't allow further uses of this session
            // without cookies. BUT: having a valid session that has the attribute
            // __REFUSE_COOKIES__ set, will be considered as not doing cookies at all. See
            // below where mark_session_as_no_cookies is set for the reason behind this.
            Boolean refuse_param = (Boolean) session.getAttribute(REFUSE_COOKIES);
            if (refuse_param != null && refuse_param.booleanValue()) {
                does_cookies = false;
                refuse_cookies = true;
            } else {
                refuse_cookies = false;
                does_cookies = doCookieTest(req, res, session);
            }
            Boolean secure = (Boolean) session.getAttribute(SessionAdmin.SESSION_IS_SECURE);

            if (!does_cookies && !refuse_cookies) {
                LOG.debug("*** Client doesn't use cookies...");
                // We still need to check if the session itself thinks differently -
                // this happens e.g. when cookies are disabled in the middle of the session.
                Boolean need_cookies = (Boolean) session.getAttribute(SESSION_COOKIES_MARKER);
                if (need_cookies != null && need_cookies.booleanValue()) {
                    if (cookie_security_not_enforced) {
                        LOG.debug("    ... during the session cookies were ENABLED, " + "but will continue because of cookie_security_not_enforced " + session.getId());
                    } else {
                        LOG.debug("    ... but during the session cookies were already ENABLED: " + "Will invalidate the session " + session.getId());
                        session.invalidate();
                        has_session = false;
                    }
                } else {
                    LOG.debug("    ... and during the session cookies were DISABLED, too: Let's hope everything is OK...");
                }
            } else if (!does_cookies && refuse_cookies) {
                LOG.debug("*** Session REFUSES to use cookies!");
                LOG.debug("    Client may send cookies, but session refuses to handle them.");
            } else {
                LOG.debug("*** Client uses cookies.");
            }
            if (has_session) {
                if (runningUnderSSL(req)) {
                    LOG.debug("*** Found running under SSL");
                    if (secure != null && secure.booleanValue()) {
                        LOG.debug("    ... and session is secure.");
                        if (does_cookies) {
                            LOG.debug("*** Client does cookies: Double checking SSL cookie for session ID");
                            String sec_testid = (String) session.getAttribute(SECURE_SESS_COOKIE + MD5Utils.hex_md5(session.getId()));
                            LOG.debug("*** Session expects to see the cookie value " + sec_testid);
                            Cookie cookie = getSecureSessionCookie(req, session.getId());
                            cleanupCookies(req, res, cookie);
                            if (cookie != null) {
                                LOG.debug("*** Found a matching cookie ...");
                                String tmp = cookie.getValue();
                                String tmp_sec = tmp.substring(tmp.indexOf(COOKIE_VALUE_SEPARATOR) + 1);
                                if (tmp_sec.equals(sec_testid)) {
                                    LOG.debug("   ... and the value is correct! (" + tmp_sec + ")");
                                    has_ssl_session_secure = true;
                                    Cookie cookie_new = new Cookie(cookie.getName(), System.currentTimeMillis() + COOKIE_VALUE_SEPARATOR + tmp_sec);
                                    setCookiePath(req, cookie_new);
                                    // FIXME (see comment in cleanupCookies
                                    // cookie_new.setMaxAge(session.getMaxInactiveInterval());
                                    cookie_new.setMaxAge(-1);
                                    cookie_new.setSecure(true);
                                    res.addCookie(cookie_new);
                                } else {
                                    LOG.debug("   ... but the value is WRONG!");
                                    LOG.error("*** Wrong Session-ID for running secure session from cookie. " + "IP:" + req.getRemoteAddr() + " Cookie: " + cookie.getValue()
                                            + " SessID: " + session.getId());
                                    session.invalidate();
                                    has_session = false;
                                }
                            } else {
                                LOG.debug("*** Found NO matching cookie at all, but client does cookies: ***");
                                LOG.error("*** NOSECSESSIDFROMCOOKIE: " + req.getRemoteAddr() + "|" + session.getId() + "|" + req.getHeader("User-Agent") + "|"
                                        + req.getHeader("Cookie"));
                                // Most time when this happens, we are not under attack, but one of
                                // two things happened: a) a stupid behaviour (bug?)  of IE or opera
                                // strikes us bad: With these two browsers, if we accept the
                                // __PFIX_TST_* cookie, but then deny the __PFIX_SEC_* cookie AND
                                // also deny for all further cookies from the domain, the stupid
                                // browser will still continue to send the __PFIX_TST_* cookie, so
                                // we will continue to come into this branch over and over
                                // again... or b) We simply have stored too many cookies for the
                                // secure cookie to be send by the client. So we try to mark the now
                                // created session to decide in the following requests that this
                                // session does NOT use cookies at all, despite what ever the
                                // __PFIX_TST_* cookie says.  Basically we completely switch off
                                // cookie handling for this new session.
                                mark_session_as_no_cookies = (String) session.getAttribute(VISIT_ID);
                                session.invalidate();
                                has_session = false;
                            }
                        } else {
                            // We don't do cookies, so we simply have to believe it...
                            has_ssl_session_secure = true;
                        }
                    } else {
                        LOG.debug("    ... but session is insecure!");
                        has_ssl_session_insecure = true;
                    }
                } else if (secure != null && secure.booleanValue()) {
                    LOG.debug("*** Found secure session but NOT running under SSL => Destroying session.");
                    session.invalidate();
                    has_session = false;
                }
            }
        } else if (req.getRequestedSessionId() != null) {
            LOG.debug("*** Found old and invalid session in request");
            // We have no valid session, but the request contained an invalid session id.
            // case a) This may be an invalid id because we invalidated the session when jumping
            // into the secure SSL session (see redirectToSecureSSLSession below). by using the back button
            // of the browser, the user may have come back to a (non-ssl) page (in his browser history) that contains
            // links with the old "parent" session id embedded. We need to check for this and create a
            // new session but reuse the visit id of the currently running SSL session.
            if (!runningUnderSSL(req) && sessionAdmin.idWasParentSession(req.getRequestedSessionId())) {
                LOG.debug("    ... but this session was the parent of a currently running secure session.");
                HttpSession secure_session = sessionAdmin.getChildSessionForParentId(req.getRequestedSessionId());
                if (secure_session != null) {
                    does_cookies = doCookieTest(req, res, secure_session);
                }
                // We'll try to get back there securely by first jumping back to a new (insecure) SSL session,
                // and after that the the jump to the secure SSL session will not create a new one, but reuse
                // the already running secure session instead (but only if a secure cookie can identify the request as
                // coming from the browser that made the initial jump http->https).
                if (does_cookies) {
                    LOG.debug("    ... client handles cookies, so we'll check if we can reuse the parent session.");
                    force_jump_back_to_ssl = true;
                } else {
                    // OK, it seems as if we will not be able to identify the peer by comparing cookies.
                    // So the only thing we can do is to reuse the VISIT_ID.
                    LOG.debug("    ... but can't reuse the secure session because the client doesn't handle cookies.");
                    force_reuse_visit_id = true;
                }
            } else {
                // Normally the balancer (or, more accurate: mod_jk) has a chance to choose the right server for a 
                // new session, but with a session id in the URL it wasn't able to. So we redirect to a "fresh" request 
                // without _any_ id, giving the balancer the possibility to choose a different server. (this can be 
                // overridden by supplying the parameter __forcelocal=1 to the request). All this makes only sense of 
                // course if we are running in a cluster of servers behind a balancer that chooses the right server
                // based on the session id included in the URL.
                String forcelocal = req.getParameter(PARAM_FORCELOCAL);
                if (forcelocal != null && (forcelocal.equals("1") || forcelocal.equals("true") || forcelocal.equals("yes"))) {
                    LOG.debug("    ... but found __forcelocal parameter to be set.");
                } else {
                    LOG.debug("    ... and __forcelocal is NOT set.");
                    redirectToClearedRequest(req, res);
                    return;
                    // End of request cycle.
                }
            }
        }

        PfixServletRequest preq = null;
        if (has_session) {
            preq = (PfixServletRequest) session.getAttribute(STORED_REQUEST);
            if (preq != null) {
                LOG.debug("*** Found old PfixServletRequest object in session");
                session.removeAttribute(STORED_REQUEST);
                preq.updateRequest(req);
            }
        }
        if (preq == null) {
            LOG.debug("*** Creating PfixServletRequest object.");
            preq = new PfixServletRequestImpl(req, this.getServletManagerConfig().getProperties());
        }

        FactoryInitWorker.tryReloadLog4j();
        
        // End of initialization. Now we handle all cases where we need to redirect.

        if (force_jump_back_to_ssl && allowSessionCreate()) {
            LOG.debug("=> I");
            forceRedirectBackToInsecureSSL(preq, req, res);
            return;
            // End of request cycle.
        }
        if (force_reuse_visit_id && allowSessionCreate()) {
            LOG.debug("=> II");
            forceNewSessionSameVisit(preq, req, res);
            return;
            // End of request cycle.
        }
        if (has_ssl_session_insecure) {
            LOG.debug("=> III");
            redirectToSecureSSLSession(preq, req, res, mark_session_as_no_cookies);
            return;
            // End of request cycle.
        }
        if (needsSession() && allowSessionCreate() && needsSSL(preq) && !has_ssl_session_secure) {
            LOG.debug("=> IV");
            redirectToInsecureSSLSession(preq, req, res, mark_session_as_no_cookies);
            return;
            // End of request cycle.
        }
        if (!has_session && needsSession() && allowSessionCreate() && !needsSSL(preq)) {
            LOG.debug("=> V");
            redirectToSession(preq, req, res, mark_session_as_no_cookies);
            return;
            // End of request cycle.
        }
        if (!has_session && !needsSession() && needsSSL(preq) && !runningUnderSSL(req)) {
            LOG.debug("=> VI");
            redirectToSSL(req, res);
            return;
            // End of request cycle.
        }

        LOG.debug("*** >>> End of redirection management, handling request now.... <<< ***\n");

        callProcess(preq, req, res);
    }

    private void redirectToClearedRequest(HttpServletRequest req, HttpServletResponse res) {
        LOG.debug("===> Redirecting to cleared Request URL");
        String redirect_uri = SessionHelper.getClearedURL(req.getScheme(), getServerName(req), req, getServletManagerConfig().getProperties());
        relocate(res, HttpServletResponse.SC_MOVED_PERMANENTLY, redirect_uri);
    }

    private void redirectToSSL(HttpServletRequest req, HttpServletResponse res) {
        LOG.debug("===> Redirecting to session-less request URL under SSL");
        String redirect_uri = SessionHelper.getClearedURL("https", getServerName(req), req, getServletManagerConfig().getProperties());
        relocate(res, redirect_uri);
    }

    private void redirectToSecureSSLSession(PfixServletRequest preq, HttpServletRequest req, HttpServletResponse res, String msanc) {
        HttpSession session = req.getSession(false);
        String visit_id = (String) session.getAttribute(VISIT_ID);
        String parentid = (String) session.getAttribute(CHECK_FOR_RUNNING_SSL_SESSION);
        if (parentid != null && !parentid.equals("")) {
            LOG.debug("*** The current insecure SSL session says to check for a already running SSL session for reuse");
            HttpSession secure_session = sessionAdmin.getChildSessionForParentId(parentid);
            if (secure_session != null) {
                String secure_id = secure_session.getId();
                String sec_testid = (String) secure_session.getAttribute(SECURE_SESS_COOKIE + MD5Utils.hex_md5(secure_id));
                LOG.debug("*** We have found a candidate: SessionId=" + secure_id + " now search for cookie...");
                LOG.debug("*** Session expects to see the cookie value " + sec_testid);
                // But we need to make sure that the current request comes
                // from the same user who created this secure session.
                // We do this by checking for a (secure) cookie with a corresponding session id.
                Cookie cookie = getSecureSessionCookie(req, secure_id);
                if (cookie != null) {
                    LOG.debug("*** Found a matching cookie ...");
                    String tmp = cookie.getValue();
                    String tmp_sec = tmp.substring(tmp.indexOf(COOKIE_VALUE_SEPARATOR) + 1);
                    if (tmp_sec.equals(sec_testid)) {
                        LOG.debug("   ... and the value is correct! (" + tmp_sec + ")");
                        LOG.debug("==> Redirecting to the secure SSL URL with the already running secure session " + secure_id);
                        String redirect_uri = SessionHelper.encodeURL("https", getServerName(req), req, secure_id, getServletManagerConfig().getProperties());
                        relocate(res, redirect_uri);
                        return;
                    } else {
                        LOG.debug("   ... but the value is WRONG!");
                        // throw new RuntimeException("Wrong Session-ID for running secure session from cookie.");
                        LOG.error("Wrong Session-ID for running secure session from cookie.");
                    }
                } else {
                    LOG.debug("*** NO matching SecureSessionCookie (not even a wrong one...)");
                }
            }
        }

        LOG.debug("*** Saving session data...");
        HashMap<String, Object> map = new HashMap<String, Object>();
        SessionHelper.saveSessionData(map, session);
        // Before we invalidate the current session we save the traillog
        SessionInfoStruct infostruct = sessionAdmin.getInfo(session);
        LinkedList<TrailElement> traillog = new LinkedList<TrailElement>();
        String old_id = session.getId();
        if (infostruct != null) {
            traillog = sessionAdmin.getInfo(session).getTraillog();
        } else {
            LOG.warn("*** Infostruct == NULL ***");
        }

        LOG.debug("*** Invalidation old session (Id: " + old_id + ")");
        session.invalidate();
        session = req.getSession(true);

        // First of all we put the old session id into the new session (__PARENT_SESSION_ID__)
        session.setAttribute(SessionAdmin.PARENT_SESS_ID, old_id);
        if (visit_id != null) {
            // Don't call this.registerSession(...) here. We don't want to log this as a different visit.
            // Now we register the new session with saved traillog
            sessionAdmin.registerSession(session, traillog, infostruct.getData().getServerName(), infostruct.getData().getRemoteAddr());
        } else {
            // Register a new session now.
            registerSession(req, session);
        }
        LOG.debug("*** Got new Session (Id: " + session.getId() + ")");
        LOG.debug("*** Copying data back to new session");
        SessionHelper.copySessionData(map, session);
        session.setAttribute(SessionHelper.SESSION_ID_URL, SessionHelper.getURLSessionId(req));
        LOG.debug("*** Setting " + SessionHelper.SESSION_ID_URL + "  to " + session.getAttribute(SessionHelper.SESSION_ID_URL));
        LOG.debug("*** Setting SECURE flag");
        session.setAttribute(SessionAdmin.SESSION_IS_SECURE, Boolean.TRUE);
        session.setAttribute(STORED_REQUEST, preq);

        if (msanc == null) {
            Cookie cookie = getSecureSessionCookie(req, session.getId());
            if (cookie != null) {
                setCookiePath(req, cookie);
                cookie.setMaxAge(0);
                cookie.setSecure(true);
                res.addCookie(cookie);
            }

            String sec_testid = Long.toHexString((long) (Math.random() * Long.MAX_VALUE));
            LOG.debug("*** Secure Test-ID used in session and cookie: " + sec_testid);
            String sec_cookie = MD5Utils.hex_md5(session.getId());
            session.setAttribute(SECURE_SESS_COOKIE + sec_cookie, sec_testid);

            cookie = new Cookie(SECURE_SESS_COOKIE + sec_cookie, System.currentTimeMillis() + COOKIE_VALUE_SEPARATOR + sec_testid);
            setCookiePath(req, cookie);
            // FIXME (see comment in cleanupCookies
            //cookie.setMaxAge(session.getMaxInactiveInterval());
            cookie.setMaxAge(-1);
            cookie.setSecure(true);
            res.addCookie(cookie);

            // Make sure a test cookie is created for the new session if needed
            createTestCookie(req, res);
        }

        LOG.debug("===> Redirecting to secure SSL URL with session (Id: " + session.getId() + ")");
        String redirect_uri = SessionHelper.encodeURL("https", getServerName(req), req, getServletManagerConfig().getProperties());
        relocate(res, redirect_uri);
    }

    private void redirectToInsecureSSLSession(PfixServletRequest preq, HttpServletRequest req, HttpServletResponse res, String msanc) {
        boolean reuse_session = false;
        if (req.isRequestedSessionIdValid()) {
            reuse_session = true;
            LOG.debug("*** reusing existing session for jump http=>https");
        }
        HttpSession session = req.getSession(true);
        if (!reuse_session) {
            if (msanc == null) {
                registerSession(req, session);
            } else {
                session.setAttribute(VISIT_ID, msanc);
                LOG.debug("*** Setting REFUSE COOKIES flag in session (Id: " + session.getId() + ")");
                session.setAttribute(REFUSE_COOKIES, Boolean.TRUE);
                sessionAdmin.registerSession(session, getServerName(req), req.getRemoteAddr());
            }
        }

        session.setAttribute(SessionHelper.SESSION_ID_URL, SessionHelper.getURLSessionId(req));
        LOG.debug("*** Setting INSECURE flag in session (Id: " + session.getId() + ")");
        session.setAttribute(SessionAdmin.SESSION_IS_SECURE, Boolean.FALSE);
        session.setAttribute(STORED_REQUEST, preq);

        // Make sure a test cookie is created if needed
        if (msanc == null) {
            createTestCookie(req, res);
        }

        LOG.debug("===> Redirecting to insecure SSL URL with session (Id: " + session.getId() + ")");
        String redirect_uri = SessionHelper.encodeURL("https", getServerName(req), req, getServletManagerConfig().getProperties());
        relocate(res, redirect_uri);
    }

    private void forceRedirectBackToInsecureSSL(PfixServletRequest preq, HttpServletRequest req, HttpServletResponse res) {
        // When we come here, we KNOW that there's a secure SSL session already running, so this session here is
        // only used for the jump to SSL so we can get the cookie to check the identity of the caller.
        String parentid = req.getRequestedSessionId();
        HttpSession session = req.getSession(true);
        session.setAttribute(SessionHelper.SESSION_ID_URL, SessionHelper.getURLSessionId(req));
        session.setAttribute(CHECK_FOR_RUNNING_SSL_SESSION, parentid);
        LOG.debug("*** Setting INSECURE flag in session (Id: " + session.getId() + ")");
        session.setAttribute(SessionAdmin.SESSION_IS_SECURE, Boolean.FALSE);
        session.setAttribute(STORED_REQUEST, preq);

        HttpSession child = sessionAdmin.getChildSessionForParentId(parentid);
        String testrand = (String) child.getAttribute(RAND_SESS_COOKIE_VALUE);
        if (testrand == null || testrand.equals("")) {
            // Make sure a test cookie is created
            createTestCookie(req, res);
        } else {
            session.setAttribute(RAND_SESS_COOKIE_VALUE, testrand);
        }

        LOG.debug("===> Redirecting to SSL URL with session (Id: " + session.getId() + ")");
        String redirect_uri = SessionHelper.encodeURL("https", getServerName(req), req, getServletManagerConfig().getProperties());
        relocate(res, redirect_uri);
    }

    private void forceNewSessionSameVisit(PfixServletRequest preq, HttpServletRequest req, HttpServletResponse res) {
        // When we come here, we KNOW that there's a secure SSL session already running, but unfortunately
        // it seems that the browser doesn't send cookies. So we will not be able to know for sure that the request comes
        // from the legitimate user. The only thing we can do is to copy the VISIT_ID, which helps to keep the
        // statistic clean :-)
        String parentid = req.getRequestedSessionId();
        HttpSession child = sessionAdmin.getChildSessionForParentId(parentid);
        String curr_visit_id = (String) child.getAttribute(VISIT_ID);
        HttpSession session = req.getSession(true);

        String testrand = (String) child.getAttribute(RAND_SESS_COOKIE_VALUE);
        if (testrand == null || testrand.equals("")) {
            // Make sure a test cookie is created
            createTestCookie(req, res);
        } else {
            session.setAttribute(RAND_SESS_COOKIE_VALUE, testrand);
        }

        LinkedList<TrailElement> traillog = sessionAdmin.getInfo(child).getTraillog();
        session.setAttribute(SessionHelper.SESSION_ID_URL, SessionHelper.getURLSessionId(req));
        session.setAttribute(VISIT_ID, curr_visit_id);
        sessionAdmin.registerSession(session, traillog, getServerName(req), req.getRemoteAddr());
        LOG.debug("===> Redirecting with session (Id: " + session.getId() + ") using OLD VISIT_ID: " + curr_visit_id);
        session.setAttribute(STORED_REQUEST, preq);
        String redirect_uri = SessionHelper.encodeURL(req.getScheme(), getServerName(req), req, getServletManagerConfig().getProperties());
        relocate(res, redirect_uri);
    }

    private void redirectToSession(PfixServletRequest preq, HttpServletRequest req, HttpServletResponse res, String msanc) {
        HttpSession session = req.getSession(true);
        session.setAttribute(SessionHelper.SESSION_ID_URL, SessionHelper.getURLSessionId(req));
        if (msanc == null) {
            registerSession(req, session);
            createTestCookie(req, res);
        } else {
            session.setAttribute(VISIT_ID, msanc);
            LOG.debug("*** Setting REFUSE COOKIES flag in session (Id: " + session.getId() + ")");
            session.setAttribute(REFUSE_COOKIES, Boolean.TRUE);
            sessionAdmin.registerSession(session, getServerName(req), req.getRemoteAddr());
        }

        LOG.debug("===> Redirecting to URL with session (Id: " + session.getId() + ")");
        session.setAttribute(STORED_REQUEST, preq);
        String redirect_uri = SessionHelper.encodeURL(req.getScheme(), getServerName(req), req, getServletManagerConfig().getProperties());
        relocate(res, redirect_uri);
    }

    private boolean doCookieTest(HttpServletRequest req, HttpServletResponse res, HttpSession sess) {
        if (sess == null) {
            sess = req.getSession(false);
        }
        // If in this session the client has been found to do cookies already, don't check the test
        // cookie value again.  We still have to check if there are any cookies at all (a
        // test cookie should be there, but maybe the wrong value because another session is
        // opened in parallel), because we need to guard against clients which supply cookies over
        // the whole redirect chain, but don't supply cookies on the following request, and we want
        // to correctly react on people who turn off cookies during the session.
        if (sess != null) {
            LOG.debug("*** Testing for marked session...");
            Cookie[] cookies = req.getCookies();
            if(cookies == null) {
                //Workaround for cookie loss problem: 
                //Despite receiving a non-empty request cookie header from the browser Tomcat
                //sometimes inexplicably returns null calling HttpServletRequest.getCookies().
                //In this case we directly parse the cookie header by calling the utility
                //method CookieUtils.getCookies().
                cookies = CookieUtils.getCookies(req);
                if (cookies != null) {
                    String sessionId = sess.getId();
                    String userAgent = req.getHeader("User-Agent");
                    if (userAgent == null) userAgent = "-";
                    String cookieHeader = req.getHeader("Cookie");
                    LOG.warn("COOKIE_LOSS_WORKAROUND|" + sessionId + "|" + userAgent + "|" + cookieHeader);
                }
            }
            boolean sessionusescookies = false;
            Boolean doescookies = (Boolean) sess.getAttribute(SESSION_COOKIES_MARKER);
            if (doescookies != null && doescookies.booleanValue()) {
                sessionusescookies = true;
                LOG.debug("    ...session is already marked as using cookies, looking for ANY test cookie...");
            } else {
                LOG.debug("    ...session is NOT already marked as using cookies!");
            }

            String rand = (String) sess.getAttribute(RAND_SESS_COOKIE_VALUE);
            if (rand != null) {
                LOG.debug("*** Testing for cookie " + TEST_COOKIE + "...");
                if (cookies != null) {
                    for (int i = 0; i < cookies.length; i++) {
                        Cookie cookie = cookies[i];
                        if (cookie.getName().equals(TEST_COOKIE)) {
                            if (sessionusescookies) {
                                // No need to check the value...
                                LOG.debug("    ... found it, no need to check the value (because session is marked).");
                                return true;
                            } else {
                                LOG.debug("    ... found it, checking value " + rand);
                                if (cookie.getValue().equals(rand)) {
                                    LOG.debug("    ... value matches! Marking session...");
                                    sess.setAttribute(SESSION_COOKIES_MARKER, Boolean.TRUE);
                                    return true;
                                } else {
                                    LOG.debug("    ... value is WRONG.");
                                }
                            }
                            break;
                        }
                    }
                    LOG.debug("*** Client sends cookies, but not our test cookie! ***");
                }
            }
        }
        return false;
    }

    private boolean createTestCookie(HttpServletRequest req, HttpServletResponse res) {
        HttpSession sess = req.getSession(false);
        String rand = null;
        if (sess != null) {
            rand = (String) sess.getAttribute(RAND_SESS_COOKIE_VALUE);
            if (rand != null) {
                LOG.debug("*** Already found a test cookie value in session: " + rand);
            } else {
                rand = Long.toHexString((long) (Math.random() * Long.MAX_VALUE));
                LOG.debug("*** Creating a random test cookie value: " + rand);
            }
            Cookie newprobe = new Cookie(TEST_COOKIE, rand);
            setCookiePath(req, newprobe);
            res.addCookie(newprobe);
            sess.setAttribute(RAND_SESS_COOKIE_VALUE, rand);
            return true;
        }
        return false;
    }

    private Cookie getSecureSessionCookie(HttpServletRequest req, String sessionid) {
        Cookie[] cookies = CookieUtils.getCookies(req);
        Cookie tmp;
        if (cookies != null) {
            for (int i = 0; i < cookies.length; i++) {
                tmp = cookies[i];
                if (tmp.getName().equals(SECURE_SESS_COOKIE + MD5Utils.hex_md5(sessionid))) return tmp;
            }
        }
        return null;
    }

    private void registerSession(HttpServletRequest req, HttpSession session) {
        if (session != null) {
            synchronized (TIMESTAMP_ID) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
                String timestamp = sdf.format(new Date());
                NumberFormat nf = NumberFormat.getInstance();
                nf.setMinimumIntegerDigits(3);

                if (timestamp.equals(TIMESTAMP_ID)) {
                    INC_ID++;
                } else {
                    TIMESTAMP_ID = timestamp;
                    INC_ID = 0;
                }
                if (INC_ID >= 1000) {
                    LOG.warn("*** More than 999 connects/sec! ***");
                }
                String sessid = session.getId();
                String mach = "";
                if (sessid.lastIndexOf(".") > 0) {
                    mach = sessid.substring(sessid.lastIndexOf("."));
                }
                session.setAttribute(VISIT_ID, TIMESTAMP_ID + "-" + nf.format(INC_ID) + mach);
            }
            StringBuffer logbuff = new StringBuffer();
            logbuff.append(session.getAttribute(VISIT_ID) + "|" + session.getId() + "|");
            logbuff.append(getServerName(req) + "|" + req.getRemoteAddr() + "|" + req.getHeader("user-agent") + "|");
            if (req.getHeader("referer") != null) {
                logbuff.append(req.getHeader("referer"));
            }
            logbuff.append("|");
            if (req.getHeader("accept-language") != null) {
                logbuff.append(req.getHeader("accept-language"));
            }
            LOGGER_VISIT.warn(logbuff.toString());
            sessionAdmin.registerSession(session, getServerName(req), req.getRemoteAddr());
        }
    }

    
    public void afterPropertiesSet() throws Exception {
        init();
    }
    
    public void init() throws ServletException {
        ServletContext ctx = getServletContext();
        LOG.debug("*** Servlet container is '" + ctx.getServerInfo() + "'");
        int major = ctx.getMajorVersion();
        int minor = ctx.getMinorVersion();
        if ((major == 2 && minor >= 3) || (major > 2)) {
            LOG.warn("*** Servlet container with support for Servlet API " + major + "." + minor + " detected");
        } else {
            throw new ServletException("*** Can't detect servlet container with support for Servlet API 2.3 or higher");
        }
        
        initCookieSec();
        initExceptionConfigs();
        initServletEncoding();
    }

    /**
     * This is only for broken clients who suddenly stop supplying cookies in the middle of a
     * session.  Windows XP internet configuration wizard (which speaks http) seems to be an example of a
     * client that remembers the cookies over the first request/relocate/relocate cycle, but never sends a
     * cookie again on the next request.
     * You are strongly advised to NOT set the corresponding property to true, unless you deal with broken software.
     */
    private void initCookieSec() {
        String csec = this.getServletManagerConfig().getProperties().getProperty(PROP_COOKIE_SEC_NOT_ENFORCED);
        if (csec != null && csec.equals("true")) {
            cookie_security_not_enforced = true;
        } else {
            cookie_security_not_enforced = false;
        }
    }

    private void callProcess(PfixServletRequest preq, HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        try {
            if(!FactoryInitWorker.isConfigured() && !FactoryInitUtil.isInitialized()) {
                FactoryInitException initEx=FactoryInitWorker.getInitException();
                if(initEx!=null) {
                    initEx=initEx.copy();
                    initEx.fillInStackTrace();
                    throw initEx;
                } else throw new IllegalStateException("Factories aren't initialized yet.");
            }
            res.setContentType(DEF_CONTENT_TYPE);
            process(preq, res);
        } catch (Throwable e) {
            LOG.error("Exception in process", e);
            ExceptionConfig exconf = getExceptionConfigForThrowable(e.getClass());
            if(exconf != null && exconf.getProcessor()!= null) { 
                if ( preq.getLastException() == null ) {  
                    ExceptionProcessor eproc = exconf.getProcessor();
                    eproc.processException(e, exconf, preq,
                                       getServletContext(),
                                       req, res, this.getServletManagerConfig().getProperties());
                }
            } 
            if(!res.isCommitted()) throw new ServletException("Exception in process.",e);
        }
    }

    /**
     * 
     * @return null if no processor is responsible for the passed throwable
     * @throws ServletException
     * @throws ClassNotFoundException
     */
    private ExceptionConfig getExceptionConfigForThrowable(Class<? extends Throwable> clazz) throws ServletException {
        ExceptionConfig exConf=null;
        if(clazz!=null) {
            exConf=exceptionConfigs.get(clazz);
            if(exConf==null) {
                if(clazz.getSuperclass() == Object.class) {
                    LOG.warn("No exception processor, page or forward configured for " + clazz.getName()); 
                } else {
                    exConf=getExceptionConfigForThrowable(clazz.getSuperclass().asSubclass(Throwable.class));
                }
            }
        }
        return exConf;
    }
    
    /**
     * This method uses all properties prefixed with 'exception' to build ExceptionConfig
     * objects, which are then stored in the exConfig-Map, keyed by the type attribute
     * of the ExceptionConfig.
     * Syntax of key: "exception" '.' JAVATYPE '.' ("forward"|"page"|"processor")
     * @exception ServletException if the exception configuration defined in the properties
     * is somehow invalid
     */
    private void initExceptionConfigs() throws ServletException {
        Map<String, ExceptionConfig> tmpExConf = new HashMap<String, ExceptionConfig>();
        Properties properties = this.getServletManagerConfig().getProperties();
        Enumeration<?> props = properties.propertyNames();
        while (props.hasMoreElements()) {
            String propName = (String) props.nextElement();

            if (propName.startsWith(PROP_EXCEPTION)) {
                String propValue = properties.getProperty(propName);

                String type = null;
                String attrName = null;
                IndexOutOfBoundsException ioobex = null;
                try {
                    int dot1 = propName.indexOf('.');
                    int dot2 = propName.lastIndexOf('.');
                    type = propName.substring(dot1 + 1, dot2);
                    attrName = propName.substring(dot2 + 1);
                } catch (IndexOutOfBoundsException e) {
                    // logging is done below
                    ioobex = e;
                }
                if (type == null || attrName == null || "".equals(type) || "".equals(attrName)) {
                    LOG.debug("Could not parse property key \"" + propName + "\" into three non-empty parts separated by '.'", ioobex);
                }

                ExceptionConfig exConf = (ExceptionConfig) tmpExConf.get(type);

                LOG.debug("Property found for exception processing: " + propName + "=" + propValue);

                if (exConf == null) {
                    exConf = new ExceptionConfig();
                    exConf.setType(type);
                    tmpExConf.put(type, exConf);
                }

                try {
                    if ("forward".equals(attrName)) {
                        exConf.setForward(Boolean.valueOf(propValue).booleanValue());
                    } else if ("page".equals(attrName)) {
                        exConf.setPage(propValue);
                    } else if ("processor".equals(attrName)) {
                        Class<?> procClass = Class.forName(propValue);
                        ExceptionProcessor exProc = (ExceptionProcessor) procClass.newInstance();
                        exConf.setProcessor(exProc);
                    }
                } catch (ClassCastException ex) {
                    throw new ServletException("INVALID CONF: Class " + propValue + " is not an instance of 'ExceptionProcessor'");
                } catch (IllegalAccessException ex) {
                    throw new ServletException("INVALID CONF: Can't create instance of class " + propName, ex);
                } catch (SecurityException ex) {
                    throw new ServletException("INVALID CONF: Can't create instance of class " + propName, ex);
                } catch (ClassNotFoundException ex) {
                    throw new ServletException("INVALID CONF: Can't create instance of class " + propName, ex);
                } catch (InstantiationException ex) {
                    throw new ServletException("INVALID CONF: Can't create instance of class " + propName, ex);
                }
            }
        }

        LOG.debug("Finished reading properties for Exception configuration! \n" + tmpExConf);

        exceptionConfigs.clear();

        // validate the ExceptionConfig-instances and save them, keyed by their type-attribute
        for (Iterator<ExceptionConfig> values = tmpExConf.values().iterator(); values.hasNext();) {
            ExceptionConfig exConfig = values.next();
            if (exConfig.validate() == false)
                throw new ServletException("INVALID ExceptionConfig: \n" + exConfig);
            else {
                try {
                    Class<?> clazz=Class.forName(exConfig.getType());
                    exceptionConfigs.put(clazz.asSubclass(Throwable.class), exConfig);
                } catch(ClassNotFoundException x) {
                    throw new ServletException("Can't find exception class: "+exConfig.getType());
                }
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Complete ExceptionConfig is:");
            LOG.debug("\n" + exceptionConfigs);
        }
    }

    /**
     * Sets the servlet's encoding, which is used as character encoding for decoding/encoding 
     * requests/responses. Be aware that this setting only applies to the appropriate Readers, 
     * Writers and body request parameters. It has no effect on the byte streams. The URI
     * encoding (which is set on Tomcat connector level and can't be changed here) is set always
     * be the same as the body encoding.
     */
    private void initServletEncoding() {
        //Try to get servlet encoding from properties:
        String encoding = this.getServletManagerConfig().getProperties().getProperty(SERVLET_ENCODING);
        if (encoding == null || encoding.trim().equals(""))
            LOG.warn("No servlet encoding property set");
        else if (!Charset.isSupported(encoding))
            LOG.error("Servlet encoding '" + encoding + "' is not supported.");
        else
            servletEncoding = encoding;

        //Try to get servlet encoding from init parameters:
        if (servletEncoding == null) {
            encoding = getServletEncoding();
            if (encoding == null || encoding.trim().equals(""))
                LOG.warn("No servlet encoding init parameter set");
            else if (!Charset.isSupported(encoding))
                LOG.error("Servlet encoding '" + encoding + "' is not supported.");
            else
                servletEncoding = encoding;
        }
        //Use default servlet encoding:
        if (servletEncoding == null) {
            servletEncoding = DEFAULT_ENCODING;
            LOG.warn("Using default servlet encoding: " + DEFAULT_ENCODING);
        }

        LOG.debug("Servlet encoding was set to '" + servletEncoding + "'.");
    }

    protected abstract void process(PfixServletRequest preq, HttpServletResponse res) throws Exception;

    public static final int HTTP_PORT  = 80;
    public static final int HTTPS_PORT = 443;

    public static boolean isDefault(String scheme, int port) {
        if (scheme.equals("http") && port == HTTP_PORT) {
            return true;
        } else if (scheme.equals("https") && port == HTTPS_PORT) {
            return true;
        } else {
            return false;
        }
    }

    private class SortableCookie implements Comparable<SortableCookie> {
        public final Cookie cookie;
        public final long   lasttouch;

        public final int compareTo(final SortableCookie in) {
            if (in.lasttouch > lasttouch) return -1;
            if (in.lasttouch < lasttouch)
                return 1;
            else
                return 0;
        }

        public SortableCookie(Cookie cookie, long lasttouch) {
            this.cookie = cookie;
            this.lasttouch = lasttouch;
            assert (cookie != null) : "cookie argument must not be null";
            assert (lasttouch > 0) : "lasttouch argument must be > 0";
        }
    }

    private void cleanupCookies(HttpServletRequest req, HttpServletResponse res, Cookie cookie) {
        HttpSession session = req.getSession(false);
        assert (session != null) : "session can't be null here...";

        Long timeout = System.currentTimeMillis() - (1000 * session.getMaxInactiveInterval());
        assert (timeout > 0) : "timeout can't be negative...";

        Cookie[] cookies = CookieUtils.getCookies(req);
        if (cookies != null && cookies.length > 0) {
            TreeSet<SortableCookie> cset = new TreeSet<SortableCookie>();
            for (int i = 0; i < cookies.length; i++) {
                Cookie tmp = cookies[i];
                boolean secCookie=tmp.getName().startsWith(SECURE_SESS_COOKIE);
                boolean oldSecCookie=secCookie?false:tmp.getName().startsWith(SECURE_SESS_COOKIE_OLD);
                if ( (secCookie || oldSecCookie)  && (cookie == null || !tmp.getName().equals(cookie.getName()))) {
                    String value = tmp.getValue();
                    int sepIndex = -1;
                    if(secCookie) sepIndex = value.indexOf(COOKIE_VALUE_SEPARATOR);
                    else {
                        sepIndex = value.indexOf(COOKIE_VALUE_SEPARATOR_OLD);
                        if(sepIndex == -1) {
                            //Value doesn't contain old separator, possibly it's an old cookie
                            //with a truncated value (due to changed Tomcat cookie parsing behaviour)
                            sepIndex = value.length();
                        }
                    }
                    String stamp = value.substring(0, sepIndex);
                    try {
                        long lasttouch = Long.parseLong(stamp);
                        cset.add(new SortableCookie(tmp, lasttouch));
                        LOG.debug("~~~ Adding cookie " + lasttouch + "->" + tmp.getName());
                    } catch (NumberFormatException e) {
                        setCookiePath(req, tmp);
                        tmp.setMaxAge(0);
                        tmp.setSecure(true);
                        res.addCookie(tmp);
                    }
                }
            }
            int count = 0;
            int length = cset.size();
            for (Iterator<SortableCookie> iter = cset.iterator(); iter.hasNext(); count++) {
                SortableCookie current = iter.next();
                Cookie curr_cookie = current.cookie;
                long curr_lasttouch = current.lasttouch;

                LOG.debug("--- Checking cookie " + count + "->" + curr_lasttouch + "->" + curr_cookie.getName());

                if (count <= (length - MAX_PARALLEL_SEC_SESSIONS)) {
                    LOG.debug("   -> removing cookie because number of secure session cookies too high");
                    curr_cookie.setMaxAge(0);
                    curr_cookie.setSecure(true);
                    setCookiePath(req, curr_cookie);
                    res.addCookie(curr_cookie);
                } else if (curr_lasttouch < timeout) {
                    // FIXME We shoudln't need to check for "old" cookies here, because the
                    // lifetime of a cookie should ideally be set to the max. inactive time of the
                    // session, so the browser would stop sending old cookies by itself. But I'm not
                    // entirely sure if we can really depend on having the time set right on all
                    // clients... What happens if the client clock is set half an hour too early?
                    // Will the browser stop sending the cookie half an hour before the session is
                    // really invalidated? As we need the timestamp info anyway to sort the cookies,
                    // we can also do the timeout removing here.
                    LOG.debug("   -> removing cookie because timestamp too old");
                    curr_cookie.setMaxAge(0);
                    curr_cookie.setSecure(true);
                    setCookiePath(req, curr_cookie);
                    res.addCookie(curr_cookie);
                } else {
                    break;
                }
            }
        }
    }

    private void setCookiePath(HttpServletRequest req, Cookie cookie) {
        if (req.getContextPath().length() > 0) {
            cookie.setPath(req.getContextPath());
        } else {
            cookie.setPath("/");
        }
    }
    
    public void setServletEncoding(String encoding) {
        this.servletEncoding = encoding;
    }
    
    public String getServletEncoding() {
        return servletEncoding;
    }
    
    public void setServletContext(ServletContext context) {
        this.servletContext = context;
    }
    
    public ServletContext getServletContext() {
        return this.servletContext;
    }
    
    public void setSessionAdmin(SessionAdmin sessionAdmin) {
        this.sessionAdmin = sessionAdmin;
    }
    
    public SessionAdmin getSessionAdmin() {
        return sessionAdmin;
    }
    
    public void setWebappAdmin(WebappAdmin webappAdmin) {
        this.webappAdmin = webappAdmin;
    }
    
}
