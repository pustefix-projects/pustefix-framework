package org.pustefixframework.http;

import java.io.IOException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import de.schlund.pfixxml.PfixServletRequest;
import de.schlund.pfixxml.PfixServletRequestImpl;
import de.schlund.pfixxml.serverutil.SessionAdmin;
import de.schlund.pfixxml.serverutil.SessionHelper;
import de.schlund.pfixxml.serverutil.SessionInfoStruct;
import de.schlund.pfixxml.serverutil.SessionInfoStruct.TrailElement;
import de.schlund.pfixxml.util.CookieUtils;

public class CookieSessionTrackingStrategy implements SessionTrackingStrategy {

    private Logger LOG = Logger.getLogger(CookieSessionTrackingStrategy.class);
    
    private static final String CHECK_FOR_RUNNING_SSL_SESSION = "__CHECK_FOR_RUNNING_SSL_SESSION__";
    private static int INC_ID = 0;
    private static final String PARAM_FORCELOCAL = "__forcelocal";
    private static final String STORED_REQUEST = "__STORED_PFIXSERVLETREQUEST__";
    private static String TIMESTAMP_ID = "";
    private static final String          INITIAL_SESSION_CHECK         = "__INITIAL_SESSION_CHECK__";
    public static final String VISIT_ID = "__VISIT_ID__";
    
    private SessionTrackingStrategyContext context;
    
    public void init(SessionTrackingStrategyContext context) {
        this.context = context;
    }
    
    public void handleRequest(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        
        HttpSession session = null;
        boolean has_session = false;
        boolean has_ssl_session_insecure = false;
        boolean has_ssl_session_secure = false;
        boolean force_jump_back_to_ssl = false;
        boolean force_reuse_visit_id = false;
        boolean does_cookies = false;
        
        if (req.isRequestedSessionIdValid()) {
            session = req.getSession(false);
            has_session = true;
            LOG.debug("*** Found valid session with ID " + session.getId());

            if(session.getAttribute(INITIAL_SESSION_CHECK) != null && req.isRequestedSessionIdFromCookie()) {
                session.removeAttribute(INITIAL_SESSION_CHECK);
                session.setAttribute(AbstractPustefixRequestHandler.SESSION_ATTR_COOKIE_SESSION, true);
                String redirect_uri = SessionHelper.getClearedURL(req.getScheme(), AbstractPustefixRequestHandler.getServerName(req), req, context.getServletManagerConfig().getProperties());
                AbstractPustefixRequestHandler.relocate(res, HttpServletResponse.SC_MOVED_TEMPORARILY, redirect_uri);
                return;
            }
            
            Boolean secure = (Boolean) session.getAttribute(SessionAdmin.SESSION_IS_SECURE);

            if (has_session) {
                if (req.isSecure()) {
                    LOG.debug("*** Found running under SSL");
                    if (secure != null && secure.booleanValue()) {
                        has_ssl_session_secure = true;
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
        } else if (req.getRequestedSessionId() != null && context.wantsCheckSessionIdValid()) {
            LOG.debug("*** Found old and invalid session in request");
            // We have no valid session, but the request contained an invalid session id.
            // case a) This may be an invalid id because we invalidated the session when jumping
            // into the secure SSL session (see redirectToSecureSSLSession below). by using the back button
            // of the browser, the user may have come back to a (non-ssl) page (in his browser history) that contains
            // links with the old "parent" session id embedded. We need to check for this and create a
            // new session but reuse the visit id of the currently running SSL session.
            if (!req.isSecure() && context.getSessionAdmin().idWasParentSession(req.getRequestedSessionId())) {
                LOG.debug("    ... but this session was the parent of a currently running secure session.");
                HttpSession secure_session = context.getSessionAdmin().getChildSessionForParentId(req.getRequestedSessionId());
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
            preq = new PfixServletRequestImpl(req, context.getServletManagerConfig().getProperties());
        }

        //TODO: call it later
        //PustefixInit.tryReloadLog4j();
        
        // End of initialization. Now we handle all cases where we need to redirect.

        if (force_jump_back_to_ssl && context.allowSessionCreate()) {
            LOG.debug("=> I");
            forceRedirectBackToInsecureSSL(preq, req, res);
            return;
            // End of request cycle.
        }
        if (force_reuse_visit_id && context.allowSessionCreate()) {
            LOG.debug("=> II");
            forceNewSessionSameVisit(preq, req, res);
            return;
            // End of request cycle.
        }
        if (has_ssl_session_insecure) {
            LOG.debug("=> III");
            redirectToSecureSSLSession(preq, req, res);
            return;
            // End of request cycle.
        }
        
        if (context.needsSession() && context.allowSessionCreate() && context.needsSSL(preq) && !has_ssl_session_secure) {  
            if(req.isSecure()) {
                LOG.debug("=> IVa");
                redirectToSSLSession(preq, req, res);
            } else {
                LOG.debug("=> IVb");
                redirectToInsecureSSLSession(preq, req, res);
            }
            return;
            // End of request cycle.
        }
        if (!has_session && context.needsSession() && context.allowSessionCreate() && !context.needsSSL(preq)) {
            LOG.debug("=> V");
            redirectToSession(preq, req, res);
            return;
            // End of request cycle.
        }
        if (!has_session && !context.needsSession() && context.needsSSL(preq) && !req.isSecure()) {
            LOG.debug("=> VI");
            redirectToSSL(req, res);
            return;
            // End of request cycle.
        }

        LOG.debug("*** >>> End of redirection management, handling request now.... <<< ***\n");

        if(session != null) {
            if(session.getAttribute(VISIT_ID) == null) {
                StringBuilder sb = new StringBuilder();
                sb.append("Found session without visit_id: ");
                sb.append(req.getRemoteAddr()).append("|");
                sb.append(req.getRequestURI()).append("|");
                sb.append(session.getId()).append("|");
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
                sb.append(format.format(new Date(session.getCreationTime()))).append("|");
                sb.append(format.format(new Date(session.getLastAccessedTime()))).append("|");
                Enumeration<?> e = session.getAttributeNames();
                while(e.hasMoreElements()) sb.append(e.nextElement()).append("|");
                e = req.getHeaderNames();
                while(e.hasMoreElements()) {
                    String name = (String)e.nextElement();
                    Enumeration<?> v = req.getHeaders(name);
                    while(v.hasMoreElements()) {
                        String value = (String)v.nextElement();
                        sb.append(name).append(":").append(value).append("|");
                    }
                }
                LOG.warn(sb.toString());
                session.invalidate();
                redirectToClearedRequest(req, res);
                return;
            }
        }
        
        context.callProcess(preq, req, res);
    }
   
    private boolean doCookieTest(HttpServletRequest req, HttpServletResponse res, HttpSession sess) {
        //TODO: replace calls
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
                    return true;
                }
            } else return true;
        }
        return false;
    }
    

    
    private void redirectToClearedRequest(HttpServletRequest req, HttpServletResponse res) {
        LOG.debug("===> Redirecting to cleared Request URL");
        String redirect_uri = SessionHelper.getClearedURL(req.getScheme(), AbstractPustefixRequestHandler.getServerName(req), req, context.getServletManagerConfig().getProperties());
        if(req.isRequestedSessionIdFromCookie()) {
            Cookie cookie = new Cookie("JSESSIONID", "");
            cookie.setMaxAge(0);
            cookie.setPath((req.getContextPath().equals("")) ? "/" : req.getContextPath());
            res.addCookie(cookie);
        }
        AbstractPustefixRequestHandler.relocate(res, HttpServletResponse.SC_MOVED_PERMANENTLY, redirect_uri);
    }
    
    private void redirectToSSL(HttpServletRequest req, HttpServletResponse res) {
        LOG.debug("===> Redirecting to session-less request URL under SSL");
        String redirect_uri = SessionHelper.getClearedURL("https", AbstractPustefixRequestHandler.getServerName(req), req, context.getServletManagerConfig().getProperties());
        AbstractPustefixRequestHandler.relocate(res, redirect_uri);
    }
    
    private void redirectToInsecureSSLSession(PfixServletRequest preq, HttpServletRequest req, HttpServletResponse res) {
        boolean reuse_session = false;
        if (req.isRequestedSessionIdValid()) {
            reuse_session = true;
            LOG.debug("*** reusing existing session for jump http=>https");
        }
        HttpSession session = req.getSession(true);
        LOG.warn("SESSION1|" + session.getId());
        if (!reuse_session) {
            registerSession(req, session);
        }

        LOG.debug("*** Setting INSECURE flag in session (Id: " + session.getId() + ")");
        session.setAttribute(SessionAdmin.SESSION_IS_SECURE, Boolean.FALSE);
        session.setAttribute(STORED_REQUEST, preq);

        LOG.debug("===> Redirecting to insecure SSL URL with session (Id: " + session.getId() + ")");
        String redirect_uri = SessionHelper.encodeURL("https", AbstractPustefixRequestHandler.getServerName(req), req, context.getServletManagerConfig().getProperties());
        AbstractPustefixRequestHandler.relocate(res, redirect_uri);
    }
    
    private void redirectToSession(PfixServletRequest preq, HttpServletRequest req, HttpServletResponse res) {
        HttpSession session = req.getSession(true);
        LOG.warn("GETSESSION2|" + session.getId());
        registerSession(req, session);
        LOG.debug("===> Redirecting to URL with session (Id: " + session.getId() + ")");
        session.setAttribute(STORED_REQUEST, preq);
        session.setAttribute(INITIAL_SESSION_CHECK, session.getId());
        String redirect_uri = SessionHelper.encodeURL(req.getScheme(), AbstractPustefixRequestHandler.getServerName(req), req, context.getServletManagerConfig().getProperties());
        AbstractPustefixRequestHandler.relocate(res, redirect_uri);
    }
    
    private void redirectToSSLSession(PfixServletRequest preq, HttpServletRequest req, HttpServletResponse res) {
        HttpSession session = req.getSession(true);
        LOG.warn("GETSESSION3|" + session.getId());
        registerSession(req, session);

        LOG.debug("===> Redirecting to URL with session (Id: " + session.getId() + ")");
        session.setAttribute(STORED_REQUEST, preq);
        session.setAttribute(INITIAL_SESSION_CHECK, session.getId());
        session.setAttribute(SessionAdmin.SESSION_IS_SECURE, Boolean.TRUE);
        String redirect_uri = SessionHelper.encodeURL("https", AbstractPustefixRequestHandler.getServerName(req), req, context.getServletManagerConfig().getProperties());
        AbstractPustefixRequestHandler.relocate(res, redirect_uri);
    }
    
    private void redirectToSecureSSLSession(PfixServletRequest preq, HttpServletRequest req, HttpServletResponse res) {
        HttpSession session = req.getSession(false);
        String visit_id = (String) session.getAttribute(VISIT_ID);
        
        LOG.debug("*** Saving session data...");
        HashMap<String, Object> map = new HashMap<String, Object>();
        SessionHelper.saveSessionData(map, session);
        // Before we invalidate the current session we save the traillog
        SessionInfoStruct infostruct = context.getSessionAdmin().getInfo(session);
        LinkedList<TrailElement> traillog = new LinkedList<TrailElement>();
        String old_id = session.getId();
        if (infostruct != null) {
            traillog = context.getSessionAdmin().getInfo(session).getTraillog();
        } else {
            LOG.warn("*** Infostruct == NULL ***");
        }

        LOG.debug("*** Invalidation old session (Id: " + old_id + ")");
        session.invalidate();
        session = req.getSession(true);
        LOG.warn("GETSESSION4|" + session.getId());

        // First of all we put the old session id into the new session (__PARENT_SESSION_ID__)
        session.setAttribute(SessionAdmin.PARENT_SESS_ID, old_id);
        if (visit_id != null) {
            // Don't call this.registerSession(...) here. We don't want to log this as a different visit.
            // Now we register the new session with saved traillog
            context.getSessionAdmin().registerSession(session, traillog, infostruct.getData().getServerName(), infostruct.getData().getRemoteAddr());
        } else {
            // Register a new session now.
            registerSession(req, session);
        }
        LOG.debug("*** Got new Session (Id: " + session.getId() + ")");
        LOG.debug("*** Copying data back to new session");
        SessionHelper.copySessionData(map, session);
        LOG.debug("*** Setting SECURE flag");
        session.setAttribute(SessionAdmin.SESSION_IS_SECURE, Boolean.TRUE);
        session.setAttribute(STORED_REQUEST, preq);
        session.setAttribute(INITIAL_SESSION_CHECK, session.getId());

        LOG.debug("===> Redirecting to secure SSL URL with session (Id: " + session.getId() + ")");
        String redirect_uri = SessionHelper.encodeURL("https", AbstractPustefixRequestHandler.getServerName(req), req, context.getServletManagerConfig().getProperties());
        AbstractPustefixRequestHandler.relocate(res, redirect_uri);
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
            logbuff.append(AbstractPustefixRequestHandler.getServerName(req) + "|" + req.getRemoteAddr() + "|" + req.getHeader("user-agent") + "|");
            if (req.getHeader("referer") != null) {
                logbuff.append(req.getHeader("referer"));
            }
            logbuff.append("|");
            if (req.getHeader("accept-language") != null) {
                logbuff.append(req.getHeader("accept-language"));
            }
            AbstractPustefixRequestHandler.LOGGER_VISIT.warn(logbuff.toString());
            context.getSessionAdmin().registerSession(session, AbstractPustefixRequestHandler.getServerName(req), req.getRemoteAddr());
        }
    }
        
    private void forceRedirectBackToInsecureSSL(PfixServletRequest preq, HttpServletRequest req, HttpServletResponse res) {
        // When we come here, we KNOW that there's a secure SSL session already running, so this session here is
        // only used for the jump to SSL so we can get the cookie to check the identity of the caller.
        String parentid = req.getRequestedSessionId();
        HttpSession session = req.getSession(true);
        LOG.warn("GETSESSION5|" + session.getId());
        session.setAttribute(CHECK_FOR_RUNNING_SSL_SESSION, parentid);
        LOG.debug("*** Setting INSECURE flag in session (Id: " + session.getId() + ")");
        session.setAttribute(SessionAdmin.SESSION_IS_SECURE, Boolean.FALSE);
        session.setAttribute(STORED_REQUEST, preq);

        LOG.debug("===> Redirecting to SSL URL with session (Id: " + session.getId() + ")");
        String redirect_uri = SessionHelper.encodeURL("https", AbstractPustefixRequestHandler.getServerName(req), req, context.getServletManagerConfig().getProperties());
        AbstractPustefixRequestHandler.relocate(res, redirect_uri);
    }
    
    private void forceNewSessionSameVisit(PfixServletRequest preq, HttpServletRequest req, HttpServletResponse res) {
        // When we come here, we KNOW that there's a secure SSL session already running, but unfortunately
        // it seems that the browser doesn't send cookies. So we will not be able to know for sure that the request comes
        // from the legitimate user. The only thing we can do is to copy the VISIT_ID, which helps to keep the
        // statistic clean :-)
        String parentid = req.getRequestedSessionId();
        HttpSession child = context.getSessionAdmin().getChildSessionForParentId(parentid);
        String curr_visit_id = (String) child.getAttribute(VISIT_ID);
        HttpSession session = req.getSession(true);
        LOG.warn("GETSESSION6|" + session.getId());

        LinkedList<TrailElement> traillog = context.getSessionAdmin().getInfo(child).getTraillog();
        session.setAttribute(VISIT_ID, curr_visit_id);
        context.getSessionAdmin().registerSession(session, traillog, AbstractPustefixRequestHandler.getServerName(req), req.getRemoteAddr());
        LOG.debug("===> Redirecting with session (Id: " + session.getId() + ") using OLD VISIT_ID: " + curr_visit_id);
        session.setAttribute(STORED_REQUEST, preq);
        String redirect_uri = SessionHelper.encodeURL(req.getScheme(), AbstractPustefixRequestHandler.getServerName(req), req, context.getServletManagerConfig().getProperties());
        AbstractPustefixRequestHandler.relocate(res, redirect_uri);
    }
    
}
