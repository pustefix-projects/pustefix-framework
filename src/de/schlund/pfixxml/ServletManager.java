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

package de.schlund.pfixxml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Category;

import de.schlund.pfixxml.exceptionhandler.ExceptionHandler;
import de.schlund.pfixxml.serverutil.ContainerUtil;
import de.schlund.pfixxml.serverutil.SessionAdmin;

 /*
 *
 */

/**
 * ServletManager.java
 *
 *
 * Created: Wed May  8 16:39:06 2002
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 * @version
 */

public abstract class ServletManager extends HttpServlet {
    public  static final String JSERV_IDENTITY     = "org.apache.jserv";
    public  static final String TC4_IDENTITY       = "org.apache.catalina";
    public  static final String JSERV_CLASS        = "de.schlund.pfixxml.serverutil.jserv.ModernJServUtil";
    public  static final String TC4_CLASS          = "de.schlund.pfixxml.serverutil.tomcat4.TomcatUtil";
    public  static final String STORED_REQUEST     = "__STORED_PFIXSERVLETREQUEST__";
    public  static final String SESSION_IS_SECURE  = "__SESSION_IS_SECURE__";
    public  static final String VISIT_ID           = "__VISIT_ID__";
    public  static final String SESSION_ID_URL     = "__SESSION_ID_URL__";
    public  static final String PARAM_FORCELOCAL   = "__forcelocal";
    public  static final String PROP_LOADINDEX     = "__PROPERTIES_LOAD_INDEX";
    public  static final String DEF_CONTENT_TYPE   = "text/html; charset=iso-8859-1";
    private static final String SECURE_SESS_COOKIE = "__PFIX_SECURE_SSL_SESS__";
    private static final String TEST_COOKIE        = "__PFIX_TEST__";
    public  static final String CHECK_FOR_RUNNING_SSL_SESSION = "__CHECK_FOR_RUNNING_SSL_SESSION__";
    private static       String TIMESTAMP_ID       = "";
    private static       int    INC_ID             = 0;

    private SessionAdmin     sessionadmin  = SessionAdmin.getInstance();
    private Category         LOGGER_VISIT  = Category.getInstance("LOGGER_VISIT");
    private Category         CAT           = Category.getInstance(ServletManager.class);
    private ExceptionHandler xhandler      = ExceptionHandler.getInstance();
    private long             common_mtime  = 0;
    private long             servlet_mtime = 0;
    private long             loadindex     = 0;
    private Properties       properties;
    private ContainerUtil    conutil;
    private File             commonpropfile;
    private File             servletpropfile;
    
    protected Properties getProperties() {
        return properties;
    }

    protected ContainerUtil getContainerUtil() {
        return conutil;
    }

    protected boolean runningUnderSSL(HttpServletRequest req) {
        if (req.getScheme().equals("https") && req.getServerPort() == 443) {
            return true;
        } else {
            return false;
        }
    }
    
    protected boolean needsSSL() {
        String needs_ssl = properties.getProperty("servlet.needsSSL");
        if (needs_ssl != null && (needs_ssl.equals("true") || needs_ssl.equals("yes") || needs_ssl.equals("1"))) {
            return true;
        } else {
            return false;
        }
    }

    abstract protected boolean needsSession();
    abstract protected boolean allowSessionCreate();
   
    protected void relocate(HttpServletResponse res, String reloc_url) {
        CAT.debug("\n\n        ======> relocating to " + reloc_url + "\n");
        res.setHeader("Expires", "Mon, 26 Jul 1997 05:00:00 GMT");
        res.setHeader("Pragma", "no-cache");
        res.setHeader("Cache-Control", "no-cache, no-store, private, must-revalidate");
        res.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
        res.setHeader("Location", reloc_url);
    }

    public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        doGet(req, res);
    }
    
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        if (CAT.isDebugEnabled()) {
            CAT.debug("\n ------------------- Start of new Request ---------------");
            CAT.debug("====> Scheme://Server:Port " + req.getScheme() + "://" + req.getServerName() + ":" + req.getServerPort());
            CAT.debug("====> URI:   " + req.getRequestURI());
            CAT.debug("====> Query: " + req.getQueryString());
            CAT.debug("----> needsSession=" + needsSession() + " needsSSL=" + needsSSL() + " allowSessionCreate=" + allowSessionCreate());
			CAT.debug("====> Sessions: " + SessionAdmin.getInstance().toString());
            Cookie[] cookies = req.getCookies();
            if (cookies != null) {
                for (int i = 0; i < cookies.length; i++) {
                    Cookie tmp = cookies[i];
                    CAT.debug(">>>>> Cookie: " + tmp.getName() + " -> " + tmp.getValue());
                }
            }
        }
        HttpSession session                  = null;
        boolean     has_session              = false;
        boolean     has_ssl_session_insecure = false;
        boolean     has_ssl_session_secure   = false;
        boolean     force_jump_back_to_ssl   = false;
        boolean     force_reuse_visit_id     = false;
        boolean     does_cookies             = doCookieTest(req, res);
        if (req.isRequestedSessionIdValid()) {
            session     = req.getSession(false);
            has_session = true;
            CAT.debug("*** Found valid session with ID " + session.getId());
            if (runningUnderSSL(req)) {
                CAT.debug("*** Found running under SSL");
                Boolean secure = (Boolean) conutil.getSessionValue(session, SESSION_IS_SECURE);
                if (secure != null && secure.booleanValue()) {
                    CAT.debug("    ... and session is secure.");
                    has_ssl_session_secure = true;
                } else {
                    CAT.debug("    ... but session is insecure!");
                    has_ssl_session_insecure = true;
                }
            }
        } else if (req.getRequestedSessionId() != null) {
            CAT.debug("*** Found old and invalid session in request");
            // We have no valid session, but the request contained an invalid session id.
            // case a) This may be an invalid id because we invalidated the session when jumping
            // into the secure SSL session (see redirectToSecureSSLSession below). by using the back button
            // of the browser, the user may have come back to a (non-ssl) page (in his browser history) that contains
            // links with the old "parent" session id embedded. We need to check for this and create a
            // new session but reuse the visit id of the currently running SSL session.
            if (!runningUnderSSL(req) && SessionAdmin.getInstance().idWasParentSession(req.getRequestedSessionId())) {
                CAT.debug("    ... but this session was the parent of a currently running secure session.");
                // We'll try to get back there securely by first jumping back to a new (insecure) SSL session,
                // and after that the the jump to the secure SSL session will not create a new one, but reuse
                // the already running secure session instead (but only if a secure cookie can identify the request as
                // coming from the browser that made the initial jump http->https).
                if (does_cookies) {
                    force_jump_back_to_ssl = true;
                } else {
                    // OK, it seems as if we will not be able to identify the peer by comparing cookies.
                    // So the only thing we can do is to reuse the VISIT_ID.
                    force_reuse_visit_id = true;
                }
            } else {
                // Normally the balancer has a chance to choose the right server for a new session, but
                // with a session id in the URL it wasn't able to. So we redirect to a "fresh" request without _any_ id,
                // giving the balancer the possibility to choose a different server. (this can be overridden by
                // supplying the parameter __forcelocal=1 to the request). All this makes only sense of course
                // if we are running in a cluster of servers behind a balancer that chooses the right server
                // based on the session id included in the URL.
                String forcelocal = req.getParameter(PARAM_FORCELOCAL);
                if (forcelocal != null && (forcelocal.equals("1") || forcelocal.equals("true") || forcelocal.equals("yes"))) {
                    CAT.debug("    ... but found __forcelocal parameter to be set.");
                } else {
                    CAT.debug("    ... and __forcelocal is NOT set.");
                    redirectToClearedRequest(req, res);
                    return;
                    // End of request cycle.
                }
            }
        }
        
        PfixServletRequest preq = null;
        if (has_session) {
            preq = (PfixServletRequest) conutil.getSessionValue(session, STORED_REQUEST);
            if (preq != null) {
                CAT.debug("*** Found old PfixServletRequest object in session");
                conutil.removeSessionValue(session, STORED_REQUEST);
                preq.updateRequest(req);
            }
        }
        if (preq == null) {
            CAT.debug("*** Creating PfixServletRequest object.");
            preq = new PfixServletRequest(req, properties, conutil);
        }

        tryReloadProperties(preq);
        
        // End of initialization. Now we handle all cases where we need to redirect.

        if (force_jump_back_to_ssl) {
            forceRedirectBackToInsecureSSL(preq, req, res);
            return;
            // End of request cycle.
        }
        if (force_reuse_visit_id) {
            forceNewSessionSameVisit(preq, req, res);
            return;
            // End of request cycle.
        }
        if (has_ssl_session_insecure) {
            redirectToSecureSSLSession(preq, req, res);
            return;
            // End of request cycle.
        }
        if (needsSession() && needsSSL() && !has_ssl_session_secure) {
            redirectToInsecureSSLSession(preq, req, res);
            return;
            // End of request cycle.
        }
        if (!has_session && needsSession() && !needsSSL()) {
            redirectToSession(preq, req, res);
            return;
            // End of request cycle.
        }
        if (!has_session && !needsSession() && needsSSL() && !runningUnderSSL(req)) {
            redirectToSSL(req, res);
            return;
            // End of request cycle.
        }
        
        CAT.debug("*** >>> End of redirection management, handling request now.... <<< ***\n");
        callProcess(preq, req, res);
    }

    
    private void redirectToClearedRequest(HttpServletRequest req, HttpServletResponse res) {
        CAT.debug("===> Redirecting to cleared Request URL");
        String redirect_uri = conutil.getClearedURL(req.getScheme(), req.getServerName(), req, res);
        relocate(res, redirect_uri);
    }

    private void redirectToSSL(HttpServletRequest req, HttpServletResponse res) {
        CAT.debug("===> Redirecting to session-less request URL under SSL");
        String redirect_uri = conutil.getClearedURL("https", req.getServerName(), req, res);
        relocate(res, redirect_uri);
    }

    private void redirectToSecureSSLSession(PfixServletRequest preq, HttpServletRequest req, HttpServletResponse res) {
        HttpSession session  = req.getSession(false);
        String      parentid = (String) conutil.getSessionValue(session, CHECK_FOR_RUNNING_SSL_SESSION);
        if (parentid != null && !parentid.equals("")) {
            CAT.debug("*** The current insecure SSL session says to check for a already running SSL session for reuse");
            HttpSession secure_session = SessionAdmin.getInstance().getChildSessionForParentId(parentid);
            if (secure_session != null) {
                String secure_id = secure_session.getId();
                CAT.debug("*** We have found a candidate: SessionId=" + secure_id + " now search for cookie...");
                // But we need to make sure that the current request comes
                // from the same user who created this secure session.
                // We do this by checking for a (secure) cookie with a corresponding session id.
                Cookie cookie = getSecureSessionCookie(req);
                if (cookie != null) {
                    CAT.debug("*** Found a matching cookie ...");
                    if (cookie.getValue().equals(secure_id)) {
                        CAT.debug("   ... and the value is correct!");
                        CAT.debug("==> Redirecting to the secure SSL URL with the already running secure session " + secure_id);
                        String redirect_uri = conutil.encodeURL("https", req.getServerName(), req, res, secure_id);
                        relocate(res, redirect_uri);
                        return;
                    } else {
                        CAT.debug("   ... but the value is WRONG!");
                        throw new RuntimeException("Wrong Session-ID for running secure session from cookie.");
                    }
                }
            }
        }
        
        CAT.debug("*** Saving session data...");
        HashMap map = new HashMap();
        conutil.saveSessionData(map, session);
        // Before we invalidate the current session we save the traillog
        LinkedList traillog = SessionAdmin.getInstance().getInfo(session).getTraillog();
        String     old_id   = session.getId();
        CAT.debug("*** Invalidation old session (Id: " + old_id + ")");
        session.invalidate();
        session = req.getSession(true);
        // First of all we put the old session id into the new session (__PARENT_SESSION_ID__)
        conutil.setSessionValue(session, SessionAdmin.PARENT_SESS_ID, old_id);
        // Don't call this.registerSession(...) here. We don't want to log this as a different visit.
        // Now we register the new session with saved traillog
        SessionAdmin.getInstance().registerSession(session, traillog, conutil);
        CAT.debug("*** Got new Session (Id: " + session.getId() + ")");
        CAT.debug("*** Copying data back to new session");
        conutil.copySessionData(map, session);
        CAT.debug("*** Setting ContainerUtil.SESSION_ID_URL to " +  conutil.getSessionValue(session, ContainerUtil.SESSION_ID_URL));
        conutil.setSessionValue(session, ContainerUtil.SESSION_ID_URL, conutil.getURLSessionId(req, res));
        CAT.debug("*** Setting SECURE flag");
        conutil.setSessionValue(session, SESSION_IS_SECURE, Boolean.TRUE);
        CAT.debug("===> Redirecting to secure SSL URL with session (Id: " + session.getId() + ")");
        conutil.setSessionValue(session, STORED_REQUEST, preq);
        
        Cookie cookie = getSecureSessionCookie(req);
        if (cookie != null) {
            cookie.setMaxAge(0);
            res.addCookie(cookie);
        }
        cookie = new Cookie(SECURE_SESS_COOKIE, session.getId());
        cookie.setPath("/");
        cookie.setMaxAge(-1);
        cookie.setSecure(true);
        res.addCookie(cookie);
        
        String redirect_uri = conutil.encodeURL("https", req.getServerName(), req, res);
        relocate(res, redirect_uri);
    }

    private void redirectToInsecureSSLSession(PfixServletRequest preq, HttpServletRequest req, HttpServletResponse res) {
        boolean reuse_session = false;
        if (req.isRequestedSessionIdValid()) {
            reuse_session = true;
            CAT.debug("*** reusing existing session for jump http=>https");
        }
        HttpSession session = req.getSession(true);
        if (!reuse_session) {
            registerSession(req, res, session);
        }
        conutil.setSessionValue(session, ContainerUtil.SESSION_ID_URL, conutil.getURLSessionId(req, res));
        CAT.debug("*** Setting INSECURE flag in session (Id: " + session.getId() + ")");
        conutil.setSessionValue(session, SESSION_IS_SECURE, Boolean.FALSE);
        conutil.setSessionValue(session, STORED_REQUEST, preq);
        CAT.debug("===> Redirecting to insecure SSL URL with session (Id: " + session.getId() + ")");
        String redirect_uri = conutil.encodeURL("https", req.getServerName(), req, res);
        relocate(res, redirect_uri);
    }

    private void forceRedirectBackToInsecureSSL(PfixServletRequest preq, HttpServletRequest req, HttpServletResponse res) {
        // When we come here, we KNOW that there's a secure SSL session already running, so this session here is
        // only used for the jump to SSL so we can get the cookie to check the identity of the caller.
        // Because of this we don't bother copying the VISIT_ID or register the session with the SessionAdmin.
        String      parentid      = req.getRequestedSessionId();
        HttpSession session       = req.getSession(true);
        conutil.setSessionValue(session, ContainerUtil.SESSION_ID_URL, conutil.getURLSessionId(req, res));
        conutil.setSessionValue(session, CHECK_FOR_RUNNING_SSL_SESSION, parentid);
        CAT.debug("*** Setting INSECURE flag in session (Id: " + session.getId() + ")");
        conutil.setSessionValue(session, SESSION_IS_SECURE, Boolean.FALSE);
        conutil.setSessionValue(session, STORED_REQUEST, preq);
        CAT.debug("===> Redirecting to SSL URL with session (Id: " + session.getId() + ")");
        String redirect_uri = conutil.encodeURL("https", req.getServerName(), req, res);
        relocate(res, redirect_uri);
    }
    
    private void forceNewSessionSameVisit(PfixServletRequest preq, HttpServletRequest req, HttpServletResponse res) {
        // When we come here, we KNOW that there's a secure SSL session already running, but unfortunately
        // it seems that the browser doesn't send cookies. So we will not be able to know for sure that the request comes
        // from the legitimate user. The only thing we can do is to copy the VISIT_ID, which helps to keep the
        // statistic clean :-)
        String      parentid      = req.getRequestedSessionId();
        HttpSession child         = SessionAdmin.getInstance().getChildSessionForParentId(parentid);
        String      curr_visit_id = (String) conutil.getSessionValue(child, VISIT_ID);
        HttpSession session       = req.getSession(true);
        LinkedList  traillog      = SessionAdmin.getInstance().getInfo(child).getTraillog();
        conutil.setSessionValue(session, ContainerUtil.SESSION_ID_URL, conutil.getURLSessionId(req, res));
        conutil.setSessionValue(session, VISIT_ID, curr_visit_id);
        SessionAdmin.getInstance().registerSession(session, traillog, conutil);
        CAT.debug("===> Redirecting with session (Id: " + session.getId() + ") using OLD VISIT_ID: " + curr_visit_id);
        conutil.setSessionValue(session, STORED_REQUEST, preq);
        String redirect_uri = conutil.encodeURL(req.getScheme(), req.getServerName(), req, res);
        relocate(res, redirect_uri);
    }

    private void redirectToSession(PfixServletRequest preq, HttpServletRequest req, HttpServletResponse res) {
        HttpSession session = req.getSession(true);
        conutil.setSessionValue(session, ContainerUtil.SESSION_ID_URL, conutil.getURLSessionId(req, res));
        registerSession(req, res, session);
        CAT.debug("===> Redirecting to URL with session (Id: " + session.getId() + ")");
        conutil.setSessionValue(session, STORED_REQUEST, preq);
        String redirect_uri = conutil.encodeURL(req.getScheme(), req.getServerName(), req, res);
        relocate(res, redirect_uri);
    }

    private boolean doCookieTest(HttpServletRequest req, HttpServletResponse res) {
        Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            return true;
        } else {
            Cookie probe = new Cookie(TEST_COOKIE, "TRUE");
            probe.setPath("/");
            res.addCookie(probe);
            return false;
        }
    }
    
    private Cookie getSecureSessionCookie(HttpServletRequest req) {
        Cookie[] cookies = req.getCookies();
        Cookie   tmp;
        if (cookies != null) {
            for (int i = 0; i < cookies.length; i++) {
                tmp = cookies[i];
                if (tmp.getName().equals(SECURE_SESS_COOKIE))
                    return tmp;
            }
        }
        return null;
    }
    
    private void registerSession(HttpServletRequest req, HttpServletResponse res, HttpSession session) {
        if (session != null) {
            synchronized (TIMESTAMP_ID) {
                SimpleDateFormat sdf       = new SimpleDateFormat("yyyyMMddHHmmss");
                String           timestamp = sdf.format(new Date());
                NumberFormat     nf        = NumberFormat.getInstance();
                nf.setMinimumIntegerDigits(3);
                    
                if (timestamp.equals(TIMESTAMP_ID)) {
                    INC_ID++;
                } else {
                    TIMESTAMP_ID = timestamp;
                    INC_ID       = 0;
                }
                if (INC_ID >= 1000) {
                    CAT.warn("*** More than 999 connects/sec! ***");
                }
                String sessid = session.getId();
                String mach   = "";
                if (sessid.lastIndexOf(".") > 0) {
                    mach = sessid.substring(sessid.lastIndexOf("."));
                }
                conutil.setSessionValue(session, VISIT_ID, TIMESTAMP_ID + "-" + nf.format(INC_ID) + mach);
            }
            StringBuffer logbuff = new StringBuffer();
            logbuff.append(conutil.getSessionValue(session, VISIT_ID) + "|" + session.getId() + "|");
            logbuff.append(req.getServerName() + "|" + req.getRemoteAddr() + "|" + req.getHeader("user-agent") + "|");
            if (req.getHeader("referer") != null) {
                logbuff.append(req.getHeader("referer"));
            }
            logbuff.append("|");
            if (req.getHeader("accept-language") != null) {
                logbuff.append(req.getHeader("accept-language"));
            }
            LOGGER_VISIT.warn(logbuff.toString());
            SessionAdmin.getInstance().registerSession(session, conutil);
        }
    }

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        properties = new Properties(System.getProperties());

        String configclassname = config.getClass().getName();
        CAT.debug("*** ServletConfig class '" + configclassname + "'");
        if (configclassname.startsWith(JSERV_IDENTITY)) {
            CAT.warn("*** JServ detected");
            try {
                conutil = (ContainerUtil) Class.forName(JSERV_CLASS).newInstance();
            } catch (Exception e) {
                throw new ServletException("*** Couldn't initialize JServ ContainerUtil '" + JSERV_CLASS + "'" + e);
            }
        } else if (configclassname.startsWith(TC4_IDENTITY)) {
            CAT.warn("*** Tomcat-4 detected");
            try {
                conutil = (ContainerUtil) Class.forName(TC4_CLASS).newInstance();
            } catch (Exception e) {
                throw new ServletException("*** Couldn't initialize Tomcat-4 ContainerUtil '" + TC4_CLASS + "'" + e);
            }
        } else {
            throw new ServletException("*** Can't detect servlet container for config class '" + configclassname + "'");
        }

        String commonpropfilename = config.getInitParameter("servlet.commonpropfile");
        if (commonpropfilename != null) {
            commonpropfile = new File(commonpropfilename);
            common_mtime = loadPropertyfile(properties, commonpropfile);
        }
        
        String servletpropfilename = config.getInitParameter("servlet.propfile");
        if (servletpropfilename != null) {
            servletpropfile = new File(servletpropfilename);
            servlet_mtime   = loadPropertyfile(properties, servletpropfile);
        }
        loadindex = 0;
        properties.setProperty(PROP_LOADINDEX, "" + loadindex);
    }

    protected boolean tryReloadProperties(PfixServletRequest preq) throws ServletException {
        if ((commonpropfile  != null && commonpropfile.lastModified()  > common_mtime) ||
            (servletpropfile != null && servletpropfile.lastModified() > servlet_mtime)) {

            CAT.warn("\n\n##############################\n" +
                         "#### Reloading properties ####\n" +
                         "##############################\n");
            properties.clear();
            if (commonpropfile != null) {
                common_mtime = loadPropertyfile(properties, commonpropfile);
            }
            if (servletpropfile != null) {
                servlet_mtime = loadPropertyfile(properties, servletpropfile);
            }
            properties.setProperty(PROP_LOADINDEX, "" + (loadindex + 1));
            return true;
        } else {
            return false;
        }
        
    }

    private long loadPropertyfile(Properties props, File propfile) throws ServletException {
        long mtime;
        try {
            mtime = propfile.lastModified();
            props.load(new FileInputStream(propfile));
        } catch (FileNotFoundException e) {
            throw new ServletException("*** [" + propfile.getName() + "] Not found: " + e.toString());
        } catch (IOException e) {
            throw new ServletException("*** [" + propfile.getName() + "] IO-error: " + e.toString());
        }
        return mtime;
    }

    private void callProcess(PfixServletRequest preq, HttpServletRequest req,
                             HttpServletResponse res) throws ServletException {
        try {
            res.setContentType(DEF_CONTENT_TYPE);
            process(preq, res);
        } catch (Exception e) {
            xhandler.handle(e, preq, properties, res);
            throw(new ServletException(e.toString()));
        }
    }
    
    protected abstract void process(PfixServletRequest preq, HttpServletResponse res) throws Exception;
    
}// ServletManager
