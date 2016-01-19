package org.pustefixframework.http;

import java.io.IOException;
import java.io.PrintWriter;
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

public class CookieOnlySessionTrackingStrategy implements SessionTrackingStrategy {

    private Logger LOGGER_SESSION = Logger.getLogger("LOGGER_SESSION");
    
    private static final String STORED_REQUEST = "__STORED_PFIXSERVLETREQUEST__";
    static final String COOKIE_SESSION_SSL = "_PFXSSL_";
    static final String COOKIE_SESSION_SSL_CHECK = "_PFXSSLCHK_";
    static final String COOKIE_SESSION_RESET = "_PFXRST_";
    static final String PARAM_FORCELOCAL = "__forcelocal";
    
    private SessionTrackingStrategyContext context;
    
    public void init(SessionTrackingStrategyContext context) {
         this.context = context;
    }
    
    public void handleRequest(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

        if("/pfxsession".equals(req.getPathInfo())) {
            
            int cookiesEnabled = 0;
            if(req.getParameter("nocookies") != null) {
            	cookiesEnabled = 1;
            }
            int sessionValidity;
            String sessionId = "-";
            if(req.isRequestedSessionIdValid()) { //valid session id
                sessionValidity = 0;
                sessionId = req.getRequestedSessionId();
            } else if(req.getRequestedSessionId() == null) { //no session id
            	sessionValidity = 1;
            } else { //invalid session
            	sessionValidity = 2;
            	sessionId = req.getRequestedSessionId();
            }
            LOGGER_SESSION.info("SESSION_COOKIE_CHECK|" + AbstractPustefixRequestHandler.getRemoteAddr(req) + "|" + cookiesEnabled + "|" + sessionValidity + "|" + sessionId);
            sendInfo(res, "" + sessionValidity);
            return;
        }
        
        PfixServletRequest preq = null;
        try {
        
        HttpSession session = req.getSession(false);
        
        if(session == null) {
            preq = new PfixServletRequestImpl(req, context.getServletManagerConfig().getProperties(), context);
        } else {
            preq = (PfixServletRequest)session.getAttribute(STORED_REQUEST);
            if(preq == null) {
                preq = new PfixServletRequestImpl(req, context.getServletManagerConfig().getProperties(), context);
            } else {
                session.removeAttribute(STORED_REQUEST);
                preq.updateRequest(req);
            }
        }
        
        Cookie[] cookies = CookieUtils.getCookies(req);
        if(getCookie(cookies, COOKIE_SESSION_SSL_CHECK) != null) {
            resetCookie(COOKIE_SESSION_SSL_CHECK, req, res);
        }
        if(getCookie(cookies, COOKIE_SESSION_RESET) != null) {
            resetCookie(COOKIE_SESSION_RESET, req, res);
        }
        
        if(context.needsSession()) { //requires session
            
            if(req.isRequestedSessionIdValid()) { //has valid session
            
                if(context.needsSSL(preq)) { //requires SSL
                    
                    if(req.isSecure()) { //has SSL
                        
                        Boolean hasSecureFlag = (Boolean)session.getAttribute(SessionAdmin.SESSION_IS_SECURE);
                        if(hasSecureFlag != Boolean.TRUE) { //session isn't secure
                            
                            //copy existing session data into new secure session
                            session = copySession(session, req);
                            addCookie(COOKIE_SESSION_SSL, "true", req, res);
                            
                        }
                        
                    } else { //doesn't have SSL
                        
                        //redirect to https
                        redirectToSSL(req, res, HttpServletResponse.SC_MOVED_TEMPORARILY);
                        return;
                            
                    }
                    
                }
            
            } else if(req.getRequestedSessionId() != null) { //has invalid session
                
                //requested session id is invalid, therefor we make a redirect and
                //delete the session cookie, giving loadbalancers relying on sticky
                //sessions the chance to choose a new worker
             
                if(req.isSecure() && getCookie(cookies, COOKIE_SESSION_SSL_CHECK) != null) {
                    
                    //was redirected to SSL to check for secure session
                    //let's redirect back to http because no valid secure session was found
                    resetSession(req, res);
                    resetCookie(COOKIE_SESSION_SSL, req, res);
                    redirect(req, res, HttpServletResponse.SC_MOVED_TEMPORARILY, "http");
                    return;
                    
                } else {
                    
                    if(context.needsSSL(preq) && !req.isSecure()) { //requires SSL and has no SSL
                        
                        resetSession(req, res);
                        redirectToSSL(req, res, HttpServletResponse.SC_MOVED_PERMANENTLY);
                        return;
                            
                    } else { //doesn't require SSL or already has
                            
                        boolean resetTry = false;
                        Cookie cookie = getCookie(cookies, COOKIE_SESSION_RESET);
                        if(cookie != null && cookie.getValue().equals(req.getRequestedSessionId())) resetTry = true;
                        
                        String forcelocal = req.getParameter(PARAM_FORCELOCAL);
                        String active = (String)req.getAttribute("JK_LB_ACTIVATION");
                        if ((req.getMethod().equals("POST") && (active == null || active.equals("ACT")))  
                                || resetTry || (forcelocal != null && 
                                (forcelocal.equals("1") || forcelocal.equals("true") || forcelocal.equals("yes")))) {
                            
                            createSession(req, res);
                            if(req.isSecure()) {
                                addCookie(COOKIE_SESSION_SSL, "true", req, res);
                            }
                                
                        } else {
                            
                            resetSession(req, res);
                            redirect(req, res, HttpServletResponse.SC_MOVED_PERMANENTLY, req.getScheme());
                            return;
        
                        }
                    
                    }
                
                }
                
            } else { //has no session
                
                if(getCookie(cookies, COOKIE_SESSION_SSL) != null) { //was running under SSL
                    
                    if(req.isSecure()) {
                        
                        if(getCookie(cookies, COOKIE_SESSION_SSL_CHECK) != null) {
                    
                            //was redirected to SSL to check for secure session
                            //let's redirect back to http because no secure session was found
                            resetCookie(COOKIE_SESSION_SSL, req, res);
                            redirect(req, res, HttpServletResponse.SC_MOVED_TEMPORARILY, "http");
                            return;
                            
                        } else {
                            
                            createSession(req, res);
                            
                        }
                       
                        
                    } else {
                    
                        //possibly has a secure session, so let's redirect to SSL and see if session is sent
                        
                        addCookie(COOKIE_SESSION_SSL_CHECK, "true", req, res);
                        redirectToSSLCheck(req, res, HttpServletResponse.SC_MOVED_TEMPORARILY);
                        return;
                    
                    }
                    
                } else { //wasn't running under SSL
                    
                    if(context.needsSSL(preq)) { //requires SSL
                        
                        if(req.isSecure()) { //has SSL
                            
                            session = createSession(req, res);
                            addCookie(COOKIE_SESSION_SSL, "true", req, res);

                        } else { //doesn't have SSL
                            
                            if(req.getMethod().equals("POST")) {
                                
                                //create intermediate session here to preserve posted parameters over redirect
                                session = createSession(req, res);
                                session.setAttribute(STORED_REQUEST, preq);
                                
                            }
                            
                            redirectToSSL(req, res, HttpServletResponse.SC_MOVED_PERMANENTLY);
                            return;
                            
                        }
                        
                    } else { //doesn't require SSL
                        
                        createSession(req, res);
                        
                    }
                    
                }
                
            }
        
        } else { //no session required
                        
            if(context.needsSSL(preq) && !req.isSecure()) { //requires SSL, but has no SSL
                            
                //redirecting to SSL
                redirectToSSL(req, res, HttpServletResponse.SC_MOVED_PERMANENTLY);
                return;
                    
            }
            
            //redirect to SSL when not running under SSL but session already used SSL before, thus
            //the secure session cookie will be sent again and requests are server sticky, which is
            //needed for the deref mechanism to work (because of server bound signature keys)
            if(req.getRequestedSessionId() == null && getCookie(cookies, COOKIE_SESSION_SSL) != null && !req.isSecure()) {
                redirectToSSL(req, res, HttpServletResponse.SC_MOVED_TEMPORARILY);
                return;
            }
                    
        }
        
        preq.updateRequest(req);
        context.callProcess(preq, req, res);
        
        } finally {
            if(preq != null) {
                preq.resetRequest();
            }
        }
    }
    
    private void resetSession(HttpServletRequest req, HttpServletResponse res) {
        if(req.isRequestedSessionIdFromCookie()) {
            Cookie cookie = new Cookie("JSESSIONID", "");
            cookie.setMaxAge(0);
            cookie.setPath((req.getContextPath().equals("")) ? "/" : req.getContextPath());
            res.addCookie(cookie);
            Cookie resetCookie = new Cookie(COOKIE_SESSION_RESET, req.getRequestedSessionId());
            resetCookie.setMaxAge(60);
            resetCookie.setPath((req.getContextPath().equals("")) ? "/" : req.getContextPath());
            res.addCookie(resetCookie);
        }
    }
    
    private void redirectToSSL(HttpServletRequest req, HttpServletResponse res, int statusCode) {
        redirect(req, res, statusCode, "https");
    }
    
    private void redirectToSSLCheck(HttpServletRequest req, HttpServletResponse res, int statusCode) {
        String redirect_uri = SessionHelper.getClearedURL("https", AbstractPustefixRequestHandler.getServerName(req), req, context.getServletManagerConfig().getProperties());
        if(req.getMethod().equals("POST") && req.getParameter("__lf") != null) {
            if(req.getQueryString() == null) {
                redirect_uri += ("?");
            } else {
                redirect_uri += ("&");
            }
            redirect_uri += ("__lf=" + req.getParameter("__lf"));
        }
        AbstractPustefixRequestHandler.relocate(res, statusCode, redirect_uri);
    }
    
    private void redirect(HttpServletRequest req, HttpServletResponse res, int statusCode, String scheme) {
        String redirect_uri = SessionHelper.getClearedURL(scheme, AbstractPustefixRequestHandler.getServerName(req), req, context.getServletManagerConfig().getProperties());
        AbstractPustefixRequestHandler.relocate(res, statusCode, redirect_uri);
    }
     
    private HttpSession createSession(HttpServletRequest req, HttpServletResponse res) throws ServletException {
        if(context.allowSessionCreate()) {
             HttpSession session = req.getSession(true);
             LOGGER_SESSION.info("Create session: " + session.getId());
             session.setAttribute(AbstractPustefixRequestHandler.SESSION_ATTR_COOKIE_SESSION, true);
             if(req.isSecure()) {
            	 session.setAttribute(SessionAdmin.SESSION_IS_SECURE, Boolean.TRUE);
             }
             context.registerSession(req, session);
             return session;
        } else {
            throw new ServletException("Creating session not allowed.");
        }
   }
    
    private HttpSession copySession(HttpSession oldSession, HttpServletRequest req) throws ServletException {
        
        if(context.allowSessionCreate()) {
        
            HashMap<String, Object> map = new HashMap<String, Object>();
            SessionHelper.saveSessionData(map, oldSession);
            
            SessionInfoStruct infostruct = context.getSessionAdmin().getInfo(oldSession);
            LinkedList<TrailElement> traillog = new LinkedList<TrailElement>();
            if (infostruct != null) {
                traillog = context.getSessionAdmin().getInfo(oldSession).getTraillog();
            }
    
            SessionUtils.invalidate(oldSession);
            
            HttpSession newSession = req.getSession(true);
            LOGGER_SESSION.info("Create session: " + newSession.getId());
            newSession.setAttribute(AbstractPustefixRequestHandler.SESSION_ATTR_COOKIE_SESSION, true);
            if(req.isSecure()) {
           	 	newSession.setAttribute(SessionAdmin.SESSION_IS_SECURE, Boolean.TRUE);
            }
            
            if(infostruct == null) {
                context.registerSession(req, newSession);
            } else {
                context.getSessionAdmin().registerSession(newSession, traillog, infostruct.getData().getServerName(), infostruct.getData().getRemoteAddr());
            }
                
            SessionHelper.copySessionData(map, newSession);
            return newSession;
        
        } else {
            throw new ServletException("Creating session not allowed.");
        }
    }
    
    private static void addCookie(String name, String value, HttpServletRequest req, HttpServletResponse res) {
        Cookie resetCookie = new Cookie(name, value);
        resetCookie.setMaxAge(-1);
        resetCookie.setPath((req.getContextPath().equals("")) ? "/" : req.getContextPath());
        res.addCookie(resetCookie);
    }
    
    private static void resetCookie(String name, HttpServletRequest req, HttpServletResponse res) {
        Cookie sslCookie = new Cookie(name, "");
        sslCookie.setMaxAge(0);
        sslCookie.setPath((req.getContextPath().equals("")) ? "/" : req.getContextPath());
        res.addCookie(sslCookie);
    }
    
    private static Cookie getCookie(Cookie[] cookies, String name) {
        if(cookies != null) {
            for(Cookie cookie: cookies) {
                if(cookie.getName().equals(name)) {
                    return cookie;
                }
            }
        }
        return null;
    }
    
    public static void sendInfo(HttpServletResponse res, String msg) throws IOException {
        res.setContentType("text/plain");
        res.setHeader("Cache-Control", "no-store, no-cache");
        res.setHeader("Pragma", "no-cache");
        PrintWriter writer = res.getWriter();
        writer.print(msg);
        writer.close();
    }
    
}
