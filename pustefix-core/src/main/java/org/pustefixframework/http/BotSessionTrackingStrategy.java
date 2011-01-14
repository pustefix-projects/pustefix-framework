package org.pustefixframework.http;

import java.io.IOException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import de.schlund.pfixxml.PfixServletRequest;
import de.schlund.pfixxml.PfixServletRequestImpl;
import de.schlund.pfixxml.serverutil.SessionHelper;

public class BotSessionTrackingStrategy implements SessionTrackingStrategy {
    
    private Logger LOG = Logger.getLogger(CookieSessionTrackingStrategy.class);
    
    private static int INC_ID = 0;
    private static final String STORED_REQUEST = "__STORED_PFIXSERVLETREQUEST__";
    private static String TIMESTAMP_ID = "";

    public static final String VISIT_ID = "__VISIT_ID__";
    
    private SessionTrackingStrategyContext context;
    
    public void init(SessionTrackingStrategyContext context) {
        this.context = context;
    }
    
    public void handleRequestByStrategy(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        
        if(req.getRequestedSessionId() != null && req.isRequestedSessionIdFromURL()) {
            String redirect_uri = SessionHelper.getClearedURL(req.getScheme(), AbstractPustefixRequestHandler.getServerName(req), req, context.getServletManagerConfig().getProperties());
            AbstractPustefixRequestHandler.relocate(res, HttpServletResponse.SC_MOVED_PERMANENTLY, redirect_uri);
            return;
        }
        
        PfixServletRequest preq = new PfixServletRequestImpl(req, context.getServletManagerConfig().getProperties());
        
        if(!req.isSecure() && context.needsSSL(preq)) {
            String redirect_uri = SessionHelper.getClearedURL("https", AbstractPustefixRequestHandler.getServerName(req), req, context.getServletManagerConfig().getProperties());
            AbstractPustefixRequestHandler.relocate(res, redirect_uri);
            return;
        }
        
        HttpSession session = null;
        if(context.needsSession()) {
            session = req.getSession(true);
            session.setAttribute(SessionHelper.SESSION_ID_URL, SessionHelper.getURLSessionId(req));
            registerSession(req, session);
            session.setAttribute(STORED_REQUEST, preq);
            session.setAttribute(AbstractPustefixRequestHandler.SESSION_ATTR_COOKIE_SESSION, true);
            preq.updateRequest(req);
        }
        
        context.callProcess(preq, req, res);
       
        session.setMaxInactiveInterval(10);
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
    
}
