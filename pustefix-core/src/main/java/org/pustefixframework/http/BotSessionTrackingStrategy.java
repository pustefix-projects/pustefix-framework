package org.pustefixframework.http;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import de.schlund.pfixxml.PfixServletRequest;
import de.schlund.pfixxml.PfixServletRequestImpl;
import de.schlund.pfixxml.serverutil.SessionHelper;

public class BotSessionTrackingStrategy implements SessionTrackingStrategy {
    
    private static final String STORED_REQUEST = "__STORED_PFIXSERVLETREQUEST__";
    
    private SessionTrackingStrategyContext context;
    
    public void init(SessionTrackingStrategyContext context) {
        this.context = context;
    }
    
    public void handleRequest(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        
        if(req.getRequestedSessionId() != null && req.isRequestedSessionIdFromURL()) {
            String redirect_uri = SessionHelper.getClearedURL(req.getScheme(), AbstractPustefixRequestHandler.getServerName(req), req, context.getServletManagerConfig().getProperties());
            AbstractPustefixRequestHandler.relocate(res, HttpServletResponse.SC_MOVED_PERMANENTLY, redirect_uri);
            return;
        }
        
        PfixServletRequest preq = new PfixServletRequestImpl(req, context.getServletManagerConfig().getProperties(), context);
        
        if(!req.isSecure() && context.needsSSL(preq)) {
            String redirect_uri = SessionHelper.getClearedURL("https", AbstractPustefixRequestHandler.getServerName(req), req, context.getServletManagerConfig().getProperties());
            AbstractPustefixRequestHandler.relocate(res, HttpServletResponse.SC_MOVED_PERMANENTLY, redirect_uri);
            return;
        }
        
        HttpSession session = null;
        if(context.needsSession()) {
            session = req.getSession(false);
            if(session == null) {
                session = req.getSession(true);
                context.registerSession(req, session);
                session.setMaxInactiveInterval(30);
            } else {
                if(session.getMaxInactiveInterval() == 30) {
                    session.setMaxInactiveInterval(5 * 60);
                }
            }
            session.setAttribute(STORED_REQUEST, preq);
            session.setAttribute(AbstractPustefixRequestHandler.SESSION_ATTR_COOKIE_SESSION, true);
            preq.updateRequest(req);
        }
        
        context.callProcess(preq, req, res);
    }
    
}
